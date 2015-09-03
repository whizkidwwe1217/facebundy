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
import com.jeonsoft.facebundypro.data.Subject;
import com.jeonsoft.facebundypro.data.SubjectsDataSource;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.HttpRequestMethod;
import com.jeonsoft.facebundypro.net.HttpServiceRequest;
import com.jeonsoft.facebundypro.net.ReachableServerHost;
import com.jeonsoft.facebundypro.net.ReachableServerHostListener;
import com.jeonsoft.facebundypro.net.ServerHostStatus;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.utils.StringUtils;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 6/24/2015.
 */
public class SubjectMultiPartUploadService extends IntentService {
    public static final String SERVICE_NAME = "MultiPartUploadService";
    public static final String TAG = "com.jeonsoft.facebundy.uploadservice.MultiPartUploadService";
    public static final int UPLOAD_NOTIFICATION_ID = 1;
    public static final int UPLOAD_NOTIFICATION_ID_DONE = 2;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notification;
    private PowerManager.WakeLock wakeLock;

    public SubjectMultiPartUploadService() {
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
                        new CallMatchingAsync().execute(reachableHost);
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

    class CallMatchingAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<BasicNameValuePair> p = new ArrayList<BasicNameValuePair>();
            HttpServiceRequest hsr = new HttpServiceRequest(params[0].concat("/face/request_matching"), p, HttpRequestMethod.Get);
            JSONObject o;
            String msg = "";
            try {
                o = hsr.getJSON();
                if (o != null) {
                    Logger.logE(o.getString("success"));
                    msg = o.getString("success");
                }
            } catch (Exception ex) {
                if (ex != null && ex.getMessage() != null)
                    Logger.logE(ex.getMessage());
                else
                    Logger.logE("An error has occurred while requesting match.");
            }
            return msg;
        }
    }

    public static String getActionBroadcast() {
        return UploadService.NAMESPACE + UploadService.BROADCAST_ACTION_SUFFIX;
    }

    private void broadCastCompleted() {
        final Intent intent = new Intent(TAG);
        intent.putExtra(UploadService.UPLOAD_ID, 0);
        intent.putExtra(UploadService.STATUS, UploadService.STATUS_COMPLETED);
        intent.putExtra(UploadService.SERVER_RESPONSE_CODE, "");
        intent.putExtra(UploadService.SERVER_RESPONSE_MESSAGE, "");
        intent.putExtra(UploadService.TAG_FILENAME, "");
        intent.putExtra(UploadService.TOTAL, 1);
        intent.putExtra(UploadService.CURRENT, 1);

        sendBroadcast(intent);
    }

    private void createSubject(String accessCode, String url, SubjectsDataSource DS) throws IOException, JSONException {
        ArrayList<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("access_code", accessCode));
        HttpServiceRequest hsr = new HttpServiceRequest(url.concat("/face_template/download_employee_face"), params, HttpRequestMethod.Get);
        JSONObject o = hsr.getJSON();
        if (o != null) {
            JSONObject templates = o.getJSONObject("templates");
            JSONArray updated_from_server = templates.getJSONArray("updated_from_server");
            for (int i = 0; i < updated_from_server.length(); i++) {
                JSONObject t = updated_from_server.getJSONObject(i);
                String accesscode = t.getString("accesscode");
                String timestamp = t.getString("timestamp");
                String template = t.getString("template");
                String thumbnail = t.getString("thumbnail");
                String thumbFile = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext()).concat("/").concat(accessCode).concat(".jpg");
                String tempFile = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext()).concat("/").concat(accessCode).concat(".dat");
                byte[] thumbBuffer = StringUtils.decodeAndSaveBase64(thumbnail, thumbFile);
                byte[] tempBuffer = StringUtils.decodeAndSaveBase64(template, tempFile);
                DS.createSubject(accesscode, timestamp, tempBuffer, thumbBuffer);
            }
        } else {
            Logger.logE("Nothing to download.");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            wakeLock.acquire();
            String error = "";
            try {
                createNotification();

                boolean hasErrors = false;

                String url = intent.getStringExtra("URL");
                ArrayList<BasicNameValuePair> params = new ArrayList<>();
                SubjectsDataSource DS = SubjectsDataSource.getInstance(getApplicationContext());

                try {
                    DS.open();

                    List<Subject> subjects = DS.getAllSubjects();

                    if (subjects != null && subjects.size() > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (Subject subject: subjects) {
                            sb.append(String.format("'%s',", subject.getAccessCode().trim()));
                        }
                        sb = sb.deleteCharAt(sb.toString().lastIndexOf(','));
                        params.add(new BasicNameValuePair("access_code_list", sb.toString()));
                    } else {
                        Logger.logE("No Subjects.");
                    }

                    HttpServiceRequest _hsr = new HttpServiceRequest(url.concat("/face_template/download_list"), params, HttpRequestMethod.Get);
                    JSONObject _o = _hsr.getJSON();
                    if (_o != null) {
                        JSONObject _tempList = _o.getJSONObject("templates");
                        JSONArray _updated = _tempList.getJSONArray("updated_from_server");
                        int _count = 0;
                        double _max = _updated.length();
                        for (int _i = 0; _i < _updated.length(); _i++) {
                            _count++;
                            double _progress = ((_count+1)/_max) * 100.0d;
                            updateNotificationProgress((int) Math.round(_progress));
                            JSONObject t = _updated.getJSONObject(_i);
                            String accessCode = t.getString("accesscode");
                            createSubject(accessCode, url, DS);
                        }
                    }

                    /*HttpServiceRequest hsr = new HttpServiceRequest(url.concat("/face_template/download"), params, HttpRequestMethod.Get);
                    JSONObject o = hsr.getJSON();
                    if (o != null) {
                        JSONObject templates = o.getJSONObject("templates");
                        JSONArray updated_from_server = templates.getJSONArray("updated_from_server");
                        int count = 0;
                        double max = updated_from_server.length();

                        for (int i = 0; i < updated_from_server.length(); i++) {
                            count++;
                            double progress = ((count+1) / max) * 100.0d;
                            updateNotificationProgress((int) Math.round(progress));

                            JSONObject t = updated_from_server.getJSONObject(i);
                            String accessCode = t.getString("accesscode");
                            String timestamp = t.getString("timestamp");
                            String template = t.getString("template");
                            String thumbnail = t.getString("thumbnail");
                            String thumbFile = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext()).concat("/").concat(accessCode).concat(".jpg");
                            String tempFile = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext()).concat("/").concat(accessCode).concat(".dat");
                            byte[] thumbBuffer = StringUtils.decodeAndSaveBase64(thumbnail, thumbFile);
                            byte[] tempBuffer = StringUtils.decodeAndSaveBase64(template, tempFile);
                            DS.createSubject(accessCode, timestamp, tempBuffer, thumbBuffer);
                        }
                    } else {
                        Logger.logE("Nothing to download.");
                    }*/

                    subjects = DS.getAllSubjects();
                    String path = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext());

                    int count = 0;
                    double max = subjects.size();

                    for(Subject subject : subjects) {
                        File fileThumb = new File(path + "/" + subject.getAccessCode() + ".jpg");
                        File fileTemplate = new File(path + "/" + subject.getAccessCode() + ".dat");
                        count++;
                        double progress = ((count+1) / max) * 100.0d;
                        updateNotificationProgress((int) Math.round(progress));

                        if (fileTemplate.exists()) {
                            MultiPartUtility mpu = new MultiPartUtility(url + "/face_template/upload", "UTF-8");
                            mpu.addFormField("access_code", subject.getAccessCode());
                            mpu.addFilePart("face_template", fileTemplate);
                            mpu.addFilePart("face_thumbnail", fileThumb);

                            List<String> response = mpu.finish();

                            String reply = "";

                            for (String line : response) {
                                reply += line;
                            }
                            Logger.logE(reply);
                        }
                    }
                } catch (Exception e) {
                    hasErrors = true;
                    error = "Unknown error.";
                    if (e != null && e.getMessage() != null)
                        error = "Error: " + e.getMessage();
                    //updateNotificationError(error);
                    Logger.logE(error);
                } finally {
                    DS.close();
                }

                if (hasErrors)
                    updateNotificationError("Face template sync completed with errors. " + error);
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

    private void createNotification() {
        notification.setContentTitle("FaceBundy").setContentText("Synching face templates...")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(100, 0, true).setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    private void updateNotificationProgress(final int progress) {
        notification.setContentTitle("FaceBundy").setContentText("Synching face templates...")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(100, progress, false).setOngoing(true);
        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    private void updateNotificationCompleted() {
        stopForeground(false);

        notification.setContentTitle("FaceBundy").setContentText("Face template sync complete.")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setColor(Color.GREEN)
                .setSmallIcon(R.drawable.face_bundy_launcher).setProgress(0, 0, false).setOngoing(false);
        notificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, notification.build());
        broadCastCompleted();
    }

    private void updateNotificationError(String error) {
        stopForeground(false);
        notification.setContentTitle("FaceBundy").setContentText("Face template sync error. ")
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
