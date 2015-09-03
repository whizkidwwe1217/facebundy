package com.jeonsoft.facebundypro.logging;

import android.util.Log;

/**
 * Created by WendellWayne on 2/14/2015.
 */
public final class Logger {
    public static String TAG = "FaceBundy";

    public static void logE(String message) {
        Log.e(Logger.TAG, message);
    }

    public static void logI(String message) {
        Log.i(Logger.TAG, message);
    }

    public static void logD(String message) {
        Log.d(Logger.TAG, message);
    }
}
