package fr.areastudio.jwterritorio.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Assignments;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.JsonUpdater;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;
import fr.areastudio.jwterritorio.model.Visit;

public abstract class GetTerritoryTask extends AsyncTask<Void, Void, Boolean> {

    private final WeakReference<Context> weakContext;
    private final SharedPreferences settings;
    private String mUrl;
    private SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd");

    private static String minVersion = "2";
    private String serverVersion = "1";

    public GetTerritoryTask(Context context, String url) {
        this.weakContext = new WeakReference<>(context);
        mUrl = url;
        this.settings = context.getSharedPreferences(MainActivity.PREFS,0);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Context context = weakContext.get();
            if (context != null){
                String password = context.getSharedPreferences(
                        MainActivity.PREFS, 0).getString("password","");
                String user = context.getSharedPreferences(
                        MainActivity.PREFS, 0).getString("user","");
                String loginUrl = mUrl + context.getString(R.string.login_url);
                loginUrl = loginUrl.replace("{email}", user);
                loginUrl = loginUrl.replace("{password}", password);
                if (!checkLogin(loginUrl)) {
                    return false;
                }
            }

            if (new Select().from(DbUpdate.class).execute().size() > 0){
                String json = new JsonUpdater(weakContext.get()).writeJson();
                if (json != null) {

                    URL url = new URL(mUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("charset", "utf-8");
                    connection.setUseCaches (false);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());

                    wr.write(json.getBytes());
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



            URL url = new URL(mUrl + "&operation=getAll");
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
            JSONObject dataJson = new JSONObject(out.toString());
            JSONArray territoryJson = dataJson.getJSONArray("territory");
            JSONArray addressJson = dataJson.getJSONArray("address");
            JSONArray publisherJson = dataJson.getJSONArray("user");
            JSONArray visitJson = dataJson.optJSONArray("visit");
            JSONArray settingsJson = dataJson.optJSONArray("settings");
            JSONArray assignJson = dataJson.optJSONArray("assignments");

            ArrayList<String> existingpubs = new ArrayList<String>();
            for (int i = 0; i < publisherJson.length(); i++) {
                JSONObject j = publisherJson.getJSONObject(i);
                Publisher p = new Select().from(Publisher.class).where("uuid = ?", j.getString("_id")).executeSingle();
                if (p == null){
                    p = new Publisher();
                    p.uuid = j.optString("_id");
                    p.name = j.getString("name");
                    p.email = j.getString("email");
                    p.type = j.getString("type");
                }
                p.type = j.getString("type");
                p.save();
                existingpubs.add(p.uuid);
            }
            List<Publisher> pubs = new Select().from(Publisher.class).execute();
            for(Publisher p : pubs){
                if (!existingpubs.contains(p.uuid)){
                    p.delete();
                }
            }

            ArrayList<String> existingterrs = new ArrayList<String>();
            for (int i = 0; i < territoryJson.length(); i++) {
                JSONObject j = territoryJson.getJSONObject(i);
                Territory t = new Select().from(Territory.class).where("uuid = ?",j.getString("_id")).executeSingle();
                if (t == null){
                    t = new Territory();
                }
                t.name = j.getString("name");
                t.number = j.getString("number");
                t.image = j.optString("image","");
                t.uuid = j.getString("_id");
                existingterrs.add(t.uuid);
                t.assignedPub = new Select().from(Publisher.class).where("uuid = ?", j.optString("assignedPublisher")).executeSingle();
                t.save();
            }
            List<Territory> terrs = new Select().from(Territory.class).execute();
            for(Territory t : terrs){
                if (!existingterrs.contains(t.uuid)){
                    t.delete();
                }
            }
            ArrayList<String> existingAdd = new ArrayList<String>();
            for (int i = 0; i < addressJson.length(); i++) {

                JSONObject j = addressJson.getJSONObject(i);
                Address a = new Select().from(Address.class).where("uuid = ?",j.getString("_id")).executeSingle();
                if (a == null){
                    a = new  Address();
                }
                a.uuid = j.getString("_id");
                existingAdd.add(a.uuid);
                a.name = j.getString("name");
                a.address = j.getString("address");
                a.lat = j.getString("lat").replace(",", ".");
                a.lng = j.getString("lng").replace(",", ".");
                a.homeDescription = j.optString("homeDescription");
                a.familyDescription = j.optString("familyDescription");
                a.description = j.optString("description");
                a.language = j.optString("language");
                a.phone = j.optString("phone");
                a.type = j.optString("type", "");
                a.blind = j.optInt("blind",0) == 1;
                a.mute = j.optInt("mute",0) == 1;
                a.deaf = j.optInt("deaf",0) == 1;
                a.sign = j.optInt("sign",0) == 1;
                a.gender = j.optString("gender","m");
                a.age = j.optString("age","ADULT");
                a.status = j.optString("status","");
                if (!"".equals(j.optString("lastVisit", ""))) {
                    a.lastVisit = dateformatter.parse(j.getString("lastVisit"));
                }
                a.territory = new Select().from(Territory.class).where("uuid = ?", j.getString("territory")).executeSingle();
                a.assignedPub = new Select().from(Publisher.class).where("uuid = ?", j.optString("publisher")).executeSingle();
                a.save();

            }
            List<Address> adds = new Select().from(Address.class).execute();
            for(Address a : adds){
                if (!existingAdd.contains(a.uuid)){
                        a.delete();
                }
            }
            if (visitJson != null) {
                for (int i = 0; i < visitJson.length(); i++) {
                    JSONObject j = visitJson.getJSONObject(i);
                    Visit a = new Select().from(Visit.class).where("uuid = ?", j.getString("_id")).executeSingle();
                    if (a == null) {
                        a = new Visit();
                    }
                    a.uuid = j.getString("uuid");
                    a.type = j.getString("type");
                    a.publisher = new Select().from(Publisher.class).where("uuid = ?", j.getString("publisher")).executeSingle();
                    a.address = new Select().from(Address.class).where("uuid = ?", j.getString("address")).executeSingle();
                    a.notes = j.optString("notes", "");
                    a.nextTheme = j.optString("nextTheme", "");
                    a.publication = j.optInt("publication", 0);
                    a.video = j.optInt("video", 0);
                    if (!j.optString("nextVisitDate", "false").equals("false")) {
                        a.nextVisitDate = dateformatter.parse(j.getString("nextVisitDate"));
                    }
                    if (!j.optString("date", "false").equals("false")) {
                        a.date = dateformatter.parse(j.getString("date"));
                    }
                    a.save();
                }
//                List<Visit> vis = new Select().from(Visit.class).execute();
//                for(Visit v : vis){
//                    if (!existingvisits.contains(v.uuid) ){
//                        v.delete();
//                    }
//                }

            }
            if (settingsJson != null) {
                for (int i = 0; i < settingsJson.length(); i++) {
                    JSONObject j = settingsJson.getJSONObject(i);
                    settings.edit().putString(j.getString("name"),j.getString("value")).apply();
                }
            }

            if (assignJson != null) {
                new Delete().from(Assignments.class).execute();
                for (int i = 0; i < assignJson.length(); i++) {
                    JSONObject j = assignJson.getJSONObject(i);
                    new Assignments(j.getString("_id"),(Territory)(new Select().from(Territory.class).where("uuid = ?", j.getString("territory")).executeSingle()),
                            (Publisher)(new Select().from(Publisher.class).where("uuid = ?", j.getString("publisher")).executeSingle()),
                            j.optString("dateBegin", "false").equals("false") ? null : "".equals(j.getString("dateBegin")) ? null : dateformatter.parse(j.getString("dateBegin")),
                            j.optString("dateEnd", "false").equals("false") ? null : "".equals(j.getString("dateEnd")) ? null : dateformatter.parse(j.getString("dateEnd"))).save();

                }
            }

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

    private boolean checkLogin(String mUrl){
        try {
            URL url = new URL(mUrl);
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

            mainJson.getString("email");
            mainJson.getString("_id");
            mainJson.getString("type");
            this.serverVersion = mainJson.optString("version", "1");

            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected  void onPostExecute(final Boolean success){
        if (!this.serverVersion.equals(minVersion)){
            Context context = weakContext.get();
            if (context != null){
                Toast.makeText(context,R.string.version_mismatch, Toast.LENGTH_SHORT).show();
            }
        }
    };
}