package com.jeonsoft.facebundypro;

import android.content.Context;

import com.jeonsoft.facebundypro.data.TimeLog;
import com.jeonsoft.facebundypro.data.TimelogDataSource;
import com.jeonsoft.facebundypro.licensing.AppEditions;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.settings.GlobalConstants;
import com.jeonsoft.facebundypro.uploadservice.BasicPhotoUploadStrategy;
import com.jeonsoft.facebundypro.uploadservice.FaceMatchingUploadStrategy;
import com.jeonsoft.facebundypro.uploadservice.ITimelogUploadStrategy;
import com.jeonsoft.facebundypro.uploadservice.UploadService;

import java.util.List;

/**
 * Created by WendellWayne on 3/7/2015.
 */
public final class TimeLogUploadHelper {
    private Context context;
    private static TimeLogUploadHelper instance;
    private ITimelogUploadStrategy uploadStrategy;

    public void register() {
        uploadStrategy.registerReceiver();
    }

    public void unregister() {
        uploadStrategy.unregisterReceiver();
    }

    private TimeLogUploadHelper(Context context) {
        this.context = context;
        UploadService.NAMESPACE = "com.jeonsoft.facebundypro";
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public static TimeLogUploadHelper getInstance(Context context) {
        if (instance == null)
            instance = new TimeLogUploadHelper(context);
        return instance;
    }

    public void upload(String host) {
        TimelogDataSource DS = TimelogDataSource.getInstance(context);
        AppEditions edition = GlobalConstants.getInstance().getAppEdition();
        try {
            DS.open();
            List<TimeLog> timelogs = DS.getAllTimeLogs();
            if (edition == AppEditions.Basic || edition == AppEditions.ExtractionOnly) {
                uploadStrategy = new BasicPhotoUploadStrategy(context);
            } else {
                uploadStrategy = new FaceMatchingUploadStrategy(context);
            }
            uploadStrategy.upload(timelogs, host);
        } catch (Exception e) {
            Logger.logE(e.getMessage());
        } finally {
            DS.close();
        }
    }
}
