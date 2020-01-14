package fr.areastudio.jwterritorio.model;

import android.content.Context;

import com.activeandroid.query.Select;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.activities.MainActivity;

public class JsonExporter {


    private final Context context;
    private SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd");

    public JsonExporter(Context context) {
        this.context = context;
    }

    public String writeJson() {

        try {

            List<Territory> territory = new Select().from(Territory.class).execute();
            List<Address> addresses = new Select().from(Address.class).execute();
            List<Publisher> users = new Select().from(Publisher.class).execute();
            List<Visit> visits = new Select().from(Visit.class).execute();


            JSONObject main = new JSONObject();
            JSONArray jterritories = new JSONArray();
            JSONArray jaddresses = new JSONArray();
            JSONArray jusers = new JSONArray();
            JSONArray jvisits = new JSONArray();

            main.put("territory", jterritories);
            main.put("address", jaddresses);
            main.put("user", jusers);
            main.put("visit", jvisits);

            for (Territory u : territory) {
                JSONObject o = new JSONObject();
                o.put("_id", u.uuid);
                o.put("name", u.name);
                o.put("number", u.number);
                o.put("image", u.image);
                if (u.assignedPub != null) {
                    o.put("assignedPublisher", u.assignedPub.uuid);
                }
                jterritories.put(o);
            }
            for (Publisher u : users) {
                JSONObject o = new JSONObject();
                o.put("_id", u.uuid);
                o.put("name", u.name);
                o.put("email", u.email);
                o.put("type", u.type);
                jusers.put(o);
            }
            for (Address u : addresses) {
                JSONObject o = new JSONObject();
                o.put("_id", u.uuid);
                o.put("name", u.name);
                o.put("address", u.address);
                o.put("lat", u.lat);
                o.put("lng", u.lng);
                o.put("homeDescription", u.homeDescription);
                o.put("familyDescription", u.familyDescription);
                o.put("description", u.description);
                o.put("language", u.language);
                o.put("phone", u.phone);
                o.put("type", u.type);
                o.put("blind", u.blind ? "1" : "0");
                o.put("mute", u.mute ? "1" : "0");
                o.put("deaf", u.deaf ? "1" : "0");
                o.put("sign", u.sign ? "1" : "0");
                o.put("gender", u.gender);
                o.put("age", u.age);
                o.put("status", u.status);
                o.put("myLocalDir", u.myLocalDir ? "1" : "0");
                if (u.lastVisit != null) {
                    o.put("lastVisit", dateformatter.format(u.lastVisit));
                }
                if (u.territory != null) {
                    o.put("territory", u.territory.uuid);
                }
                if (u.assignedPub != null) {
                    o.put("publisher", u.assignedPub.uuid);
                }
                jaddresses.put(o);

            }
            for (Visit u : visits) {
                JSONObject o = new JSONObject();
                o.put("_id", u.uuid);
                o.put("type", u.type);
                if (u.publisher != null) {
                    o.put("publisher", u.publisher.uuid);
                }
                if (u.address != null) {
                    o.put("address", u.address.uuid);
                }
                o.put("notes", u.notes);
                o.put("nextTheme", u.nextTheme);
                o.put("publication", u.publication);
                o.put("video", u.video);
                if (u.nextVisitDate != null) {
                    o.put("nextVisitDate",dateformatter.format(u.nextVisitDate));
                }
                if (u.date != null) {
                   o.put("date",dateformatter.format(u.date));
                }
                jvisits.put(o);
            }
            return main.toString();
        } catch (JsonSyntaxException | JSONException e) {
            e.printStackTrace();
            if (context != null) {
                context.getSharedPreferences(
                        MainActivity.PREFS, 0).edit().putString("error_log", e.getMessage() + e.getStackTrace()[0].toString());
            }

        }
        return null;
    }

    public void importFile(InputStream in) {


        JSONObject dataJson = null;
        String inputLine;
        String json = "";
        try {
            BufferedReader bin = new BufferedReader(new InputStreamReader(
                    in, "utf-8"));

            while ((inputLine = bin.readLine()) != null) {
                json += inputLine;
            }
            in.close();

            dataJson = new JSONObject(json);

            JSONArray territoryJson = dataJson.getJSONArray("territory");
            JSONArray addressJson = dataJson.getJSONArray("address");
            JSONArray publisherJson = dataJson.getJSONArray("user");
            JSONArray visitJson = dataJson.optJSONArray("visit");

            for (int i = 0; i < publisherJson.length(); i++) {
                JSONObject j = publisherJson.getJSONObject(i);
                Publisher p = new Select().from(Publisher.class).where("uuid = ?", j.getString("_id")).executeSingle();
                if (p == null) {
                    p = new Publisher();
                    p.uuid = j.optString("_id");
                    p.name = j.getString("name");
                    p.email = j.getString("email");
                    p.type = j.getString("type");
                }
                p.type = j.getString("type");
                p.save();
            }
            for (int i = 0; i < territoryJson.length(); i++) {
                JSONObject j = territoryJson.getJSONObject(i);
                Territory t = new Select().from(Territory.class).where("uuid = ?", j.getString("_id")).executeSingle();
                if (t == null) {
                    t = new Territory();
                }
                t.name = j.getString("name");
                t.number = j.getString("number");
                t.image = j.optString("image", "");
                t.uuid = j.getString("_id");
//                existingterrs.add(t.uuid);
                t.assignedPub = new Select().from(Publisher.class).where("uuid = ?", j.optString("assignedPublisher")).executeSingle();
                t.save();
            }
            for (int i = 0; i < addressJson.length(); i++) {

                JSONObject j = addressJson.getJSONObject(i);
                Address a = new Select().from(Address.class).where("uuid = ?", j.getString("_id")).executeSingle();
                if (a == null) {
                    a = new Address();
                }
                a.uuid = j.getString("_id");
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
                a.blind = j.optInt("blind", 0) == 1;
                a.mute = j.optInt("mute", 0) == 1;
                a.deaf = j.optInt("deaf", 0) == 1;
                a.sign = j.optInt("sign", 0) == 1;
                a.myLocalDir = j.optInt("myLocalDir", 0) == 1;
                a.gender = j.optString("gender", "m");
                a.age = j.optString("age", "ADULT");
                a.status = j.optString("status", "");
                if (!"".equals(j.optString("lastVisit", ""))) {
                    a.lastVisit = dateformatter.parse(j.getString("lastVisit"));
                }
                a.territory = new Select().from(Territory.class).where("uuid = ?", j.getString("territory")).executeSingle();
                a.assignedPub = new Select().from(Publisher.class).where("uuid = ?", j.optString("publisher")).executeSingle();
                a.save();

            }
            if (visitJson != null) {
                for (int i = 0; i < visitJson.length(); i++) {
                    JSONObject j = visitJson.getJSONObject(i);
                    Visit a = new Select().from(Visit.class).where("uuid = ?", j.getString("_id")).executeSingle();
                    if (a == null) {
                        a = new Visit();
                    }
                    a.uuid = j.getString("_id");
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

            }
        } catch (Exception e) {
            System.err.println("Restore error : " + e.getMessage());
            e.printStackTrace();
        }
    }

}