package com.jeonsoft.facebundypro;

import android.app.Application;
import android.os.AsyncTask;
import android.test.ApplicationTestCase;

import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.net.HttpRequestMethod;
import com.jeonsoft.facebundypro.net.HttpServiceRequest;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testActivation() {
        new RequestAsync().execute();
    }

    class RequestAsync extends AsyncTask<Void, String, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Logger.logE(s);
        }

        @Override
        protected String doInBackground(Void... params) {
            ArrayList<BasicNameValuePair> httpParams = new ArrayList<>();
            HttpServiceRequest sr = new HttpServiceRequest("http://10.0.0.82:3006/license/get_authenticity_token",  httpParams, HttpRequestMethod.Get);
            String token = "";
            try {
                token = sr.getString();
            } catch (Exception ex) {
                Logger.logE("Error requesting authenticity token.");
            }
            return token;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Logger.logD("Requesting authenticity token.");
        }
    }
}