package fr.areastudio.jwterritorio;

import android.content.SharedPreferences;

import com.activeandroid.query.Select;

import fr.areastudio.jwterritorio.activities.MainActivity;
import fr.areastudio.jwterritorio.model.Publisher;

/**
 * Created by Julien on 16/04/2018.
 */

public class MyApplication extends com.activeandroid.app.Application {

    private Publisher me;

    public void onCreate() {
        super.onCreate();
        SharedPreferences settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        Publisher me = new Select().from(Publisher.class).where("email = ?",settings.getString("user","")).executeSingle();
        if(me != null){
            setMe(me);
        }
    }
    public void setMe(Publisher me) {
        this.me = me;
    }

    public Publisher getMe() {
        return this.me;
    }
}
