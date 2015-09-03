package com.jeonsoft.facebundypro.uploadservice;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.jeonsoft.facebundypro.data.TimelogDataSource;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.HttpRequestMethod;
import com.jeonsoft.facebundypro.net.HttpServiceRequest;
import com.jeonsoft.facebundypro.settings.GlobalConstants;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by WendellWayne on 3/10/2015.
 */
public class TimelogService extends IntentService {
    public static final String ACCESS_CODE = "access_code";
    public static final String TIME = "time";
    public static final String TYPE = "type";
    public static final String GPS_LATITUDE = "latitude";
    public static final String GPS_LONGITUDE = "longitude";
    public static final String EDITION = "edition";
    public static final String ID = "id";

    public static final String ERROR_MSG = "error_message";
    public static final String STATUS = "status";
    private static final int UPLOAD_NOTIFICATION_ID = 5678;
    private static final int UPLOAD_NOTIFICATION_ID_DONE = 5679;
    protected static final String PARAM_NOTIFICATION_CONFIG = "notificationConfig";
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_ERROR = 3;
    public static final String PROGRESS = "progress";
    private UploadNotificationConfig notificationConfig;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    private PowerManager.WakeLock wakeLock;
    private int lastPublishedProgress;
    public static final String TAG = "AndroidFaceBundyUploadService";
    public static final String NOTIFICATION = "com.jeonsoft.facebundypro.uploadservice";

    public TimelogService() {
        super("TimelogService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    private void deleteTimeLog(final int id) {
        if (id == -1)
            return;
        TimelogDataSource ds = TimelogDataSource.getInstance(GlobalConstants.getInstance().getContext());
        try {
            ds.open();
            ds.deleteById(id);
        } catch (Exception ex) {
            Logger.logE(ex.getMessage());
        } finally {
            ds.close();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String accessCode = intent.getStringExtra(ACCESS_CODE);
        String time = intent.getStringExtra(TIME);
        String type = intent.getStringExtra(TYPE);
        String url = intent.getStringExtra("URL");
        int edition = intent.getIntExtra(EDITION, 0);
        int id = intent.getIntExtra(ID, -1);
        double latitude = intent.getDoubleExtra(GPS_LATITUDE, 0);
        double longitude = intent.getDoubleExtra(GPS_LONGITUDE, 0);
        notificationConfig = intent.getParcelableExtra(PARAM_NOTIFICATION_CONFIG);

        lastPublishedProgress = 0;
        wakeLock.acquire();

        try {
            createNotification();

            ArrayList<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ACCESS_CODE, accessCode));
            params.add(new BasicNameValuePair(TIME, time));
            params.add(new BasicNameValuePair(TYPE, type.equals("IN") ? "1" : "2"));
            params.add(new BasicNameValuePair(GPS_LATITUDE, String.valueOf(latitude)));
            params.add(new BasicNameValuePair(GPS_LONGITUDE, String.valueOf(longitude)));
            params.add(new BasicNameValuePair(EDITION, String.valueOf(edition)));
            HttpServiceRequest hr = new HttpServiceRequest(url.concat("/face/upload_time_log"), params, HttpRequestMethod.Post);
            JSONObject o = hr.getJSON();
            if (o != null) {
                if (o.getString("success").equals("true")) {
                    deleteTimeLog(id);
                    broadcastCompleted(id, accessCode, time, type, latitude, longitude);
                } else {
                    JSONObject errors = o.getJSONObject("errors");
                    broadcastError(errors.getString("reason"));
                }
            } else {
                broadcastError("Error uploading time log.");
            }
        } catch(Exception ex) {
            broadcastError(ex.getMessage());
            Logger.logE(ex.getMessage());
        } finally {
            wakeLock.release();
        }
    }

    private void broadcastCompleted(int id, String accessCode, String time, String type, double latitude, double longitude) {
        updateNotificationCompleted();
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(ID, id);
        intent.putExtra(STATUS, STATUS_COMPLETED);
        intent.putExtra(ACCESS_CODE, accessCode);
        intent.putExtra(TIME, time);
        intent.putExtra(TYPE, type);
        intent.putExtra(GPS_LATITUDE, latitude);
        intent.putExtra(GPS_LONGITUDE, longitude);
        sendBroadcast(intent);
    }

    private void broadcastError(String errorMsg) {
        updateNotificationError();
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(STATUS, STATUS_ERROR);
        intent.putExtra(ERROR_MSG, errorMsg);
        sendBroadcast(intent);
    }

    private void createNotification() {
        notification.setContentTitle(notificationConfig.getTitle()).setContentText(notificationConfig.getMessage())
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(notificationConfig.getIconResourceID()).setProgress(100, 0, true).setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    private void updateNotificationProgress(final int progress) {
        notification.setContentTitle(notificationConfig.getTitle()).setContentText(notificationConfig.getMessage())
                .setSmallIcon(notificationConfig.getIconResourceID()).setProgress(100, progress, false)
                .setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    private void updateNotificationCompleted() {
        stopForeground(notificationConfig.isAutoClearOnSuccess());

        if (!notificationConfig.isAutoClearOnSuccess()) {
            notification.setContentTitle(notificationConfig.getTitle())
                    .setContentText(notificationConfig.getCompleted())
                    .setSmallIcon(notificationConfig.getIconResourceID()).setProgress(0, 0, false).setOngoing(false);

            notificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, notification.build());
        }
    }

    private void updateNotificationError() {
        stopForeground(false);

        notification.setContentTitle(notificationConfig.getTitle()).setContentText(notificationConfig.getError())
                .setSmallIcon(notificationConfig.getIconResourceID()).setProgress(0, 0, false).setOngoing(false);

        notificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, notification.build());
    }
}
