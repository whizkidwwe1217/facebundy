package com.jeonsoft.facebundypro.uploadservice;

import android.content.Context;

import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.TimeLog;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.utils.DeviceUtils;

import java.io.File;
import java.util.List;

/**
 * Created by WendellWayne on 3/9/2015.
 */
public class BasicPhotoUploadStrategy extends AbstractTimelogUploadStrategy {
    @Override
    public void registerReceiver() {

    }

    @Override
    public void unregisterReceiver() {

    }


    public BasicPhotoUploadStrategy(Context context) {
        super(context);
    }

    @Override
    public void upload(List<TimeLog> timelogs, String url) {
        int total = timelogs.size();
        int current = 0;

        for (TimeLog log : timelogs) {
            File file = new File(log.getFilename());
            if (file.exists()) {
                ++current;
                UploadRequest request = new UploadRequest(context, String.valueOf(log.getId()), url + "/face/upload_face_log", total, current);
                request.addFileToUpload(file.getAbsolutePath(), "face_image", log.getFilename(), ContentType.APPLICATION_OCTET_STREAM);
                request.addParameter("access_code", log.getAccessCode());
                request.addParameter("time", log.getTime());
                request.addParameter("type", log.getType().equals("IN") ? "1" : "2");
                request.addParameter("latitude", String.valueOf(log.getGpsLatitude()));
                request.addParameter("longitude", String.valueOf(log.getGpsLongitude()));
                request.addParameter("edition", String.valueOf(log.getEdition()));
                request.addParameter("serial_no", DeviceUtils.getDeviceId(context));
                request.addParameter("model", DeviceUtils.getModel());
                request.addParameter("manufacturer", DeviceUtils.getManufacturer());
                request.setNotificationConfig(R.drawable.face_bundy_launcher,
                        "Face Time Log Upload",
                        "Uploading time log...", "Upload time log completed.", "Error uploading time log.", false);
                try {
                    UploadService.startUpload(request);

                } catch (Exception ex) {
                    Logger.logE(ex.getMessage());
                }
            }
        }
    }
}
