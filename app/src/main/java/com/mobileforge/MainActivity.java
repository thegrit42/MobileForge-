package com.mobileforge;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MobileForge";
    private File workDir;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use app-specific external storage - no permissions needed
        workDir = new File(getExternalFilesDir(null), "projects");
        if (!workDir.exists()) {
            boolean created = workDir.mkdirs();
            Log.d(TAG, "Work directory created: " + created + " at " + workDir.getAbsolutePath());
        }

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Enable console logging for debugging
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, "WebView: " + cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });

        webView.addJavascriptInterface(new FileAPI(), "FileAPI");
        webView.addJavascriptInterface(new BuildAPI(), "BuildAPI");

        Log.d(TAG, "Loading index.html");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class FileAPI {
        @JavascriptInterface
        public String getWorkDir() {
            return workDir.getAbsolutePath();
        }

        @JavascriptInterface
        public String listFiles() {
            Log.d(TAG, "listFiles() called");
            try {
                File[] files = workDir.listFiles();
                if (files == null || files.length == 0) {
                    Log.d(TAG, "No files found");
                    return "[]";
                }
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (i > 0) json.append(",");
                    json.append("{\"name\":\"").append(f.getName()).append("\",\"isDir\":").append(f.isDirectory()).append("}");
                }
                json.append("]");
                Log.d(TAG, "Returning " + files.length + " files");
                return json.toString();
            } catch (Exception e) {
                Log.e(TAG, "listFiles error", e);
                return "[]";
            }
        }

        @JavascriptInterface
        public String readFile(String name) {
            Log.d(TAG, "readFile() called: " + name);
            try {
                File f = new File(workDir, name);
                if (!f.exists()) {
                    Log.e(TAG, "File does not exist: " + f.getAbsolutePath());
                    return null;
                }
                FileInputStream fis = new FileInputStream(f);
                byte[] data = new byte[(int) f.length()];
                fis.read(data);
                fis.close();
                Log.d(TAG, "File read successfully: " + data.length + " bytes");
                return new String(data, "UTF-8");
            } catch (Exception e) {
                Log.e(TAG, "readFile error", e);
                return null;
            }
        }

        @JavascriptInterface
        public boolean writeFile(String name, String content) {
            Log.d(TAG, "writeFile() called: " + name + " (" + content.length() + " chars)");
            try {
                File f = new File(workDir, name);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(content.getBytes("UTF-8"));
                fos.close();
                Log.d(TAG, "File written successfully to: " + f.getAbsolutePath());
                return true;
            } catch (Exception e) {
                Log.e(TAG, "writeFile error", e);
                return false;
            }
        }

        @JavascriptInterface
        public boolean deleteFile(String name) {
            Log.d(TAG, "deleteFile() called: " + name);
            try {
                File f = new File(workDir, name);
                boolean deleted = f.delete();
                Log.d(TAG, "File deleted: " + deleted);
                return deleted;
            } catch (Exception e) {
                Log.e(TAG, "deleteFile error", e);
                return false;
            }
        }
    }

    public class BuildAPI {
        private File ecjDexJar;
        private File buildDir;
        private File mfnlGenDir;

        public BuildAPI() {
            ecjDexJar = new File(getFilesDir(), "ecj_dex.jar");
            buildDir = new File(getExternalFilesDir(null), "build");
            mfnlGenDir = new File(buildDir, "mfnl_generated");
            if (!buildDir.exists()) {
                buildDir.mkdirs();
            }
            if (!mfnlGenDir.exists()) {
                mfnlGenDir.mkdirs();
            }
            extractEcjIfNeeded();
        }

        private void extractEcjIfNeeded() {
            try {
                // ALWAYS re-extract to ensure fresh copy from APK assets
                if (ecjDexJar.exists()) {
                    ecjDexJar.setWritable(true, false);  // Make writable so we can delete
                    ecjDexJar.delete();
                    Log.d(TAG, "Deleted old ecj_dex.jar to force fresh extraction");
                }

                Log.d(TAG, "Extracting ecj_dex.jar from assets...");
                InputStream is = getAssets().open("ecj_dex.jar");
                FileOutputStream fos = new FileOutputStream(ecjDexJar);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
                Log.d(TAG, "ecj_dex.jar extracted successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to extract ecj DEX file", e);
            }
        }

        @JavascriptInterface
        public String buildAPK() {
            Log.d(TAG, "buildAPK() called");
            try {
                StringBuilder resultLog = new StringBuilder();

                // Step 1: Find all .mfnl files
                List<File> mfnlFiles = new ArrayList<>();
                findMFNLFiles(workDir, mfnlFiles);

                // Step 2: Compile MFNL to Java if MFNL files exist
                if (!mfnlFiles.isEmpty()) {
                    Log.d(TAG, "Found " + mfnlFiles.size() + " MFNL files");
                    resultLog.append("=== MFNL Compilation ===\n");
                    resultLog.append("Found " + mfnlFiles.size() + " MFNL file(s)\n\n");

                    for (File mfnlFile : mfnlFiles) {
                        Log.d(TAG, "Compiling MFNL: " + mfnlFile.getName());
                        resultLog.append("Compiling " + mfnlFile.getName() + "...\n");

                        com.mobileforge.compiler.MFNLCompiler.CompileResult mfnlResult =
                            com.mobileforge.compiler.MFNLCompiler.compile(mfnlFile, mfnlGenDir);

                        resultLog.append(mfnlResult.message).append("\n");

                        if (!mfnlResult.success) {
                            return resultLog.toString();
                        }
                    }

                    resultLog.append("\n");
                }

                // Step 3: Find all .java files (including generated ones)
                List<File> javaFiles = new ArrayList<>();
                findJavaFiles(workDir, javaFiles);
                findJavaFiles(mfnlGenDir, javaFiles);

                if (javaFiles.isEmpty()) {
                    return resultLog.toString() + "ERROR: No .java files found. " +
                           "Please create a .mfnl or .java file first.";
                }

                Log.d(TAG, "Found " + javaFiles.size() + " Java files total");
                resultLog.append("=== Java Compilation ===\n");
                resultLog.append("Found " + javaFiles.size() + " Java file(s)\n\n");

                // Step 4: Prepare output directory
                File classesDir = new File(buildDir, "classes");
                if (classesDir.exists()) {
                    deleteRecursive(classesDir);
                }
                classesDir.mkdirs();

                // Step 5: Compile using ecj
                String ecjResult = compileJavaFiles(javaFiles, classesDir);
                resultLog.append(ecjResult);

                return resultLog.toString();
            } catch (Exception e) {
                Log.e(TAG, "buildAPK error", e);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                return "ERROR: " + e.getMessage() + "\n" + sw.toString();
            }
        }

        private void findJavaFiles(File dir, List<File> javaFiles) {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f.isDirectory()) {
                    findJavaFiles(f, javaFiles);
                } else if (f.getName().endsWith(".java")) {
                    javaFiles.add(f);
                }
            }
        }

        private void findMFNLFiles(File dir, List<File> mfnlFiles) {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f.isDirectory()) {
                    findMFNLFiles(f, mfnlFiles);
                } else if (f.getName().endsWith(".mfnl")) {
                    mfnlFiles.add(f);
                }
            }
        }

        private String compileJavaFiles(List<File> javaFiles, File outputDir) {
            // Capture output (declare outside try so catch can access)
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            try {
                Log.d(TAG, "Loading ECJ BatchCompiler from DEX...");

                // Create optimized dex output directory
                File dexOutputDir = new File(getCodeCacheDir(), "ecj_dex");
                if (!dexOutputDir.exists()) {
                    dexOutputDir.mkdirs();
                }

                // Create URLClassLoader for resources
                URL resourcesUrl = resourcesDir.toURI().toURL();
                URLClassLoader resourcesLoader = new URLClassLoader(new URL[]{resourcesUrl}, getClass().getClassLoader());

                // Load ECJ classes from DEX with resources loader as parent
                dalvik.system.DexClassLoader dexLoader = new dalvik.system.DexClassLoader(
                    ecjDexJar.getAbsolutePath(),
                    dexOutputDir.getAbsolutePath(),
                    null,
                    resourcesLoader  // Chain resources
                );

                // Load BatchCompiler class (resources now accessible via parent)
                Class<?> batchCompilerClass = dexLoader.loadClass("org.eclipse.jdt.core.compiler.batch.BatchCompiler");

                // Build compiler arguments
                List<String> args = new ArrayList<>();

                // Source files
                for (File javaFile : javaFiles) {
                    args.add(javaFile.getAbsolutePath());
                }

                // Compiler options
                args.add("-d");
                args.add(outputDir.getAbsolutePath());
                args.add("-source");
                args.add("1.8");
                args.add("-target");
                args.add("1.8");
                args.add("-nowarn");
                args.add("-proc:none");  // Skip annotation processing

                Log.d(TAG, "Compiler args: " + args.toString());

                // Create output writers
                PrintWriter outWriter = new PrintWriter(out, true);
                PrintWriter errWriter = new PrintWriter(err, true);

                // Invoke BatchCompiler.compile() via reflection
                Method compileMethod = batchCompilerClass.getMethod(
                    "compile",
                    String[].class,
                    PrintWriter.class,
                    PrintWriter.class,
                    Class.forName("org.eclipse.jdt.core.compiler.CompilationProgress", false, dexLoader)
                );

                Boolean success = (Boolean) compileMethod.invoke(
                    null,  // static method
                    (Object) args.toArray(new String[0]),
                    outWriter,
                    errWriter,
                    null
                );

                outWriter.flush();
                errWriter.flush();

                String outStr = out.toString();
                String errStr = err.toString();

                Log.d(TAG, "Compilation result: " + success);
                Log.d(TAG, "Compiler output: " + outStr);
                Log.d(TAG, "Compiler errors: " + errStr);

                if (success) {
                    // Step 6: Convert .class files to .dex
                    String dexResult = convertClassesToDex(outputDir);
                    return "SUCCESS: Compiled " + javaFiles.size() + " file(s)\n" + outStr + errStr + "\n\n" + dexResult;
                } else {
                    return "COMPILATION FAILED:\n" + outStr + errStr;
                }

            } catch (Exception e) {
                Log.e(TAG, "Compilation error", e);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String errOutput = err.toString();
                return "ERROR: " + e.getMessage() + "\n" +
                       (errOutput.isEmpty() ? "" : "STDERR:\n" + errOutput + "\n") +
                       sw.toString();
            }
        }

        private String convertClassesToDex(File classesDir) {
            try {
                Log.d(TAG, "=== DEX Conversion ===");
                StringBuilder result = new StringBuilder("=== DEX Conversion ===\n");

                // Find all .class files
                List<File> classFiles = new ArrayList<>();
                findClassFiles(classesDir, classFiles);

                if (classFiles.isEmpty()) {
                    return "ERROR: No .class files found to convert";
                }

                Log.d(TAG, "Found " + classFiles.size() + " .class files");
                result.append("Found " + classFiles.size() + " .class file(s)\n\n");

                // Output directory for .dex files
                File dexDir = new File(buildDir, "dex");
                if (dexDir.exists()) {
                    deleteRecursive(dexDir);
                }
                dexDir.mkdirs();

                // Convert each .class file to .dex
                int converted = 0;
                for (File classFile : classFiles) {
                    try {
                        Log.d(TAG, "Converting: " + classFile.getName());

                        // Read .class file bytes
                        FileInputStream fis = new FileInputStream(classFile);
                        byte[] classData = new byte[(int) classFile.length()];
                        fis.read(classData);
                        fis.close();

                        // Convert to .dex
                        byte[] dexData = PureCodeDEXGenerator.convertClassToDex(classData);

                        // Write .dex file
                        String dexFileName = classFile.getName().replace(".class", ".dex");
                        File dexFile = new File(dexDir, dexFileName);
                        FileOutputStream fos = new FileOutputStream(dexFile);
                        fos.write(dexData);
                        fos.close();

                        converted++;
                        Log.d(TAG, "Converted: " + classFile.getName() + " -> " + dexFileName);

                    } catch (Exception e) {
                        Log.e(TAG, "Failed to convert " + classFile.getName(), e);
                        result.append("ERROR converting " + classFile.getName() + ": " + e.getMessage() + "\n");
                        return result.toString();
                    }
                }

                result.append("SUCCESS: Converted " + converted + " .class file(s) to .dex\n");
                result.append("DEX files saved to: " + dexDir.getAbsolutePath());

                return result.toString();

            } catch (Exception e) {
                Log.e(TAG, "DEX conversion error", e);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                return "DEX CONVERSION ERROR: " + e.getMessage() + "\n" + sw.toString();
            }
        }

        private void findClassFiles(File dir, List<File> classFiles) {
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f.isDirectory()) {
                    findClassFiles(f, classFiles);
                } else if (f.getName().endsWith(".class")) {
                    classFiles.add(f);
                }
            }
        }

        private void deleteRecursive(File file) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        deleteRecursive(f);
                    }
                }
            }
            file.delete();
        }
    }
}
