package com.mobileforge;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.zip.Adler32;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is the complete, combined code for the .class to .dex converter.
 * It contains all classes, constants, and methods from our 86-part session.
 */
public class PureCodeDEXGenerator {

    // =========================================================================
    // PART 1-9: .CLASS FILE PARSER
    // =========================================================================

    // --- .class Constant Pool Tags ---
    final static int CONSTANT_Utf8 = 1;
    final static int CONSTANT_Integer = 3;
    final static int CONSTANT_Float = 4;
    final static int CONSTANT_Long = 5;
    final static int CONSTANT_Double = 6;
    final static int CONSTANT_Class = 7;
    final static int CONSTANT_String = 8;
    final static int CONSTANT_Fieldref = 9;
    final static int CONSTANT_Methodref = 10;
    final static int CONSTANT_InterfaceMethodref = 11;
    final static int CONSTANT_NameAndType = 12;

    // --- .java Opcode Constants ---
    final static int JAVA_ICONST_M1 = 0x02;
    final static int JAVA_ICONST_0 = 0x03;
    final static int JAVA_ICONST_1 = 0x04;
    final static int JAVA_ICONST_2 = 0x05;
    final static int JAVA_ICONST_3 = 0x06;
    final static int JAVA_ICONST_4 = 0x07;
    final static int JAVA_ICONST_5 = 0x08;
    final static int JAVA_LCONST_0 = 0x09;
    final static int JAVA_LCONST_1 = 0x0a;
    final static int JAVA_LDC = 0x12;
    final static int JAVA_LDC2_W = 0x14;
    final static int JAVA_ILOAD_0 = 0x1a;
    final static int JAVA_ILOAD_1 = 0x1b;
    final static int JAVA_ILOAD_2 = 0x1c;
    final static int JAVA_ILOAD_3 = 0x1d;
    final static int JAVA_LLOAD_0 = 0x1e;
    final static int JAVA_LLOAD_1 = 0x1f;
    final static int JAVA_LLOAD_2 = 0x20;
    final static int JAVA_LLOAD_3 = 0x21;
    final static int JAVA_DLOAD_0 = 0x26;
    final static int JAVA_DLOAD_1 = 0x27;
    final static int JAVA_DLOAD_2 = 0x28;
    final static int JAVA_DLOAD_3 = 0x29;
    final static int JAVA_ALOAD_0 = 0x2a;
    final static int JAVA_ALOAD_1 = 0x2b;
    final static int JAVA_ALOAD_2 = 0x2c;
    final static int JAVA_ALOAD_3 = 0x2d;
    final static int JAVA_IALOAD = 0x2e;
    final static int JAVA_AALOAD = 0x32;
    final static int JAVA_ISTORE_0 = 0x3b;
    final static int JAVA_ISTORE_1 = 0x3c;
    final static int JAVA_ISTORE_2 = 0x3d;
    final static int JAVA_ISTORE_3 = 0x3e;
    final static int JAVA_LSTORE_0 = 0x3f;
    final static int JAVA_LSTORE_1 = 0x40;
    final static int JAVA_LSTORE_2 = 0x41;
    final static int JAVA_LSTORE_3 = 0x42;
    final static int JAVA_DSTORE_0 = 0x47;
    final static int JAVA_DSTORE_1 = 0x48;
    final static int JAVA_DSTORE_2 = 0x49;
    final static int JAVA_DSTORE_3 = 0x4a;
    final static int JAVA_ASTORE_0 = 0x4b;
    final static int JAVA_ASTORE_1 = 0x4c;
    final static int JAVA_ASTORE_2 = 0x4d;
    final static int JAVA_ASTORE_3 = 0x4e;
    final static int JAVA_IASTORE = 0x4f;
    final static int JAVA_AASTORE = 0x53;
    final static int JAVA_DUP = 0x59;
    final static int JAVA_IADD = 0x60;
    final static int JAVA_LADD = 0x61;
    final static int JAVA_ISUB = 0x64;
    final static int JAVA_LSUB = 0x65;
    final static int JAVA_IFEQ = 0x99;
    final static int JAVA_IFNE = 0x9a;
    final static int JAVA_IF_ICMPEQ = 0x9f;
    final static int JAVA_IF_ICMPNE = 0xa0;
    final static int JAVA_GOTO = 0xa7;
    final static int JAVA_IRETURN = 0xac;
    final static int JAVA_LRETURN = 0xad;
    final static int JAVA_FRETURN = 0xae;
    final static int JAVA_DRETURN = 0xaf;
    final static int JAVA_ARETURN = 0xb0;
    final static int JAVA_RETURN = 0xb1;
    final static int JAVA_GETSTATIC = 0xb2;
    final static int JAVA_PUTSTATIC = 0xb3;
    final static int JAVA_GETFIELD = 0xb4;
    final static int JAVA_PUTFIELD = 0xb5;
    final static int JAVA_INVOKEVIRTUAL = 0xb6;
    final static int JAVA_INVOKESPECIAL = 0xb7;
    final static int JAVA_INVOKESTATIC = 0xb8;
    final static int JAVA_NEW = 0xbb;
    final static int JAVA_NEWARRAY = 0xbc;
    final static int JAVA_ANEWARRAY = 0xbd;
    final static int JAVA_ARRAYLENGTH = 0xbe;
    final static int JAVA_CHECKCAST = 0xc0;
    final static int JAVA_INSTANCEOF = 0xc1;
    
    // --- .dex Opcode Constants ---
    final static int DALVIK_NOP = 0x00;
    final static int DALVIK_MOVE = 0x01;
    final static int DALVIK_MOVE_FROM16 = 0x02;
    final static int DALVIK_MOVE_WIDE_FROM16 = 0x05;
    final static int DALVIK_MOVE_OBJECT = 0x07;
    final static int DALVIK_MOVE_OBJECT_FROM16 = 0x08;
    final static int DALVIK_RETURN_VOID = 0x0e;
    final static int DALVIK_RETURN = 0x0f;
    final static int DALVIK_RETURN_WIDE = 0x10;
    final static int DALVIK_RETURN_OBJECT = 0x11;
    final static int DALVIK_CONST_4 = 0x12;
    final static int DALVIK_CONST_16 = 0x13;
    final static int DALVIK_CONST_WIDE_16 = 0x15;
    final static int DALVIK_CONST_STRING = 0x1a;
    final static int DALVIK_CHECK_CAST = 0x1f;
    final static int DALVIK_INSTANCE_OF = 0x20;
    final static int DALVIK_ARRAY_LENGTH = 0x21;
    final static int DALVIK_NEW_INSTANCE = 0x22;
    final static int DALVIK_NEW_ARRAY = 0x23;
    final static int DALVIK_GOTO_16 = 0x29;
    final static int DALVIK_IF_EQ = 0x32;
    final static int DALVIK_IF_NE = 0x33;
    final static int DALVIK_IF_EQZ = 0x38;
    final static int DALVIK_IF_NEZ = 0x39;
    final static int DALVIK_AGET = 0x44;
    final static int DALVIK_AGET_OBJECT = 0x46;
    final static int DALVIK_APUT = 0x4b;
    final static int DALVIK_APUT_OBJECT = 0x4d;
    final static int DALVIK_IGET = 0x52;
    final static int DALVIK_IGET_OBJECT = 0x54;
    final static int DALVIK_IPUT = 0x5e;
    final static int DALVIK_IPUT_OBJECT = 0x60;
    final static int DALVIK_SGET = 0x60;
    final static int DALVIK_SGET_OBJECT = 0x62;
    final static int DALVIK_SPUT = 0x67;
    final static int DALVIK_SPUT_OBJECT = 0x69;
    final static int DALVIK_INVOKE_DIRECT = 0x6e;
    final static int DALVIK_INVOKE_DIRECT_RANGE = 0x74;
    final static int DALVIK_INVOKE_VIRTUAL_RANGE = 0x75;
    final static int DALVIK_INVOKE_STATIC_RANGE = 0x77;
    final static int DALVIK_INVOKE_INTERFACE_RANGE = 0x78;
    final static int DALVIK_ADD_INT = 0x90;
    final static int DALVIK_SUB_INT = 0x91;
    final static int DALVIK_LADD = 0x9b;
    final static int DALVIK_LSUB = 0x9c;
    
    // --- .dex Map Item Type Codes ---
    final static int TYPE_HEADER_ITEM = 0x0000;
    final static int TYPE_STRING_ID_ITEM = 0x0001;
    final static int TYPE_TYPE_ID_ITEM = 0x0002;
    final static int TYPE_PROTO_ID_ITEM = 0x0003;
    final static int TYPE_FIELD_ID_ITEM = 0x0004;
    final static int TYPE_METHOD_ID_ITEM = 0x0005;
    final static int TYPE_CLASS_DEF_ITEM = 0x0006;
    final static int TYPE_MAP_LIST = 0x1000;
    final static int TYPE_TYPE_LIST = 0x1001;
    final static int TYPE_CLASS_DATA_ITEM = 0x2000;
    final static int TYPE_CODE_ITEM = 0x2001;
    final static int TYPE_STRING_DATA_ITEM = 0x2002;

    // --- .class Parser Helper Classes ---

    /**
     * A simple helper class to read bytes from a byte array
     * and keep track of the current position.
     */
    static class ByteReader {
        byte[] data;
        int index; // Current position in the data

        public ByteReader(byte[] classData) {
            this.data = classData;
            this.index = 0;
        }

        public int readU1() {
            return (data[index++] & 0xFF);
        }

        public int readU2() {
            int val = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
            index += 2;
            return val;
        }

        public int readU4() {
            int val = ((data[index] & 0xFF) << 24) |
                      ((data[index + 1] & 0xFF) << 16) |
                      ((data[index + 2] & 0xFF) << 8)  |
                      ((data[index + 3] & 0xFF));
            index += 4;
            return val;
        }

        public byte[] readBytes(int length) {
            byte[] bytes = new byte[length];
            System.arraycopy(data, index, bytes, 0, length);
            index += length;
            return bytes;
        }
    }
    
    /** A simple class to hold the .class file header info. */
    static class ClassHeader {
        int magic;
        int minorVersion;
        int majorVersion;
    }

    /** Base class for all constant pool entries. */
    static class CpInfo {
        int tag;
        public CpInfo(int tag) { this.tag = tag; }
    }

    /** Stores a UTF-8 string (tag 1). */
    static class ConstantUtf8Info extends CpInfo {
        String value;
        public ConstantUtf8Info(String value) {
            super(CONSTANT_Utf8);
            this.value = value;
        }
    }

    /** Stores a reference to a class (tag 7). */
    static class ConstantClassInfo extends CpInfo {
        int nameIndex;
        public ConstantClassInfo(int nameIndex) {
            super(CONSTANT_Class);
            this.nameIndex = nameIndex;
        }
    }

    /** Stores a reference to a string literal (tag 8). */
    static class ConstantStringInfo extends CpInfo {
        int stringIndex;
        public ConstantStringInfo(int stringIndex) {
            super(CONSTANT_String);
            this.stringIndex = stringIndex;
        }
    }
    
    /** Stores an integer (tag 3). */
    static class ConstantIntegerInfo extends CpInfo {
        int value;
        public ConstantIntegerInfo(int value) {
            super(CONSTANT_Integer);
            this.value = value;
        }
    }

    /** Stores a float (tag 4). */
    static class ConstantFloatInfo extends CpInfo {
        float value;
        public ConstantFloatInfo(float value) {
            super(CONSTANT_Float);
            this.value = value;
        }
    }

    /** Stores a Long or Double (tag 5 or 6). */
    static class ConstantLargeNumericInfo extends CpInfo {
        long highBytes;
        long lowBytes;
        public ConstantLargeNumericInfo(int tag, long high, long low) {
            super(tag);
            this.highBytes = high;
            this.lowBytes = low;
        }
    }

