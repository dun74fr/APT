package fr.areastudio.jwterritorio.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.List;

import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.common.PermissionUtils;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Medic;

public class EmergencyMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

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
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Medic medic = (Medic) marker.getTag();
                View v = getLayoutInflater().inflate(R.layout.fragment_medic, null);
                if (medic.category != null) {
                    ((TextView) v.findViewById(R.id.category)).setText(medic.category.name);
                }
                ((TextView)v.findViewById(R.id.name)).setText(medic.name);
                ((TextView)v.findViewById(R.id.address)).setText(medic.address);

                return v;
            }
        });
        long[] ids = getIntent().getLongArrayExtra("ids");
        Long[] idsl = new Long[ids.length];

        for (int i = 0; i < ids.length ; i++){
            idsl[i] = ids[i];
        }

        Character[] placeholdersArray = new Character[ids.length];
        for (int i = 0; i < ids.length; i++) {
            placeholdersArray[i] = '?';
        }

        String placeholders = TextUtils.join(",", placeholdersArray);



        List<Medic> medics = new Select().from(Medic.class) .where("Id IN (" + placeholders +")", idsl)
                .execute();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Medic medic: medics
             ) {
            if (medic.lat != null && medic.lng !=null) {
                LatLng gps = new LatLng(Double.parseDouble(medic.lat), Double.parseDouble(medic.lng));
                builder.include(gps);
                Marker m;
                if (medic.category != null){
                    if (medic.category.uuid.equals("1")) {
                        m = mMap.addMarker(new MarkerOptions().position(gps).title(medic.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_hospital_3_48)));
                    }
                    else if (medic.category.uuid.equals("2")) {
                        m = mMap.addMarker(new MarkerOptions().position(gps).title(medic.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_tooth_48)));
                    }
                    else if (medic.category.uuid.equals("3")) {
                        m = mMap.addMarker(new MarkerOptions().position(gps).title(medic.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_microscope_48)));
                    }
                    else if (medic.category.uuid.equals("4")){
                        m = mMap.addMarker(new MarkerOptions().position(gps).title(medic.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_pill_48)));
                    }
                    else {
                        m = mMap.addMarker(new MarkerOptions().position(gps).title(medic.name));
                    }
                }
                else {
                    m = mMap.addMarker(new MarkerOptions().position(gps).title(medic.name));
                }

                m.setTag(medic);

            }
        }
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);

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
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
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
