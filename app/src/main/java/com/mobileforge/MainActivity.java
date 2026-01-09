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
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
        private File ecjJar;
        private File buildDir;
        private File mfnlGenDir;

        public BuildAPI() {
            ecjJar = new File(getFilesDir(), "ecj.jar");
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
            if (ecjJar.exists()) {
                Log.d(TAG, "ecj.jar already extracted");
                return;
            }
            try {
                Log.d(TAG, "Extracting ecj.jar from assets...");
                InputStream is = getAssets().open("ecj.jar");
                FileOutputStream fos = new FileOutputStream(ecjJar);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
                Log.d(TAG, "ecj.jar extracted to: " + ecjJar.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "Failed to extract ecj.jar", e);
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
            try {
                Log.d(TAG, "Loading ecj.jar from: " + ecjJar.getAbsolutePath());

                // Load ecj.jar
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[] { ecjJar.toURI().toURL() },
                    getClass().getClassLoader()
                );

                // Load the Main class from ecj
                Class<?> mainClass = classLoader.loadClass("org.eclipse.jdt.internal.compiler.batch.Main");

                // Prepare arguments for ecj
                List<String> args = new ArrayList<>();

                // Add all source files
                for (File javaFile : javaFiles) {
                    args.add(javaFile.getAbsolutePath());
                }

                // Add compiler options
                args.add("-d");
                args.add(outputDir.getAbsolutePath());
                args.add("-source");
                args.add("1.8");
                args.add("-target");
                args.add("1.8");
                args.add("-nowarn");

                Log.d(TAG, "Compiler args: " + args.toString());

                // Capture output
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                PrintWriter outWriter = new PrintWriter(out);
                PrintWriter errWriter = new PrintWriter(err);

                // Invoke ecj Main.compile()
                Method compileMethod = mainClass.getMethod("compile", String[].class, PrintWriter.class, PrintWriter.class, null);
                Object result = compileMethod.invoke(
                    mainClass.newInstance(),
                    (Object) args.toArray(new String[0]),
                    outWriter,
                    errWriter,
                    null
                );

                outWriter.flush();
                errWriter.flush();

                String outStr = out.toString();
                String errStr = err.toString();
                boolean success = (Boolean) result;

                Log.d(TAG, "Compilation result: " + success);
                Log.d(TAG, "Compiler output: " + outStr);
                Log.d(TAG, "Compiler errors: " + errStr);

                if (success) {
                    return "SUCCESS: Compiled " + javaFiles.size() + " file(s)\n" + outStr + errStr;
                } else {
                    return "COMPILATION FAILED:\n" + outStr + errStr;
                }

            } catch (Exception e) {
                Log.e(TAG, "Compilation error", e);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                return "ERROR: " + e.getMessage() + "\n" + sw.toString();
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
