package com.jeonsoft.facebundypro.net;

/**
 * Created by WendellWayne on 3/20/2015.
 */
public interface ReachableServerHostListener {
    void onStatusChanged(ServerHostStatus status, String host);
    void onReachableHostAcquired(String reachableHost);
    void onFailedHostAcquisition(String message);
}
