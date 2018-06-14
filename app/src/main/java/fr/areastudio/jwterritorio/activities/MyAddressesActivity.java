package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;

public class MyAddressesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyGroupAddressAdapter.MyAddressListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;
    private SharedPreferences settings;
    private RecyclerView mRecyclerView;
    //private MyAddressAdapter mMyAddressAdapter;
    MyGroupAddressAdapter mMyAddressAdapter;
    private View selectedBar;
    private TextView selectedText;
    private DrawerManager drawerManager;
    private ImageView mAssignBtn;
    private List<Address> selectedList;
    private SwipeRefreshLayout mSwipeRefresh;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_main:
                    intent = new Intent(MyAddressesActivity.this,MainActivity.class);
                    MyAddressesActivity.this.startActivity(intent);
                    return true;
                case R.id.navigation_assign:
                    intent = new Intent(MyAddressesActivity.this,AssignActivity.class);
                    MyAddressesActivity.this.startActivity(intent);
                    return true;
                case R.id.navigation_web:
                    intent = new Intent(MyAddressesActivity.this,WebActivity.class);
                    MyAddressesActivity.this.startActivity(intent);
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_addresses);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (((MyApplication)getApplication()).getMe().type.equals("PUBLISHER")){
            navigation.getMenu().removeItem(R.id.navigation_assign);
        }
        navigation.getMenu().findItem(R.id.navigation_address_list).setChecked(true);
        drawerManager = new DrawerManager(this);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mSwipeRefresh = findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);

