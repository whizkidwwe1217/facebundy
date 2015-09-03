package com.jeonsoft.facebundypro.net;

import android.content.Context;
import android.os.AsyncTask;

import java.net.InetAddress;
import java.net.URL;

/**
 * Created by WendellWayne on 3/20/2015.
 */
public class ReachableServerHost {
    private Context context;
    private String[] hosts;
    private ReachableServerHostListener callback;
    private int defaultPort;
    public static final int DEFAULT_PORT = 80;

    public ReachableServerHost(Context context, String[] hosts, ReachableServerHostListener callback) {
        this(context, hosts, DEFAULT_PORT, callback);
    }

    public ReachableServerHost(Context context, String[] hosts, int defaultPort, ReachableServerHostListener callback) {
        this.context = context;
        this.hosts = hosts;
        this.callback = callback;
        this.defaultPort = defaultPort;

        if (hosts == null)
            throw new NullPointerException("No hosts provided.");
    }

    public synchronized void execute() {
        try {
            NetworkUtils nu = NetworkUtils.getInstance(context);
            boolean isConnected = false;

            if (nu.isMobileConnected()) {
                isConnected = true;
            }
            if (nu.isWifiConnected()) {
                isConnected = true;
            }

            if (isConnected) {
                new GetReachableAddress(context).execute(hosts);
            } else {
                throw new RuntimeException("No network connection found.");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    class GetReachableAddress extends AsyncTask<String[], String, String> {
        private Context context;

        public GetReachableAddress(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null)
                callback.onStatusChanged(ServerHostStatus.Initializing, "Initializing hosts...");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null && !s.isEmpty()) {
                if (callback != null)
                    callback.onReachableHostAcquired(s);
            } else {
                if (callback != null)
                    callback.onFailedHostAcquisition("No reachable host found.");
            }
        }

        @Override
        protected String doInBackground(String[]... params) {
            NetworkUtils nu = NetworkUtils.getInstance(context);
            String[] urls = params[0];

            for (int i = 0; i < urls.length; i++) {
                try {
                    URL url = new URL(urls[i]);
                    if (callback != null)
                        callback.onStatusChanged(ServerHostStatus.Connecting, urls[i]);
                    InetAddress address = nu.isResolvable(url.getHost());
                    if (address != null) {
                        int port = url.getPort();
                        if (port == -1)
                            port = 80;
                        if (nu.canConnect(address, port)) {
                            if (callback != null)
                                callback.onStatusChanged(ServerHostStatus.Connected, urls[i]);
                            return urls[i];
                        } else {
                            if (callback != null)
                                callback.onStatusChanged(ServerHostStatus.CantConnect, urls[i]);
                        }
                    } else {
                        if (callback != null)
                            callback.onStatusChanged(ServerHostStatus.CantResolve, urls[i]);
                    }
                } catch(Exception ex) {
                    if (callback != null)
                        callback.onStatusChanged(ServerHostStatus.Unknown, "Error - " + ex.getMessage() + ": " + urls[i]);
                    continue;
                }
            }
            return null;
        }
    }
}
