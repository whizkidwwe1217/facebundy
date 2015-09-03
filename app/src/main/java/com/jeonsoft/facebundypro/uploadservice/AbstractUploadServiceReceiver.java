package com.jeonsoft.facebundypro.uploadservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Abstract broadcast receiver from which to inherit when creating a receiver for {@link UploadService}.
 * 
 * It provides the boilerplate code to properly handle broadcast messages coming from the upload service and dispatch
 * them to the proper handler method.
 * 
 * @author alexbbb (Alex Gotev)
 * @author eliasnaur
 * 
 */
public abstract class AbstractUploadServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            if (UploadService.getActionBroadcast().equals(intent.getAction())) {
                final int status = intent.getIntExtra(UploadService.STATUS, 0);
                final String uploadId = intent.getStringExtra(UploadService.UPLOAD_ID);
                final int total = intent.getIntExtra(UploadService.TOTAL_ITEMS_TO_UPLOAD, 0);
                final int totalUploaded = intent.getIntExtra(UploadService.TOTAL_ITEMS_UPLOADED, 0);

                switch (status) {
                    case UploadService.STATUS_ERROR:
                        final Exception exception = (Exception) intent
                                .getSerializableExtra(UploadService.ERROR_EXCEPTION);
                        onError(uploadId, exception);
                        break;

                    case UploadService.STATUS_COMPLETED:
                        final int responseCode = intent.getIntExtra(UploadService.SERVER_RESPONSE_CODE, 0);
                        final String responseMsg = intent.getStringExtra(UploadService.SERVER_RESPONSE_MESSAGE);
                        final String tag = intent.getStringExtra(UploadService.TAG_FILENAME);
                        final int current = intent.getIntExtra(UploadService.CURRENT, 0);
                        final int total2 = intent.getIntExtra(UploadService.TOTAL, 0);
                        onCompleted(uploadId, responseCode, responseMsg, total, totalUploaded, tag, total2, current);
                        break;

                    case UploadService.STATUS_IN_PROGRESS:
                        final int progress = intent.getIntExtra(UploadService.PROGRESS, 0);
                        onProgress(uploadId, progress);
                        break;

                    default:
                        break;
                }
            }
        }

    }

    /**
     * Register this upload receiver. It's recommended to register the receiver in Activity's onResume method.
     * 
     * @param activity activity in which to register this receiver
     */
    public void register(final Activity activity) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UploadService.getActionBroadcast());
        activity.registerReceiver(this, intentFilter);
    }

    /**
     * Unregister this upload receiver. It's recommended to unregister the receiver in Activity's onPause method.
     * 
     * @param activity activity in which to unregister this receiver
     */
    public void unregister(final Activity activity) {
        activity.unregisterReceiver(this);
    }

    /**
     * Called when the upload progress changes.
     * 
     * @param uploadId unique ID of the upload request
     * @param progress value from 0 to 100
     */
    public abstract void onProgress(final String uploadId, final int progress);

    /**
     * Called when an error happens during the upload.
     * 
     * @param uploadId unique ID of the upload request
     * @param exception exception that caused the error
     */
    public abstract void onError(final String uploadId, final Exception exception);

    /**
     * Called when the upload is completed successfully.
     * 
     * @param uploadId unique ID of the upload request
     * @param serverResponseCode status code returned by the server
     * @param serverResponseMessage string containing the response received from the server
     */
    public abstract void onCompleted(final String uploadId, final int serverResponseCode,
                                     final String serverResponseMessage, final int totalItemsToUpload, final int totalItemsUploaded, final String tag, final int total, final int current);
}
