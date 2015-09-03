package com.jeonsoft.facebundypro.views;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by WendellWayne on 3/18/2015.
 */
public class SubjectManagerService extends IntentService {
    public SubjectManagerService() {
        super("SubjectManagerService");
    }

    public static final String SUBJECT_MANAGER_SEVICE = "com.jeonsoft.facebundypro.views";

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent i = new Intent(SUBJECT_MANAGER_SEVICE);
        sendBroadcast(i);
    }
}
