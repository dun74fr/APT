package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.PermissionUtils;
import fr.areastudio.jwterritorio.model.Address;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String ids;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ids = getIntent().getStringExtra("ids");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        enableMyLocation();

        if (!((MyApplication)getApplication()).getMe().type.equals("PUBLISHER") && getIntent().getLongArrayExtra("ids") == null){
            List<Address> addresses = new Select().from(Address.class).execute();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Address ad : addresses
                    ) {
                if (ad.lat != null && ad.lng != null) {
                    try {
                        LatLng gps = new LatLng(Double.parseDouble(ad.lat), Double.parseDouble(ad.lng));
                        builder.include(gps);
                        IconGenerator ic = new IconGenerator(this);
                        TextView t = new TextView(this);
                        if (ad.territory.assignedPub != null){
                            ic.setColor(getResources().getColor(R.color.red));
                        }
                        else {
                            ic.setColor(getResources().getColor(R.color.secondaryDarkColor));
                        }
                        t.setText(ad.territory.name.substring(ad.territory.name.indexOf("-",ad.territory.name.length()-6)+1));
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
        }
        else {
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


                    ((TextView)v.findViewById(R.id.address)).setText(ad.address);
                    v.findViewById(R.id.lastContactImg).setVisibility(View.GONE);
                    ImageView gender = v.findViewById(R.id.icon_gender);
                    if ("f".equals(ad.gender)) {
                        gender.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.icons8_user_female_skin_type_4_50));
                    }
                    else {
                        gender.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.icons8_user_male_skin_type_4_50));
                    }

                    if (ad.getLastVisit() != null) {
                        ((TextView)v.findViewById(R.id.lastContact)).setText(dateFormatter.format(ad.getLastVisit().date));
                        v.findViewById(R.id.lastContactImg).setVisibility(View.VISIBLE);
                    }
                    if (ad.territory.assignedPub != null) {
                        ((TextView)v.findViewById(R.id.assigned)).setText(ad.territory.assignedPub.name);
                    }

                    return v;
                }
            });
            long[] ids = getIntent().getLongArrayExtra("ids");
            Long[] idsl = new Long[ids.length];

            for (int i = 0; i < ids.length; i++) {
                idsl[i] = ids[i];
            }

            Character[] placeholdersArray = new Character[ids.length];
            for (int i = 0; i < ids.length; i++) {
                placeholdersArray[i] = '?';
            }

            String placeholders = TextUtils.join(",", placeholdersArray);


            List<Address> addresses = new Select().from(Address.class).where("Id IN (" + placeholders + ")", idsl)
                    .execute();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Address ad : addresses
                    ) {
                if (ad.lat != null && ad.lng != null) {
                    try {
                        LatLng gps = new LatLng(Double.parseDouble(ad.lat), Double.parseDouble(ad.lng));
                        builder.include(gps);
                        Marker m = mMap.addMarker(new MarkerOptions().position(gps).title(ad.name));
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
        }
//        try {
//            StringBuilder buf = new StringBuilder();
//            InputStream json = getAssets().open("db/oruro.geojson");
//            BufferedReader in =
//                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
//            String str;
//
//            while ((str = in.readLine()) != null) {
//                buf.append(str);
//            }
//
//            in.close();
//            JSONObject routes = new JSONObject(buf.toString());
//            GeoJsonLayer layer = new GeoJsonLayer(mMap, routes);
//            layer.addLayerToMap();
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
