package fr.areastudio.jwterritorio.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.News;

public class LastInfoDownloader extends AsyncTask<String, Integer, Boolean> {

    private final Context context;
    private SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd");
    public LastInfoDownloader(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        try {
            URL url = new URL((context.getSharedPreferences(MainActivity.PREFS,0).getString("serverUrl","") + context.getString(R.string.last_news_url)).replace("\n",""));

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

//            Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
//                    .serializeNulls()
//                    .create();
            JSONArray newsJson = new JSONArray(out.toString());


            try {
                boolean newNews = false;
                for (int i = 0; i < newsJson.length(); i++) {
                    JSONObject n = newsJson.getJSONObject(i);
                    if (!new Select().from(News.class).where("uuid = ? ",n.optString("_id")).exists()){
                        News news = new News();
                        news.uuid =n.optString("_id");
                        news.title =n.optString("title");
                        news.content = n.optString("content");
                        news.date = dateformatter.parse(n.getString("date"));
                        news.alert = "1" .equals(n.optString("alert"));
                        news.save();
                        newNews = true;
                    }

                }
                return newNews;
            }catch (JsonSyntaxException e){
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("NEWS_DOWN", " " + e.getMessage());
        }
        return false;
    }
}