package com.jeonsoft.facebundypro.uploadservice;

import android.app.IntentService;
import android.content.Intent;

import com.jeonsoft.facebundypro.data.EmployeeDataSource;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.HttpRequestMethod;
import com.jeonsoft.facebundypro.net.HttpServiceRequest;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by WendellWayne on 5/13/2015.
 */
public class EmployeeListDownloadService extends IntentService {
    public static final String DOWNLOAD_SERVICE_NOTIFICATION = "com.jeonsoft.facebundypro.uploadservice";
    public static final int SUCCESS = 1;
    public static final int FAILED = 0;
    public static final String STATUS = "status";
    public static final String MSG = "message";
    public static final String TOTAL = "total";
    public static final String CURRENT = "current";
    public EmployeeListDownloadService() {
        super("EmployeeListDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("URL");
        ArrayList<BasicNameValuePair> params = new ArrayList<>();
        EmployeeDataSource ds = EmployeeDataSource.getInstance(getApplicationContext());

        try {
            ds.open();
            ds.clear();
            HttpServiceRequest hsr = new HttpServiceRequest(url.concat("/employee/download"), params, HttpRequestMethod.Get);
            JSONObject o = hsr.getJSON();
            if (o != null) {
                JSONObject templates = o.getJSONObject("employees");
                JSONArray updated_from_server = templates.getJSONArray("active_employees");
                int total = updated_from_server.length();
                for (int i = 0; i < updated_from_server.length(); i++) {
                    JSONObject t = updated_from_server.getJSONObject(i);
                    String accessCode = t.getString("accesscode");
                    String name = t.getString("formalname");
                    String nickname = t.getString("nickname");
                    String lastname = t.getString("lastname");
                    String firstname = t.getString("firstname");
                    ds.createEmployee(accessCode, name, nickname, lastname, firstname);
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
            String error = "Error synching employee list.";
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
