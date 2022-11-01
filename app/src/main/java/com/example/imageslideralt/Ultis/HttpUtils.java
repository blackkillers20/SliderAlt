package com.example.imageslideralt.Ultis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HttpUtils {

    private static final int CONN_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 30000;
    private static final Logger logger = Logger.getLogger(HttpUtils.class.getName());
    private HashMap<String, String> headers = new HashMap<>();

    // START REQUIRE IMPROVEMENTS
    private CookieManager cookieManager;

    public HttpUtils(){
        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    private long lastResponseTime = Long.MIN_VALUE;
    public long getLastResponseTime() {
        return lastResponseTime;
    }


    public void setForgiveCookies(boolean forgive) {
        cookieManager.setCookiePolicy( forgive ? CookiePolicy.ACCEPT_NONE : CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }
    private boolean autoRedirectEnabled = true;
    protected void setAutoRedirectEnabled(boolean autoRedirectEnabled) {
        this.autoRedirectEnabled = autoRedirectEnabled;
    }
    public boolean isAutoRedirectEnabled() {
        return autoRedirectEnabled;
    }
    // END REQUIRE IMPROVEMENTS




    static {
        if (!Boolean.getBoolean("sun.net.http.errorstream.enableBuffering")) {
            logger.config("System property \"sun.net.http.errorstream.enableBuffering\" is not set to true, this will cause issues");
        }
    }

    private boolean throwExceptions;

    /**
     * Throws exceptions if server responds with error code >= 400
     *
     */
    public void setThrowExceptions(boolean throwExceptions) {
        this.throwExceptions = throwExceptions;
    }

    public boolean isThrowingExceptions() {
        return throwExceptions;
    }

    public void setKeepAlive(boolean keepAlive) {
        setHeader("Connection", keepAlive ? "keep-alive" : null);
    }

    public void setHeader(String name, String value) {
        if (value == null) headers.remove(name);
        else headers.put(name, value);
    }

    protected URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void download(URL url, File targetFile) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(CONN_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);
        applyHeaders(urlConnection);
        FileOutputStream baos = new FileOutputStream(targetFile);
        execute(urlConnection, baos);
        baos.flush();
        baos.close();
    }

    public Uri download(Context context, URL url, String fileName) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(CONN_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);
        applyHeaders(urlConnection);
        File targetFile = new File(context.getFilesDir(), fileName);
        FileOutputStream baos = new FileOutputStream(targetFile);
        execute(urlConnection, baos);
        baos.flush();
        baos.close();
        return Uri.fromFile(targetFile);
    }

    public Bitmap downloadToBitmap(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(CONN_TIMEOUT);
        urlConnection.setReadTimeout(READ_TIMEOUT);
        applyHeaders(urlConnection);
        return executeAndGetBitmap(urlConnection);
    }

    private void writeContent(HttpURLConnection urlConnection, String data) throws IOException {
        urlConnection.setInstanceFollowRedirects( autoRedirectEnabled );
        logger.finest(data);
        byte[] bytes = data.getBytes("UTF-8");
        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(bytes.length);
        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        out.write(bytes);
        out.flush();
        out.close();
    }

    private void writeContent(HttpURLConnection urlConnection, InputStream data) throws IOException {
        urlConnection.setInstanceFollowRedirects( autoRedirectEnabled );
        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(data.available());
        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        byte[] bytes = new byte[512];
        int readBytes;
        while ((readBytes = data.read(bytes)) > 0) {
            out.write(bytes, 0, readBytes);
        }
        out.flush();
        out.close();
    }

    protected String executeAndGetResponse(HttpURLConnection request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(request, baos);
        baos.flush();
        baos.close();
        String ret = baos.toString("UTF-8");
        logger.finest(ret);
        return ret;
        // return new String(baos.toByteArray(), "UTF-8");
    }

    protected Bitmap executeAndGetBitmap(HttpURLConnection request) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        execute(request, baos);
        baos.flush();
        baos.close();
        String ret = baos.toString("UTF-8");
        logger.finest(ret);
        return BitmapFactory.decodeByteArray(ret.getBytes(), 0, ret.getBytes().length);
        // return new String(baos.toByteArray(), "UTF-8");
    }

    protected void execute(HttpURLConnection connection, OutputStream stream) throws IOException {
        logger.finest(connection.getRequestMethod() + " " + connection.getURL() + " " + connection.getResponseCode()
                + " " + connection.getResponseMessage());
        connection.setInstanceFollowRedirects( autoRedirectEnabled );
        int status = connection.getResponseCode();

        if (throwExceptions && status >= 400) {
            throw new IOException(
                    "Server has responded with " + status + " " + connection.getResponseMessage());
        }

        InputStream input;
//        if (status >= 400)
//            input = connection.getErrorStream();
//        else
//            input = connection.getInputStream();
        if (status >= 200 && status <= 299) {
            input = connection.getInputStream();
        } else {
            input = connection.getErrorStream();
        }
        if (input == null) return;

        if (stream != null) {
            byte[] buffer = new byte[512];
            int byteLetti;
            while ((byteLetti = input.read(buffer)) > 0) {
                stream.write(buffer, 0, byteLetti);
            }
        }
        input.close();
        lastResponseTime = System.currentTimeMillis();
    }

    protected void applyHeaders(HttpURLConnection request) {
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }
}
