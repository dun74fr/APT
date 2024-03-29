package fr.areastudio.jwterritorio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.activeandroid.query.Select;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.model.Publisher;
//import fr.areastudio.jwterritorio.services.Updater;


public class SplashActivity extends AppCompatActivity {

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);


        if (!settings.contains("user") ||!settings.contains("password")){
            Intent intent = new Intent(this,LoginActivity.class);
            this.startActivity(intent);
        }
        else {
            Publisher me = new Select().from(Publisher.class).where("email = ?",settings.getString("user","")).executeSingle();
            if(me == null){
                settings.edit().remove("user");
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            else {
                ((MyApplication) getApplication()).setMe(me);
//                Updater alarm = new Updater();
//                alarm.setAlarm(getApplicationContext());

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

        }
        finish();
    }


}