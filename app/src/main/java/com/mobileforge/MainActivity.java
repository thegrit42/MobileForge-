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
}
