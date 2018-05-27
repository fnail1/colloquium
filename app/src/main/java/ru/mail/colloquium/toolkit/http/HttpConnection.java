package ru.mail.colloquium.toolkit.http;

import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ru.mail.colloquium.diagnostics.Logger;
import ru.mail.colloquium.toolkit.io.FileOpException;

public abstract class HttpConnection {

    private static final AtomicInteger counter = new AtomicInteger((int) SystemClock.elapsedRealtime());

    public static HttpConnectionBuilder builder(String url) throws IOException, ClientException {
        return new HttpConnectionImpl(url);
    }


    public static HttpConnectionBuilder builder(URL url) throws IOException, ClientException {
        return new HttpConnectionImpl(url);
    }

    public static File downloadImage(String url, File path, boolean overwrite)
            throws IOException, ClientException, ServerException, FileOpException {
        File tempFile = new File(path.getParent(), path.getName() + "-" + counter.incrementAndGet() + ".tmp");
        File image = new HttpConnectionImpl(url)
                .setMethod(Method.GET)
                .setKeepAlive(false)
                .setLogger(Logger.LOG_GLIDE ? "HttpConnection.downloadImage" : null)
                .build()
                .downloadFile(path, tempFile, overwrite);
        return image;
    }

    /*package local*/ HttpConnection() {
    }

    public abstract File downloadFile(File path, File tempFile, boolean overwrite) throws IOException, ServerException, FileOpException;

    public abstract int getResponseCode() throws IOException;

    public abstract Map<String, List<String>> getHeaderFields();

    public abstract String getHeaderField(String header);

    public abstract void emptyAndClose();

    public abstract int getContentLength();

    public abstract InputStream getInputStream() throws IOException;

    public abstract String getContentType();

    public abstract String getResponseAsString() throws IOException;

    public abstract void downloadContent(OutputStream outputStream) throws IOException, ServerException;

    public abstract RangeHeader getResponseRange();

    public abstract void forceClose();

    public abstract void prepareForImageLoading();

    public abstract void connect() throws IOException;


    public enum Method {
        GET, POST, HEAD, DELETE
    }

}
