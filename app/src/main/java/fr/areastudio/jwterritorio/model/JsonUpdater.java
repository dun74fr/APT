package fr.areastudio.jwterritorio.model;

import android.content.Context;

import com.activeandroid.query.Select;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.activities.MainActivity;

public class JsonUpdater {


    private final Context context;
    private SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd");

    public JsonUpdater(Context context){
        this.context = context;
    }

    public String writeJson() {

        try {
            List<DbUpdate> updates = new Select().from(DbUpdate.class).execute();

            JSONArray main = new JSONArray();
            for (DbUpdate u: updates) {
                JSONObject o = new JSONObject();
                o.put("model",u.model);
                o.put("updateType",u.updateType);
                o.put("uuid",u.uuid);
                o.put("publisherUuid",u.publisherUuid);
                o.put("date",dateformatter.format(u.date));
                if (u.model.equals("TERRITORY")){
                    JSONObject m = new JSONObject();
                    Territory t = new Select().from(Territory.class).where("uuid = ?", u.uuid).executeSingle();
                    if (t == null){
                        continue;
                    }
                    m.put("assignedPublisher",t.assignedPub == null ? "" : t.assignedPub.uuid);
                    o.put("object",m);
                }
                else if (u.model.equals("VISIT")){
                    JSONObject m = new JSONObject();
                    o.put("model","ADDRESS");
                    o.put("updateType","UPDATE");
                    Visit v = new Select().from(Visit.class).where("uuid = ?", u.uuid).executeSingle();
                    if (v == null){
                        continue;
                    }
                    o.put("uuid",v.address.uuid);
                    m.put("lastVisit",dateformatter.format(v.date));

                    m.put("publisher",v.publisher.uuid);
                    o.put("object",m);
                }
                else if (u.model.equals("ADDRESS")){
                    JSONObject m = new JSONObject();
                    Address a = new Select().from(Address.class).where("uuid = ?", u.uuid).executeSingle();
                    if (a == null){
                        continue;
                    }
                    if (u.updateType.equals("CREATE")){
                        m.put("_created_by",context.getSharedPreferences(MainActivity.PREFS,0).getString("user",""));
                    }
                    m.put("type",a.type);
                    m.put("uuid",a.uuid);
                    m.put("description",a.description);
                    m.put("address",a.address);
                    m.put("sign",a.sign ? "1" :"0");
                    m.put("mute",a.mute ? "1" :"0");
                    m.put("deaf",a.deaf ? "1" :"0");
                    m.put("blind",a.blind ? "1" :"0");
                    m.put("homeDescription", a.homeDescription);
                    m.put("familyDescription", a.familyDescription);
                    m.put("age", a.age);
                    m.put("language", a.language);
                    m.put("gender", a.gender);
                    m.put("lat", a.lat);
                    m.put("lng", a.lng);
                    m.put("name", a.name);
                    m.put("phone", a.phone);
                    m.put("status", "".equals(a.status) ? "VALID":a.status);
                    if (a.territory == null) {
                        Territory undefined = new Select().from(Territory.class).where("number = ?","-1").executeSingle();
                        a.territory = undefined;
                        if (a.territory == null){
                            m.put("territory", "");
                        }
                        else {
                            m.put("territory", a.territory.uuid);
                        }
                    }else {
                        m.put("territory", a.territory.uuid);
                    }
                    m.put("publisher",a.assignedPub == null ? null : a.assignedPub.uuid);
                    o.put("object",m);
                }
                main.put(o);
            }
            return main.toString();
        }catch (JsonSyntaxException | JSONException e){
            e.printStackTrace();
            if (context != null){
                context.getSharedPreferences(
                        MainActivity.PREFS, 0).edit().putString("error_log", e.getMessage() + e.getStackTrace()[0].toString());
            }

        }
        return null;
    }

}