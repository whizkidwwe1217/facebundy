package com.jeonsoft.facebundypro.location;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.settings.CacheManager;

/**
 * Created by WendellWayne on 3/11/2015.
 */
public class GPSTracker extends Service implements LocationListener {
    public interface GpsTrackerListener {
        void onLocationChange(Location location);
        void onStatusChange(String provider, int status, Bundle extras);
        void onProviderEnabled(String provider);
        void onLocateStarted();
        void onFinish(String message);
    }

    private GpsTrackerListener gpsTrackerListener;

    public void setGpsTrackerListener(GpsTrackerListener gpsTrackerListener) {
        this.gpsTrackerListener = gpsTrackerListener;
    }

    private boolean useNetWorkTime = false;

    public void setUseNetWorkTime(boolean useNetWorkTime) {
        this.useNetWorkTime = useNetWorkTime;
    }

    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    // Minimum time between updates in milliseconds -> 10 meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // Minimum time between updates in milliseconds -> 1 minute
    /*private long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    public void setMinimumTimeBetweenUpdates(long minTime) {
        MIN_TIME_BW_UPDATES = minTime;
    }*/

    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        if (gpsTrackerListener != null)
            gpsTrackerListener.onLocateStarted();
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Logger.logE("No GPS and network providers enabled.");
                if (gpsTrackerListener != null)
                    gpsTrackerListener.onFinish("No GPS and network providers enabled.");
            } else {
                canGetLocation = true;
                long minTime = 30000 * 60 * 1;
                try {
                    String interval = CacheManager.getInstance(getBaseContext()).getStringPreference("pref_key_gps_time_autosync_interval");

                    if (interval.equals("1")) {
                        minTime = (1000 * 60) * 60;
                    } else if (interval.equals("2")) {
                        minTime = ((1000 * 60) * 60) * 2;
                    } else if (interval.equals("3")) {
                        minTime = ((1000 * 60) * 60) * 4;
                    }
                } catch (Exception ex) {}

                if (isNetworkEnabled && useNetWorkTime) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            minTime, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Logger.logE("Acquire location from network provider.");

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                minTime,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Logger.logE("Acquire location from GPS provider.");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                if (gpsTrackerListener != null)
                    gpsTrackerListener.onFinish("Locate finish.");
            }
        } catch (Exception e) {
            Logger.logE(e.getMessage());
            if (gpsTrackerListener != null)
                gpsTrackerListener.onFinish("Error finding location: " + e.getMessage());
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (gpsTrackerListener != null)
            gpsTrackerListener.onLocationChange(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (gpsTrackerListener != null)
            gpsTrackerListener.onStatusChange(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (gpsTrackerListener != null)
            gpsTrackerListener.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public double getLatitude() {
        if (location != null)
            latitude = location.getLatitude();
        return latitude;
    }

    public double getLongitude() {
        if (location != null)
            longitude = location.getLongitude();
        return longitude;
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void stopGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }
}