package com.jeonsoft.facebundypro.uploadservice;

import com.jeonsoft.facebundypro.data.TimeLog;

import java.util.List;

/**
 * Created by WendellWayne on 3/9/2015.
 */
public interface ITimelogUploadStrategy {
    void upload(List<TimeLog> timelogs, String url);
    void registerReceiver();
    void unregisterReceiver();
}
