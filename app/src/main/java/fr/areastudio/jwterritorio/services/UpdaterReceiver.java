package fr.areastudio.jwterritorio.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.util.Date;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.activities.GetTerritoryTask;
import fr.areastudio.jwterritorio.activities.MainActivity;
import fr.areastudio.jwterritorio.activities.MyAddressesActivity;
import fr.areastudio.jwterritorio.activities.UpdateDBTask;
import fr.areastudio.jwterritorio.common.UUIDGenerator;
import fr.areastudio.jwterritorio.model.DbUpdate;
import fr.areastudio.jwterritorio.model.Territory;

public class UpdaterReceiver extends BroadcastReceiver {
    public static final int WIFI = 1;
    public static final int ANY = 0;


    // Whether the display should be refreshed.
    private Context context;
    private LocationManager locationManager;
    private static String TAG = "LocationService";
    private Location mCurrentLocation;
    private SharedPreferences settings;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        update();

    }

    public void update() {

        getLastKnownLocation();
        if (mCurrentLocation == null || ((MyApplication)context.getApplicationContext()).getMe() == null){
            return;
        }
        Log.d(TAG,mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
        DbUpdate update = new DbUpdate();
        update.uuid = mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
        update.publisherUuid = ((MyApplication)context.getApplicationContext()).getMe().name;
        update.model = "CHECKPOINT";
        update.date = new Date();
        update.save();

        if (!isOnline()) {
            return;
        }

        settings = context.getSharedPreferences(
                MainActivity.PREFS, 0);

//        new UpdateDBTask(context,context.getResources().getString(R.string.territory_url), settings.getString("congregation_uuid", "")){
//            @Override
//            protected void onPostExecute(Boolean success) {
//                Log.d(TAG, "updatedinDB : " + success);
//                if (!success){
//                    Toast.makeText(UpdaterReceiver.this.context,R.string.update_error,Toast.LENGTH_LONG).show();
//                }
//            }
//        }.execute();
        new GetTerritoryTask(context,context.getResources().getString(R.string.territory_url), settings.getString("congregation_uuid", "")) {
            @Override
            protected void onPostExecute(Boolean success) {
                if (!success){
                    Toast.makeText(UpdaterReceiver.this.context,R.string.update_error,Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

    }

    public Location getLastKnownLocation() {
        Location lastKnownGPSLocation;
        Location lastKnownNetworkLocation;
        String gpsLocationProvider = LocationManager.GPS_PROVIDER;
        String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            lastKnownNetworkLocation = locationManager.getLastKnownLocation(networkLocationProvider);
            lastKnownGPSLocation = locationManager.getLastKnownLocation(gpsLocationProvider);

            if (lastKnownGPSLocation != null) {
                Log.i(TAG, "lastKnownGPSLocation is used.");
                this.mCurrentLocation = lastKnownGPSLocation;
            } else if (lastKnownNetworkLocation != null) {
                Log.i(TAG, "lastKnownNetworkLocation is used.");
                this.mCurrentLocation = lastKnownNetworkLocation;
            } else {
                Log.e(TAG, "lastLocation is not known.");
                return null;
            }

            return mCurrentLocation;

        } catch (SecurityException sex) {
            Log.e(TAG, "Location permission is not granted!");
        }

        return null;
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) this.context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public boolean isWifi() {
        ConnectivityManager connMgr = (ConnectivityManager) this.context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

}
