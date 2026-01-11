package com.mobileforge.compiler;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MFNLCompiler {
    private static final String TAG = "MFNLCompiler";

    public static class CompileResult {
        public boolean success;
        public String message;
        public List<File> generatedClassFiles;

        public CompileResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.generatedClassFiles = new ArrayList<>();
        }
    }

    public static CompileResult compile(File mfnlFile, File outputDir) {
        Log.d(TAG, "Compiling MFNL file: " + mfnlFile.getAbsolutePath());

        try {
            // Read MFNL source
            String source = readFile(mfnlFile);
            if (source == null || source.trim().isEmpty()) {
                return new CompileResult(false, "ERROR: Empty or unreadable MFNL file");
            }

            Log.d(TAG, "MFNL source length: " + source.length() + " chars");

            // Lexical analysis
            Log.d(TAG, "Starting lexical analysis...");
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.tokenize();
            Log.d(TAG, "Tokenization complete: " + tokens.size() + " tokens");

            // Syntax analysis
            Log.d(TAG, "Starting syntax analysis...");
            Parser parser = new Parser(tokens);
            ASTNode.Program program = parser.parse();
            Log.d(TAG, "Parsing complete: " + program.statements.size() + " statements");

            // Bytecode generation
            Log.d(TAG, "Starting bytecode generation...");
            SimpleJavaCompiler.CompileResult compileResult =
                SimpleJavaCompiler.compileFromAST(program, "com.mobileforge.generated.MainActivity");

            if (!compileResult.success) {
                return new CompileResult(false, compileResult.message);
            }

            Log.d(TAG, "Bytecode generation complete: " + compileResult.classData.length + " bytes");

            // Write .class file
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Create package directory
            File packageDir = new File(outputDir, "com/mobileforge/generated");
            if (!packageDir.exists()) {
                packageDir.mkdirs();
            }

            File classFile = new File(packageDir, "MainActivity.class");
            writeFile(classFile, compileResult.classData);

            Log.d(TAG, ".class file written: " + classFile.getAbsolutePath());

            CompileResult result = new CompileResult(true, "SUCCESS: MFNL compiled to bytecode\nGenerated: " + classFile.getName());
            result.generatedClassFiles.add(classFile);
            return result;

        } catch (Exception e) {
            Log.e(TAG, "Compilation error", e);
            return new CompileResult(false, "ERROR: " + e.getMessage() + "\n" + getStackTrace(e));
        }
    }

    public static CompileResult compileMultiple(List<File> mfnlFiles, File outputDir) {
        List<File> allGeneratedFiles = new ArrayList<>();
        StringBuilder messages = new StringBuilder();

        for (File mfnlFile : mfnlFiles) {
            CompileResult result = compile(mfnlFile, outputDir);
            if (!result.success) {
                return result; // Return first error
            }
            allGeneratedFiles.addAll(result.generatedClassFiles);
            messages.append(result.message).append("\n");
        }

        CompileResult finalResult = new CompileResult(true, messages.toString());
        finalResult.generatedClassFiles = allGeneratedFiles;
        return finalResult;
    }

    private static String readFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return new String(data, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "Error reading file", e);
            return null;
        }
    }

    private static void writeFile(File file, String content) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes("UTF-8"));
        fos.close();
    }

    private static void writeFile(File file, byte[] data) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    private static String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("  at ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
