package fr.areastudio.jwterritorio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Category;
import fr.areastudio.jwterritorio.model.Medic;
import fr.areastudio.jwterritorio.model.Speciality;
import fr.areastudio.jwterritorio.model.Zone;

public class EmergencyActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;
    private SharedPreferences settings;
    private RecyclerView mRecyclerView;
    private EmergencyAdapter emergencyAdapter;
    private DrawerManager drawerManager;
    private SwipeRefreshLayout mSwipeRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawerManager = new DrawerManager(this);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);
        mSwipeRefresh = findViewById(R.id.activity_main_swipe_refresh_layout);
        emergencyAdapter = new EmergencyAdapter(this, new ArrayList<Medic>());
        emergencyAdapter.setListener(new EmergencyAdapter.MedicListener() {
            @Override
            public void onClick(Medic medic) {
//                Intent intent = new Intent(EmergencyActivity.this, ViewAddressActivity.class);
//                intent.putExtra("address_id",address.getId());
//                EmergencyActivity.this.startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(emergencyAdapter);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                new GetMedicTask(getResources().getString(R.string.emergency_url),settings.getString("city", "")).execute();
            }
        });

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Medic> medics = new Select().from(Medic.class).where("city = ?", settings.getString("city", "")).execute();

        if (medics.size() == 0) {
            mSwipeRefresh.setRefreshing(true);
            new GetMedicTask(getResources().getString(R.string.emergency_url),settings.getString("city", "")).execute();

        }
        else {
            emergencyAdapter.setMedics(medics);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.emergency, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_map) {
            long[] ids = new long[emergencyAdapter.getMedics().size()];
            for (int i = 0; i < emergencyAdapter.getMedics().size(); i++) {
                ids[i] = emergencyAdapter.getMedics().get(i).getId();
            }

            Intent map = new Intent(this, EmergencyMapsActivity.class);
            map.putExtra("ids", ids);
            this.startActivity(map);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent scan = new Intent(this, SimpleScannerActivity.class);
                    this.startActivity(scan);
                } else {


                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public class GetMedicTask extends AsyncTask<Void, Void, Boolean> {

        private  String mUrl;
        private  String mCity;

        GetMedicTask(String url, String city) {
            mUrl = url;
            mCity = city;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(mUrl + "?city=" + mCity);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                conn.setReadTimeout(60000 /* milliseconds */);
                conn.setConnectTimeout(90000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                conn.connect();
                if (conn.getResponseCode() == -1) {
                    return null;
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
                JSONArray medicsJson = mainJson.getJSONArray("data");

                new Delete().from(Medic.class).execute();
                for (int i = 0; i < medicsJson.length(); i++){
                    JSONObject j = medicsJson.getJSONObject(i);
                    Medic m = new Medic();
                    m.name = j.getString("name");
                    m.address = j.getString("address");
                    m.insurance = j.getBoolean("insurance");
                    m.witness = j.getBoolean("witness");
                    m.pharmacy = j.getBoolean("pharmacy");
                    m.intensiveTherapy = j.getBoolean("intensive_therapy");
                    m.operatingRoom = j.getBoolean("operating_room");
                    m.priceList = j.getDouble("pricelist");
                    m.credit = j.getBoolean("credit");
                    m.city = mCity;
                    if (j.has("lat") && j.has("lng")) {
                        m.lat = String.valueOf(j.getDouble("lat"));
                        m.lng = String.valueOf(j.getDouble("lng"));
                    }

                    if (j.get("image") instanceof String) {
                        m.image = j.getString("image");
                    }
                    if (j.get("zone_id") instanceof JSONArray) {
                        Zone z = new Select().from(Zone.class).where("uuid = ?", j.getJSONArray("zone_id").getString(0)).executeSingle();
                        if (z == null) {
                            z = new Zone();
                            z.uuid = j.getJSONArray("zone_id").getString(0);
                            z.name = j.getJSONArray("zone_id").getString(1);
                            z.save();
                        }
                        m.zone = z;
                    }
                    if (j.get("category_id") instanceof JSONArray) {
                        Category c = new Select().from(Category.class).where("uuid = ?", j.getJSONArray("category_id").getString(0)).executeSingle();
                        if (c == null) {
                            c = new Category();
                            c.uuid = j.getJSONArray("category_id").getString(0);
                            c.name = j.getJSONArray("category_id").getString(1);
                            c.save();
                        }
                        m.category = c;
                    }
                    if (j.get("speciality_id") instanceof JSONArray)
                    {
                        Speciality s = new Select().from(Speciality.class).where("uuid = ?", j.getJSONArray("speciality_id").getString(0)).executeSingle();
                        if (s == null) {
                            s = new Speciality();
                            s.uuid = j.getJSONArray("speciality_id").getString(0);
                            s.name = j.getJSONArray("speciality_id").getString(1);
                            s.save();
                        }
                        m.speciality = s;
                    }
                    m.save();
                }

                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //mAuthTask = null;
            mSwipeRefresh.setRefreshing(false);

            if (success) {
                refresh();
            }
        }

    }
}
