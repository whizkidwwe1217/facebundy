package com.jeonsoft.facebundypro.uploadservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.jeonsoft.facebundypro.TimeLogUploadHelper;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ConnectivityHelper;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;

/**
 * Created by WendellWayne on 4/27/2015.
 */
public class AutoUploadService extends BroadcastReceiver {
    public static String AUTO_UPLOAD_TIMELOG = "auto_upload_timelog";
    public static String TAG = "com.jeonsoft.facebundypro.uploadservice.autoupload";
    /*private Context context;

    public AutoUploadService(Context context) {
        this.context = context;
        IntentFilter filter = new IntentFilter(TAG);
        try {
            if (uploadReceiver != null)
                context.unregisterReceiver(uploadReceiver);
        } catch(Exception e){}
        context.registerReceiver(uploadReceiver, filter);
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();

        /*Bundle extras = intent.getExtras();
        StringBuilder msg = new StringBuilder();

        Format formatter = new SimpleDateFormat("hh:mm:ss a");
        msg.append(formatter.format(new Date()));
        String interval = CacheManager.getInstance(context).getStringPreference(context.getString(R.string.pref_key_auto_upload_sched));
        Toast.makeText(context, msg + " -> " + interval, Toast.LENGTH_LONG).show();*/
        uploadTimeLogs(context);
        wl.release();
    }

    /*private final AbstractUploadServiceReceiver uploadReceiver = new AbstractUploadServiceReceiver() {
        @Override
        public void onProgress(String uploadId, int progress) {
            Logger.logI("The progress of the upload with ID " + uploadId + " is: " + progress);
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            String message = "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage();
            Logger.logE(message);
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage, int totalItemsToUpload, int totalItemsUploaded, String tag, int total, int current) {
            String message = "Upload with ID " + uploadId + " is completed: " + serverResponseCode + ", "
                    + serverResponseMessage;
            Logger.logI(message);

            final String filename = tag;
            final String id = uploadId;
            TimelogDataSource ds = TimelogDataSource.getInstance(context);
            try {
                ds.open();
                ds.beginTransaction();
                ds.deleteById(Integer.parseInt(id));

                File file = new File(filename);
                if (file.exists()) {
                    file.delete();
                    Logger.logI(filename + " deleted.");
                }
                ds.setTransactionSuccessful();
            } catch (Exception ex) {
                Logger.logE(ex.getMessage());
            } finally {
                ds.endTransaction();
                ds.close();
            }
        }
    };*/

    private void uploadTimeLogs(final Context context) {
        if(ConnectivityHelper.isConnected(context)) {
            final TimeLogUploadHelper helper = TimeLogUploadHelper.getInstance(context);
            String[] hosts = CacheManager.getInstance(context).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
            new ReachableServerHost(context, hosts, new ReachableServerHostListener() {
                @Override
                public void onStatusChanged(ServerHostStatus status, String host) {
                    Logger.logD(status.toString() + ": " + host);
                }

                @Override
                public void onReachableHostAcquired(String reachableHost) {
                    helper.upload(reachableHost);
                }

                @Override
                public void onFailedHostAcquisition(String message) {
                    Logger.logE(message);
                }
            }).execute();
        } else {
            Logger.logE("Please connect to the internet to upload time logs.");
        }
    }

    public void setAlarm(Context context, int hours) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AutoUploadService.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        int interval;
        if (hours == 0)
            interval = 1000 * (30 * 60);
        else
            interval = 1000 * (hours * 60 * 60);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AutoUploadService.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
