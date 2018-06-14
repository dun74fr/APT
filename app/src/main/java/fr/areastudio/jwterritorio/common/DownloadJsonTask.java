package fr.areastudio.jwterritorio.common;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadJsonTask extends AsyncTask<String, Void, InputStream> {

    public DownloadJsonTask(){

    }
    @Override
    protected InputStream doInBackground(String... urls) {
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            // return getResources().getString(R.string.connection_error);
        }
        return null;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));

        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        conn.connect();
        if (conn.getResponseCode() == -1) {
            return null;
        }
        InputStream input = new BufferedInputStream(conn.getInputStream());

        // OutputStream output = new FileOutputStream(out);

        byte data[] = new byte[1024];
        long total = 0;
        int count;
        while ((count = input.read(data)) != -1) {
            out.write(data, 0, count);
        }

        out.flush();

        input.close();
        return new ByteArrayInputStream(out.toByteArray());

    }
}