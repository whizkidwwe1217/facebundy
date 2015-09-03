package com.jeonsoft.facebundypro;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.gc.materialdesign.views.ButtonRectangle;
import com.jeonsoft.facebundypro.authentication.ActivationKeyGenerator;
import com.jeonsoft.facebundypro.biometrics.licensing.LicensingServiceManager;
import com.jeonsoft.facebundypro.net.HttpRequestMethod;
import com.jeonsoft.facebundypro.net.HttpServiceRequest;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.NetworkUtils;
import com.jeonsoft.facebundypro.settings.CacheManager;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.utils.DeviceUtils;
import com.jeonsoft.facebundypro.utils.DialogAsync;
import com.jeonsoft.facebundypro.utils.IOUtils;
import com.jeonsoft.facebundypro.utils.ObjectCallback;
import com.jeonsoft.facebundypro.utils.StringUtils;
import com.jeonsoft.facebundypro.views.BaseActionBarActivity;
import com.jeonsoft.facebundypro.widgets.Style;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by WendellWayne on 2/27/2015.
 */
public class ActivationActivity extends BaseActionBarActivity {
    public static final String ACTIVATION_KEY = "ActivationKey";
    public static final String LICENSE_NO = "LicenseNo";
    public static final String EDITION = "Edition";
    public static final String COMPANY_ID = "CompanyId";
    public static final String ACCESS_CODE_LENGTH = "AccessCodeLength";
    public static final String SERVER_URLS = "ServerUrls";
    public static final String SERIAL_FILE_1 = "SerialFile1";
    public static final String SERIAL_FILE_2 = "SerialFile2";
    public static final String DEVICE_NAME = "DeviceName";

    private ButtonRectangle btnActivate;
    private EditText edtActivationCode, edtActivationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        btnActivate = (ButtonRectangle) findViewById(R.id.btnActivate);
        edtActivationCode = (EditText) findViewById(R.id.edtActivationCode);
        edtActivationName = (EditText) findViewById(R.id.edtActivationName);

        btnActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.getInstance(ActivationActivity.this).isDeviceOnline())
                    validateKey(edtActivationCode.getText().toString());
                else
                    showCrouton("You need an internet connection to activate this device.", Style.ALERT);
            }
        });
    }

    private void validateKey(String key) {
        ActivationKeyGenerator keygen = new ActivationKeyGenerator();
        try {
            if (keygen.decode(key) != null)
                activate();
            else
                showCrouton("Validation error: Invalid license info.", Style.ALERT);
        } catch (Exception ex) {
            showCrouton("Validation error: " + ex.getMessage(), Style.ALERT);
        }
    }

    private void activate() {
        new RequestAsync().execute();
    }

    class RequestAsync extends AsyncTask<Void, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ActivationActivity.this.btnActivate.setEnabled(true);
            if (!s.equals("")) {
                sendActivationRequest(s);
            }
            else
                showCrouton("Activation error. Request not authenticated", Style.ALERT);
        }

        @Override
        protected String doInBackground(Void... params) {
            ArrayList<BasicNameValuePair> httpParams = new ArrayList<>();
            HttpServiceRequest sr = new HttpServiceRequest(GlobalConstants.getFacebundyAuthenticationTokenUrl(),  httpParams, HttpRequestMethod.Get);
            String token = "";
            try {
                JSONObject json = sr.getJSON();
                token = json.getString("token");
            } catch (Exception ex) {
                Logger.logE("Error requesting authenticity token." + ex != null ? ex.getMessage() : "Error requesting authenticity token.");
            }
            return token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Logger.logD("Requesting authenticity token.");
            ActivationActivity.this.btnActivate.setEnabled(false);
        }
    }

    private void sendActivationRequest(final String authenticationToken) {
        DialogAsync<String, JSONObject> da = new DialogAsync<>(this, new ObjectCallback<String, JSONObject>() {
            @Override
            public JSONObject onObjectRequestProcess(String[] params) {
                ArrayList<BasicNameValuePair> httpParams = new ArrayList<>();
                httpParams.add(new BasicNameValuePair("device_name", edtActivationName.getText().toString().trim()));
                httpParams.add(new BasicNameValuePair("activation_key", edtActivationCode.getText().toString().trim().replace("-", "")));
                httpParams.add(new BasicNameValuePair("device_info", DeviceUtils.generateJSONDeviceInfo(ActivationActivity.this)));
                HttpServiceRequest sr = new HttpServiceRequest(GlobalConstants.getFacebundyLicenseActivationUrl(),  httpParams, HttpRequestMethod.Post);
                sr.setIsProtectFromForgery(true);
                sr.setAuthenticityToken(authenticationToken);
                JSONObject json = null;
                try {
                    json = sr.getJSON();
                } catch (Exception ex) {
                    Logger.logE(ex.getMessage());
                }
                return json;
            }

            @Override
            public void onObjectRequestComplete(JSONObject result) {
                String msg;
                if (result != null) {
                    try {
                        JSONObject jsonLicense = result.getJSONObject("license");
                        int edition = jsonLicense.optInt("edition", 0);
                        int company_id = jsonLicense.optInt("company_id", 0);
                        String code = jsonLicense.optString("activation_key", "");
                        String name = jsonLicense.optString("device_name", "");
                        String message = jsonLicense.getString("message");
                        JSONArray arr = jsonLicense.getJSONArray("server_urls");
                        String urls = "";

                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                String url = o.getString("name");
                                int url_type = o.getInt("connection_type_id");
                                //urls += String.valueOf(url_type).concat(url).concat("\n");
                                urls += url.concat("\n");
                            }
                        }
                        int license_no = jsonLicense.optInt("license_no", 0);
                        int access_code_length = jsonLicense.optInt("access_code_length", 4);
                        String error_message = jsonLicense.getString("error_msg");
                        int message_id = jsonLicense.getInt("message_id");
                        String serial_file_content_1 = jsonLicense.getString("serial_file_content_1");
                        String serial_file_content_2 = jsonLicense.getString("serial_file_content_2");
                        String serial_file_name_1 = jsonLicense.getString("serial_file_name_1");
                        String serial_file_name_2 = jsonLicense.getString("serial_file_name_2");
                        if (message_id == 0 || message_id == 1) {
                            if (message_id == 1)
                                Logger.logI("Device activated but no device information provided.");
                            if (edition != 0) {
                                Intent intent = new Intent();
                                intent.putExtra(ACTIVATION_KEY, code);
                                intent.putExtra(DEVICE_NAME, name);
                                intent.putExtra(LICENSE_NO, license_no);
                                intent.putExtra(ACCESS_CODE_LENGTH, access_code_length);
                                intent.putExtra(EDITION, edition);
                                intent.putExtra(COMPANY_ID, company_id);
                                intent.putExtra(SERVER_URLS, urls);
                                CacheManager.getInstance(ActivationActivity.this).setPreference(CacheManager.SERVER_HOSTS, urls);
                                try {
                                    if (!serial_file_name_1.equals(""))
                                        StringUtils.decodeAndSaveBase64(serial_file_content_1, IOUtils.combinePath(LicensingServiceManager.LICENSES_DIRECTORY, serial_file_name_1));
                                    if (!serial_file_name_2.equals(""))
                                        StringUtils.decodeAndSaveBase64(serial_file_content_2, IOUtils.combinePath(LicensingServiceManager.LICENSES_DIRECTORY, serial_file_name_2));
                                } catch (IOException ex) {
                                    logError(ex.getMessage());
                                }
                                setResult(FaceBundyActivity.LICENSE_ACTIVATED_RESULT_CODE, intent);
                                finish();
                            } else {
                                msg = "Activation failed. Please contact your device administrator.";
                                Logger.logE(msg);
                                showCrouton(msg, Style.ALERT);
                            }
                        } else {
                            Logger.logE(message);
                            showCrouton(error_message, Style.ALERT);
                        }
                    } catch (JSONException ex) {
                        Logger.logE(ex.getMessage());
                        showCrouton(ex.getMessage(), Style.ALERT);
                    }
                } else {
                    msg = "An error occurred during activation.";
                    Logger.logE(msg);
                    showCrouton(msg, Style.ALERT);
                }
            }

            @Override
            public void onObjectRequestError(String message) {
                Logger.logE(message);
            }

            @Override
            public void onRequestCancelled() {

            }

            @Override
            public void onPreRequest() {

            }
        }, "Please wait while we are verifying your license...");
        da.execute();
    }
}