    /** Stores Fieldref, Methodref, or InterfaceMethodref (tags 9, 10, 11). */
    static class ConstantRefInfo extends CpInfo {
        int classIndex;
        int nameAndTypeIndex;
        public ConstantRefInfo(int tag, int classIndex, int nameAndTypeIndex) {
            super(tag);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    /** Stores a name and type descriptor (tag 12). */
    static class ConstantNameAndTypeInfo extends CpInfo {
        int nameIndex;
        int descriptorIndex;
        public ConstantNameAndTypeInfo(int nameIndex, int descriptorIndex) {
            super(CONSTANT_NameAndType);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }
    }
    
    /** Stores the class's identity information. */
    static class ClassIdentity {
        int accessFlags;
        int thisClassIndex;
        int superClassIndex;
    }
    
    /** Base class for all attributes. */
    static class AttributeInfo {
        int attributeNameIndex;
        byte[] info;

        public AttributeInfo(int nameIndex, byte[] data) {
            this.attributeNameIndex = nameIndex;
            this.info = data;
        }
    }

    /** Stores all information about a single field. */
    static class FieldInfo {
        int accessFlags;
        int nameIndex;
        int descriptorIndex;
        AttributeInfo[] attributes;

        public FieldInfo(int flags, int nameIdx, int descIdx, AttributeInfo[] attrs) {
            this.accessFlags = flags;
            this.nameIndex = nameIdx;
            this.descriptorIndex = descIdx;
            this.attributes = attrs;
        }
    }

    /** Stores all information about a single method. */
    static class MethodInfo {
        int accessFlags;
        int nameIndex;
        int descriptorIndex;
        AttributeInfo[] attributes;

        public MethodInfo(int flags, int nameIdx, int descIdx, AttributeInfo[] attrs) {
            this.accessFlags = flags;
            this.nameIndex = nameIdx;
            this.descriptorIndex = descIdx;
            this.attributes = attrs;
        }
    }
    
    /** Stores a single entry in the Code attribute's exception table. */
    static class ExceptionTableEntry {
        int startPc;
        int endPc;
        int handlerPc;
        int catchType;

        public ExceptionTableEntry(int start, int end, int handler, int type) {
            this.startPc = start;
            this.endPc = end;
            this.handlerPc = handler;
            this.catchType = type;
        }
    }

    /** A container for all data parsed from a "Code" attribute. */
    static class CodeAttribute {
        int maxStack;
        int maxLocals;
        byte[] code;
        ExceptionTableEntry[] exceptionTable;
        AttributeInfo[] nestedAttributes;

        public CodeAttribute(int stack, int locals, byte[] code, 
                             ExceptionTableEntry[] table, AttributeInfo[] nested) {
            this.maxStack = stack;
            this.maxLocals = locals;
            this.code = code;
            this.exceptionTable = table;
            this.nestedAttributes = nested;
        }
    }
    
    /** A container class that holds all parsed .class file info. */
    static class ClassFile {
        ClassHeader header;
        CpInfo[] constantPool;
        ClassIdentity identity;
        int[] interfaces;
        FieldInfo[] fields;
        MethodInfo[] methods;
        AttributeInfo[] classAttributes;
    }

    // --- .dex Writer Helper Classes ---

    /**
     * A helper class to write bytes into a ByteBuffer,
     * automatically handling little-endian conversion.
     */
    static class ByteWriter {
        ByteBuffer buffer;
        
        public ByteWriter(int initialCapacity) {
            buffer = ByteBuffer.allocate(initialCapacity);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        
        public int getPosition() {
            return buffer.position();
        }
        
        public void writeU1(int value) {
            buffer.put((byte) (value & 0xFF));
        }

        public void writeU2(int value) {
            buffer.putShort((short) (value & 0xFFFF));
        }

        public void writeU4(int value) {
            buffer.putInt(value);
        }
        
        public void writeBytes(byte[] data) {
            buffer.put(data);
        }
        
        public void writeU4At(int offset, int value) {
            int oldPosition = buffer.position();
            buffer.position(offset);
            buffer.putInt(value);
            buffer.position(oldPosition);
        }
        
        /** Writes a 32-bit integer in unsigned LEB128 format. */
        public void writeUleb128(int value) {
            int remaining = value >>> 7;
            while (remaining != 0) {
                buffer.put((byte) ((value & 0x7F) | 0x80));
                value = remaining;
                remaining >>>= 7;
            }
            buffer.put((byte) (value & 0x7F));
        }
        
        /** Writes a 32-bit integer in signed LEB128 format. */
        public void writeSleb128(int value) {
            int remaining = value >> 7;
            boolean hasMore = true;
            int end = ((value & 0x80000000) == 0) ? 0 : -1;

            while (hasMore) {
                hasMore = (remaining != end) || 
                          ((remaining & 1) != ((value >> 6) & 1));
                
                buffer.put((byte) ((value & 0x7F) | (hasMore ? 0x80 : 0)));
                value = remaining;
                remaining >>= 7;
            }
        }
        
        /** Aligns the writer's position to a 4-byte boundary. */
        public int alignTo4() {
            int currentPosition = getPosition();
            int remainder = currentPosition % 4;
            if (remainder != 0) {
                int padding = 4 - remainder;
                for (int i = 0; i < padding; i++) {
                    writeU1(0); // Write null bytes
                }
            }
            return getPosition();
        }
        
        public byte[] toByteArray() {
            byte[] result = new byte[buffer.position()];
            buffer.rewind();
            buffer.get(result, 0, result.length);
            return result;
        }
    }
    
    /** A container for the .dex file header data. */
    static class DexHeader {
        public static final byte[] DEX_FILE_MAGIC = { 
            0x64, 0x65, 0x78, 0x0A, 0x30, 0x33, 0x35, 0x00 
        };
        public static final int ENDIAN_CONSTANT = 0x12345678;
        public static final int HEADER_SIZE = 0x70; // 112 bytes
        public static final byte[] NO_SIGNATURE = new byte[20];
    }
    
    /** Container for the result of writing the string section. */
    static class StringSection {
        Map<String, Integer> stringIdMap;
        int stringIdsSize;
        int stringIdsOff;

        public StringSection(Map<String, Integer> map, int size, int offset) {
            this.stringIdMap = map;
            this.stringIdsSize = size;
            this.stringIdsOff = offset;
        }
    }
    
    /** Container for the result of writing the type section. */
    static class TypeSection {
        Map<String, Integer> typeIdMap;
        int typeIdsSize;
        int typeIdsOff;

        public TypeSection(Map<String, Integer> map, int size, int offset) {
            this.typeIdMap = map;
            this.typeIdsSize = size;
            this.typeIdsOff = offset;
        }
    }
    
    /** A simple class to hold a parsed method descriptor. */
    static class MethodDescriptor {
        String returnType;
        List<String> parameters;

        public MethodDescriptor(String ret, List<String> params) {
            this.returnType = ret;
            this.parameters = params;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MethodDescriptor that = (MethodDescriptor) obj;
            return returnType.equals(that.returnType) && 
                   parameters.equals(that.parameters);
        }

        @Override
        public int hashCode() {
            return 31 * returnType.hashCode() + parameters.hashCode();
        }
    }
    
    /** Container for the result of writing the proto section. */
    static class ProtoSection {
        Map<MethodDescriptor, Integer> protoIdMap;
        int protoIdsSize;
        int protoIdsOff;

        public ProtoSection(Map<MethodDescriptor, Integer> map, int size, int offset) {
            this.protoIdMap = map;
            this.protoIdsSize = size;
            this.protoIdsOff = offset;
        }
    }
    
    /** A helper class representing a Field ID. */
    static class DexFieldId implements Comparable<DexFieldId> {
        int classIdx;
        int typeIdx;
        int nameIdx;

        public DexFieldId(int classIdx, int typeIdx, int nameIdx) {
            this.classIdx = classIdx;
            this.typeIdx = typeIdx;
            this.nameIdx = nameIdx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DexFieldId that = (DexFieldId) o;
            return classIdx == that.classIdx &&
                   typeIdx == that.typeIdx &&
                   nameIdx == that.nameIdx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(classIdx, typeIdx, nameIdx);
        }

        @Override
        public int compareTo(DexFieldId other) {
            if (this.classIdx != other.classIdx) {
                return Integer.compare(this.classIdx, other.classIdx);
            }
            if (this.nameIdx != other.nameIdx) {
                return Integer.compare(this.nameIdx, other.nameIdx);
            }
            return Integer.compare(this.typeIdx, other.typeIdx);
        }
    }
    
    /** Container for the result of writing the field section. */
    static class FieldSection {
        Map<DexFieldId, Integer> fieldIdMap;
        int fieldIdsSize;
        int fieldIdsOff;

        public FieldSection(Map<DexFieldId, Integer> map, int size, int offset) {
            this.fieldIdMap = map;
            this.fieldIdsSize = size;
            this.fieldIdsOff = offset;
        }
    }
    
    /** A helper class representing a Method ID. */
    static class DexMethodId implements Comparable<DexMethodId> {
        int classIdx;
        int protoIdx;
        int nameIdx;

        public DexMethodId(int classIdx, int protoIdx, int nameIdx) {
            this.classIdx = classIdx;
            this.protoIdx = protoIdx;
            this.nameIdx = nameIdx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DexMethodId that = (DexMethodId) o;
            return classIdx == that.classIdx &&
                   protoIdx == that.protoIdx &&
                   nameIdx == that.nameIdx;
        }

        @Override
        public int hashCode() {
            return Objects.hash(classIdx, protoIdx, nameIdx);
        }

        @Override
        public int compareTo(DexMethodId other) {
            if (this.classIdx != other.classIdx) {
                return Integer.compare(this.classIdx, other.classIdx);
            }
            if (this.nameIdx != other.nameIdx) {
                return Integer.compare(this.nameIdx, other.nameIdx);
            }
            return Integer.compare(this.protoIdx, other.protoIdx);
        }
    }

    /** Container for the result of writing the method section. */
    static class MethodSection {
        Map<DexMethodId, Integer> methodIdMap;
        int methodIdsSize;
        int methodIdsOff;

        public MethodSection(Map<DexMethodId, Integer> map, int size, int offset) {
            this.methodIdMap = map;
            this.methodIdsSize = size;
            this.methodIdsOff = offset;
        }
    }
    
    /** A container for the 32-byte class_def_item. */
    static class ClassDefItem {
        int classIdx, accessFlags, superclassIdx;
        int interfacesOff, sourceFileIdx, annotationsOff;
        int classDataOff, staticValuesOff;
        
        public static final int NO_INDEX = 0xFFFFFFFF; // -1
        public static final int NO_OFFSET = 0;
    }
    
    /** Container for the result of writing the class_def. */
    static class ClassDefSection {
        int classDefOffset;
        public ClassDefSection(int offset) {
            this.classDefOffset = offset;
        }
    }

    /** A helper class for an encoded field. */
    static class DexEncodedField {
        int fieldIdx;
        int accessFlags;
        public DexEncodedField(int index, int flags) {
            this.fieldIdx = index;
            this.accessFlags = flags;
        }
    }

    /** A helper class for an encoded method. */
    static class DexEncodedMethod {
        int methodIdx;
        int accessFlags;
        int codeOff;
        public DexEncodedMethod(int index, int flags, int codeOffset) {
            this.methodIdx = index;
            this.accessFlags = flags;
            this.codeOff = codeOffset;
        }
    }

    /** A container for a 16-byte code_item header and data. */
    static class CodeItem {
        int registersSize;
        int insSize;
        int outsSize;
        int triesSize;
        int debugInfoOff;
        int insnsSize;
        short[] insns; 
        int maxOuts = 0;
        
        List<DalvikTryItem> tries = new ArrayList<>();
        List<DalvikHandlerList> handlerLists = new ArrayList<>();
    }

    /** Simulates the Java stack and maps stack items to Dalvik registers. */
    static class StackTracker {
        int stackPointer;
        int registersSize;
        int insSize;
        
        public StackTracker(int registersSize, int insSize) {
            this.registersSize = registersSize;
            this.insSize = insSize;
            this.stackPointer = 0;
        }
        
        public int getLocalRegister(int javaLocalIndex) {
            int parameterBase = registersSize - insSize;
            return parameterBase + javaLocalIndex;
        }
        
        public int push() {
            int registerForThisItem = stackPointer;
            stackPointer++;
            return registerForThisItem;
        }
        
        public int pop() {
            stackPointer--;
            return stackPointer;
        }
        
        public int peek() {
            return stackPointer - 1;
        }
        
        public int pushWide() {
            int registerForThisItem = stackPointer;
            stackPointer += 2;
            return registerForThisItem;
        }
        
        public int popWide() {
            stackPointer -= 2;
            return stackPointer;
        }
        
        public void reset() {
            stackPointer = 0;
        }
    }

    /** A container to return a field's ID and its type. */
    static class ResolvedField {
        int fieldId;
        String fieldType;
        public ResolvedField(int id, String type) {
            this.fieldId = id;
            this.fieldType = type;
        }
    }

    /** A 12-byte structure for a single entry in the map_list. */
    static class MapItem {
        int type, size, offset;
        public MapItem(int type, int size, int offset) {
            this.type = type;
            this.size = size;
            this.offset = offset;
        }
    }
    
    /** A helper class to build the map_list. */
    static class MapListBuilder {
        private List<MapItem> items = new ArrayList<>();

        public void add(int type, int size, int offset) {
            if (size > 0) {
                items.add(new MapItem(type, size, offset));
            }
        }
        
        public int write(ByteWriter writer) {
            writer.alignTo4();
            int mapListOffset = writer.getPosition();
            items.sort(Comparator.comparingInt(item -> item.offset));
            int mapSize = items.size();
            writer.writeU4(mapSize);
            for (MapItem item : items) {
                writer.writeU2(item.type);
                writer.writeU2(0); // 'unused'
                writer.writeU4(item.size);
                writer.writeU4(item.offset);
            }
            return mapListOffset;
        }
    }
    
    /** A helper class to store a "fixup" task for Pass 2. */
    static class BranchFixup {
        int dalvikInsnIndex;
        int javaTargetOffset;
        int javaOpcode;
        int registerToTest;
        int registerToTest2; // For 2-operand compares

        public BranchFixup(int dalvikIndex, int javaTarget, int opcode, int reg1) {
            this(dalvikIndex, javaTarget, opcode, reg1, 0);
        }
        
        public BranchFixup(int dalvikIndex, int javaTarget, int opcode, int reg1, int reg2) {
            this.dalvikInsnIndex = dalvikIndex;
            this.javaTargetOffset = javaTarget;
            this.javaOpcode = opcode;
            this.registerToTest = reg1;
            this.registerToTest2 = reg2;
        }
    }
    
    /** Represents a Dalvik try_item (8 bytes). */
    static class DalvikTryItem {
        int startAddr, insnCount, handlerOff;
        public DalvikTryItem(int start, int count, int offset) {
            this.startAddr = start;
            this.insnCount = count;
            this.handlerOff = offset;
        }
    }

    /** Represents a single catch block: (type, address). */
    static class DalvikCatchHandler {
        int typeId, javaHandlerPc, dalvikHandlerPc;
        public DalvikCatchHandler(int typeId, int javaPc) {
            this.typeId = typeId;
            this.javaHandlerPc = javaPc;
        }
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            DalvikCatchHandler that = (DalvikCatchHandler) o;
            return typeId == that.typeId && javaHandlerPc == that.javaHandlerPc;
        }
        @Override public int hashCode() { return Objects.hash(typeId, javaHandlerPc); }
    }

    /** Represents a list of handlers for a single 'try'. */
    static class DalvikHandlerList {
        List<DalvikCatchHandler> handlers = new ArrayList<>();
        int javaCatchAllPc = -1;
        int dalvikCatchAllPc = -1;
        
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            DalvikHandlerList that = (DalvikHandlerList) o;
            return javaCatchAllPc == that.javaCatchAllPc &&
                   handlers.equals(that.handlers);
        }
        @Override public int hashCode() { return Objects.hash(handlers, javaCatchAllPc); }
    }


