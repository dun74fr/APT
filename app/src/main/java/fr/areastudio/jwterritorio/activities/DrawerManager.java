package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.activeandroid.query.Select;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Publisher;

/**
 * Created by Julien on 16/03/2018.
 */

class DrawerManager implements NavigationView.OnNavigationItemSelectedListener {

    private final TextView pubCongregation;
    private final Activity activity;
    private SharedPreferences settings;
    private TextView pubEmail;
    private TextView pubName;

    public DrawerManager(Activity activity){

        settings = activity.getSharedPreferences(
                MainActivity.PREFS, 0);
        this.activity = activity;
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        pubName = headerLayout.findViewById(R.id.pubName);
        pubEmail = headerLayout.findViewById(R.id.pubEmail);
        pubCongregation = headerLayout.findViewById(R.id.pubCongregation);

        Publisher pub = new Select().from(Publisher.class).where("uuid = ?",settings.getString("user_id","")).executeSingle();
        if (pub == null)
        {
            return;
        }
        try {
            PackageInfo pInfo = this.activity.getPackageManager().getPackageInfo(this.activity.getPackageName(), 0);
            String version = pInfo.versionName;
            ((TextView)headerLayout.findViewById(R.id.version)).setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        pubName.setText(pub.name);
        pubEmail.setText(pub.email);
        pubCongregation.setText(pub.congregation.name);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_emergencias) {
            Intent intent = new Intent(activity,EmergencyActivity.class);
            activity.startActivity(intent);
        }
        if (id == R.id.nav_calls) {
            Intent intent = new Intent(activity,ContactActivity.class);
            activity.startActivity(intent);
        }
        if (id == R.id.poi) {
            Intent intent = new Intent(activity,WebPointsActivity.class);
            activity.startActivity(intent);
        }
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }
}
