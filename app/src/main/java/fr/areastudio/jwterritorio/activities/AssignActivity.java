package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;
//import fr.areastudio.jwterritorio.services.UpdaterReceiver;

public class AssignActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AssignAddressAdapter.AssignAddressListener {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;
    private SharedPreferences settings;
    private RecyclerView mRecyclerView;
    private AssignAddressAdapter mAssignAddressAdapter;
    private View selectedBar;
    private TextView selectedText;
    private DrawerManager drawerManager;
    private ImageView mAssignBtn;
    private List<Territory> selectedList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefresh;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_main:
                    intent = new Intent(AssignActivity.this, MainActivity.class);
                    AssignActivity.this.startActivity(intent);
                    return true;
                case R.id.navigation_address_list:
                    intent = new Intent(AssignActivity.this, MyAddressesActivity.class);
                    AssignActivity.this.startActivity(intent);
                    return true;
//                case R.id.navigation_web:
//                    intent = new Intent(AssignActivity.this, WebActivity.class);
//                    AssignActivity.this.startActivity(intent);
//                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_address);
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
        navigation.getMenu().findItem(R.id.navigation_assign).setChecked(true);

        drawerManager = new DrawerManager(this);
        mSwipeRefresh = findViewById(R.id.activity_main_swipe_refresh_layout);
        selectedBar = findViewById(R.id.selectedBar);
        selectedBar.setVisibility(View.GONE);
        selectedText = findViewById(R.id.selectedText);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);



        mAssignAddressAdapter = new AssignAddressAdapter(this, new ArrayList<ExpandableGroup>());
        mAssignAddressAdapter.setListener(this);
        mRecyclerView.setAdapter(mAssignAddressAdapter);
        mAssignBtn = findViewById(R.id.assignBtn);
        mAssignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(AssignActivity.this);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setContentView(R.layout.assign_dialog);
                String text = "";
                for (int i = 0; i < selectedList.size(); i++) {
                    if (!text.equals("")) {
                        text += ",";
                    }
                    text += selectedList.get(i).uuid;
                }
                text = "jwterr://" + text;
                final ImageView qr = dialog.findViewById(R.id.qr);
                TextView assignByWeb = dialog.findViewById(R.id.assign_web);
                final View assignByWebLayout = dialog.findViewById(R.id.assign_web_block);
                ImageView assignByWebBtn = dialog.findViewById(R.id.assignBtn);
                final Spinner publishers = dialog.findViewById(R.id.publishers);
                publishers.setAdapter(new ArrayAdapter<Publisher>(AssignActivity.this, android.R.layout.simple_spinner_dropdown_item,new Select().from(Publisher.class).where("email <> ?", "").<Publisher>execute()));
                assignByWeb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        qr.setVisibility(View.GONE);
                        assignByWebLayout.setVisibility(View.VISIBLE);
                        dialog.setOnDismissListener(null);
                    }
                });
                assignByWebBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(Territory t : selectedList){
                            t.assignedPub = (Publisher) publishers.getSelectedItem();
                            t.save();
                            DbUpdate dbUpdate = new DbUpdate();
                            dbUpdate.date = new Date();
                            dbUpdate.model = "TERRITORY";
                            dbUpdate.updateType = "UPDATE";
                            dbUpdate.publisherUuid = ((MyApplication) getApplication()).getMe().uuid;
                            dbUpdate.uuid = t.uuid;
                            dbUpdate.save();
                        }
                        dialog.dismiss();
                        refresh(true);
                    }
                });
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qr.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                        if (ContextCompat.checkSelfPermission(AssignActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            String[] perms = {Manifest.permission.CAMERA};
                            ActivityCompat.requestPermissions(AssignActivity.this,perms, MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                        else {
                            Intent scan = new Intent(AssignActivity.this, SimpleScannerActivity.class);
                            AssignActivity.this.startActivity(scan);

                        }
                    }
                });
            }
        });

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });
        refresh();
    }

    private void refresh(){
        refresh(false);
    }
    private void refresh(boolean force){
        onSelectionChanged(new ArrayList<Territory>());
        List<Territory> territories = new Select().from(Territory.class).orderBy("name").execute();
        if (territories.size() > 0 && !force ) {
            List<MapGroup> mapgroups = new ArrayList<>();
            for (Territory t : territories) {
                mapgroups.add(new MapGroup(t, t.getAddresses()));
            }
            mAssignAddressAdapter = new AssignAddressAdapter(this,mapgroups);
            mRecyclerView.setAdapter(mAssignAddressAdapter);
            mAssignAddressAdapter.setListener(this);
        }
        else {
            mSwipeRefresh.setRefreshing(true);
            new GetTerritoryTask(this,settings.getString("serverUrl","")) {
                @Override
                protected void onPostExecute(Boolean success) {
                    {
                        super.onPostExecute(success);
                        //mAuthTask = null;
                        mSwipeRefresh.setRefreshing(false);

                        if (success) {
                            if (new Select().from(Territory.class).execute().size() > 0) {
                                refresh();
                            }
                        }
                        else {
                            Toast.makeText(AssignActivity.this,R.string.update_error,Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }.execute();
        }

    }
    @Override
    public void onAddressClicked(Address address) {
        Intent intent = new Intent(AssignActivity.this, ViewAddressActivity.class);
        intent.putExtra("address_id",address.getId());
        AssignActivity.this.startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        onSelectionChanged(new ArrayList<Territory>());
        refresh();
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
        getMenuInflater().inflate(R.menu.assign, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent map = new Intent(this, MapsActivity.class);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_map) {
            ArrayList<Long> idsA = new ArrayList<>();
            if (selectedList.size() > 0) {
                for (Territory t : selectedList) {
                    for (Address a : t.getAddresses()) {
                        idsA.add(a.getId());
                    }
                }

                long[] ids = new long[idsA.size()];
                for (int i = 0; i < idsA.size(); i++) {
                    ids[i] = idsA.get(i);
                }
                map.putExtra("ids", ids);
            }
            this.startActivity(map);
            return true;
        }
        if (id == R.id.action_addAddress) {
            Intent newAddress = new Intent(this, NewAddressActivity.class);
            this.startActivity(newAddress);
            return true;
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
    public void onSelectionChanged(List<Territory> selectedList) {
        this.selectedList = selectedList;
        selectedBar.setVisibility(View.GONE);
        if (selectedList.size() > 0) {
            selectedBar.setVisibility(View.VISIBLE);
            selectedText.setText(getString(R.string.selected_to_assign, selectedList.size()));
        }
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


}
