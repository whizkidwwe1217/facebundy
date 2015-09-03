package com.jeonsoft.facebundypro.uploadservice;

import android.app.Activity;
import android.content.Context;

import com.jeonsoft.facebundypro.R;
import com.jeonsoft.facebundypro.data.Subject;
import com.jeonsoft.facebundypro.logging.Logger;
import com.jeonsoft.facebundypro.settings.GlobalConstants;

import java.io.File;
import java.util.List;

/**
 * Created by WendellWayne on 3/16/2015.
 */
public class SubjectUploader {
    private Context context;
    private static SubjectUploader instance;

    public static SubjectUploader getInstance(Context context) {
        if (instance == null) {
            instance = new SubjectUploader(context);
        }
        return instance;
    }

    private SubjectUploader(Context context) {
        this.context = context;
    }

    public void register() {
        uploadReceiver.register((Activity) context);
    }

    public void unregister() {
        uploadReceiver.unregister((Activity) context);
    }

    private final AbstractUploadServiceReceiver uploadReceiver = new AbstractUploadServiceReceiver() {

        @Override
        public void onProgress(String uploadId, int progress) {
            Logger.logI("The progress of the upload with ID " + uploadId + " is: " + progress);
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            String message = "Error in upload with ID: " + uploadId + ". " + exception.getLocalizedMessage();
            Logger.logE(message);
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, String serverResponseMessage, int totalItems, int totalUploaded, String tag, int total, int current) {
            String message = "Upload with ID " + uploadId + " is completed: " + serverResponseCode + ", "
                    + serverResponseMessage;
            Logger.logI(message);
        }
    };

    public void upload(List<Subject> subjects, String url) {
        int total = subjects.size();
        int current = 0;

        for (Subject subject : subjects) {
            String path = GlobalConstants.getEmployeeFaceTemplatesDir(context);
            File fileThumb = new File(path + "/" + subject.getAccessCode() + ".jpg");
            File fileTemplate = new File(path + "/" + subject.getAccessCode() + ".dat");

            if (fileTemplate.exists()) {
                ++current;
                UploadRequest request = new UploadRequest(context, String.valueOf(subject.getId()), url + "/face_template/upload", total, current);
                request.addFileToUpload(fileTemplate.getAbsolutePath(), "face_template", fileTemplate.getAbsolutePath(), ContentType.APPLICATION_OCTET_STREAM);
                request.addFileToUpload(fileThumb.getAbsolutePath(), "face_thumbnail", fileThumb.getAbsolutePath(), ContentType.APPLICATION_OCTET_STREAM);
                request.addParameter("access_code", subject.getAccessCode());
                request.setNotificationConfig(R.drawable.face_bundy_logo,
                        "Face Template Upload",
                        "Uploading face template...", "Upload face template completed.", "Error uploading face template.", false);
                try {
                    UploadService.startUpload(request);
                } catch (Exception ex) {
                    Logger.logE("Error synching face template: " + ex.getMessage());
                }
            }
        }
    }
}
