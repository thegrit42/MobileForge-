package com.mobileforge;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarResourceClassLoader extends ClassLoader {
    private File jarFile;

    public JarResourceClassLoader(File jarFile, ClassLoader parent) {
        super(parent);
        this.jarFile = jarFile;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            ZipFile zip = new ZipFile(jarFile);
            ZipEntry entry = zip.getEntry(name);
            if (entry != null) {
                return zip.getInputStream(entry);
            }
        } catch (Exception e) {
            // Fall through
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public URL getResource(String name) {
        try {
            ZipFile zip = new ZipFile(jarFile);
            ZipEntry entry = zip.getEntry(name);
            if (entry != null) {
                // Return jar:file:// URL
                return new URL("jar:file:" + jarFile.getAbsolutePath() + "!/" + name);
            }
        } catch (Exception e) {
            // Fall through
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws java.io.IOException {
        // Just delegate to parent
        return super.getResources(name);
    }
}
