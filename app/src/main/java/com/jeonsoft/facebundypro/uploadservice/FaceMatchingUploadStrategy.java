package com.jeonsoft.facebundypro.uploadservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.TimeLog;
import com.jeonsoft.facebundypro.data.TimelogDataSource;
import com.jeonsoft.facebundypro.logging.Logger;

import java.util.List;

/**
 * Created by WendellWayne on 3/10/2015.
 */
public class FaceMatchingUploadStrategy extends AbstractTimelogUploadStrategy {
    private final BroadcastReceiver receiver = new FaceMatchingBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(TimelogService.STATUS, 0);
            int id = intent.getIntExtra(TimelogService.ID, -1);
            Logger.logE("Receiving..." + String.valueOf(status) + " : " + String.valueOf(id));

            switch (status) {
                case TimelogService.STATUS_COMPLETED:
                    deleteTimeLog(id);
                    break;
                case TimelogService.STATUS_ERROR:
                    String error = intent.getStringExtra(TimelogService.ERROR_MSG);
                    Logger.logE(error);
                    break;
            }
        }
    };

    private void deleteTimeLog(final int id) {
        if (id == -1)
            return;
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TimelogDataSource ds = TimelogDataSource.getInstance(context);
                try {
                    ds.open();
                    //ds.beginTransaction();
                    ds.deleteById(id);
                } catch (Exception ex) {
                    Logger.logE(ex.getMessage());
                } finally {
                    //ds.endTransaction();
                    ds.close();
                }
            }
        });
    }

    public FaceMatchingUploadStrategy(Context context) {
        super(context);
    }

    @Override
    public void registerReceiver() {
        context.registerReceiver(receiver, new IntentFilter(TimelogService.NOTIFICATION));
    }

    @Override
    public void unregisterReceiver() {
        context.unregisterReceiver(receiver);
    }

    @Override
    public void upload(List<TimeLog> timelogs, String url) {
        for (TimeLog log : timelogs) {
            Intent intent = new Intent(context, TimelogService.class);
            UploadNotificationConfig notificationConfig = new UploadNotificationConfig(R.drawable.face_bundy_logo,
                    "Face Time Log Upload", "Uploading time log...",
                    "Upload time log completed.",
                    "Error uploading time log.", false);
            intent.putExtra(TimelogService.PARAM_NOTIFICATION_CONFIG, notificationConfig);
            intent.putExtra("URL", url);
            intent.putExtra(TimelogService.ID, log.getId());
            intent.putExtra(TimelogService.ACCESS_CODE, log.getAccessCode());
            intent.putExtra(TimelogService.TIME, log.getTime());
            intent.putExtra(TimelogService.TYPE, log.getType());
            intent.putExtra(TimelogService.GPS_LATITUDE, log.getGpsLatitude());
            intent.putExtra(TimelogService.GPS_LONGITUDE, log.getGpsLongitude());
            intent.putExtra(TimelogService.EDITION, log.getEdition());
            context.startService(intent);
        }
    }
}
