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
        private File buildDir;
        private File mfnlGenDir;

        public BuildAPI() {
            buildDir = new File(getExternalFilesDir(null), "build");
            mfnlGenDir = new File(buildDir, "mfnl_generated");
            if (!buildDir.exists()) {
                buildDir.mkdirs();
            }
            if (!mfnlGenDir.exists()) {
                mfnlGenDir.mkdirs();
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

                resultLog.append("SUCCESS: MFNL compilation complete\n");
                return resultLog.toString();
            } catch (Exception e) {
                Log.e(TAG, "buildAPK error", e);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                return "ERROR: " + e.getMessage() + "\n" + sw.toString();
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
