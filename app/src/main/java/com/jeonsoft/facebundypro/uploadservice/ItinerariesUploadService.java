package com.jeonsoft.facebundypro.uploadservice;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.ItineraryProject;
import com.jeonsoft.facebundypro.data.ItineraryProjectDataSource;
import com.jeonsoft.facebundypro.data.ItineraryProjectTask;
import com.jeonsoft.facebundypro.data.ItineraryProjectTaskDataSource;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.utils.DeviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by WendellWayne on 7/8/2015.
 */
public class ItinerariesUploadService extends IntentService {
    public static final String SERVICE_NAME = "ItinerariesUploadService";
    public static final String TAG = "com.jeonsoft.facebundy.uploadservice.ItinerariesUploadService";
    public static final int UPLOAD_NOTIFICATION_ID = 1;
    public static final int UPLOAD_NOTIFICATION_ID_DONE = 2;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    private PowerManager.WakeLock wakeLock;

    public ItinerariesUploadService() {
        super(SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    private void requestServerMatching() {
        new RequestServerMatchingAsync().execute();
    }

    class RequestServerMatchingAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String[] hosts = CacheManager.getInstance(getApplicationContext()).getStringPreference(CacheManager.SERVER_HOSTS).split("\n"); //new String[] {"http://10.0.0.82:3003", "http://activation.facebundy.com"};
                new ReachableServerHost(getApplicationContext(), hosts, new ReachableServerHostListener() {
                    @Override
                    public void onStatusChanged(ServerHostStatus status, String host) {
                        Logger.logD(status.toString() + ": " + host);
                    }

                    @Override
                    public void onReachableHostAcquired(String reachableHost) {
                        //new CallMatchingAsync().execute(reachableHost);
                    }

                    @Override
                    public void onFailedHostAcquisition(String message) {

                    }
                }).execute();
            } catch(Exception ex) {
                Logger.logE(ex.getMessage());
            }
            return null;
        }
    }

    public static String getActionBroadcast() {
        return UploadService.NAMESPACE + UploadService.BROADCAST_ACTION_SUFFIX;
    }


    private void broadCastCompleted() {
        final Intent intent = new Intent(getActionBroadcast());
        intent.putExtra(UploadService.UPLOAD_ID, 0);
        intent.putExtra(UploadService.STATUS, UploadService.STATUS_COMPLETED);
        intent.putExtra(UploadService.SERVER_RESPONSE_CODE, "");
        intent.putExtra(UploadService.SERVER_RESPONSE_MESSAGE, "");
        intent.putExtra(UploadService.TAG_FILENAME, "");
        intent.putExtra(UploadService.TOTAL, 1);
        intent.putExtra(UploadService.CURRENT, 1);

        sendBroadcast(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            wakeLock.acquire();
            try {
                createNotification();

                boolean hasErrors = false;
                ItineraryProjectDataSource DS = ItineraryProjectDataSource.getInstance(getApplicationContext());
                try {
                    DS.open();
                    List<ItineraryProject> itineraries = DS.getAllItinerary();
                    DS.close();
                    int i = 0;
                    double max = itineraries.size();
                    String url = intent.getStringExtra("URL").concat("/itinerary/upload");
                    int successCount = 0;
                    for(ItineraryProject itinerary : itineraries) {
                        i++;
                        double progress = ((i+1) / max) * 100.0d;
                        updateNotificationProgress((int) Math.round(progress));
                        MultiPartUtility mpu = new MultiPartUtility(url, "UTF-8");
                        mpu.addFormField("access_code", itinerary.accessCode);
                        mpu.addFormField("time_in", itinerary.timeIn);
                        mpu.addFormField("time_out", itinerary.timeOut);
                        mpu.addFormField("itinerary_id", String.valueOf(itinerary.id));
                        mpu.addFormField("project", itinerary.project);
                        mpu.addFormField("location", itinerary.location);
                        mpu.addFormField("latitude", String.valueOf(itinerary.latitude));
                        mpu.addFormField("longitude", String.valueOf(itinerary.longitude));
                        mpu.addFormField("status", String.valueOf(itinerary.status));
                        mpu.addFormField("serial_no", DeviceUtils.getDeviceId(getApplicationContext()));
                        mpu.addFormField("model", DeviceUtils.getModel());
                        mpu.addFormField("manufacturer", DeviceUtils.getManufacturer());
                        JSONObject jTasks = new JSONObject();
                        JSONArray tasksArray = new JSONArray();


                        ItineraryProjectTaskDataSource tds = ItineraryProjectTaskDataSource.getInstance(getApplicationContext());
                        try {
                            tds.open();
                            List<ItineraryProjectTask> pTasks = tds.getAllItineraryTasksByProject(itinerary.id);
                            for(ItineraryProjectTask t : pTasks) {
                                JSONObject o = new JSONObject();
                                o.put("id", t.id);
                                o.put("access_code", t.accessCode);
                                o.put("time_in", t.timeIn);
                                o.put("task", t.task);
                                o.put("notes", t.notes);
                                tasksArray.put(o);
                            }
                            jTasks.put("tasks", tasksArray);
                        } catch(Exception ex) {
                            Logger.logE(ex.getMessage());
                        } finally {
                            if (tds != null)
                                tds.close();
                        }
                        mpu.addFormField("tasks", jTasks.toString());
                        List<String> response = mpu.finish();

                        String reply = "";
                        for (String line : response) {
                            reply += line;
                        }
                        if (reply.contains("\"success\":true")) {
                            ItineraryProjectDataSource ds = ItineraryProjectDataSource.getInstance(getApplicationContext());
                            try {
                                ds.open();
                                ds.beginTransaction();
                                ds.deleteById(itinerary.id);
                                ds.setTransactionSuccessful();
                                successCount++;
                            } catch (Exception ex) {
                                Logger.logE(ex.getMessage());
                            } finally {
                                if (ds != null) {
                                    ds.endTransaction();
                                    ds.close();
                                }
                            }
                        }
                        Logger.logE(reply);
                    }
                    if (i >= 1 && successCount > 0) {
                        //requestServerMatching();
                        broadCastCompleted();
                    }
                } catch (Exception e) {
                    hasErrors = true;
                    String error = "Unknown error.";
                    if (e != null && e.getMessage() != null)
                        error = "Error: " + e.getMessage();
                    //updateNotificationError(error);
                    Logger.logE(error);
                } finally {

                }

                if (hasErrors)
                    updateNotificationError("Upload completed with errors.");
                else
                    updateNotificationCompleted();
            } catch(Exception ex) {
                Logger.logE("Error: " + ex.getMessage());
            } finally {
                wakeLock.release();
                clearNotification();
            }
        }
    }

    private JSONObject createJsonObj(String key, Object value) throws JSONException {
        JSONObject o = new JSONObject();
        o.put(key, value);
        return o;
    }

    private void createNotification() {
        notification.setContentTitle("FaceBundy").setContentText("Uploading itineraries...")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(100, 0, true).setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    private void updateNotificationProgress(final int progress) {
        notification.setContentTitle("FaceBundy").setContentText("Uploading itineraries...")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(100, progress, false).setOngoing(true);
        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    private void updateNotificationCompleted() {
        stopForeground(false);

        notification.setContentTitle("FaceBundy").setContentText("Itineraries upload complete.")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setColor(Color.GREEN)
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(0, 0, false).setOngoing(false);
        notificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, notification.build());
    }

    private void updateNotificationError(String error) {
        stopForeground(false);
        notification.setContentTitle("FaceBundy").setContentText("Itineraries upload error. ")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setColor(Color.RED)
                .setSubText(error)
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(0, 0, false).setOngoing(false);
        notificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, notification.build());
    }

    private void clearNotification() {
        stopForeground(false);
    }
}
