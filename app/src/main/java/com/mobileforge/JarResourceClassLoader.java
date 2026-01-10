package com.mobileforge;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarResourceClassLoader extends ClassLoader {
    private static final String TAG = "JarResourceClassLoader";
    private File jarFile;
    private Map<String, byte[]> resourceCache;

    public JarResourceClassLoader(File jarFile, ClassLoader parent) {
        super(parent);
        this.jarFile = jarFile;
        this.resourceCache = new HashMap<>();
        Log.d(TAG, "Created classloader for JAR: " + jarFile.getAbsolutePath());
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        Log.d(TAG, "getResourceAsStream: " + name);

        // Check cache first
        if (resourceCache.containsKey(name)) {
            Log.d(TAG, "Found in cache: " + name);
            return new ByteArrayInputStream(resourceCache.get(name));
        }

        // Try to load from JAR
        ZipFile zip = null;
        try {
            zip = new ZipFile(jarFile);
            ZipEntry entry = zip.getEntry(name);
            if (entry != null) {
                Log.d(TAG, "Found in JAR: " + name + " (size: " + entry.getSize() + ")");
                InputStream in = zip.getInputStream(entry);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                in.close();
                byte[] data = baos.toByteArray();
                resourceCache.put(name, data);
                return new ByteArrayInputStream(data);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading resource: " + name, e);
        } finally {
            try {
                if (zip != null) zip.close();
            } catch (Exception e) {}
        }

        // Fall back to parent
        Log.d(TAG, "Not found in JAR, trying parent: " + name);
        return super.getResourceAsStream(name);
    }

    @Override
    public URL getResource(String name) {
        // Try parent first for system resources
        URL parentResource = super.getResource(name);
        if (parentResource != null) {
            return parentResource;
        }

        // Check if resource exists in JAR
        ZipFile zip = null;
        try {
            zip = new ZipFile(jarFile);
            ZipEntry entry = zip.getEntry(name);
            if (entry != null) {
                return new URL("jar:file:" + jarFile.getAbsolutePath() + "!/" + name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting resource URL: " + name, e);
        } finally {
            try {
                if (zip != null) zip.close();
            } catch (Exception e) {}
        }
        return null;
    }
}
