package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.PermissionUtils;
import fr.areastudio.jwterritorio.model.Address;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);
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
            if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ImageView cross = findViewById(R.id.cross);
        Button send_gps = findViewById(R.id.send_gps);

        enableMyLocation();

        if (getCallingActivity() != null) {
            cross.setVisibility(View.VISIBLE);
            send_gps.setVisibility(View.VISIBLE);
            send_gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent _result = new Intent();
                    _result.putExtra("lat", mMap.getCameraPosition().target.latitude);
                    _result.putExtra("lng", mMap.getCameraPosition().target.longitude);
                    setResult(Activity.RESULT_OK, _result);
                    finish();
                }
            });

        }
            if (!((MyApplication) getApplication()).getMe().type.equals("PUBLISHER") && getIntent().getLongArrayExtra("ids") == null && getCallingActivity() == null) {
                List<Address> addresses = new Select().from(Address.class).execute();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Address ad : addresses
                ) {
                    if (ad.lat != null && ad.lng != null && ad.territory != null) {
                        try {
                            LatLng gps = new LatLng(Double.parseDouble(ad.lat), Double.parseDouble(ad.lng));
                            builder.include(gps);
                            IconGenerator ic = new IconGenerator(this);
                            TextView t = new TextView(this);
                            if (ad.territory != null && ad.territory.assignedPub != null) {
                                ic.setColor(getResources().getColor(R.color.red));
                            } else {
                                ic.setColor(getResources().getColor(R.color.secondaryDarkColor));
                            }
                            t.setText(ad.territory.name.substring(ad.territory.name.indexOf("-", ad.territory.name.length() - 6) + 1));
                            ic.setContentView(t);
                            Marker m = mMap.addMarker(new MarkerOptions().position(gps).icon(BitmapDescriptorFactory.fromBitmap(ic.makeIcon())).visible(true));
                            m.setTag(ad);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                    }
                }
                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                mMap.animateCamera(cu);
            } else {
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        Address ad = (Address) marker.getTag();
                        View v = getLayoutInflater().inflate(R.layout.fragment_address, null);
                        TextView name = v.findViewById(R.id.name);
                        TextView terr = v.findViewById(R.id.terr);
                        terr.setText(ad.territory == null ? "" : ad.territory.name);
                        name.setText(ad.name);


                        ((TextView) v.findViewById(R.id.address)).setText(ad.address);
                        v.findViewById(R.id.lastContactImg).setVisibility(View.GONE);
                        ImageView gender = v.findViewById(R.id.icon_gender);
                        if ("f".equals(ad.gender)) {
                            gender.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.icons8_user_female_skin_type_4_50));
                        } else {
                            gender.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.icons8_user_male_skin_type_4_50));
                        }

                        if (ad.lastVisit != null) {
                            ((TextView) v.findViewById(R.id.lastContact)).setText(dateFormatter.format(ad.lastVisit));
                            v.findViewById(R.id.lastContactImg).setVisibility(View.VISIBLE);
                        }
                        if (ad.territory.assignedPub != null) {
                            ((TextView) v.findViewById(R.id.assigned)).setText(ad.territory.assignedPub.name);
                        }

                        return v;
                    }
                });
                long[] ids = getIntent().getLongArrayExtra("ids");
                if (ids != null) {
                    Long[] idsl = new Long[ids.length];

                    for (int i = 0; i < ids.length; i++) {
                        idsl[i] = ids[i];
                    }

                    Character[] placeholdersArray = new Character[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        placeholdersArray[i] = '?';
                    }

                    String placeholders = TextUtils.join(",", placeholdersArray);


                    List<Address> addresses = new Select().from(Address.class).where("Id IN (" + placeholders + ")", (Object[]) idsl)
                            .execute();

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    boolean haspoints = false;
                    for (Address ad : addresses
                    ) {
                        if (ad.lat != null && ad.lng != null) {
                            try {
                                LatLng gps = new LatLng(Double.parseDouble(ad.lat), Double.parseDouble(ad.lng));
                                builder.include(gps);
                                Marker m = mMap.addMarker(new MarkerOptions().position(gps).title(ad.name));
                                m.setTag(ad);
                                haspoints = true;

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                        }
                    }


                    if (!haspoints) {
                        builder.include(new LatLng(-17, -66));
                    }
                    LatLngBounds bounds = builder.build();

                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = getResources().getDisplayMetrics().heightPixels;
                    int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                    mMap.animateCamera(cu);
                }
            }


    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
