package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.Date;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.PermissionUtils;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.JsonExporter;
import fr.areastudio.jwterritorio.model.News;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences settings;
    public static final String PREFS = "JWTERRITORY";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 2;


    private DrawerManager drawerManager;
    private RecyclerView mRecyclerView;
    private NewsAdapter mNewsAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_assign:
                    intent = new Intent(MainActivity.this,AssignActivity.class);
                    MainActivity.this.startActivity(intent);
                    return true;
//                case R.id.navigation_web:
//                    intent = new Intent(MainActivity.this,WebActivity.class);
//                    MainActivity.this.startActivity(intent);
//                    return true;
                case R.id.navigation_address_list:
                    intent = new Intent(MainActivity.this,MyAddressesActivity.class);
                    MainActivity.this.startActivity(intent);
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);

        super.onCreate(savedInstanceState);
        if (getApplication() == null || ((MyApplication)getApplication()).getMe() == null || ((MyApplication)getApplication()).getMe().type == null) {
            settings.edit().remove("user").apply();
            Intent intent = new Intent(this,LoginActivity.class);
            this.startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawerManager = new DrawerManager(this);
        mRecyclerView = findViewById(R.id.news);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);

        mNewsAdapter = new NewsAdapter(this,new Select().from(News.class).where("read = 0 or presistent = 1").<News>execute());
        mRecyclerView.setAdapter(mNewsAdapter);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        try {
            if (((MyApplication) getApplication()).getMe().type.equals("PUBLISHER")) {
                navigation.getMenu().removeItem(R.id.navigation_assign);
            }
        }catch (Exception e){
            settings.edit().remove("user").apply();
            Intent intent = new Intent(this,LoginActivity.class);
            this.startActivity(intent);
            finish();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, READ_EXTERNAL_STORAGE_REQUEST_CODE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, true);
        }

        navigation.setSelectedItemId(R.id.navigation_main);
        if (getIntent().getScheme() != null && getIntent().getScheme().equals("jwterr")){
            new AlertDialog.Builder(MainActivity.this).setMessage(getIntent().getDataString()).setCancelable(true).create().show();
        }
        //settings.edit().remove("LAST_LANG_CHECK").apply();
        if (isOnline() && new Date().getTime() - settings.getLong("LAST_LANG_CHECK", 0) > 1000 * 60 * 60 * 12) {
            new LastInfoDownloader(this) {
                @Override
                protected void onPostExecute(Boolean newInfo) {
                    settings.edit().putLong("LAST_LANG_CHECK", new Date().getTime()).apply();
                    if (newInfo) {
                        Toast.makeText(MainActivity.this, R.string.new_info, Toast.LENGTH_LONG).show();
                        mNewsAdapter = new NewsAdapter(MainActivity.this,new Select().from(News.class).where("read = 0 or presistent = 1").<News>execute());
                        mRecyclerView.setAdapter(mNewsAdapter);

                    }
                }
            }.execute();

            new GetTerritoryTask(this,settings.getString("serverUrl","")) {
                @Override
                protected void onPostExecute(Boolean success) {
                    {
                        super.onPostExecute(success);
                        //mAuthTask = null;
                        if (!success) {
                            new AlertDialog.Builder(MainActivity.this).setMessage(R.string.update_error).setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    recreate();
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                        }
                    }
                }
            }.execute();
        }
        Intent intent = getIntent();
        if (intent.getData() != null && intent.getData().getScheme() != null) {
            if (intent.getData().getScheme().equals("content")) {
                Uri uri = intent.getData();
                try {
                    new JsonExporter(this).importFile(getContentResolver().openInputStream(uri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getData().getScheme().equals("file")) {
                Uri uri = intent.getData();

                try {
                    new JsonExporter(this).importFile(getContentResolver().openInputStream(uri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getData().getScheme().equals("http")) {
                System.out.println(intent.getData().getPath());
                String uuid = intent.getData().getPath().replace("/share/","");
                Address add = new Select().from(Address.class).where("uuid = ?",uuid).executeSingle();
                if (add != null){
                    add.myLocalDir = true;
                    add.save();
                }
            }
        }

    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (!"ADMINISTRATOR".equals(((MyApplication)getApplication()).getMe().type)) {
            menu.removeItem(R.id.action_generates13);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this).setMessage(R.string.loose_everything).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    settings.edit().remove("user").remove("password").apply();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create().show();
            return true;
        }
        if (id == R.id.action_sendlog){
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"soporte.apt.bolivia@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "APT Error log");
            i.setType("text/html");
            i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml(settings.getString("error_log","No error recorded")));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.send_error_log)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.action_clearsync) {
            new AlertDialog.Builder(this).setMessage(R.string.loose_unsync).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new Delete().from(DbUpdate.class).execute();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create().show();
            return true;
        }
        if (id == R.id.action_help) {
               startActivity(new Intent(this,HelpActivity.class));
            return true;
        }
        if (id == R.id.action_generates13) {
            new Printer(this).doWebViewPrint();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }


    }


}