    // =========================================================================
    // PARSER METHODS
    // =========================================================================

    /** Parses the 8-byte header from a .class file's byte array. */
    public static ClassHeader parseClassHeader(byte[] classData) throws Exception {
        if (classData.length < 8) {
            throw new Exception("Data is too short to be a .class file.");
        }
        int magic = ((classData[0] & 0xFF) << 24) |
                    ((classData[1] & 0xFF) << 16) |
                    ((classData[2] & 0xFF) << 8)  |
                    ((classData[3] & 0xFF));
        if (magic != 0xCAFEBABE) {
            throw new Exception("Invalid .class file: Magic number is not 0xCAFEBABE.");
        }
        int minor = ((classData[4] & 0xFF) << 8) | (classData[5] & 0xFF);
        int major = ((classData[6] & 0xFF) << 8) | (classData[7] & 0xFF);
        ClassHeader header = new ClassHeader();
        header.magic = magic;
        header.minorVersion = minor;
        header.majorVersion = major;
        return header;
    }

    /** Parses the constant pool from a .class file. */
    public static CpInfo[] parseConstantPool(ByteReader reader) throws Exception {
        int constantPoolCount = reader.readU2();
        CpInfo[] constantPool = new CpInfo[constantPoolCount];
        for (int i = 1; i < constantPoolCount; i++) {
            int tag = reader.readU1();
            switch (tag) {
                case CONSTANT_Utf8:
                    int length = reader.readU2();
                    byte[] bytes = reader.readBytes(length);
                    String utf8String = new String(bytes, "UTF-8");
                    constantPool[i] = new ConstantUtf8Info(utf8String);
                    break;
                case CONSTANT_Integer:
                    constantPool[i] = new ConstantIntegerInfo(reader.readU4());
                    break;
                case CONSTANT_Float:
                    constantPool[i] = new ConstantFloatInfo(Float.intBitsToFloat(reader.readU4()));
                    break;
                case CONSTANT_Long:
                case CONSTANT_Double:
                    long high = reader.readU4();
                    long low = reader.readU4();
                    constantPool[i] = new ConstantLargeNumericInfo(tag, high, low);
                    i++; // Longs and Doubles take two slots
                    break;
                case CONSTANT_Class:
                    constantPool[i] = new ConstantClassInfo(reader.readU2());
                    break;
                case CONSTANT_String:
                    constantPool[i] = new ConstantStringInfo(reader.readU2());
                    break;
                case CONSTANT_Fieldref:
                case CONSTANT_Methodref:
                case CONSTANT_InterfaceMethodref:
                    constantPool[i] = new ConstantRefInfo(tag, reader.readU2(), reader.readU2());
                    break;
                case CONSTANT_NameAndType:
                    constantPool[i] = new ConstantNameAndTypeInfo(reader.readU2(), reader.readU2());
                    break;
                default:
                    throw new Exception("Unsupported constant pool tag: " + tag);
            }
        }
        return constantPool;
    }

    /** Parses the class identity block (access_flags, this_class, super_class). */
    public static ClassIdentity parseClassIdentity(ByteReader reader) {
        ClassIdentity identity = new ClassIdentity();
        identity.accessFlags = reader.readU2();
        identity.thisClassIndex = reader.readU2();
        identity.superClassIndex = reader.readU2();
        return identity;
    }

    /** Parses the list of implemented interfaces. */
    public static int[] parseInterfaces(ByteReader reader) {
        int interfaceCount = reader.readU2();
        int[] interfaces = new int[interfaceCount];
        for (int i = 0; i < interfaceCount; i++) {
            interfaces[i] = reader.readU2();
        }
        return interfaces;
    }

    /** Parses the list of attributes for a field or method. */
    private static AttributeInfo[] parseAttributes(ByteReader reader) {
        int attributesCount = reader.readU2();
        AttributeInfo[] attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            int attributeNameIndex = reader.readU2();
            int attributeLength = reader.readU4();
            byte[] attributeData = reader.readBytes(attributeLength);
            attributes[i] = new AttributeInfo(attributeNameIndex, attributeData);
        }
        return attributes;
    }

