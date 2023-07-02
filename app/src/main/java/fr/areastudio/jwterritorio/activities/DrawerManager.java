package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.io.File;
import java.io.FileOutputStream;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.JsonExporter;
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
        pubCongregation.setText(settings.getString("congregation",""));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_backup) {
            String json = new JsonExporter(this.activity).writeJson();
            String fileName = "apt_backup.aptbk";
            File dir = new File(activity.getCacheDir(), "bkp");
            dir.mkdirs();
            File file = new File(dir, fileName);
            try {
                FileOutputStream fOut = new FileOutputStream(file);
                fOut.write(json.getBytes());
                fOut.flush();
                fOut.close();

            Uri uri = FileProvider.getUriForFile(this.activity, activity.getPackageName()+".fileprovider", file);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("*/*");
            intent.setDataAndType(uri, activity.getContentResolver().getType(uri));
            intent.putExtra(Intent.EXTRA_SUBJECT, "APT backup");
            intent.putExtra(Intent.EXTRA_TEXT, "Here's the backup file");
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            try {
                activity.startActivity(Intent.createChooser(intent, "Backup"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this.activity, "No App Available", Toast.LENGTH_SHORT).show();
            }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
//        if (id == R.id.nav_calls) {
//            Intent intent = new Intent(activity,ContactActivity.class);
//            activity.startActivity(intent);
//        }
//        if (id == R.id.poi) {
//            Intent intent = new Intent(activity,WebPointsActivity.class);
//            activity.startActivity(intent);
//        }
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }
}
