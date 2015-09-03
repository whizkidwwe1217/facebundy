package com.jeonsoft.facebundypro.uploadservice;

import android.content.Context;

import com.jeonsoft.facebundypro.data.TimeLog;

import java.util.List;

/**
 * Created by WendellWayne on 3/17/2015.
 */
public abstract class AbstractTimelogUploadStrategy implements ITimelogUploadStrategy {
    private ITimelogUploadListener listener;
    protected Context context;

    public AbstractTimelogUploadStrategy(Context context) {
        this.context = context;
    }

    public void setUploadListener(ITimelogUploadListener listener) {
        this.listener = listener;
    }

    protected void onUploadCompleted(String uploadId) {
        if (listener != null)
            listener.onUploadCompleted(uploadId);
    }

    @Override
    public abstract void upload(List<TimeLog> timelogs, String url);

}
