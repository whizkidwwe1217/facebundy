package com.jeonsoft.facebundypro.uploadservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jeonsoft.facebundypro.logging.Logger;

/**
 * Created by WendellWayne on 3/17/2015.
 */
public class FaceMatchingBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.logE("Receiving...from broadcast");
    }
}
