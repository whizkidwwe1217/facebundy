package com.jeonsoft.facebundypro.net;

/**
 * Created by Wayne on 4/10/2014.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class HttpContentProvider {

    public HttpContentProvider() {

    }

    private static CookieStore cookieStore = new BasicCookieStore();

    /*
        Request JSON using concatenated parameters.
     */
    public JSONObject makeHttpRequest(String url, ArrayList<BasicNameValuePair> params) throws IOException, JSONException {
        if (params != null) {
            if (params.size() > 0)
                url = url.concat("?");
            for (int i = 0; i < params.size(); i++) {
                NameValuePair param = params.get(i);
                url = url.concat(i == 0 ? "" : "&").concat(URLEncoder.encode(param.getName(), "utf-8")).concat("=").concat(URLEncoder.encode(param.getValue(), "utf-8"));
            }
        }
        Log.e("JSON", url);
        URL mUrl = new URL(url);

        URLConnection con = mUrl.openConnection();

        DefaultHttpClient client = new DefaultHttpClient();
        HttpContext ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpPost post = new HttpPost(url);
        HttpResponse response = client.execute(post, ctx);
        //Use to retrieve cookies
        for (Cookie cookie : cookieStore.getCookies()) {
            Log.e("Cookie", cookie.toString());
        }
        HttpEntity httpEntity = response.getEntity();
        BufferedReader in = new BufferedReader(new InputStreamReader(httpEntity.getContent()));

        //BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); // This line is not working when redirecting page

        String line;
        String json = "";
        while ((line = in.readLine()) != null) {
            json += line;
        }

        JSONObject o = new JSONObject(json);
        return o;
    }

    public String requestHttpHtmlContent(String url, ArrayList<BasicNameValuePair> params) throws IOException {
        if (params != null) {
            if (params.size() > 0)
                url = url.concat("?");
            for (int i = 0; i < params.size(); i++) {
                NameValuePair param = params.get(i);
                url = url.concat(i == 0 ? "" : "&").concat(URLEncoder.encode(param.getName(), "utf-8")).concat("=").concat(URLEncoder.encode(param.getValue(), "utf-8"));
            }
        }
        Log.e("html", url);
        URL mUrl = new URL(url);

        URLConnection con = mUrl.openConnection();
        con.addRequestProperty("Content-Type", "text/html");
        con.addRequestProperty("Accept", "text/html");

        DefaultHttpClient client = new DefaultHttpClient();
        HttpContext ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpPost post = new HttpPost(url);
        HttpResponse response = client.execute(post, ctx);
        //Use to retrieve cookies
        /*for (Cookie cookie : cookieStore.getCookies()) {
            Log.e("Cookie", cookie.toString());
        }*/
        HttpEntity httpEntity = response.getEntity();
        BufferedReader in = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
        String line;
        String content = "";
        while ((line = in.readLine()) != null) {
            content += line;
        }
        return content;
    }

    public JSONObject makeHttpRequest(String url, String method, ArrayList<BasicNameValuePair> params) {
        InputStream is = null;
        JSONObject jObj = null;
        String json = "";
        try {
            if (method.toUpperCase().equals("POST")) {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Accept", "application/json");
                HttpContext ctx = new BasicHttpContext();
                ctx.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                HttpResponse httpResponse = httpClient.execute(httpPost, ctx);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            } else if (method.toUpperCase().equals("GET")) {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);
                HttpContext ctx = new BasicHttpContext();
                ctx.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                HttpResponse httpResponse = httpClient.execute(httpGet, ctx);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }
            Log.e("JSON URL", url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
            is.close();
            json = sb.toString();
            Log.e("JSON Content", sb.toString());
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return jObj;
    }

    public Bitmap getDownsampledBitmapFromStreamUrl(String urlStr, int requiredWidth, int requiredHeight) throws IOException {
        Bitmap bmp;
        URL url = new URL(urlStr);
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(urlStr);
        HttpContext ctx = new BasicHttpContext();
        ctx.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpResponse httpResponse = httpClient.execute(httpGet, ctx);
        HttpEntity httpEntity = httpResponse.getEntity();
        InputStream input = httpEntity.getContent();

        //URLConnection connection = url.openConnection();
        //connection.connect();

        //InputStream input = new BufferedInputStream(url.openStream());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();
        input = new BufferedInputStream(url.openStream());
        options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[32 * 1024];
        options.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeStream(input, null, options);
        input.close();

        return bmp;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > requiredHeight || width > requiredWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) requiredHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) requiredWidth);
            }
        }
        return inSampleSize;
    }
}