//        mMyAddressAdapter = new MyAddressAdapter(this, new ArrayList<Address>());
//        mMyAddressAdapter.setListener(new MyAddressAdapter.MyAddressListener() {
//            @Override
//            public void onClick(Address address) {
//                Intent intent = new Intent(MyAddressesActivity.this, ViewAddressActivity.class);
//                intent.putExtra("address_id",address.getId());
//                MyAddressesActivity.this.startActivity(intent);
//            }
//        });
//        mRecyclerView.setAdapter(mMyAddressAdapter);

        FloatingActionButton fab = findViewById(R.id.add_address);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newAddress = new Intent(MyAddressesActivity.this,NewAddressActivity.class);
                MyAddressesActivity.this.startActivity(newAddress);

            }
        });
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
    private void refresh(){
        refresh(false);
    }

    private void refresh(boolean force) {
        final Publisher me = ((MyApplication)getApplication()).getMe();
        List<Territory> territories = new Select().from(Territory.class).where("publisher = ?",me.getId()).execute();

        if (territories.size() > 0 && !force) {

//            Long[] idsl = new Long[territories.size()];
//
//            for (int i = 0; i < territories.size(); i++) {
//                idsl[i] = territories.get(i).getId();
//            }
//
//            Character[] placeholdersArray = new Character[idsl.length];
//            for (int i = 0; i < idsl.length; i++) {
//                placeholdersArray[i] = '?';
//            }
//
//            String placeholders = TextUtils.join(",", placeholdersArray);
//
//            List<Address> addresses = new Select().from(Address.class).where("territory IN (" + placeholders + ")", (Object[])idsl).execute();
//            Collections.sort(addresses,new Comparator<Address>() {
//                @Override
//                public int compare(Address o1, Address o2) {
//                    return o1.territory.name.compareTo(o2.territory.name);
//                }
//            });
            List<TypeGroup> mapgroups = new ArrayList<>();
            for (Territory t : territories) {
                mapgroups.add(new TypeGroup(t.name, t.getAddresses()));
            }

            List<Address> addresses = new Select().from(Address.class).where("publisher = ?", me.getId()).or("my_local_dir = ?", true).execute();
            if (addresses.size() > 0) {
                Collections.sort(addresses, new Comparator<Address>() {
                    @Override
                    public int compare(Address o1, Address o2) {
                        return o1.type.compareTo(o2.type);
                    }
                });
                String type = null;
                TypeGroup currentGroup = null;
                for (Address a : addresses) {
                    if (!a.type.equals(type)){
                        type = a.type;
                        currentGroup = new TypeGroup(getTypeTitle(type),new ArrayList<Address>());
                        mapgroups.add(currentGroup);
                    }
                    currentGroup.getItems().add(a);
                }
            }
            mMyAddressAdapter = new MyGroupAddressAdapter(this,mapgroups);
            mMyAddressAdapter.setListener(this);
            mRecyclerView.setAdapter(mMyAddressAdapter);


            //mMyAddressAdapter.setAddresses(addresses);
        }
        else {
            territories = new Select().from(Territory.class).execute();
            if (territories.size() == 0 || force) {
                mSwipeRefresh.setRefreshing(true);
                new GetTerritoryTask(this,getResources().getString(R.string.territory_url), settings.getString("congregation_uuid", "")) {
                    @Override
                    protected void onPostExecute(Boolean success) {
                        //mAuthTask = null;
                        mSwipeRefresh.setRefreshing(false);

                        if (success) {
                            if (new Select().from(Territory.class).where("publisher = ?", me.getId()).execute().size() > 0) {
                                refresh();
                            }
                        }
                        else {
                            Toast.makeText(MyAddressesActivity.this,R.string.update_error,Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            }
            else {
                List<TypeGroup> mapgroups = new ArrayList<>();
                List<Address> addresses = new Select().from(Address.class).where("publisher = ?", me.getId()).or("my_local_dir = ?", true)
                        .execute();

                if (addresses.size() > 0) {
                    Collections.sort(addresses, new Comparator<Address>() {
                        @Override
                        public int compare(Address o1, Address o2) {
                            return o1.type.compareTo(o2.type);
                        }
                    });
                    String type = null;
                    TypeGroup currentGroup = null;
                    for (Address a : addresses) {
                        if (!a.type.equals(type)){
                            type = a.type;
                            currentGroup = new TypeGroup(getTypeTitle(type),new ArrayList<Address>());
                            mapgroups.add(currentGroup);
                        }
                        currentGroup.getItems().add(a);
                    }
                }
                mMyAddressAdapter = new MyGroupAddressAdapter(this,mapgroups);
                mMyAddressAdapter.setListener(this);
                mRecyclerView.setAdapter(mMyAddressAdapter);
                //mMyAddressAdapter.setAddresses(addresses);
            }
        }

    }

    private String getTypeTitle(String type){
        if ("false".equals(type)) {
            return getString(R.string.new_visit);
        }
        String returnString = type.replace("PHONE",getString(R.string.phone));
        returnString = returnString.replace("VISIT",getString(R.string.visit));
        returnString = returnString.replace("NOT_AT_HOME",getString(R.string.not_at_home));
        returnString = returnString.replace("BIBLE_COURSE",getString(R.string.bible_study));
        returnString = returnString.replace("|"," / ");
        return returnString;
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
        getMenuInflater().inflate(R.menu.my_addresses, menu);
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
            long[] ids = new long[mMyAddressAdapter.getAddresses().size()];
            for (int i = 0; i < mMyAddressAdapter.getAddresses().size(); i++){
                ids[i] = mMyAddressAdapter.getAddresses().get(i).getId();
            }

            Intent map = new Intent(this,MapsActivity.class);
            map.putExtra("ids", ids);
            this.startActivity(map);
            return true;
        }
        if (id == R.id.action_qrcode) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                String[] perms = {Manifest.permission.CAMERA};
                ActivityCompat.requestPermissions(this,perms, MY_PERMISSIONS_REQUEST_CAMERA);
            }
            else {
                Intent scan = new Intent(this, SimpleScannerActivity.class);
                this.startActivity(scan);

            }
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
    public void onAddressClicked(Address address) {
        Intent intent = new Intent(MyAddressesActivity.this, ViewAddressActivity.class);
                intent.putExtra("address_id",address.getId());
                MyAddressesActivity.this.startActivity(intent);
    }
}