    /** Parses the main list of fields for the class. */
    public static FieldInfo[] parseFields(ByteReader reader) {
        int fieldsCount = reader.readU2();
        FieldInfo[] fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            int accessFlags = reader.readU2();
            int nameIndex = reader.readU2();
            int descriptorIndex = reader.readU2();
            AttributeInfo[] attributes = parseAttributes(reader);
            fields[i] = new FieldInfo(accessFlags, nameIndex, descriptorIndex, attributes);
        }
        return fields;
    }

    /** Parses the main list of methods for the class. */
    public static MethodInfo[] parseMethods(ByteReader reader) {
        int methodsCount = reader.readU2();
        MethodInfo[] methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            int accessFlags = reader.readU2();
            int nameIndex = reader.readU2();
            int descriptorIndex = reader.readU2();
            AttributeInfo[] attributes = parseAttributes(reader);
            methods[i] = new MethodInfo(accessFlags, nameIndex, descriptorIndex, attributes);
        }
        return methods;
    }

    /** Parses a full .class file from a byte array. */
    public static ClassFile parseClassFile(byte[] classData) throws Exception {
        ByteReader reader = new ByteReader(classData);
        ClassFile classFile = new ClassFile();

        classFile.header = parseClassHeader(reader.data);
        reader.index = 8;
        classFile.constantPool = parseConstantPool(reader);
        classFile.identity = parseClassIdentity(reader);
        classFile.interfaces = parseInterfaces(reader);
        classFile.fields = parseFields(reader);
        classFile.methods = parseMethods(reader);
        classFile.classAttributes = parseAttributes(reader);

        return classFile;
    }

    /** Searches an attribute array for a specific attribute by name. */
    public static AttributeInfo findAttribute(CpInfo[] constantPool, 
                                              AttributeInfo[] attributes, 
                                              String attributeName) throws Exception {
        for (AttributeInfo attr : attributes) {
            if (constantPool[attr.attributeNameIndex].tag != CONSTANT_Utf8) {
                continue;
            }
            ConstantUtf8Info nameInfo = (ConstantUtf8Info) constantPool[attr.attributeNameIndex];
            if (nameInfo.value.equals(attributeName)) {
                return attr;
            }
        }
        return null;
    }

    /** Parses the raw bytes of a "Code" attribute. */
    public static CodeAttribute parseCodeAttribute(AttributeInfo codeAttrInfo) {
        ByteReader reader = new ByteReader(codeAttrInfo.info);
        int maxStack = reader.readU2();
        int maxLocals = reader.readU2();
        int codeLength = reader.readU4();
        byte[] code = reader.readBytes(codeLength);
        int exceptionTableLength = reader.readU2();
        ExceptionTableEntry[] exceptionTable = new ExceptionTableEntry[exceptionTableLength];
        for (int i = 0; i < exceptionTableLength; i++) {
            exceptionTable[i] = new ExceptionTableEntry(
                reader.readU2(), reader.readU2(), reader.readU2(), reader.readU2()
            );
        }
        AttributeInfo[] nestedAttributes = parseAttributes(reader);
        return new CodeAttribute(maxStack, maxLocals, code, exceptionTable, nestedAttributes);
    }
    
    /** Parses a Java method descriptor string. */
    public static MethodDescriptor parseMethodDescriptor(String descriptor) {
        List<String> parameters = new ArrayList<>();
        int closeParen = descriptor.indexOf(')');
        String paramString = descriptor.substring(1, closeParen);
        int index = 0;
        while (index < paramString.length()) {
            char c = paramString.charAt(index);
            int startIndex = index;
            if (c == '[') {
                while (paramString.charAt(index) == '[') index++;
                if (paramString.charAt(index) == 'L') {
                    index = paramString.indexOf(';', index) + 1;
                } else {
                    index++;
                }
            } else if (c == 'L') {
                index = paramString.indexOf(';', index) + 1;
            } else {
                index++;
            }
            parameters.add(paramString.substring(startIndex, index));
        }
        String returnType = descriptor.substring(closeParen + 1);
        return new MethodDescriptor(returnType, parameters);
    }

    /** Creates the "shorty" string from a descriptor. */
    public static String createShorty(MethodDescriptor desc) {
        StringBuilder shorty = new StringBuilder();
        shorty.append(desc.returnType.charAt(0) == '[' ? 'L' : desc.returnType.charAt(0));
        for (String param : desc.parameters) {
            shorty.append(param.charAt(0) == '[' ? 'L' : param.charAt(0));
        }
        return shorty.toString();
    }
    
    // =========================================================================
    // .DEX FILE WRITER METHODS
    // =========================================================================

    /** Writes a (mostly empty) DexHeader to the ByteWriter. */
    public static void writeEmptyHeader(ByteWriter writer) {
        writer.writeBytes(DexHeader.DEX_FILE_MAGIC); // 8
        writer.writeU4(0); // checksum (placeholder)
        writer.writeBytes(DexHeader.NO_SIGNATURE); // 20
        writer.writeU4(0); // file_size (placeholder)
        writer.writeU4(DexHeader.HEADER_SIZE); // header_size (0x70)
        writer.writeU4(DexHeader.ENDIAN_CONSTANT); // endian_tag
        writer.writeU4(0); // link_size
        writer.writeU4(0); // link_off
        writer.writeU4(0); // map_off
        writer.writeU4(0); // string_ids_size
        writer.writeU4(0); // string_ids_off
        writer.writeU4(0); // type_ids_size
        writer.writeU4(0); // type_ids_off
        writer.writeU4(0); // proto_ids_size
        writer.writeU4(0); // proto_ids_off
        writer.writeU4(0); // field_ids_size
        writer.writeU4(0); // field_ids_off
        writer.writeU4(0); // method_ids_size
        writer.writeU4(0); // method_ids_off
        writer.writeU4(0); // class_defs_size
        writer.writeU4(0); // class_defs_off
        writer.writeU4(0); // data_size
        writer.writeU4(0); // data_off
    }

    /** Collects/writes all strings and records them in the map builder. */
    public static StringSection writeStrings(ByteWriter writer, CpInfo[] constantPool,
                                            MapListBuilder mapBuilder) throws Exception {
        Set<String> sortedStrings = new TreeSet<>();
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] != null && constantPool[i].tag == CONSTANT_Utf8) {
                sortedStrings.add(((ConstantUtf8Info) constantPool[i]).value);
            }
        }

        // Also add shorty strings for all method descriptors
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] == null || (constantPool[i].tag != CONSTANT_Methodref &&
                                            constantPool[i].tag != CONSTANT_InterfaceMethodref)) continue;
            ConstantRefInfo methodRef = (ConstantRefInfo) constantPool[i];
            ConstantNameAndTypeInfo nameAndType = (ConstantNameAndTypeInfo) constantPool[methodRef.nameAndTypeIndex];
            String descriptorString = ((ConstantUtf8Info) constantPool[nameAndType.descriptorIndex]).value;
            MethodDescriptor desc = parseMethodDescriptor(descriptorString);
            String shorty = createShorty(desc);
            sortedStrings.add(shorty);
        }
        int stringCount = sortedStrings.size();
        int[] stringDataOffsets = new int[stringCount];
        Map<String, Integer> stringIdMap = new HashMap<>();
        
        int stringIdsOffset = writer.getPosition();
        mapBuilder.add(TYPE_STRING_ID_ITEM, stringCount, stringIdsOffset);
        for (int i = 0; i < stringCount; i++) writer.writeU4(0);

        int stringDataStartOffset = writer.getPosition();
        int stringIndex = 0;
        for (String s : sortedStrings) {
            stringIdMap.put(s, stringIndex);
            stringDataOffsets[stringIndex] = writer.getPosition();
            byte[] bytes = s.getBytes("UTF-8");
            writer.writeUleb128(bytes.length);
            writer.writeBytes(bytes);
            writer.writeU1(0); // Null terminator
            stringIndex++;
        }
        mapBuilder.add(TYPE_STRING_DATA_ITEM, 1, stringDataStartOffset);

        int currentPosition = writer.getPosition();
        writer.buffer.position(stringIdsOffset);
        for (int offset : stringDataOffsets) writer.writeU4(offset);
        writer.buffer.position(currentPosition);

        writer.writeU4At(0x38, stringCount);
        writer.writeU4At(0x3C, stringIdsOffset);
        
        return new StringSection(stringIdMap, stringCount, stringIdsOffset);
    }

    /** Collects/writes all class types and records them. */
    public static TypeSection writeTypes(ByteWriter writer, CpInfo[] constantPool, 
                                         StringSection stringSection,
                                         MapListBuilder mapBuilder) throws Exception {
        Set<String> sortedTypeNames = new TreeSet<>();

        // Add object types from CONSTANT_Class
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] != null && constantPool[i].tag == CONSTANT_Class) {
                ConstantClassInfo classInfo = (ConstantClassInfo) constantPool[i];
                ConstantUtf8Info utf8Info = (ConstantUtf8Info) constantPool[classInfo.nameIndex];
                sortedTypeNames.add(utf8Info.value);
            }
        }

        // Add types from all method/field descriptors (primitives, arrays)
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] == null) continue;
            if (constantPool[i].tag == CONSTANT_Methodref || 
                constantPool[i].tag == CONSTANT_InterfaceMethodref ||
                constantPool[i].tag == CONSTANT_Fieldref) {
                ConstantRefInfo ref = (ConstantRefInfo) constantPool[i];
                ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) constantPool[ref.nameAndTypeIndex];
                String desc = ((ConstantUtf8Info) constantPool[nat.descriptorIndex]).value;

                // Extract types from descriptor
                if (constantPool[i].tag == CONSTANT_Fieldref) {
                    // Field: type
                    sortedTypeNames.add(desc);
                } else {
                    // Method: (params)return
                    MethodDescriptor md = parseMethodDescriptor(desc);
                    sortedTypeNames.add(md.returnType);
                    for (String param : md.parameters) {
                        sortedTypeNames.add(param);
                    }
                }
            }
        }

        int typeCount = sortedTypeNames.size();
        Map<String, Integer> typeIdMap = new HashMap<>();

        writer.alignTo4();
        int typeIdsOffset = writer.getPosition();
        mapBuilder.add(TYPE_TYPE_ID_ITEM, typeCount, typeIdsOffset);
        
        int typeIndex = 0;
        for (String typeName : sortedTypeNames) {
            Integer stringId = stringSection.stringIdMap.get(typeName);
            if (stringId == null) throw new Exception("Missing string ID for type: " + typeName);
            writer.writeU4(stringId);
            typeIdMap.put(typeName, typeIndex);
            typeIndex++;
        }
        
        writer.writeU4At(0x40, typeCount);
        writer.writeU4At(0x44, typeIdsOffset);
        return new TypeSection(typeIdMap, typeCount, typeIdsOffset);
    }

    /** Collects/writes all method protos and records them. */
    public static ProtoSection writeProtos(ByteWriter writer, CpInfo[] constantPool,
                                           StringSection stringSection, TypeSection typeSection,
                                           MapListBuilder mapBuilder) throws Exception {
        Map<MethodDescriptor, Integer> protoIdMap = new LinkedHashMap<>();
        Map<List<Integer>, Integer> typeListOffsetMap = new HashMap<>();
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] == null || (constantPool[i].tag != CONSTANT_Methodref && 
                                            constantPool[i].tag != CONSTANT_InterfaceMethodref)) continue;
            ConstantRefInfo methodRef = (ConstantRefInfo) constantPool[i];
            ConstantNameAndTypeInfo nameAndType = (ConstantNameAndTypeInfo) constantPool[methodRef.nameAndTypeIndex];
            String descriptorString = ((ConstantUtf8Info) constantPool[nameAndType.descriptorIndex]).value;
            MethodDescriptor desc = parseMethodDescriptor(descriptorString);
            if (!protoIdMap.containsKey(desc)) {
                protoIdMap.put(desc, protoIdMap.size());
            }
        }
        int protoCount = protoIdMap.size();
        int[] shortyStringIds = new int[protoCount];
        int[] returnTypeIds = new int[protoCount];
        int[] paramListOffsets = new int[protoCount];

        writer.alignTo4();
        int typeListStartOffset = writer.getPosition();
        int typeListCount = 0;
        
        for (MethodDescriptor desc : protoIdMap.keySet()) {
            int protoId = protoIdMap.get(desc);
            if (desc.parameters.isEmpty()) {
                paramListOffsets[protoId] = 0;
            } else {
                List<Integer> paramTypeIds = new ArrayList<>();
                for (String paramString : desc.parameters) {
                    Integer paramTypeId = typeSection.typeIdMap.get(paramString);
                    if (paramTypeId == null) {
                        throw new Exception("Missing type ID for parameter: " + paramString);
                    }
                    paramTypeIds.add(paramTypeId);
                }
                if (typeListOffsetMap.containsKey(paramTypeIds)) {
                    paramListOffsets[protoId] = typeListOffsetMap.get(paramTypeIds);
                } else {
                    int newOffset = writer.getPosition();
                    writer.writeU4(paramTypeIds.size());
                    for (Integer typeId : paramTypeIds) writer.writeU2(typeId);
                    if (paramTypeIds.size() % 2 != 0) writer.writeU2(0);
                    paramListOffsets[protoId] = newOffset;
                    typeListOffsetMap.put(paramTypeIds, newOffset);
                    typeListCount++;
                }
            }
            String shorty = createShorty(desc);
            shortyStringIds[protoId] = stringSection.stringIdMap.get(shorty);
            Integer returnTypeId = typeSection.typeIdMap.get(desc.returnType);
            if (returnTypeId == null) {
                throw new Exception("Missing type ID for return type: " + desc.returnType);
            }
            returnTypeIds[protoId] = returnTypeId;
        }
        mapBuilder.add(TYPE_TYPE_LIST, typeListCount, typeListStartOffset);

        writer.alignTo4();
        int protoIdsOffset = writer.getPosition();
        mapBuilder.add(TYPE_PROTO_ID_ITEM, protoCount, protoIdsOffset);
        for (int i = 0; i < protoCount; i++) {
            writer.writeU4(shortyStringIds[i]);
            writer.writeU4(returnTypeIds[i]);
            writer.writeU4(paramListOffsets[i]);
        }
        
        writer.writeU4At(0x48, protoCount);
        writer.writeU4At(0x4C, protoIdsOffset);
        return new ProtoSection(protoIdMap, protoCount, protoIdsOffset);
    }
    
    /** Collects/writes the field_id_list and records it. */
    public static FieldSection writeFields(ByteWriter writer, CpInfo[] constantPool,
                                           StringSection stringSection, TypeSection typeSection,
                                           MapListBuilder mapBuilder) throws Exception {
        Set<DexFieldId> sortedFields = new TreeSet<>();
        Map<String, Integer> stringIdMap = stringSection.stringIdMap;
        Map<String, Integer> typeIdMap = typeSection.typeIdMap;
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] == null || constantPool[i].tag != CONSTANT_Fieldref) continue;
            ConstantRefInfo fieldRef = (ConstantRefInfo) constantPool[i];
            ConstantClassInfo classInfo = (ConstantClassInfo) constantPool[fieldRef.classIndex];
            ConstantNameAndTypeInfo nameAndType = (ConstantNameAndTypeInfo) constantPool[fieldRef.nameAndTypeIndex];
            String className = ((ConstantUtf8Info) constantPool[classInfo.nameIndex]).value;
            String fieldName = ((ConstantUtf8Info) constantPool[nameAndType.nameIndex]).value;
            String fieldType = ((ConstantUtf8Info) constantPool[nameAndType.descriptorIndex]).value;
            Integer classDexIdx = typeIdMap.get(className);
            Integer typeDexIdx = typeIdMap.get(fieldType);
            Integer nameDexIdx = stringIdMap.get(fieldName);
            if (classDexIdx == null || typeDexIdx == null || nameDexIdx == null) continue;
            sortedFields.add(new DexFieldId(classDexIdx, typeDexIdx, nameDexIdx));
        }

        writer.alignTo4();
        int fieldIdsOffset = writer.getPosition();
        int fieldCount = sortedFields.size();
        Map<DexFieldId, Integer> fieldIdMap = new HashMap<>();
        mapBuilder.add(TYPE_FIELD_ID_ITEM, fieldCount, fieldIdsOffset);

        int fieldIndex = 0;
        for (DexFieldId fieldId : sortedFields) {
            writer.writeU2(fieldId.classIdx);
            writer.writeU2(fieldId.typeIdx);
            writer.writeU4(fieldId.nameIdx);
            fieldIdMap.put(fieldId, fieldIndex++);
        }
        
        writer.writeU4At(0x50, fieldCount);
        writer.writeU4At(0x54, fieldIdsOffset);
        return new FieldSection(fieldIdMap, fieldCount, fieldIdsOffset);
    }
    
    /** Collects/writes the method_id_list and records it. */
    public static MethodSection writeMethods(ByteWriter writer, CpInfo[] constantPool,
                                             StringSection stringSection, TypeSection typeSection,
                                             ProtoSection protoSection,
                                             MapListBuilder mapBuilder) throws Exception {
        Set<DexMethodId> sortedMethods = new TreeSet<>();
        Map<String, Integer> stringIdMap = stringSection.stringIdMap;
        Map<String, Integer> typeIdMap = typeSection.typeIdMap;
        Map<MethodDescriptor, Integer> protoIdMap = protoSection.protoIdMap;
        for (int i = 1; i < constantPool.length; i++) {
            if (constantPool[i] == null || (constantPool[i].tag != CONSTANT_Methodref &&
                                            constantPool[i].tag != CONSTANT_InterfaceMethodref)) continue;
            ConstantRefInfo methodRef = (ConstantRefInfo) constantPool[i];
            ConstantClassInfo classInfo = (ConstantClassInfo) constantPool[methodRef.classIndex];
            ConstantNameAndTypeInfo nameAndType = (ConstantNameAndTypeInfo) constantPool[methodRef.nameAndTypeIndex];
            String className = ((ConstantUtf8Info) constantPool[classInfo.nameIndex]).value;
            String methodName = ((ConstantUtf8Info) constantPool[nameAndType.nameIndex]).value;
            String methodDesc = ((ConstantUtf8Info) constantPool[nameAndType.descriptorIndex]).value;
            Integer classDexIdx = typeIdMap.get(className);
            Integer nameDexIdx = stringIdMap.get(methodName);
            MethodDescriptor desc = parseMethodDescriptor(methodDesc);
            Integer protoDexIdx = protoIdMap.get(desc);
            if (classDexIdx == null || nameDexIdx == null || protoDexIdx == null) continue;
            sortedMethods.add(new DexMethodId(classDexIdx, protoDexIdx, nameDexIdx));
        }

        writer.alignTo4();
        int methodIdsOffset = writer.getPosition();
        int methodCount = sortedMethods.size();
        Map<DexMethodId, Integer> methodIdMap = new HashMap<>();
        mapBuilder.add(TYPE_METHOD_ID_ITEM, methodCount, methodIdsOffset);

        int methodIndex = 0;
        for (DexMethodId methodId : sortedMethods) {
            writer.writeU2(methodId.classIdx);
            writer.writeU2(methodId.protoIdx);
            writer.writeU4(methodId.nameIdx);
            methodIdMap.put(methodId, methodIndex++);
        }
        
        writer.writeU4At(0x58, methodCount);
        writer.writeU4At(0x5C, methodIdsOffset);
        return new MethodSection(methodIdMap, methodCount, methodIdsOffset);
    }
    
    /** Writes the class_def_item list and records it. */
    public static ClassDefSection writeClassDef(ByteWriter writer, ClassFile classFile, 
                                                TypeSection typeSection, StringSection stringSection,
                                                MapListBuilder mapBuilder) throws Exception {
        ConstantClassInfo thisClassInfo = (ConstantClassInfo) classFile.constantPool[classFile.identity.thisClassIndex];
        String thisClassName = ((ConstantUtf8Info) classFile.constantPool[thisClassInfo.nameIndex]).value;
        ConstantClassInfo superClassInfo = (ConstantClassInfo) classFile.constantPool[classFile.identity.superClassIndex];
        String superClassName = ((ConstantUtf8Info) classFile.constantPool[superClassInfo.nameIndex]).value;
        Integer classDexIdx = typeSection.typeIdMap.get(thisClassName);
        Integer superclassDexIdx = typeSection.typeIdMap.get(superClassName);
        if (classDexIdx == null || superclassDexIdx == null) {
            throw new Exception("Missing type ID for class or superclass");
        }
        int accessFlags = classFile.identity.accessFlags;
        int sourceFileDexIdx = ClassDefItem.NO_INDEX;

        writer.alignTo4();
        int classDefOffset = writer.getPosition();
        int classDefCount = 1;
        mapBuilder.add(TYPE_CLASS_DEF_ITEM, classDefCount, classDefOffset);

        writer.writeU4(classDexIdx);
        writer.writeU4(accessFlags);
        writer.writeU4(superclassDexIdx);
        writer.writeU4(ClassDefItem.NO_OFFSET); // interfaces_off
        writer.writeU4(sourceFileDexIdx);       // source_file_idx
        writer.writeU4(ClassDefItem.NO_OFFSET); // annotations_off
        writer.writeU4(ClassDefItem.NO_OFFSET); // class_data_off (PLACEHOLDER)
        writer.writeU4(ClassDefItem.NO_OFFSET); // static_values_off
        
        writer.writeU4At(0x60, classDefCount);
        writer.writeU4At(0x64, classDefOffset);
        return new ClassDefSection(classDefOffset);
    }
    
    /** Writes the class_data_item, records it, and translates its methods. */
    public static void writeClassData(ByteWriter writer, ClassFile classFile, 
                                      ClassDefSection classDefSection, CpInfo[] constantPool,
                                      StringSection stringSection, TypeSection typeSection,
                                      ProtoSection protoSection, FieldSection fieldSection, 
                                      MethodSection methodSection,
                                      MapListBuilder mapBuilder) throws Exception {
        writer.alignTo4();
        int classDataOffset = writer.getPosition();
        mapBuilder.add(TYPE_CLASS_DATA_ITEM, 1, classDataOffset);

        List<DexEncodedField> staticFields = new ArrayList<>();
        List<DexEncodedField> instanceFields = new ArrayList<>();
        List<DexEncodedMethod> directMethods = new ArrayList<>();
        List<DexEncodedMethod> virtualMethods = new ArrayList<>();
        
        for (FieldInfo field : classFile.fields) {
            ResolvedField fieldData = findDexFieldId(field, constantPool, stringSection, typeSection, fieldSection);
            if (fieldData == null) continue; 
            DexEncodedField encodedField = new DexEncodedField(fieldData.fieldId, field.accessFlags);
            if ((field.accessFlags & 0x0008) != 0) { staticFields.add(encodedField); } 
            else { instanceFields.add(encodedField); }
        }
        
        for (MethodInfo method : classFile.methods) {
            int methodDexId = findDexMethodId(method, constantPool, 
                                              stringSection, typeSection, methodSection);
            if (methodDexId == -1) continue; 
            
            int codeOffset = writeMethodCode(writer, method, constantPool,
                                             stringSection, typeSection, 
                                             protoSection, fieldSection, methodSection,
                                             mapBuilder);
            
            DexEncodedMethod encodedMethod = new DexEncodedMethod(methodDexId, method.accessFlags, codeOffset);
            boolean isDirect = (method.accessFlags & 0x0002) != 0 || 
                             (method.accessFlags & 0x0008) != 0 || 
                             isConstructor(method, constantPool, stringSection);
            if (isDirect) { directMethods.add(encodedMethod); } 
            else { virtualMethods.add(encodedMethod); }
        }
        
        staticFields.sort(Comparator.comparingInt(f -> f.fieldIdx));
        instanceFields.sort(Comparator.comparingInt(f -> f.fieldIdx));
        directMethods.sort(Comparator.comparingInt(m -> m.methodIdx));
        virtualMethods.sort(Comparator.comparingInt(m -> m.methodIdx));
        
        writer.writeUleb128(staticFields.size());
        writer.writeUleb128(instanceFields.size());
        writer.writeUleb128(directMethods.size());
        writer.writeUleb128(virtualMethods.size());
        
        int lastIdx = 0;
        for (DexEncodedField field : staticFields) {
            writer.writeUleb128(field.fieldIdx - lastIdx);
            writer.writeUleb128(field.accessFlags);
            lastIdx = field.fieldIdx;
        }
        lastIdx = 0;
        for (DexEncodedField field : instanceFields) {
            writer.writeUleb128(field.fieldIdx - lastIdx);
            writer.writeUleb128(field.accessFlags);
            lastIdx = field.fieldIdx;
        }
        lastIdx = 0;
        for (DexEncodedMethod method : directMethods) {
            writer.writeUleb128(method.methodIdx - lastIdx);
            writer.writeUleb128(method.accessFlags);
            writer.writeUleb128(method.codeOff);
            lastIdx = method.methodIdx;
        }
        lastIdx = 0;
        for (DexEncodedMethod method : virtualMethods) {
            writer.writeUleb128(method.methodIdx - lastIdx);
            writer.writeUleb128(method.accessFlags);
            writer.writeUleb128(method.codeOff);
            lastIdx = method.methodIdx;
        }

        int classDataOffOffset = classDefSection.classDefOffset + 24;
        writer.writeU4At(classDataOffOffset, classDataOffset);
    }
    
    /** Writes a complete code_item and records it. */
    public static int writeCodeItem(ByteWriter writer, CodeItem codeItem,
                                    MapListBuilder mapBuilder) {
        writer.alignTo4();
        int codeItemOffset = writer.getPosition();
        mapBuilder.add(TYPE_CODE_ITEM, 1, codeItemOffset);
        
        writer.writeU2(codeItem.registersSize);
        writer.writeU2(codeItem.insSize);
        writer.writeU2(codeItem.outsSize);
        writer.writeU2(codeItem.triesSize);
        writer.writeU4(codeItem.debugInfoOff);
        writer.writeU4(codeItem.insnsSize);
        
        for (short insn : codeItem.insns) {
            writer.writeU2(insn);
        }
        
        if (codeItem.triesSize > 0) {
            if (codeItem.insnsSize % 2 != 0) {
                writer.writeU2(0); // nop
            }
            
            // Write the try_item list
            for (DalvikTryItem tryItem : codeItem.tries) {
                writer.writeU4(tryItem.startAddr);
                writer.writeU2(tryItem.insnCount);
                writer.writeU2(tryItem.handlerOff);
            }
            
            // Write the encoded_catch_handler_list
            writer.alignTo4();
            writer.writeUleb128(codeItem.handlerLists.size());
            
            for (DalvikHandlerList handlerList : codeItem.handlerLists) {
                int handlerCount = handlerList.handlers.size();
                if (handlerList.javaCatchAllPc != -1) {
                    writer.writeSleb128(-handlerCount);
                } else {
                    writer.writeSleb128(handlerCount);
                }
                for (DalvikCatchHandler handler : handlerList.handlers) {
                    writer.writeUleb128(handler.typeId);
                    writer.writeUleb128(handler.dalvikHandlerPc);
                }
                if (handlerList.javaCatchAllPc != -1) {
                    writer.writeUleb128(handlerList.dalvikCatchAllPc);
                }
            }
        }
        
        return codeItemOffset;
    }
    
    /** Finalizes the .dex file by patching the header. */
    public static void finalizeFile(ByteWriter writer) throws Exception {
        int fileSize = writer.getPosition();
        writer.writeU4At(0x20, fileSize);

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.update(writer.buffer.array(), 32, fileSize - 32);
        byte[] signature = sha1.digest();
        
        int oldPosition = writer.buffer.position();
        writer.buffer.position(12);
        writer.writeBytes(signature);
        writer.buffer.position(oldPosition);

        Adler32 adler = new Adler32();
        adler.update(writer.buffer.array(), 12, fileSize - 12);
        long checksum = adler.getValue();
        writer.writeU4At(8, (int) checksum);
    }
    
    // =========================================================================
    // TRANSLATOR & OPCODE HELPER METHODS
    // =========================================================================

    /** Helper to check if a method is a constructor */
    private static boolean isConstructor(MethodInfo method, CpInfo[] pool, StringSection stringSection) throws Exception {
        String methodName = ((ConstantUtf8Info) pool[method.nameIndex]).value;
        return methodName.equals("<init>");
    }

    /** Helper to find the .dex ID for a .class FieldInfo */
    private static ResolvedField findDexFieldId(FieldInfo field, CpInfo[] pool, StringSection stringSection,
                                      TypeSection typeSection, FieldSection fieldSection) throws Exception {
        String fieldName = ((ConstantUtf8Info) pool[field.nameIndex]).value;
        String fieldType = ((ConstantUtf8Info) pool[field.descriptorIndex]).value;
        
        Integer nameDexIdx = stringSection.stringIdMap.get(fieldName);
        Integer typeDexIdx = typeSection.typeIdMap.get(fieldType);
        if (nameDexIdx == null || typeDexIdx == null) return null;

        for (DexFieldId fieldId : fieldSection.fieldIdMap.keySet()) {
            if (fieldId.nameIdx == nameDexIdx && fieldId.typeIdx == typeDexIdx) {
                return new ResolvedField(fieldSection.fieldIdMap.get(fieldId), fieldType);
            }
        }
        return null;
    }
    
    /** Helper to find the .dex ID for a .class FieldInfo from a ref */
    private static ResolvedField findDexFieldId(CpInfo[] pool, int javaFieldRefIndex,
                                                StringSection stringSection, TypeSection typeSection,
                                                FieldSection fieldSection) throws Exception {
        ConstantRefInfo fieldRef = (ConstantRefInfo) pool[javaFieldRefIndex];
        ConstantClassInfo classInfo = (ConstantClassInfo) pool[fieldRef.classIndex];
        ConstantNameAndTypeInfo nameAndType = (ConstantNameAndTypeInfo) pool[fieldRef.nameAndTypeIndex];
        String className = ((ConstantUtf8Info) pool[classInfo.nameIndex]).value;
        String fieldName = ((ConstantUtf8Info) pool[nameAndType.nameIndex]).value;
        String fieldType = ((ConstantUtf8Info) pool[nameAndType.descriptorIndex]).value;
        
        Integer classDexIdx = typeSection.typeIdMap.get(className);
        Integer nameDexIdx = stringSection.stringIdMap.get(fieldName);
        Integer typeDexIdx = typeSection.typeIdMap.get(fieldType);
        if (classDexIdx == null || nameDexIdx == null || typeDexIdx == null) return null;
        
        DexFieldId key = new DexFieldId(classDexIdx, typeDexIdx, nameDexIdx);
        Integer finalId = fieldSection.fieldIdMap.get(key);
        return (finalId != null) ? new ResolvedField(finalId, fieldType) : null;
    }

    /** Helper to find the .dex ID for a .class MethodInfo */
    private static int findDexMethodId(MethodInfo method, CpInfo[] pool, StringSection stringSection,
                                       TypeSection typeSection, MethodSection methodSection) throws Exception {
        String methodName = ((ConstantUtf8Info) pool[method.nameIndex]).value;
        String methodDescStr = ((ConstantUtf8Info) pool[method.descriptorIndex]).value;
        Integer nameDexIdx = stringSection.stringIdMap.get(methodName);
        if (nameDexIdx == null) return -1;
        for (DexMethodId methodId : methodSection.methodIdMap.keySet()) {
            if (methodId.nameIdx == nameDexIdx) {
                return methodSection.methodIdMap.get(methodId);
            }
        }
        return -1;
    }
    
    /** Helper to find the .dex ID for a .class MethodInfo from a ref */
    private static int findDexMethodId(CpInfo[] pool, int classMethodRefIndex,
                                       StringSection stringSection, TypeSection typeSection,
                                       ProtoSection protoSection, MethodSection methodSection) throws Exception {
        ConstantRefInfo methodRef = (ConstantRefInfo) pool[classMethodRefIndex];
        ConstantClassInfo classInfo = (ConstantClassInfo) pool[methodRef.classIndex];
        ConstantNameAndTypeInfo nameAndType = (ConstantNameAndTypeInfo) pool[methodRef.nameAndTypeIndex];
        String className = ((ConstantUtf8Info) pool[classInfo.nameIndex]).value;
        String methodName = ((ConstantUtf8Info) pool[nameAndType.nameIndex]).value;
        String methodDescStr = ((ConstantUtf8Info) pool[nameAndType.descriptorIndex]).value;
        
        Integer classDexIdx = typeSection.typeIdMap.get(className);
        Integer nameDexIdx = stringSection.stringIdMap.get(methodName);
        MethodDescriptor desc = parseMethodDescriptor(methodDescStr);
        Integer protoDexIdx = protoSection.protoIdMap.get(desc);
        if (classDexIdx == null || nameDexIdx == null || protoDexIdx == null) return -1;

        DexMethodId key = new DexMethodId(classDexIdx, protoDexIdx, nameDexIdx);
        Integer finalId = methodSection.methodIdMap.get(key);
        return (finalId != null) ? finalId : -1;
    }
    
    /** Helper to find the .dex type ID from a .class constant pool class reference. */
    private static int findDexTypeId(CpInfo[] pool, int javaClassRefIndex,
                                     TypeSection typeSection) throws Exception {
        ConstantClassInfo classInfo = (ConstantClassInfo) pool[javaClassRefIndex];
        String className = ((ConstantUtf8Info) pool[classInfo.nameIndex]).value;
        Integer typeId = typeSection.typeIdMap.get(className);
        if (typeId == null) {
            throw new Exception("Could not find type ID for class: " + className);
        }
        return typeId;
    }
    
    /** Helper to check if a type descriptor is an object. */
    private static boolean isObject(String fieldType) {
        char c = fieldType.charAt(0);
        return (c == 'L' || c == '[');
    }
    
    // --- Opcode 'make' helpers ---
    
    /** Creates a Dalvik 'const/4 vA, #B' instruction. */
    public static short makeConst4(int destReg, int value) {
        int regA = destReg & 0x0F;
        int valB = value & 0x0F;
        return (short) (DALVIK_CONST_4 | (valB << 12) | (regA << 8));
    }

    /** Creates a Dalvik 'move-xxx/from16' instruction. */
    public static short[] makeMove(int dalvikOpcode, int destReg, int srcReg) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((destReg & 0xFF) << 8));
        insns[1] = (short) (srcReg & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'invoke-xxx/range' instruction. */
    public static short[] makeInvokeRange(int dalvikOpcode, int argCount, int firstReg, int methodId) {
        short[] insns = new short[3];
        insns[0] = (short) (dalvikOpcode | ((argCount & 0xFF) << 8));
        insns[1] = (short) (methodId & 0xFFFF);
        insns[2] = (short) (firstReg & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik field access instruction (iget, iput, etc.). */
    public static short[] makeFieldOp(int dalvikOpcode, int valueReg, int objectReg, int fieldId) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((valueReg & 0x0F) << 8) | ((objectReg & 0x0F) << 12));
        insns[1] = (short) (fieldId & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'return vAA' or 'return-object vAA' instruction. */
    public static short makeReturn(int dalvikOpcode, int srcReg) {
        return (short) (dalvikOpcode | ((srcReg & 0xFF) << 8));
    }
    
    /** Creates a Dalvik 'const/16 vAA, #+BBBB' instruction. */
    public static short[] makeConst16(int destReg, int value) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_CONST_16 | ((destReg & 0xFF) << 8));
        insns[1] = (short) (value & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'const-wide/16 vAA, #+BBBB' instruction. */
    public static short[] makeConstWide16(int destReg, int value) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_CONST_WIDE_16 | ((destReg & 0xFF) << 8));
        insns[1] = (short) (value & 0xFFFF);
        return insns;
    }

    /** Creates a Dalvik 'const-string vAA, string@BBBB' instruction. */
    public static short[] makeConstString(int destReg, int stringId) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_CONST_STRING | ((destReg & 0xFF) << 8));
        insns[1] = (short) (stringId & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik static field access instruction (sget, sput). */
    public static short[] makeStaticFieldOp(int dalvikOpcode, int valueReg, int fieldId) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((valueReg & 0xFF) << 8));
        insns[1] = (short) (fieldId & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 3-operand math instruction (e.g., add-int). */
    public static short[] makeMathOp(int dalvikOpcode, int destReg, int srcReg1, int srcReg2) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((destReg & 0xFF) << 8));
        insns[1] = (short) ((srcReg1 & 0xFF) | ((srcReg2 & 0xFF) << 8));
        return insns;
    }
    
    /** Creates a Dalvik 64-bit math instruction (e.g., add-long). */
    public static short[] makeMathOpWide(int dalvikOpcode, int destReg, int srcReg1, int srcReg2) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((destReg & 0xFF) << 8));
        insns[1] = (short) ((srcReg1 & 0xFF) | ((srcReg2 & 0xFF) << 8));
        return insns;
    }
    
    /** Creates a Dalvik 'goto/16 +AAAA' instruction. */
    public static short[] makeGoto16(int offset) {
        short[] insns = new short[2];
        insns[0] = (short) DALVIK_GOTO_16;
        insns[1] = (short) (offset & 0xFFFF);
        return insns;
    }

    /** Creates a Dalvik 'if-eqz vAA, +BBBB' instruction (if reg == 0). */
    public static short[] makeIfEqz(int register, int offset) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_IF_EQZ | ((register & 0xFF) << 8));
        insns[1] = (short) (offset & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'if-nez vAA, +BBBB' instruction (if reg != 0). */
    public static short[] makeIfNez(int register, int offset) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_IF_NEZ | ((register & 0xFF) << 8));
        insns[1] = (short) (offset & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'if-eq' or 'if-ne' instruction. */
    public static short[] makeIfCmp(int dalvikOpcode, int reg1, int reg2, int offset) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((reg1 & 0x0F) << 8) | ((reg2 & 0x0F) << 12));
        insns[1] = (short) (offset & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'new-instance vAA, type@BBBB' instruction. */
    public static short[] makeNewInstance(int destReg, int typeId) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_NEW_INSTANCE | ((destReg & 0xFF) << 8));
        insns[1] = (short) (typeId & 0xFFFF);
        return insns;
    }
    
    /** Creates a Dalvik 'new-array vA, vB, type@CCCC' instruction. */
    public static short[] makeNewArray(int destReg, int sizeReg, int typeId) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_NEW_ARRAY | ((destReg & 0x0F) << 8) | ((sizeReg & 0x0F) << 12));
        insns[1] = (short) (typeId & 0xFFFF);
        return insns;
    }

    /** Creates a Dalvik 'array-length vA, vB' instruction. */
    public static short makeArrayLength(int destReg, int arrayReg) {
        return (short) (DALVIK_ARRAY_LENGTH | ((destReg & 0x0F) << 8) | ((arrayReg & 0x0F) << 12));
    }

    /** Creates a Dalvik array operation (aget, aput). */
    public static short[] makeArrayOp(int dalvikOpcode, int valueReg, int arrayReg, int indexReg) {
        short[] insns = new short[2];
        insns[0] = (short) (dalvikOpcode | ((valueReg & 0xFF) << 8));
        insns[1] = (short) ((arrayReg & 0xFF) | ((indexReg & 0xFF) << 8));
        return insns;
    }
    
    /** Creates a Dalvik 'check-cast vAA, type@BBBB' instruction. */
    public static short[] makeCheckCast(int objReg, int typeId) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_CHECK_CAST | ((objReg & 0xFF) << 8));
        insns[1] = (short) (typeId & 0xFFFF);
        return insns;
    }

    /** Creates a Dalvik 'instance-of vA, vB, type@CCCC' instruction. */
    public static short[] makeInstanceOf(int destReg, int objReg, int typeId) {
        short[] insns = new short[2];
        insns[0] = (short) (DALVIK_INSTANCE_OF | ((destReg & 0x0F) << 8) | ((objReg & 0x0F) << 12));
        insns[1] = (short) (typeId & 0xFFFF);
        return insns;
    }
    

    // =========================================================================
    // THE TRANSLATOR METHOD (writeMethodCode)
    // =========================================================================

    /**
     * Parses a method's Java code, translates it to Dalvik,
     * and writes the new code_item using a two-pass approach.
     */
    public static int writeMethodCode(ByteWriter writer, MethodInfo javaMethod,
                                      CpInfo[] constantPool, StringSection stringSection,
                                      TypeSection typeSection, ProtoSection protoSection,
                                      FieldSection fieldSection, MethodSection methodSection,
                                      MapListBuilder mapBuilder) throws Exception {
        
        AttributeInfo codeAttrInfo = findAttribute(constantPool, javaMethod.attributes, "Code");
        if (codeAttrInfo == null) return 0;
        CodeAttribute javaCode = parseCodeAttribute(codeAttrInfo);
        CodeItem dalvikCode = new CodeItem();
        String descriptor = ((ConstantUtf8Info) constantPool[javaMethod.descriptorIndex]).value;
        MethodDescriptor methodDesc = parseMethodDescriptor(descriptor);
        dalvikCode.insSize = methodDesc.parameters.size();
        if ((javaMethod.accessFlags & 0x0008) == 0) { dalvikCode.insSize++; }
        
        // TODO: This doesn't account for wide (long/double) params
        
        dalvikCode.registersSize = javaCode.maxLocals; 
        dalvikCode.debugInfoOff = 0;
        
        List<Short> dalvikInsns = new ArrayList<>();
        byte[] javaBytecode = javaCode.code;
        StackTracker stack = new StackTracker(dalvikCode.registersSize, dalvikCode.insSize);
        Map<Integer, Integer> javaPcToDalvikPcMap = new HashMap<>();
        List<BranchFixup> fixups = new ArrayList<>();
        
        int i = 0;
        
        // --- PASS 1: Translate and Record ---
        while (i < javaBytecode.length) {
            
            javaPcToDalvikPcMap.put(i, dalvikInsns.size());
            int opcode = javaBytecode[i] & 0xFF; 
            
            switch (opcode) {

                // --- 64-BIT CONST & MATH ---
                case JAVA_LCONST_0:
                    for (short s : makeConstWide16(stack.pushWide(), 0)) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LCONST_1:
                    for (short s : makeConstWide16(stack.pushWide(), 1)) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LDC2_W: 
                    // TODO: Implement const-wide helper
                    System.out.println("Warning: Skipping ldc2_w (long)");
                    i += 3; break;
                case JAVA_LADD: {
                    int srcReg2 = stack.popWide(); int srcReg1 = stack.popWide(); int destReg = stack.pushWide();
                    for (short s : makeMathOpWide(DALVIK_LADD, destReg, srcReg1, srcReg2)) { dalvikInsns.add(s); }
                    i++; break;
                }
                case JAVA_LSUB: {
                    int srcReg2 = stack.popWide(); int srcReg1 = stack.popWide(); int destReg = stack.pushWide();
                    for (short s : makeMathOpWide(DALVIK_LSUB, destReg, srcReg1, srcReg2)) { dalvikInsns.add(s); }
                    i++; break;
                }
                
                // --- 64-BIT LOAD/STORE/RETURN ---
                case JAVA_LLOAD_0: case JAVA_DLOAD_0:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.pushWide(), stack.getLocalRegister(0))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LLOAD_1: case JAVA_DLOAD_1:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.pushWide(), stack.getLocalRegister(1))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LLOAD_2: case JAVA_DLOAD_2:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.pushWide(), stack.getLocalRegister(2))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LLOAD_3: case JAVA_DLOAD_3:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.pushWide(), stack.getLocalRegister(3))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LSTORE_0: case JAVA_DSTORE_0:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.getLocalRegister(0), stack.popWide())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LSTORE_1: case JAVA_DSTORE_1:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.getLocalRegister(1), stack.popWide())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LSTORE_2: case JAVA_DSTORE_2:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.getLocalRegister(2), stack.popWide())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LSTORE_3: case JAVA_DSTORE_3:
                    for(short s : makeMove(DALVIK_MOVE_WIDE_FROM16, stack.getLocalRegister(3), stack.popWide())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_LRETURN: case JAVA_DRETURN:
                    dalvikInsns.add(makeReturn(DALVIK_RETURN_WIDE, stack.popWide()));
                    i++; break;
                    
                // --- 32-BIT LOAD/STORE ---
                case JAVA_ISTORE_0:
                    for(short s : makeMove(DALVIK_MOVE_FROM16, stack.getLocalRegister(0), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ISTORE_1:
                    for(short s : makeMove(DALVIK_MOVE_FROM16, stack.getLocalRegister(1), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ISTORE_2:
                    for(short s : makeMove(DALVIK_MOVE_FROM16, stack.getLocalRegister(2), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ISTORE_3:
                    for(short s : makeMove(DALVIK_MOVE_FROM16, stack.getLocalRegister(3), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ASTORE_0:
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, stack.getLocalRegister(0), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ASTORE_1:
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, stack.getLocalRegister(1), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ASTORE_2:
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, stack.getLocalRegister(2), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ASTORE_3:
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, stack.getLocalRegister(3), stack.pop())) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ALOAD_0:
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, stack.push(), stack.getLocalRegister(0))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ALOAD_1:
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, stack.push(), stack.getLocalRegister(1))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ILOAD_0:
                    for(short s : makeMove(DALVIK_MOVE_FROM16, stack.push(), stack.getLocalRegister(0))) { dalvikInsns.add(s); }
                    i++; break;
                case JAVA_ILOAD_1:
                    for(short s : makeMove(DALVIK_MOVE_FROM16, stack.push(), stack.getLocalRegister(1))) { dalvikInsns.add(s); }
                    i++; break; 
                case JAVA_DUP: {
                    int regToDup = stack.peek(); int newReg = stack.push();
                    for(short s : makeMove(DALVIK_MOVE_OBJECT_FROM16, newReg, regToDup)) { dalvikInsns.add(s); }
                    i++; break;
                }
                
                // --- TYPE OPS ---
                case JAVA_CHECKCAST: {
                    int javaClassIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    int dexTypeId = findDexTypeId(constantPool, javaClassIndex, typeSection);
                    int objReg = stack.peek();
                    for (short s : makeCheckCast(objReg, dexTypeId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_INSTANCEOF: {
                    int javaClassIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    int dexTypeId = findDexTypeId(constantPool, javaClassIndex, typeSection);
                    int objReg = stack.pop(); int destReg = stack.push();
                    for (short s : makeInstanceOf(destReg, objReg, dexTypeId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                
                // --- ARRAY OPS ---
                case JAVA_NEWARRAY: {
                    int sizeReg = stack.pop(); int destReg = stack.push();
                    int arrayType = javaBytecode[i+1] & 0xFF; 
                    String typeName;
                    if (arrayType == 10) { typeName = "[I"; } // T_INT
                    else { throw new Exception("Unsupported primitive array type: " + arrayType); }
                    Integer dexTypeId = typeSection.typeIdMap.get(typeName);
                    if (dexTypeId == null) { throw new Exception("Missing type ID for primitive array: " + typeName); }
                    for (short s : makeNewArray(destReg, sizeReg, dexTypeId)) { dalvikInsns.add(s); }
                    i += 2; break;
                }
                case JAVA_ANEWARRAY: {
                    int sizeReg = stack.pop(); int destReg = stack.push();
                    int javaClassIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    ConstantClassInfo classInfo = (ConstantClassInfo) constantPool[javaClassIndex];
                    String baseClassName = ((ConstantUtf8Info) constantPool[classInfo.nameIndex]).value;
                    String arrayTypeName = "[L" + baseClassName + ";";
                    Integer dexTypeId = typeSection.typeIdMap.get(arrayTypeName);
                    if (dexTypeId == null) { throw new Exception("Missing type ID for object array: " + arrayTypeName); }
                    for (short s : makeNewArray(destReg, sizeReg, dexTypeId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_ARRAYLENGTH: {
                    int arrayReg = stack.pop(); int destReg = stack.push();
                    dalvikInsns.add(makeArrayLength(destReg, arrayReg));
                    i++; break;
                }
                case JAVA_IALOAD: {
                    int indexReg = stack.pop(); int arrayReg = stack.pop(); int valueReg = stack.push();
                    for (short s : makeArrayOp(DALVIK_AGET, valueReg, arrayReg, indexReg)) { dalvikInsns.add(s); }
                    i++; break;
                }
                case JAVA_AALOAD: {
                    int indexReg = stack.pop(); int arrayReg = stack.pop(); int valueReg = stack.push();
                    for (short s : makeArrayOp(DALVIK_AGET_OBJECT, valueReg, arrayReg, indexReg)) { dalvikInsns.add(s); }
                    i++; break;
                }
                case JAVA_IASTORE: {
                    int valueReg = stack.pop(); int indexReg = stack.pop(); int arrayReg = stack.pop();
                    for (short s : makeArrayOp(DALVIK_APUT, valueReg, arrayReg, indexReg)) { dalvikInsns.add(s); }
                    i++; break;
                }
                case JAVA_AASTORE: {
                    int valueReg = stack.pop(); int indexReg = stack.pop(); int arrayReg = stack.pop();
                    for (short s : makeArrayOp(DALVIK_APUT_OBJECT, valueReg, arrayReg, indexReg)) { dalvikInsns.add(s); }
                    i++; break;
                }
                
                // --- BRANCH OPS ---
                case JAVA_NEW: {
                    int javaClassIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    int dexTypeId = findDexTypeId(constantPool, javaClassIndex, typeSection);
                    int destReg = stack.push();
                    for (short s : makeNewInstance(destReg, dexTypeId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_GOTO: {
                    short offset = (short)(((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF));
                    int javaTargetOffset = i + offset;
                    int dalvikInsnIndex = dalvikInsns.size();
                    for (short s : makeGoto16(0)) { dalvikInsns.add(s); }
                    fixups.add(new BranchFixup(dalvikInsnIndex, javaTargetOffset, JAVA_GOTO, 0, 0));
                    i += 3; break;
                }
                case JAVA_IFEQ: {
                    short offset = (short)(((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF));
                    int javaTargetOffset = i + offset;
                    int registerToTest = stack.pop();
                    int dalvikInsnIndex = dalvikInsns.size();
                    for (short s : makeIfEqz(registerToTest, 0)) { dalvikInsns.add(s); }
                    fixups.add(new BranchFixup(dalvikInsnIndex, javaTargetOffset, JAVA_IFEQ, registerToTest, 0));
                    i += 3; break;
                }
                case JAVA_IFNE: {
                    short offset = (short)(((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF));
                    int javaTargetOffset = i + offset;
                    int registerToTest = stack.pop();
                    int dalvikInsnIndex = dalvikInsns.size();
                    for (short s : makeIfNez(registerToTest, 0)) { dalvikInsns.add(s); }
                    fixups.add(new BranchFixup(dalvikInsnIndex, javaTargetOffset, JAVA_IFNE, registerToTest, 0));
                    i += 3; break;
                }
                case JAVA_IF_ICMPEQ: {
                    short offset = (short)(((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF));
                    int javaTargetOffset = i + offset;
                    int reg2 = stack.pop(); int reg1 = stack.pop();
                    int dalvikInsnIndex = dalvikInsns.size();
                    for (short s : makeIfCmp(DALVIK_IF_EQ, reg1, reg2, 0)) { dalvikInsns.add(s); }
                    fixups.add(new BranchFixup(dalvikInsnIndex, javaTargetOffset, JAVA_IF_ICMPEQ, reg1, reg2));
                    i += 3; break;
                }
                case JAVA_IF_ICMPNE: {
                    short offset = (short)(((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF));
                    int javaTargetOffset = i + offset;
                    int reg2 = stack.pop(); int reg1 = stack.pop();
                    int dalvikInsnIndex = dalvikInsns.size();
                    for (short s : makeIfCmp(DALVIK_IF_NE, reg1, reg2, 0)) { dalvikInsns.add(s); }
                    fixups.add(new BranchFixup(dalvikInsnIndex, javaTargetOffset, JAVA_IF_ICMPNE, reg1, reg2));
                    i += 3; break;
                }
                
                // --- MATH OPS ---
                case JAVA_IADD: {
                    int srcReg2 = stack.pop(); int srcReg1 = stack.pop();
                    for (short s : makeMathOp(DALVIK_ADD_INT, srcReg1, srcReg1, srcReg2)) { dalvikInsns.add(s); }
                    stack.push(); i++; break;
                }
                case JAVA_ISUB: {
                    int srcReg2 = stack.pop(); int srcReg1 = stack.pop();
                    for (short s : makeMathOp(DALVIK_SUB_INT, srcReg1, srcReg1, srcReg2)) { dalvikInsns.add(s); }
                    stack.push(); i++; break;
                }
                
                // --- FIELD & METHOD OPS ---
                case JAVA_GETSTATIC: {
                    int javaFieldIdx = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    ResolvedField field = findDexFieldId(constantPool, javaFieldIdx, stringSection, typeSection, fieldSection);
                    if (field == null) {
                        i += 3;
                        continue;
                    }
                    int op = isObject(field.fieldType) ? DALVIK_SGET_OBJECT : DALVIK_SGET;
                    for (short s : makeStaticFieldOp(op, stack.push(), field.fieldId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_PUTSTATIC: {
                    int javaFieldIdx = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    ResolvedField field = findDexFieldId(constantPool, javaFieldIdx, stringSection, typeSection, fieldSection);
                    if (field == null) {
                        i += 3;
                        continue;
                    }
                    int op = isObject(field.fieldType) ? DALVIK_SPUT_OBJECT : DALVIK_SPUT;
                    for (short s : makeStaticFieldOp(op, stack.pop(), field.fieldId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_INVOKESTATIC: {
                    int javaMethodIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    int dexMethodId = findDexMethodId(constantPool, javaMethodIndex, stringSection, typeSection, protoSection, methodSection);
                    ConstantRefInfo ref = (ConstantRefInfo) constantPool[javaMethodIndex];
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) constantPool[ref.nameAndTypeIndex];
                    String descStr = ((ConstantUtf8Info) constantPool[nat.descriptorIndex]).value;
                    int argCount = parseMethodDescriptor(descStr).parameters.size();
                    dalvikCode.maxOuts = Math.max(dalvikCode.maxOuts, argCount);
                    int[] argRegs = new int[argCount];
                    for (int j = argCount - 1; j >= 0; j--) { argRegs[j] = stack.pop(); }
                    int firstReg = (argCount > 0) ? argRegs[0] : 0;
                    for (short s : makeInvokeRange(DALVIK_INVOKE_STATIC_RANGE, argCount, firstReg, dexMethodId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_INVOKEVIRTUAL: {
                    int javaMethodIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    int dexMethodId = findDexMethodId(constantPool, javaMethodIndex, stringSection, typeSection, protoSection, methodSection);
                    ConstantRefInfo ref = (ConstantRefInfo) constantPool[javaMethodIndex];
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) constantPool[ref.nameAndTypeIndex];
                    String descStr = ((ConstantUtf8Info) constantPool[nat.descriptorIndex]).value;
                    int argCount = parseMethodDescriptor(descStr).parameters.size() + 1;
                    dalvikCode.maxOuts = Math.max(dalvikCode.maxOuts, argCount);
                    int[] argRegs = new int[argCount];
                    for (int j = argCount - 1; j >= 0; j--) { argRegs[j] = stack.pop(); }
                    for (short s : makeInvokeRange(DALVIK_INVOKE_VIRTUAL_RANGE, argCount, argRegs[0], dexMethodId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_INVOKESPECIAL: {
                    int javaMethodIndex = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    int dexMethodId = findDexMethodId(constantPool, javaMethodIndex, stringSection, typeSection, protoSection, methodSection);
                    ConstantRefInfo ref = (ConstantRefInfo) constantPool[javaMethodIndex];
                    ConstantNameAndTypeInfo nat = (ConstantNameAndTypeInfo) constantPool[ref.nameAndTypeIndex];
                    String descStr = ((ConstantUtf8Info) constantPool[nat.descriptorIndex]).value;
                    int argCount = parseMethodDescriptor(descStr).parameters.size() + 1;
                    dalvikCode.maxOuts = Math.max(dalvikCode.maxOuts, argCount);
                    int[] argRegs = new int[argCount];
                    for (int j = argCount - 1; j >= 0; j--) { argRegs[j] = stack.pop(); }
                    for (short s : makeInvokeRange(DALVIK_INVOKE_DIRECT_RANGE, argCount, argRegs[0], dexMethodId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_GETFIELD: {
                    int javaFieldIdx = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    ResolvedField field = findDexFieldId(constantPool, javaFieldIdx, stringSection, typeSection, fieldSection);
                    if (field == null) {
                        i += 3;
                        continue;
                    }
                    int objReg = stack.pop(); int valReg = stack.push();
                    int op = isObject(field.fieldType) ? DALVIK_IGET_OBJECT : DALVIK_IGET;
                    for (short s : makeFieldOp(op, valReg, objReg, field.fieldId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                case JAVA_PUTFIELD: {
                    int javaFieldIdx = ((javaBytecode[i+1] & 0xFF) << 8) | (javaBytecode[i+2] & 0xFF);
                    ResolvedField field = findDexFieldId(constantPool, javaFieldIdx, stringSection, typeSection, fieldSection);
                    if (field == null) {
                        i += 3;
                        continue;
                    }
                    int valReg = stack.pop(); int objReg = stack.pop();
                    int op = isObject(field.fieldType) ? DALVIK_IPUT_OBJECT : DALVIK_IPUT;
                    for (short s : makeFieldOp(op, valReg, objReg, field.fieldId)) { dalvikInsns.add(s); }
                    i += 3; break;
                }
                
                // --- CONSTANTS & RETURNS ---
                case JAVA_LDC: {
                    int javaConstIndex = javaBytecode[i+1] & 0xFF;
                    CpInfo constant = constantPool[javaConstIndex];
                    int destReg = stack.push();
                    if (constant.tag == CONSTANT_String) {
                        ConstantStringInfo stringInfo = (ConstantStringInfo) constant;
                        String strValue = ((ConstantUtf8Info) constantPool[stringInfo.stringIndex]).value;
                        int dexStringId = stringSection.stringIdMap.get(strValue);
                        for (short s : makeConstString(destReg, dexStringId)) { dalvikInsns.add(s); }
                    } else if (constant.tag == CONSTANT_Integer) {
                        int intValue = ((ConstantIntegerInfo) constant).value;
                        if (intValue >= -8 && intValue <= 7) {
                            dalvikInsns.add(makeConst4(destReg, intValue));
                        } else {
                            for (short s : makeConst16(destReg, intValue)) { dalvikInsns.add(s); }
                        }
                    } else {
                        // TODO: Handle other constant types
                    }
                    i += 2; break;
                }
                case JAVA_ICONST_M1: dalvikInsns.add(makeConst4(stack.push(), -1)); i++; break;
                case JAVA_ICONST_0: dalvikInsns.add(makeConst4(stack.push(), 0)); i++; break;
                case JAVA_ICONST_1: dalvikInsns.add(makeConst4(stack.push(), 1)); i++; break;
                case JAVA_ICONST_2: dalvikInsns.add(makeConst4(stack.push(), 2)); i++; break;
                case JAVA_ICONST_3: dalvikInsns.add(makeConst4(stack.push(), 3)); i++; break;
                case JAVA_ICONST_4: dalvikInsns.add(makeConst4(stack.push(), 4)); i++; break;
                case JAVA_ICONST_5: dalvikInsns.add(makeConst4(stack.push(), 5)); i++; break;
                case JAVA_IRETURN: dalvikInsns.add(makeReturn(DALVIK_RETURN, stack.pop())); i++; break;
                case JAVA_ARETURN: dalvikInsns.add(makeReturn(DALVIK_RETURN_OBJECT, stack.pop())); i++; break;
                case JAVA_FRETURN: dalvikInsns.add(makeReturn(DALVIK_RETURN, stack.pop())); i++; break;
                case JAVA_RETURN: dalvikInsns.add((short) DALVIK_RETURN_VOID); i++; break;
                    
                default:
                    System.out.println("Warning: Skipping unknown Java opcode: 0x" + Integer.toHexString(opcode));
                    // A real translator would parse the opcode length table here
                    i++; 
            }
        }
        
        javaPcToDalvikPcMap.put(javaBytecode.length, dalvikInsns.size());

        // --- PROCESS EXCEPTIONS ---
        if (javaCode.exceptionTable.length > 0) {
            Map<String, List<ExceptionTableEntry>> groupedTries = new HashMap<>();
            for (ExceptionTableEntry entry : javaCode.exceptionTable) {
                String key = entry.startPc + ":" + entry.endPc;
                if (!groupedTries.containsKey(key)) {
                    groupedTries.put(key, new ArrayList<ExceptionTableEntry>());
                }
                groupedTries.get(key).add(entry);
            }
            Map<DalvikHandlerList, Integer> handlerListIndexMap = new HashMap<>();
            for (String key : groupedTries.keySet()) {
                List<ExceptionTableEntry> javaHandlers = groupedTries.get(key);
                DalvikHandlerList handlerList = new DalvikHandlerList();
                for (ExceptionTableEntry javaEntry : javaHandlers) {
                    if (javaEntry.catchType == 0) {
                        handlerList.javaCatchAllPc = javaEntry.handlerPc;
                    } else {
                        int typeId = findDexTypeId(constantPool, javaEntry.catchType, typeSection);
                        handlerList.handlers.add(new DalvikCatchHandler(typeId, javaEntry.handlerPc));
                    }
                }
                
                int handlerListIndex;
                if (handlerListIndexMap.containsKey(handlerList)) {
                    handlerListIndex = handlerListIndexMap.get(handlerList);
                } else {
                    handlerListIndex = dalvikCode.handlerLists.size();
                    dalvikCode.handlerLists.add(handlerList);
                    handlerListIndexMap.put(handlerList, handlerListIndex);
                }
                
                int javaStartPc = javaHandlers.get(0).startPc;
                int javaEndPc = javaHandlers.get(0).endPc;
                Integer dalvikStartPc = javaPcToDalvikPcMap.get(javaStartPc);
                Integer dalvikEndPc = javaPcToDalvikPcMap.get(javaEndPc);

            dalvikCode.tries.add(new DalvikTryItem(dalvikStartPc, dalvikEndPc - dalvikStartPc, handlerListIndex));
            }

            for (DalvikHandlerList handlerList : dalvikCode.handlerLists) {
                for (DalvikCatchHandler handler : handlerList.handlers) {
                    handler.dalvikHandlerPc = javaPcToDalvikPcMap.get(handler.javaHandlerPc);
                }
                if (handlerList.javaCatchAllPc != 0) {
                    handlerList.dalvikCatchAllPc = javaPcToDalvikPcMap.get(handlerList.javaCatchAllPc);
                }
            }
        }

        for (BranchFixup fixup : fixups) {
            Integer dalvikTargetPc = javaPcToDalvikPcMap.get(fixup.javaTargetOffset);
            int offset = dalvikTargetPc - fixup.dalvikInsnIndex;
            short[] newBranch = null;
            switch (fixup.javaOpcode) {
                case JAVA_GOTO:
                    newBranch = makeGoto16(offset);
                    break;
                case JAVA_IFEQ:
                    newBranch = makeIfEqz(fixup.registerToTest, offset);
                    break;
                case JAVA_IFNE:
                    newBranch = makeIfNez(fixup.registerToTest, offset);
                    break;
                case JAVA_IF_ICMPEQ:
                    newBranch = makeIfCmp(DALVIK_IF_EQ, fixup.registerToTest, fixup.registerToTest2, offset);
                    break;
                case JAVA_IF_ICMPNE:
                    newBranch = makeIfCmp(DALVIK_IF_NE, fixup.registerToTest, fixup.registerToTest2, offset);
                    break;
            }
            if (newBranch != null) {
                for (int k = 0; k < newBranch.length; k++) {
                    dalvikInsns.set(fixup.dalvikInsnIndex + k, newBranch[k]);
                }
            }
        }

        dalvikCode.insns = new short[dalvikInsns.size()];
        for (int j = 0; j < dalvikInsns.size(); j++) {
            dalvikCode.insns[j] = dalvikInsns.get(j);
        }

        dalvikCode.insnsSize = dalvikCode.insns.length;
        dalvikCode.outsSize = stack.stackPointer;
        dalvikCode.triesSize = dalvikCode.tries.size();

        return writeCodeItem(writer, dalvikCode, mapBuilder);
    }
}

