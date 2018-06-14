package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.JsonUpdater;

public abstract class UpdateDBTask extends AsyncTask<Void, Void, Boolean> {

    private final WeakReference<Context> weakContext;
    private String mUrl;
   private String mCongregationUUID;

   public UpdateDBTask(Context context, String url, String congregationUUID) {
       this.weakContext = new WeakReference<>(context);
       mUrl = url;
       mCongregationUUID = congregationUUID;
   }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

   @Override
   protected Boolean doInBackground(Void... params) {
       try {

           if (new Select().from(DbUpdate.class).execute().size() > 0){
               String json = new JsonUpdater(weakContext.get()).writeJson();
               if (json != null) {

                   URL url = new URL(mUrl);

                   HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                   connection.setDoOutput(true);
                   connection.setDoInput(true);
                   connection.setInstanceFollowRedirects(false);
                   connection.setRequestMethod("PUT");
                   connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                   connection.setRequestProperty("charset", "utf-8");
                   connection.setUseCaches (false);

                   DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());

                   HashMap<String, String> postDataParams = new HashMap<>();
                   postDataParams.put("congregation_uuid",mCongregationUUID);
                   postDataParams.put("json",json);
                   wr.write(getPostDataString(postDataParams).getBytes());
                   wr.flush();
                   wr.close();
                   if (connection.getResponseCode() == -1) {
                       return false;
                   }
                   InputStream input = new BufferedInputStream(connection.getInputStream());
                   byte data[] = new byte[1024];
                   int count;
                   ByteArrayOutputStream out = new ByteArrayOutputStream();
                   while ((count = input.read(data)) != -1) {
                       out.write(data, 0, count);
                   }
                   out.flush();
                   input.close();
                   Log.d("UPDATE", out.toString());
                   new Delete().from(DbUpdate.class).execute();
                   return out.toString().contains("OK");
               }
           }

           return true;
       } catch (Exception e) {
           Context context = weakContext.get();
           if (context != null){
               context.getSharedPreferences(
                       MainActivity.PREFS, 0).edit().putString("error_log", e.getMessage() + e.getStackTrace()[0].toString()).apply();
           }
           return false;
       }
   }

   @Override
   protected abstract void onPostExecute(final Boolean success);
}