package com.mobileforge;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarResourceClassLoader extends ClassLoader {
    private ZipFile zipFile;

    public JarResourceClassLoader(File jarFile, ClassLoader parent) {
        super(parent);
        try {
            this.zipFile = new ZipFile(jarFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open JAR file: " + jarFile, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            ZipEntry entry = zipFile.getEntry(name);
            if (entry != null) {
                // Read entire stream into byte array to avoid ZipFile closing issues
                InputStream in = zipFile.getInputStream(entry);
                byte[] buffer = new byte[(int) entry.getSize()];
                int offset = 0;
                int remaining = buffer.length;
                while (remaining > 0) {
                    int read = in.read(buffer, offset, remaining);
                    if (read < 0) break;
                    offset += read;
                    remaining -= read;
                }
                in.close();
                return new ByteArrayInputStream(buffer);
            }
        } catch (Exception e) {
            // Fall through
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public URL getResource(String name) {
        // Check parent first
        URL parentResource = super.getResource(name);
        if (parentResource != null) {
            return parentResource;
        }

        try {
            ZipEntry entry = zipFile.getEntry(name);
            if (entry != null) {
                // Create a custom URL that our handler can understand
                return new URL("jar:file:" + zipFile.getName() + "!/" + name);
            }
        } catch (Exception e) {
            // Fall through
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws java.io.IOException {
        return super.getResources(name);
    }

    public void close() {
        try {
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
