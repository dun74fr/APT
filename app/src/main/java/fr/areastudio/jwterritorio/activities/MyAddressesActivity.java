package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.CommonTools;
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
//                case R.id.navigation_web:
//                    intent = new Intent(MyAddressesActivity.this,WebActivity.class);
//                    MyAddressesActivity.this.startActivity(intent);
//                    return true;
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
        List<Territory> territories;
        if (settings.getString("view_all","0").equals("0")) {
            territories = new Select().from(Territory.class).where("publisher = ?", me.getId()).orderBy("name").execute();
        }
        else {
            territories = new Select().from(Territory.class).orderBy("name").execute();
        }
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
                mapgroups.add(new TypeGroup(t,t.name, t.getAddresses()));
            }

            List<Address> addresses = new Select().from(Address.class).where("publisher = ?", me.getId()).or("my_local_dir = ?", true).execute();
            List<Address> newaddresses = new ArrayList<>(addresses);
            if (addresses != null) {
                for (Address ad : addresses) {
                    if (!ad.myLocalDir && !"DRAFT".equals(ad.status)){
                        newaddresses.remove(ad);
                    }
                }
            }
            addresses = new ArrayList<>(newaddresses);
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
                        currentGroup = new TypeGroup(null,getTypeTitle(type),new ArrayList<Address>());
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
                new GetTerritoryTask(this,settings.getString("serverUrl","")) {
                    @Override
                    protected void onPostExecute(Boolean success) {
                        super.onPostExecute(success);
                        //mAuthTask = null;
                        mSwipeRefresh.setRefreshing(false);

                        if (success) {
                            //if (new Select().from(Territory.class).where("publisher = ?", me.getId()).execute().size() > 0) {
                                refresh();
                            //}
                        }
                        else {
                            new AlertDialog.Builder(MyAddressesActivity.this).setMessage(R.string.update_error).setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    refresh(true);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                            //Toast.makeText(MyAddressesActivity.this,R.string.update_error,Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();
            }
            else {
                List<TypeGroup> mapgroups = new ArrayList<>();
                List<Address> addresses = new Select().from(Address.class).where("publisher = ?", me.getId()).or("my_local_dir = ?", true)
                        .execute();
                List<Address> newaddresses = new ArrayList<>(addresses);
                if (addresses != null) {
                    for (Address ad : addresses) {
                        if (!ad.myLocalDir && !"DRAFT".equals(ad.status)){
                            newaddresses.remove(ad);
                        }
                    }
                }
                addresses = new ArrayList<>(newaddresses);
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
                            currentGroup = new TypeGroup(null,getTypeTitle(type),new ArrayList<Address>());
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
        if ("".equals(type)) {
            return getString(R.string.new_visit);
        }
        return getResources().getStringArray(R.array.contact_type)[CommonTools.getPositioninArray(this, R.array.contact_type_values, type)];
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
            if (mMyAddressAdapter == null){
                return true;
            }
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

    @Override
    public void onRefresh() {
        refresh(true);
    }
}
