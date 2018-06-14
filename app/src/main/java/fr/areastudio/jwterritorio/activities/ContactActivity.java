package fr.areastudio.jwterritorio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;

public class ContactActivity extends AppCompatActivity {

    private SharedPreferences settings;

    private DrawerManager drawerManager;

    private TextView emergencyPhone;
    private TextView volunteerPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        emergencyPhone = findViewById(R.id.emergencyphone);
        volunteerPhone = findViewById(R.id.voluntaryphone);

        emergencyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactActivity.this.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + emergencyPhone.getText().toString())));
            }
        });

        volunteerPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactActivity.this.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + volunteerPhone.getText().toString())));
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawerManager = new DrawerManager(this);
        HashMap<String,String> phones = new HashMap<>();
        phones.put("cochabamba", "77949216");
        phones.put("santa cruz", "78487876");
        phones.put("la paz", "70654845");
        phones.put("sucre", "75437448");
        phones.put("potosi", "68373854");
        phones.put("oruro", "72340784");
        phones.put("tarija", "69310575");
        phones.put("cobija", "72930474");
        phones.put("guayaramerin", "73951669");
        phones.put("trinidad", "69405186");
        phones.put("riberalta", "76010955");
        phones.put("shinahota", "71548924");
        phones.put("camiri", "73373333");
        phones.put("villamontes", "73192048");
        phones.put("yacuiba", "77896545");
        phones.put("mairana", "70896501");
        phones.put("samaipata", "70896501");
        phones.put("puerto suarez", "75750283");
        phones.put("puerto quijarro", "75750283");
        String userCity = ((MyApplication)getApplication()).getMe().congregation.city;
        for (String city:phones.keySet()
             ) {
            if (userCity.toLowerCase().contains(city)){
                emergencyPhone.setText(phones.get(city));
                break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}
