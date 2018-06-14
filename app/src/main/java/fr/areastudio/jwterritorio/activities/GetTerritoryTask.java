package fr.areastudio.jwterritorio.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Congregation;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.JsonUpdater;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;
import fr.areastudio.jwterritorio.model.Visit;

public abstract class GetTerritoryTask extends AsyncTask<Void, Void, Boolean> {

    private final WeakReference<Context> weakContext;
    private String mUrl;
    private String mCongregationUUID;
    private SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public GetTerritoryTask(Context context, String url, String congregationUUID) {
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
                    if (out.toString().contains("OK")){
                        new Delete().from(DbUpdate.class).execute();
                    }
                    else {
                        if (weakContext.get() != null){
                            weakContext.get().getSharedPreferences(MainActivity.PREFS,0).edit().putString("error_log","GetTerritoryTask: " + out.toString()).apply();
                        }
                        return false;
                    }

                }
            }



            URL url = new URL(mUrl + "?congregation_uuid=" + mCongregationUUID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            conn.setReadTimeout(60000 /* milliseconds */);
            conn.setConnectTimeout(90000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            conn.connect();
            if (conn.getResponseCode() == -1) {
                return false;
            }
            InputStream input = new BufferedInputStream(conn.getInputStream());

            // OutputStream output = new FileOutputStream(out);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                out.write(data, 0, count);
            }
            out.flush();
            input.close();
            JSONObject mainJson = new JSONObject(out.toString());
            JSONObject dataJson = mainJson.getJSONObject("data");
            JSONArray territoryJson = dataJson.getJSONArray("territory");
            JSONArray addressJson = dataJson.getJSONArray("address");
            JSONArray publisherJson = dataJson.getJSONArray("publisher");
            JSONArray visitJson = dataJson.optJSONArray("visit");
            Congregation congregation = new Select().from(Congregation.class).executeSingle();
//            new Delete().from(Territory.class).execute();
//            new Delete().from(Address.class).execute();
            HashMap<Integer, String> terr = new HashMap<>();

            for (int i = 0; i < publisherJson.length(); i++) {
                JSONObject j = publisherJson.getJSONObject(i);
                Publisher p = new Select().from(Publisher.class).where("uuid = ?", j.getString("uuid")).executeSingle();
                if (p == null){
                    p = new Publisher();
                    p.congregation = congregation;
                    p.uuid = j.optString("uuid", String.valueOf(j.getInt("id")));
                    p.name = j.getString("name");
                    p.email = j.getString("email");
                    p.type = j.getString("type");
                }
                p.type = j.getString("type");
                p.save();

            }
            ArrayList<String> existingterrs = new ArrayList<String>();
            for (int i = 0; i < territoryJson.length(); i++) {
                JSONObject j = territoryJson.getJSONObject(i);
                Territory t = new Select().from(Territory.class).where("uuid = ?",j.getString("uuid")).executeSingle();
                if (t == null){
                    t = new Territory();
                }
                t.name = j.getString("name");
                t.number = j.getString("number");
                t.uuid = j.getString("uuid");
                existingterrs.add(t.uuid);
                t.congregation = congregation;
                t.assignedPub = new Select().from(Publisher.class).where("uuid = ?", j.optString("assignedPublisher")).executeSingle();
                terr.put(j.getInt("id"), t.uuid);
                t.save();
            }
            List<Territory> terrs = new Select().from(Territory.class).execute();
            for(Territory t : terrs){
                if (!existingterrs.contains(t.uuid)){
//                    if (!"DRAFT".equals(a.status)) {
                    t.delete();
//                    }
                }
            }
            ArrayList<String> existingAdd = new ArrayList<String>();
            for (int i = 0; i < addressJson.length(); i++) {

                JSONObject j = addressJson.getJSONObject(i);
                Address a = new Select().from(Address.class).where("uuid = ?",j.getString("uuid")).executeSingle();
                if (a == null){
                    a = new  Address();
                }
                a.uuid = j.getString("uuid");
                existingAdd.add(a.uuid);
                a.name = j.getString("name");
                a.address = j.getString("address");
                a.lat = j.getString("lat").replace(",", ".");
                a.lng = j.getString("lng").replace(",", ".");
                a.homeDescription = j.getString("homeDescription");
                a.familyDescription = j.optString("familyDescription");
                a.description = j.getString("description");
                a.language = j.getString("language");
                a.phone = j.getString("phone");
                a.type = j.optString("type", "");
                a.mute = j.getBoolean("mute");
                a.deaf = j.getBoolean("deaf");
                a.sign = j.optBoolean("sign", false);
                a.gender = j.getString("gender");
                a.age = j.getString("age");
                a.status = j.optString("status","");
                a.territory = new Select().from(Territory.class).where("uuid = ?", terr.get(j.getJSONArray("territory").getInt(0))).executeSingle();
                a.save();

            }
            List<Address> adds = new Select().from(Address.class).execute();
            for(Address a : adds){
                if (!existingAdd.contains(a.uuid)){
//                    if (!"DRAFT".equals(a.status)) {
                        a.delete();
//                    }
                }
            }
            if (visitJson != null) {
                //ArrayList<String> existingvisits = new ArrayList<String>();
                for (int i = 0; i < visitJson.length(); i++) {
                    JSONObject j = visitJson.getJSONObject(i);
                    Visit a = new Select().from(Visit.class).where("uuid = ?", j.getString("uuid")).executeSingle();
                    if (a == null) {
                        a = new Visit();
                    }
                    a.uuid = j.getString("uuid");
                    a.type = j.getString("type");
                    //existingvisits.add(a.uuid);
                    a.publisher = new Select().from(Publisher.class).where("uuid = ?", j.getString("publisher")).executeSingle();
                    a.address = new Select().from(Address.class).where("uuid = ?", j.getString("address")).executeSingle();
                    a.notes = j.optString("notes", "");
                    a.nextTheme = j.optString("nextTheme", "");
                    a.publication = j.optInt("publication", 0);
                    a.video = j.optInt("video", 0);
                    if (!j.optString("nextVisitDate", null).equals("false")) {
                        a.nextVisitDate = dateformatter.parse(j.getString("nextVisitDate"));
                    }
                    if (!j.optString("date", null).equals("false")) {
                        a.date = dateformatter.parse(j.getString("date"));
                    }
                    a.save();
                }
                List<Visit> vis = new Select().from(Visit.class).execute();
//                for(Visit v : vis){
//                    if (!existingvisits.contains(v.uuid) ){
//                        v.delete();
//                    }
//                }
            }
//

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Context context = weakContext.get();
            if (context != null){
                context.getSharedPreferences(
                        MainActivity.PREFS, 0).edit().putString("error_log", "GetTerritoryTask : " + e.getMessage() + e.getStackTrace()[0].toString()).apply();
            }
            return false;
        }
    }

    @Override
    protected abstract void onPostExecute(final Boolean success);
}