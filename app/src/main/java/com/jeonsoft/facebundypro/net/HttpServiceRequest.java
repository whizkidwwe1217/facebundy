package com.jeonsoft.facebundypro.net;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Wayne on 4/17/2014.
 */
public class HttpServiceRequest {
    private static CookieStore cookieStore = new BasicCookieStore();
    private String url;
    private InputStream inputStream;
    private HttpRequestMethod requestMethod;
    private ArrayList<BasicNameValuePair> parameters;
    private HttpRequestEntity requestEntity;
    private String authenticityToken;
    private boolean protectFromForgery;

    public HttpServiceRequest(String url, ArrayList<BasicNameValuePair> parameters, HttpRequestMethod requestMethod) {
        this.url = url;
        this.requestMethod = requestMethod;
        this.parameters = parameters;
    }

    public void setIsProtectFromForgery(boolean protectFromForgery) {
        this.protectFromForgery = protectFromForgery;
    }

    public boolean isProtectFromForgery() {
        return protectFromForgery;
    }

    public String getAuthenticityToken() {
        return authenticityToken;
    }

    public void setAuthenticityToken(String authenticityToken) {
        this.authenticityToken = authenticityToken;
    }

    public JSONObject getJSON() throws IOException, JSONException {
        String json = "";
        JSONObject jsonObject;

        requestEntity = getHttpRequestEntity();
        inputStream = requestEntity.Entity.getContent();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sb.append(line + "\n");
        json = sb.toString();
        jsonObject = new JSONObject(json);
        return jsonObject;
    }

    public String getString() throws IOException, JSONException {
        String text = "";
        requestEntity = getHttpRequestEntity();
        inputStream = requestEntity.Entity.getContent();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sb.append(line + "\n");
        text = sb.toString();
        return text;
    }

    public void abortRequest() {
        if (requestEntity != null && !requestEntity.Request.isAborted()) {
            requestEntity.Request.abort();
        }
    }

    public void shutdownConnection() {
        if (requestEntity != null) {
            requestEntity.Client.getConnectionManager().shutdown();
        }
    }

    public HttpRequestEntity getHttpRequestEntity() throws UnsupportedEncodingException, ClientProtocolException, IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpRequestBase request = requestMethod == HttpRequestMethod.Post ? createPostRequest() : createGetRequest();
        HttpContext ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpResponse httpResponse = httpClient.execute(request, ctx);
        HttpEntity httpEntity = httpResponse.getEntity();

        HttpRequestEntity requestEntity = new HttpRequestEntity();
        requestEntity.Entity = httpEntity;
        requestEntity.Request = request;
        requestEntity.Client = httpClient;
        return requestEntity;
    }

    private HttpPost createPostRequest() throws UnsupportedEncodingException {
        HttpPost post = new HttpPost(url);
        if (isProtectFromForgery()) {
            parameters.add(new BasicNameValuePair("authenticity_token", getAuthenticityToken()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "utf-8");
        entity.setContentType("application/json");
        post.setEntity(entity);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        return post;
    }

    private HttpGet createGetRequest() {
        String paramString = URLEncodedUtils.format(parameters, "utf-8");
        url += "?" + paramString;
        Log.e("Jeonsoft", url);
        HttpGet get = new HttpGet(url);
        return get;
    }

    public int uploadFile(String uri) {
        String filename = uri;

        DataOutputStream outputStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(uri);

        if (sourceFile.isFile()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(""); //@TODO: Upload url

            } catch (FileNotFoundException e) {

            } catch (MalformedURLException e) {

            }
        }
        return -1;
    }

    class HttpRequestEntity {
        public HttpRequestBase Request;
        public HttpEntity Entity;
        public HttpClient Client;
    }
}
