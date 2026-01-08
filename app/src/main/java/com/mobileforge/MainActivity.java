package com.mobileforge;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private File workDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        workDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MobileForge");
        if (!workDir.exists()) {
            workDir.mkdirs();
        }

        WebView wv = findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.addJavascriptInterface(new FileAPI(), "FileAPI");

        File ext = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "forge.html");
        if (ext.exists()) {
            wv.loadUrl("file://" + ext.getAbsolutePath());
        } else {
            wv.loadUrl("file:///android_asset/index.html");
        }
    }

    public class FileAPI {
        @JavascriptInterface
        public String listFiles() {
            File[] files = workDir.listFiles();
            if (files == null || files.length == 0) return "[]";
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (i > 0) json.append(",");
                json.append("{\"name\":\"").append(f.getName()).append("\",\"isDir\":").append(f.isDirectory()).append("}");
            }
            json.append("]");
            return json.toString();
        }

        @JavascriptInterface
        public String readFile(String name) {
            try {
                File f = new File(workDir, name);
                FileInputStream fis = new FileInputStream(f);
                byte[] data = new byte[(int) f.length()];
                fis.read(data);
                fis.close();
                return new String(data, "UTF-8");
            } catch (Exception e) {
                return null;
            }
        }

        @JavascriptInterface
        public boolean writeFile(String name, String content) {
            try {
                File f = new File(workDir, name);
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(content.getBytes("UTF-8"));
                fos.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public boolean deleteFile(String name) {
            try {
                File f = new File(workDir, name);
                return f.delete();
            } catch (Exception e) {
                return false;
            }
        }
    }
}
