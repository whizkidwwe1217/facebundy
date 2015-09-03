package com.jeonsoft.facebundypro.utils;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Wendell Wayne on 12/23/2014.
 */
public final class TimeUtils {
    public static final String TIME_SERVER = "pool.ntp.org"; //"time-a.nist.gov";

    public static long getCurrentNetworkTime() throws IOException {
        return getCurrentNetworkTime(1000, TIME_SERVER);
    }

    public static long getCurrentNetworkTime(int timeout, String timeServer) throws IOException {
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(timeServer);
        timeClient.setDefaultTimeout(timeout);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        return returnTime;
    }
}
