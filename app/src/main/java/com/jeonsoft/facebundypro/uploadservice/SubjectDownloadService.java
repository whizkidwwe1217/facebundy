package com.jeonsoft.facebundypro.uploadservice;

import android.app.IntentService;
import android.content.Intent;

import com.jeonsoft.facebundypro.data.Subject;
import com.jeonsoft.facebundypro.data.SubjectsDataSource;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.HttpRequestMethod;
import com.jeonsoft.facebundypro.net.HttpServiceRequest;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.utils.StringUtils;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WendellWayne on 3/24/2015.
 */
public class SubjectDownloadService extends IntentService {
    public static final String DOWNLOAD_SERVICE_NOTIFICATION = "com.jeonsoft.facebundypro.uploadservice";
    public static final int SUCCESS = 1;
    public static final int FAILED = 0;
    public static final String STATUS = "status";
    public static final String MSG = "message";
    public static final String TOTAL = "total";
    public static final String CURRENT = "current";
    public SubjectDownloadService() {
        super("SubjectDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("URL");
        ArrayList<BasicNameValuePair> params = new ArrayList<>();
        SubjectsDataSource ds = SubjectsDataSource.getInstance(getApplicationContext());

        try {
            ds.open();
            List<Subject> subjects = ds.getAllSubjects();

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

            HttpServiceRequest hsr = new HttpServiceRequest(url.concat("/face_template/download"), params, HttpRequestMethod.Get);
            JSONObject o = hsr.getJSON();
            if (o != null) {
                JSONObject templates = o.getJSONObject("templates");
                JSONArray updated_from_server = templates.getJSONArray("updated_from_server");
                int total = updated_from_server.length();
                for (int i = 0; i < updated_from_server.length(); i++) {
                    JSONObject t = updated_from_server.getJSONObject(i);
                    String accessCode = t.getString("accesscode");
                    String timestamp = t.getString("timestamp");
                    String template = t.getString("template");
                    String thumbnail = t.getString("thumbnail");
                    String thumbFile = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext()).concat("/").concat(accessCode).concat(".jpg");
                    String tempFile = GlobalConstants.getEmployeeFaceTemplatesDir(getApplicationContext()).concat("/").concat(accessCode).concat(".dat");
                    byte[] thumbBuffer = StringUtils.decodeAndSaveBase64(thumbnail, thumbFile);
                    byte[] tempBuffer = StringUtils.decodeAndSaveBase64(template, tempFile);
                    ds.createSubject(accessCode, timestamp, tempBuffer, thumbBuffer);
                    Intent intent1 = new Intent(DOWNLOAD_SERVICE_NOTIFICATION);
                    intent1.putExtra(STATUS, SUCCESS);
                    intent1.putExtra(TOTAL, total);
                    intent1.putExtra(CURRENT, i+1);
                    sendBroadcast(intent1);
                }
            } else {
                Logger.logE("Nothing to download.");
            }
        } catch(Exception ex) {
            String error = "Error synching face templates";
            if (ex != null && ex.getMessage() != null)
                error = ex.getMessage();
            Logger.logE(error);
            Intent intent1 = new Intent(DOWNLOAD_SERVICE_NOTIFICATION);
            intent1.putExtra(STATUS, FAILED);
            intent1.putExtra(MSG, error);
            sendBroadcast(intent1);
        } finally {
            try {
                ds.close();
            } catch (Exception ex) {
                Logger.logE(ex.getMessage());
            }
        }
    }
}
