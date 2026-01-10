package com.mobileforge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarURLStreamHandler extends URLStreamHandler {
    private File jarFile;

    public JarURLStreamHandler(File jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new JarURLConnection(url, jarFile);
    }

    private static class JarURLConnection extends URLConnection {
        private File jarFile;
        private String entryName;

        public JarURLConnection(URL url, File jarFile) {
            super(url);
            this.jarFile = jarFile;
            // Extract entry name from jar:file:/path/to/file.jar!/entry/name.properties
            String urlStr = url.toString();
            int sepIdx = urlStr.indexOf("!/");
            if (sepIdx != -1) {
                this.entryName = urlStr.substring(sepIdx + 2);
            }
        }

        @Override
        public void connect() throws IOException {
            // Nothing to do
        }

        @Override
        public InputStream getInputStream() throws IOException {
            ZipFile zip = new ZipFile(jarFile);
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                throw new IOException("Entry not found: " + entryName);
            }
            return zip.getInputStream(entry);
        }
    }
}
