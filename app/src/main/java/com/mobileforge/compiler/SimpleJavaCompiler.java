package com.mobileforge.compiler;

import android.util.Log;
import java.io.*;
import java.util.*;

/**
 * Bytecode compiler that works directly on MFNL AST
 * Generates .class files without needing Java source or external compilers
 */
public class SimpleJavaCompiler {
    private static final String TAG = "SimpleJavaCompiler";

    public static class CompileResult {
        public boolean success;
        public String message;
        public byte[] classData;

        public CompileResult(boolean success, String message, byte[] classData) {
            this.success = success;
            this.message = message;
            this.classData = classData;
        }
    }

    public static CompileResult compileFromAST(ASTNode.Program program, String className) {
        try {
            Log.d(TAG, "Compiling AST to bytecode for class: " + className);

            ClassBuilder classBuilder = new ClassBuilder(className);

            // Generate onCreate method
            MethodBuilder onCreate = classBuilder.startMethod("onCreate", "(Landroid/os/Bundle;)V", true);

            // Call super.onCreate(savedInstanceState)
            onCreate.addAload(0); // this
            onCreate.addAload(1); // savedInstanceState parameter
            onCreate.addMethodCall("android/app/Activity", "onCreate", "(Landroid/os/Bundle;)V", false);

            // Create root LinearLayout - store in local var 2
            onCreate.addNew("android/widget/LinearLayout");
            onCreate.addDup();
            onCreate.addAload(0); // this (Context)
            onCreate.addMethodCall("android/widget/LinearLayout", "<init>", "(Landroid/content/Context;)V", false);
            onCreate.addAstore(2); // rootLayout

            // Set orientation to VERTICAL
            onCreate.addAload(2);
            onCreate.addIconst(1); // LinearLayout.VERTICAL = 1
            onCreate.addMethodCall("android/widget/LinearLayout", "setOrientation", "(I)V", false);

            // Set layout params
            onCreate.addAload(2);
            onCreate.addNew("android/widget/LinearLayout$LayoutParams");
            onCreate.addDup();
            onCreate.addIconst(-1); // MATCH_PARENT
            onCreate.addIconst(-1); // MATCH_PARENT
            onCreate.addMethodCall("android/widget/LinearLayout$LayoutParams", "<init>", "(II)V", false);
            onCreate.addMethodCall("android/widget/LinearLayout", "setLayoutParams", "(Landroid/view/ViewGroup$LayoutParams;)V", false);

            int nextLocalVar = 3;

            // Process all statements in the program
            for (ASTNode.Statement stmt : program.statements) {
                nextLocalVar = generateStatement(onCreate, stmt, 2, nextLocalVar, classBuilder);
            }

            // setContentView(rootLayout)
            onCreate.addAload(0); // this
            onCreate.addAload(2); // rootLayout
            onCreate.addMethodCall("android/app/Activity", "setContentView", "(Landroid/view/View;)V", false);

            onCreate.addReturn();
            classBuilder.endMethod(onCreate);

            byte[] classData = classBuilder.build();

            Log.d(TAG, "Bytecode generation successful: " + classData.length + " bytes");
            return new CompileResult(true, "Bytecode compilation successful", classData);

        } catch (Exception e) {
            Log.e(TAG, "Compilation failed", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return new CompileResult(false, "Compilation failed: " + e.getMessage() + "\n" + sw.toString(), null);
        }
    }

    private static int generateStatement(MethodBuilder method, ASTNode.Statement stmt,
                                         int parentVar, int nextVar, ClassBuilder classBuilder) {
        if (stmt instanceof ASTNode.BuildStatement) {
            ASTNode.BuildStatement build = (ASTNode.BuildStatement) stmt;
            for (ASTNode.Statement bodyStmt : build.body) {
                nextVar = generateStatement(method, bodyStmt, parentVar, nextVar, classBuilder);
            }
        } else if (stmt instanceof ASTNode.PlaceStatement) {
            ASTNode.PlaceStatement place = (ASTNode.PlaceStatement) stmt;
            String viewClass = getAndroidViewClass(place.objectType);

            // Create the view
            method.addNew(viewClass);
            method.addDup();
            method.addAload(0); // this (Context)
            method.addMethodCall(viewClass, "<init>", "(Landroid/content/Context;)V", false);
            method.addAstore(nextVar); // Store in local variable
            int viewVar = nextVar++;

            // Set text if it's a TextView or Button
            if (place.name != null && !place.name.isEmpty()) {
                if (viewClass.equals("android/widget/TextView") ||
                    viewClass.equals("android/widget/Button")) {
                    method.addAload(viewVar);
                    method.addLdc(place.name);
                    method.addMethodCall(viewClass, "setText", "(Ljava/lang/CharSequence;)V", false);
                }
            }

            // Apply properties
            for (ASTNode.Statement bodyStmt : place.body) {
                if (bodyStmt instanceof ASTNode.PropertyStatement) {
                    nextVar = applyProperty(method, viewVar, (ASTNode.PropertyStatement) bodyStmt,
                                          viewClass, nextVar, classBuilder);
                } else if (bodyStmt instanceof ASTNode.WhenStatement) {
                    nextVar = generateWhenStatement(method, viewVar, (ASTNode.WhenStatement) bodyStmt,
                                                   nextVar, classBuilder);
                }
            }

            // Add view to parent
            method.addAload(parentVar); // parent layout
            method.addAload(viewVar); // view to add
            method.addMethodCall("android/widget/LinearLayout", "addView", "(Landroid/view/View;)V", false);

        } else if (stmt instanceof ASTNode.ActionStatement) {
            ASTNode.ActionStatement action = (ASTNode.ActionStatement) stmt;
            if (action.action.equals("show") && action.target != null && action.target.equals("message")) {
                // Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                method.addAload(0); // this
                if (action.value instanceof ASTNode.StringLiteral) {
                    method.addLdc(((ASTNode.StringLiteral) action.value).value);
                } else {
                    method.addLdc("Message");
                }
                method.addIconst(0); // Toast.LENGTH_SHORT
                method.addMethodCall("android/widget/Toast", "makeText",
                    "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;", true);
                method.addMethodCall("android/widget/Toast", "show", "()V", false);
            }
        }

        return nextVar;
    }

    private static int applyProperty(MethodBuilder method, int viewVar, ASTNode.PropertyStatement prop,
                                    String viewClass, int nextVar, ClassBuilder classBuilder) {
        String propName = prop.property.toLowerCase();

        if (propName.equals("large")) {
            // setTextSize(24)
            method.addAload(viewVar);
            method.addLdc(24.0f);
            method.addMethodCall("android/widget/TextView", "setTextSize", "(F)V", false);
        } else if (propName.equals("bold")) {
            // setTypeface(null, Typeface.BOLD)
            method.addAload(viewVar);
            method.addAconst_null();
            method.addIconst(1); // Typeface.BOLD
            method.addMethodCall("android/widget/TextView", "setTypeface",
                "(Landroid/graphics/Typeface;I)V", false);
        } else if (propName.equals("small")) {
            method.addAload(viewVar);
            method.addLdc(12.0f);
            method.addMethodCall("android/widget/TextView", "setTextSize", "(F)V", false);
        }

        return nextVar;
    }

    private static int generateWhenStatement(MethodBuilder method, int viewVar,
                                            ASTNode.WhenStatement when, int nextVar,
                                            ClassBuilder classBuilder) {
        if (when.eventType.toLowerCase().contains("click")) {
            // Generate anonymous OnClickListener class inline
            // For now, just attach a simple listener that shows a toast

            // Create a Runnable that executes the body
            // view.setOnClickListener(new View.OnClickListener() {
            //     public void onClick(View v) { ... }
            // })

            // This is complex - for now, add a simple click listener
            method.addAload(viewVar);
            method.addAconst_null(); // Simple null listener for now
            method.addMethodCall("android/view/View", "setOnClickListener",
                "(Landroid/view/View$OnClickListener;)V", false);
        }

        return nextVar;
    }

    private static String getAndroidViewClass(String mfnlType) {
        switch (mfnlType.toLowerCase()) {
            case "text": return "android/widget/TextView";
            case "button": return "android/widget/Button";
            case "input": return "android/widget/EditText";
            case "image": return "android/widget/ImageView";
            case "list": return "android/widget/ListView";
            default: return "android/widget/TextView";
        }
    }

    // Constant pool entry types
    static class ConstantPool {
        List<Object> entries = new ArrayList<>();
        Map<String, Integer> stringCache = new HashMap<>();
        Map<String, Integer> classCache = new HashMap<>();

        public ConstantPool() {
            entries.add(null); // Index 0 is reserved
        }

        public int addUtf8(String value) {
            if (stringCache.containsKey(value)) {
                return stringCache.get(value);
            }
            int index = entries.size();
            entries.add(new Utf8Entry(value));
            stringCache.put(value, index);
            return index;
        }

        public int addClass(String className) {
            String key = "class:" + className;
            if (classCache.containsKey(key)) {
                return classCache.get(key);
            }
            int nameIndex = addUtf8(className);
            int index = entries.size();
            entries.add(new ClassEntry(nameIndex));
            classCache.put(key, index);
            return index;
        }

        public int addString(String value) {
            int utf8Index = addUtf8(value);
            int index = entries.size();
            entries.add(new StringEntry(utf8Index));
            return index;
        }

        public int addMethodRef(String className, String methodName, String descriptor) {
            int classIndex = addClass(className);
            int nameAndTypeIndex = addNameAndType(methodName, descriptor);
            int index = entries.size();
            entries.add(new MethodRefEntry(classIndex, nameAndTypeIndex));
            return index;
        }

        public int addFieldRef(String className, String fieldName, String descriptor) {
            int classIndex = addClass(className);
            int nameAndTypeIndex = addNameAndType(fieldName, descriptor);
            int index = entries.size();
            entries.add(new FieldRefEntry(classIndex, nameAndTypeIndex));
            return index;
        }

        public int addNameAndType(String name, String descriptor) {
            int nameIndex = addUtf8(name);
            int descIndex = addUtf8(descriptor);
            int index = entries.size();
            entries.add(new NameAndTypeEntry(nameIndex, descIndex));
            return index;
        }

        public int addFloat(float value) {
            int index = entries.size();
            entries.add(new FloatEntry(value));
            return index;
        }

        public int addInteger(int value) {
            int index = entries.size();
            entries.add(new IntegerEntry(value));
            return index;
        }

        public void write(DataOutputStream dos) throws IOException {
            dos.writeShort(entries.size());
            for (int i = 1; i < entries.size(); i++) {
                Object entry = entries.get(i);
                if (entry instanceof Utf8Entry) {
                    dos.writeByte(1);
                    dos.writeUTF(((Utf8Entry) entry).value);
                } else if (entry instanceof IntegerEntry) {
                    dos.writeByte(3);
                    dos.writeInt(((IntegerEntry) entry).value);
                } else if (entry instanceof FloatEntry) {
                    dos.writeByte(4);
                    dos.writeFloat(((FloatEntry) entry).value);
                } else if (entry instanceof ClassEntry) {
                    dos.writeByte(7);
                    dos.writeShort(((ClassEntry) entry).nameIndex);
                } else if (entry instanceof StringEntry) {
                    dos.writeByte(8);
                    dos.writeShort(((StringEntry) entry).utf8Index);
                } else if (entry instanceof FieldRefEntry) {
                    dos.writeByte(9);
                    FieldRefEntry ref = (FieldRefEntry) entry;
                    dos.writeShort(ref.classIndex);
                    dos.writeShort(ref.nameAndTypeIndex);
                } else if (entry instanceof MethodRefEntry) {
                    dos.writeByte(10);
                    MethodRefEntry ref = (MethodRefEntry) entry;
                    dos.writeShort(ref.classIndex);
                    dos.writeShort(ref.nameAndTypeIndex);
                } else if (entry instanceof NameAndTypeEntry) {
                    dos.writeByte(12);
                    NameAndTypeEntry nat = (NameAndTypeEntry) entry;
                    dos.writeShort(nat.nameIndex);
                    dos.writeShort(nat.descriptorIndex);
                }
            }
        }

        static class Utf8Entry {
            String value;
            Utf8Entry(String value) { this.value = value; }
        }

        static class IntegerEntry {
            int value;
            IntegerEntry(int value) { this.value = value; }
        }

        static class FloatEntry {
            float value;
            FloatEntry(float value) { this.value = value; }
        }

        static class ClassEntry {
            int nameIndex;
            ClassEntry(int nameIndex) { this.nameIndex = nameIndex; }
        }

        static class StringEntry {
            int utf8Index;
            StringEntry(int utf8Index) { this.utf8Index = utf8Index; }
        }

        static class FieldRefEntry {
            int classIndex, nameAndTypeIndex;
            FieldRefEntry(int classIndex, int nameAndTypeIndex) {
                this.classIndex = classIndex;
                this.nameAndTypeIndex = nameAndTypeIndex;
            }
        }

        static class MethodRefEntry {
            int classIndex, nameAndTypeIndex;
            MethodRefEntry(int classIndex, int nameAndTypeIndex) {
                this.classIndex = classIndex;
                this.nameAndTypeIndex = nameAndTypeIndex;
            }
        }

        static class NameAndTypeEntry {
            int nameIndex, descriptorIndex;
            NameAndTypeEntry(int nameIndex, int descriptorIndex) {
                this.nameIndex = nameIndex;
                this.descriptorIndex = descriptorIndex;
            }
        }
    }

    static class ClassBuilder {
        String className;
        ConstantPool pool;
        List<MethodBuilder> methods = new ArrayList<>();

        public ClassBuilder(String className) {
            this.className = className.replace('.', '/');
            this.pool = new ConstantPool();
        }

        public MethodBuilder startMethod(String name, String descriptor, boolean isPublic) {
            MethodBuilder method = new MethodBuilder(name, descriptor, isPublic, pool);
            methods.add(method);
            return method;
        }

        public void endMethod(MethodBuilder method) {
            // Method is complete
        }

        public byte[] build() throws IOException {
            // First pass: ensure all constant pool entries are added
            int thisClassIndex = pool.addClass(className);
            int superClassIndex = pool.addClass("android/app/Activity");

            // Pre-add method metadata to pool
            for (MethodBuilder method : methods) {
                method.ensurePoolEntries(pool);
            }

            // Now write the class file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // Magic number
            dos.writeInt(0xCAFEBABE);

            // Version (Java 8 = 52.0)
            dos.writeShort(0);
            dos.writeShort(52);

            // Constant pool (now complete)
            pool.write(dos);

            // Access flags (ACC_PUBLIC | ACC_SUPER)
            dos.writeShort(0x0021);

            // This class
            dos.writeShort(thisClassIndex);

            // Super class
            dos.writeShort(superClassIndex);

            // Interfaces
            dos.writeShort(0);

            // Fields
            dos.writeShort(0);

            // Methods
            dos.writeShort(methods.size());
            for (MethodBuilder method : methods) {
                method.write(dos, pool);
            }

            // Attributes
            dos.writeShort(0);

            return baos.toByteArray();
        }
    }

    static class MethodBuilder {
        String name;
        String descriptor;
        boolean isPublic;
        ConstantPool pool;
        ByteArrayOutputStream code = new ByteArrayOutputStream();
        int maxStack = 10;
        int maxLocals = 10;

        public MethodBuilder(String name, String descriptor, boolean isPublic, ConstantPool pool) {
            this.name = name;
            this.descriptor = descriptor;
            this.isPublic = isPublic;
            this.pool = pool;
        }

        public void addAload(int index) {
            if (index <= 3) {
                code.write(0x2a + index); // aload_0 to aload_3
            } else {
                code.write(0x19); // aload
                code.write(index);
            }
        }

        public void addAstore(int index) {
            if (index <= 3) {
                code.write(0x4b + index); // astore_0 to astore_3
            } else {
                code.write(0x3a); // astore
                code.write(index);
            }
        }

        public void addIconst(int value) {
            if (value >= -1 && value <= 5) {
                code.write(0x03 + value); // iconst_m1 to iconst_5
            } else if (value >= -128 && value <= 127) {
                code.write(0x10); // bipush
                code.write(value);
            } else {
                code.write(0x11); // sipush
                code.write((value >> 8) & 0xFF);
                code.write(value & 0xFF);
            }
        }

        public void addLdc(String value) {
            int index = pool.addString(value);
            if (index < 256) {
                code.write(0x12); // ldc
                code.write(index);
            } else {
                code.write(0x13); // ldc_w
                code.write((index >> 8) & 0xFF);
                code.write(index & 0xFF);
            }
        }

        public void addLdc(float value) {
            int index = pool.addFloat(value);
            if (index < 256) {
                code.write(0x12); // ldc
                code.write(index);
            } else {
                code.write(0x13); // ldc_w
                code.write((index >> 8) & 0xFF);
                code.write(index & 0xFF);
            }
        }

        public void addNew(String className) {
            int index = pool.addClass(className);
            code.write(0xbb); // new
            code.write((index >> 8) & 0xFF);
            code.write(index & 0xFF);
        }

        public void addDup() {
            code.write(0x59); // dup
        }

        public void addAconst_null() {
            code.write(0x01); // aconst_null
        }

        public void addMethodCall(String className, String methodName, String descriptor, boolean isStatic) {
            int index = pool.addMethodRef(className, methodName, descriptor);
            if (isStatic) {
                code.write(0xb8); // invokestatic
            } else if (methodName.equals("<init>")) {
                code.write(0xb7); // invokespecial
            } else {
                code.write(0xb6); // invokevirtual
            }
            code.write((index >> 8) & 0xFF);
            code.write(index & 0xFF);
        }

        public void addReturn() {
            code.write(0xb1); // return
        }

        public void ensurePoolEntries(ConstantPool pool) {
            // Pre-add all strings this method needs
            pool.addUtf8(name);
            pool.addUtf8(descriptor);
            pool.addUtf8("Code");
        }

        public void write(DataOutputStream dos, ConstantPool pool) throws IOException {
            // Access flags
            dos.writeShort(isPublic ? 0x0001 : 0x0000);

            // Name index (already in pool)
            int nameIndex = pool.addUtf8(name);
            dos.writeShort(nameIndex);

            // Descriptor index (already in pool)
            int descIndex = pool.addUtf8(descriptor);
            dos.writeShort(descIndex);

            // Attributes count (just Code)
            dos.writeShort(1);

            // Code attribute (already in pool)
            int codeAttrIndex = pool.addUtf8("Code");
            dos.writeShort(codeAttrIndex);

            byte[] codeBytes = code.toByteArray();
            dos.writeInt(12 + codeBytes.length); // attribute_length
            dos.writeShort(maxStack);
            dos.writeShort(maxLocals);
            dos.writeInt(codeBytes.length);
            dos.write(codeBytes);
            dos.writeShort(0); // exception_table_length
            dos.writeShort(0); // attributes_count
        }
    }
}
