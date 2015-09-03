package com.jeonsoft.facebundypro;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import com.gc.materialdesign.widgets.SnackBar;
import com.jeonsoft.facebundypro.licensing.AppEditions;
import com.jeonsoft.facebundypro.location.GPSTracker;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.uploadservice.AutoUploadService;
import com.jeonsoft.facebundypro.utils.DeviceUtils;
import com.jeonsoft.facebundypro.utils.DialogUtils;
import com.jeonsoft.facebundypro.utils.ObjectCallback;
import com.jeonsoft.facebundypro.utils.TimeUtils;
import com.jeonsoft.facebundypro.views.ChangeLogDialog;
import com.jeonsoft.facebundypro.views.ChangePinCodeDialogFragment;
import com.jeonsoft.facebundypro.views.DateTimeConfigActivity;
import com.jeonsoft.facebundypro.views.EmployeeListActivity;
import com.jeonsoft.facebundypro.views.ItineraryProjectListActivity;
import com.jeonsoft.facebundypro.views.LogsReportActivity;
import com.jeonsoft.facebundypro.views.SubjectManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by WendellWayne on 2/14/2015.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gpsTracker != null)
            gpsTracker.stopGPS();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey() != null) {
            if (preference.getKey().equals(getString(R.string.pref_key_clear_cache))) {
                CacheManager.getInstance(getActivity()).deletePreference(CacheManager.ACTIVATION_KEY);
                CacheManager.getInstance(getActivity()).deletePreference(CacheManager.ACTIVATED);
                CacheManager.getInstance(getActivity()).deletePreference(CacheManager.EDITION);
                CacheManager.getInstance(getActivity()).deletePreference(CacheManager.COMPANY_ID);
                CacheManager.getInstance(getActivity()).deletePreference(CacheManager.ACTIVATION_KEY);

                return true;
            } else if (preference.getKey().equals(getString(R.string.pref_key_enrolled_faces))) {
                showEmployeeFaceManager();
                return true;
            } else if (preference.getKey().equals(getString(R.string.pref_key_time_logs))) {
                showTimeLogs();
                return true;
            } else if (preference.getKey().equals(getString(R.string.pref_key_admin_pin))) {
                changePin();
            } else if (preference.getKey().equals(getString(R.string.pref_key_time_logs))) {
                showTimeLogs();
                return true;
            } else if (preference.getKey().equals(getString(R.string.pref_key_datetime))) {
                getTime();
            } else if (preference.getKey().equals(getString(R.string.pref_key_employee_list))) {
                showEmployeeList();
            } else if (preference.getKey().equals(getString(R.string.pref_key_auto_upload))) {
                boolean auto = CacheManager.getInstance(getActivity()).getBooleanPreference(getString(R.string.pref_key_auto_upload));
                setAutoUploadSchedule();
                if (auto) {
                    int interval = Integer.parseInt(CacheManager.getInstance(getActivity()).getStringPreference(getActivity().getString(R.string.pref_key_auto_upload_sched)));
                    autoUploadService.setAlarm(getActivity(), interval);
                }
                else
                    autoUploadService.cancelAlarm(getActivity());
            } else if(preference.getKey().equals(getString(R.string.pref_key_server_host_test))) {
                toast("Testing server hosts...", Toast.LENGTH_SHORT);
                new TestHostAsync().execute();
            } else if(preference.getKey().equals(getString(R.string.pref_key_datetime_manual))) {
                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        int type = Integer.parseInt(String.valueOf(newValue));
                        configureDateTime(type);
                        return true;
                    }
                });

            } else if (preference.getKey().equals("pref_key_enable_auto_sync_gps") || preference.getKey().equals("pref_key_sync_gps_time")) {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!isGPSEnabled) {
                    DeviceUtils.showLocationSettingsAlert(getActivity());
                    return false;
                } else {
                    if (preference.getKey().equals("pref_key_sync_gps_time")) {
                        setGpsTime();
                    }
                }
                return true;
            } else if (preference.getKey().equals("pref_key_change_log")) {
                showChangeLog();
                return true;
            } else if (preference.getKey().equals("pref_key_itineraries")) {
                showItineraries();
                return true;
            } else
                return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void showItineraries() {
        String accessCode = "";
        Intent intent = new Intent(SettingsFragment.this.getActivity(), ItineraryProjectListActivity.class);
        intent.putExtra("ACCESS_CODE", accessCode);
        intent.putExtra("ALL_EMPLOYEES", true);
        startActivity(intent);
    }

    private void showChangeLog() {
        final ChangeLogDialog changeLogDialog = new ChangeLogDialog(getActivity());
        changeLogDialog.show();
    }

    private GPSTracker gpsTracker;

    private void setGpsTime() {
        if (CacheManager.getInstance(getActivity()).getBooleanPreference("pref_key_auto_sync_gps_time")) {
            gpsTracker = new GPSTracker(getActivity());
            /*String interval = CacheManager.getInstance(getActivity()).getStringPreference("pref_key_gps_time_autosync_interval");
            long minTime = 30000 * 60 * 1;
            if (interval.equals("1")) {
                minTime = (1000 * 60) * 60;
            } else if (interval.equals("2")) {
                minTime = ((1000 * 60) * 60) * 2;
            } else if (interval.equals("3")) {
                minTime = ((1000 * 60) * 60) * 4;
            }*/
            //gpsTracker.setMinimumTimeBetweenUpdates(minTime);
            gpsTracker.setUseNetWorkTime(false);
            gpsTracker.setGpsTrackerListener(new GPSTracker.GpsTrackerListener() {
                @Override
                public void onLocationChange(Location location) {
                    if (location != null) {
                        long elapsedTime = SystemClock.elapsedRealtime();
                        long currentTime = location.getTime();
                        CacheManager.getInstance(SettingsFragment.this.getActivity()).setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
                        CacheManager.getInstance(SettingsFragment.this.getActivity()).setPreference(CacheManager.SERVER_TIME, currentTime);
                        CacheManager.getInstance(SettingsFragment.this.getActivity()).setPreference(CacheManager.TIME_LAST_SYNC_SOURCE, GlobalConstants.GPS_SYNC_SOURCE);
                        Toast.makeText(getActivity(), "GPS time acquired.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onStatusChange(String provider, int status, Bundle extras) {
                    gpsTracker.stopGPS();
                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onLocateStarted() {

                }

                @Override
                public void onFinish(String message) {

                }
            });
            Location loc = gpsTracker.getLocation();
            long elapsedTime = SystemClock.elapsedRealtime();
            long currentTime = loc.getTime();
            CacheManager.getInstance(SettingsFragment.this.getActivity()).setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
            CacheManager.getInstance(SettingsFragment.this.getActivity()).setPreference(CacheManager.SERVER_TIME, currentTime);
            Toast.makeText(SettingsFragment.this.getActivity(), "GPS time acquired.", Toast.LENGTH_SHORT).show();
            Toast.makeText(SettingsFragment.this.getActivity(), "Synching time via GPS location.", Toast.LENGTH_LONG).show();
        }
    }

    private void showEmployeeFaceManager() {
        if (GlobalConstants.getInstance().getAppEdition() == AppEditions.ExtractionAndMatching) {
            Intent intent = new Intent(getActivity(), SubjectManager.class);
            getActivity().startActivity(intent);
        } else {
            SnackBar snackbar = new SnackBar(getActivity(), "This feature is not available for this edition.", "", null);
            snackbar.show();
        }
    }

    private void showTimeLogs() {
        Intent intent = new Intent(getActivity(), LogsReportActivity.class);
        intent.putExtra("ALL", true);
        intent.putExtra("ACCESS_CODE", "");
        startActivity(intent);
    }

    private void changePin() {
        DialogFragment pin = ChangePinCodeDialogFragment.newInstance(null, new DialogFragmentResultListener() {
            @Override
            public void onResultWithValueReturned(boolean valid, Object value) {
                if (valid) {
                    CacheManager cm = CacheManager.getInstance(getActivity());
                    cm.setPreference(CacheManager.ADMIN_PASSWORD, String.valueOf(value));
                }
            }

            @Override
            public void onResultReturned(Object value) {

            }
        });
        pin.show(((ActionBarActivity) getActivity()).getSupportFragmentManager(), "CHANGEPIN");
    }

    class TestHostAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            testHosts();
            return null;
        }
    }

    private void testHosts() {
        if(ConnectivityHelper.isConnected(getActivity())) {
            String[] hosts = CacheManager.getInstance(getActivity()).getStringPreference(CacheManager.SERVER_HOSTS).split("\n");
            new ReachableServerHost(getActivity(), hosts, new ReachableServerHostListener() {
                @Override
                public void onStatusChanged(ServerHostStatus status, String host) {
                    toast("..." + host + " => " + status.toString(), Toast.LENGTH_SHORT);
                }

                @Override
                public void onReachableHostAcquired(String reachableHost) {
                    toast("Connected to: " + reachableHost, Toast.LENGTH_LONG);
                }

                @Override
                public void onFailedHostAcquisition(String message) {
                    toast("Error: " + message, Toast.LENGTH_LONG);
                }
            }).execute();
        } else {
            toast("You are not connected to the internet.", Toast.LENGTH_SHORT);
        }
    }

    private void toast(final String message, final int length) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), message, length).show();
                    Logger.logE(message);
                }
            });
        }
    }

    private void setAutoUploadSchedule() {
        autoUploadService = new AutoUploadService();
    }

    /* AUTO UPLOAD */
    private AutoUploadService autoUploadService;

    public void startRepeatingTimer(int hours) {
        Context context = getActivity();
        if (autoUploadService != null) {
            autoUploadService.setAlarm(context, hours);
        } else {
            Logger.logE("Alarm is not defined.");
        }
    }

    public void cancelRepeatingTimer() {
        Context context = getActivity();
        if (autoUploadService != null) {
            autoUploadService.cancelAlarm(context);
        } else {
            Logger.logE("Alarm is not defined.");
        }
    }

    private Calendar getTime() {
        CacheManager cm = CacheManager.getInstance(getActivity());
        Calendar calendar;
        if (!cm.containsPreference(CacheManager.SERVER_TIME) || !cm.containsPreference(CacheManager.ELAPSED_TIME)) {
            calendar = getTimeViaInternet();
        } else {
            long elapsed = cm.getLongPreference(CacheManager.ELAPSED_TIME);
            long realtime = SystemClock.elapsedRealtime();
            long difftime = realtime - elapsed;
            if (difftime < 0)
                calendar = getTimeViaInternet();
            else
                calendar = getTimeViewPreferences();
        }
        return calendar;
    }

    private Calendar getTimeViaInternet() {
        final Context context = getActivity();
        final Calendar calSavedServerTime = Calendar.getInstance();
        final Calendar calSavedElapsedTime = Calendar.getInstance();
        final Calendar calCurrentTime = Calendar.getInstance();

        new GetNetworkTimeAsync(new ObjectCallback<Void, Long>() {
            @Override
            public Long onObjectRequestProcess(Void... params) {
                return null;
            }

            @Override
            public void onObjectRequestComplete(Long result) {
                CacheManager cm = CacheManager.getInstance(context);
                if (result != 0L) {
                    long serverTime, elapsedTime;
                    serverTime = result;
                    elapsedTime = SystemClock.elapsedRealtime();

                    calSavedServerTime.setTimeInMillis(serverTime);
                    calSavedElapsedTime.setTimeInMillis(elapsedTime);

                    cm.setPreference(CacheManager.SERVER_TIME, serverTime);
                    cm.setPreference(CacheManager.ELAPSED_TIME, elapsedTime);
                    long mRealtime = SystemClock.elapsedRealtime();
                    long mDiffTime = mRealtime - elapsedTime;
                    long mCurrentTime = mDiffTime + serverTime;
                    calCurrentTime.setTimeInMillis(mCurrentTime);

                    Calendar elapsed = Calendar.getInstance();
                    elapsed.setTimeInMillis(SystemClock.elapsedRealtime());
                    SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
                    Logger.logE("Time requested: " + df.format(calCurrentTime.getTime()));
                    Toast.makeText(getActivity(), "Time requested: " + df.format(calCurrentTime.getTime()), Toast.LENGTH_SHORT).show();
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Logger.logE("Failed to request server time.");
                            Toast.makeText(getActivity(), "Failed to request server time.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onObjectRequestError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        Logger.logE(message);
                    }
                });
            }

            @Override
            public void onRequestCancelled() {

            }

            @Override
            public void onPreRequest() {
                CacheManager cm = CacheManager.getInstance(context);
                cm.deletePreference(CacheManager.SERVER_TIME);
                cm.deletePreference(CacheManager.ELAPSED_TIME);
                Toast.makeText(getActivity(), "Requesting time from server...", Toast.LENGTH_SHORT).show();
                Logger.logE("Requesting time from server...");
            }
        }).execute();
        return calCurrentTime;
    }

    private Calendar getTimeViewPreferences() {
        CacheManager cm = CacheManager.getInstance(getActivity());
        long mElapsed = cm.getLongPreference(CacheManager.ELAPSED_TIME);
        long mServerTime = cm.getLongPreference(CacheManager.SERVER_TIME);
        long mRealtime = SystemClock.elapsedRealtime();
        long mDiffTime = mRealtime - mElapsed;
        long mCurrentTime = mDiffTime + mServerTime;
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(mCurrentTime);
        return mCalendar;
    }

    class GetNetworkTimeAsync extends AsyncTask<Void, Void, Long> {
        private ObjectCallback<Void, Long> callback;

        public GetNetworkTimeAsync(ObjectCallback<Void, Long> callback) {
            this.callback = callback;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                CacheManager cm = CacheManager.getInstance(getActivity());
                int timeout = Integer.parseInt(cm.getStringPreference("pref_key_ntp_timeout"));
                String server = cm.getStringPreference("pref_key_ntp_server");
                long networkTime = TimeUtils.getCurrentNetworkTime(timeout, server);
                return networkTime;
            } catch (Exception ex) {
                callback.onObjectRequestError("Error fetching internet time. " + ex.getMessage());
                return 0L;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            callback.onObjectRequestComplete(aLong);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.onPreRequest();
        }
    }

    private void configureDateTime(int type) {
        switch(type) {
            case 0:
                getTime();
                break;
            case 1:
                getNetworkTime();
                break;
            case 2:
                getGPSTime();
                break;
            default:
                getTimeManual();
                break;
        }
    }

    private void showEmployeeList() {
        Intent intent = new Intent(getActivity(), EmployeeListActivity.class);
        startActivity(intent);
    }

    private void getNetworkTime() {
        getTime();
    }

    private void getGPSTime() {
        setGpsTime();
    }

    private void getTimeManual() {
        Intent intent = new Intent(getActivity(), DateTimeConfigActivity.class);
        startActivity(intent);
    }
}
