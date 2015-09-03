package com.jeonsoft.facebundypro.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Wayne on 4/10/2014.
 */
public final class NetworkUtils {
    private  Context context;
    private boolean isWifiConnected;
    private boolean isMobileConnected;
    private static NetworkUtils instance;
    private ConnectivityManager cm;
    public static int CONNECTION_TIMEOUT = 10000;

    private NetworkUtils(final Context context) {
        this.context = context;
        cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static NetworkUtils getInstance(final Context context) {
        if (instance == null)
            instance = new NetworkUtils(context);
        return instance;
    }

    public boolean isDeviceOnline() {
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void determineConnectivityType() {
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            isWifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isMobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            isWifiConnected = false;
            isMobileConnected = false;
        }
    }

    public InetAddress isResolvable(String hostname) {
        try {
            return InetAddress.getByName(hostname);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    public boolean canConnect(InetAddress address, int port) {
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(address, port);

        try {
            socket.connect(socketAddress, CONNECTION_TIMEOUT);
        } catch(Exception ex) {
            return false;
        } finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch(IOException ex) {
                    Log.e("JEONSOFT", ex.getMessage());
                }
            }
        }
        return true;
    }

    public boolean isWifiConnected() {
        determineConnectivityType();
        return  isWifiConnected;
    }

    public boolean isMobileConnected() {
        determineConnectivityType();
        return isMobileConnected;
    }
}
