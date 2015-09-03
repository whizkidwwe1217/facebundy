package com.jeonsoft.facebundypro.utils;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.time.TimeTCPClient;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Wendell Wayne on 12/23/2014.
 */
public final class DateTimeUtils {
    public static final String TIME_SERVER = "pool.ntp.org"; //"time-a.nist.gov";

    public static long getCurrentNetworkTime() throws IOException {
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        timeClient.setDefaultTimeout(10000);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        return returnTime;
    }

    public static long getCurrentNetworkTimeFromTcp() throws IOException{
        TimeTCPClient client = null;

        try {
            client = new TimeTCPClient();
            client.setDefaultTimeout(60000);
            client.connect("nist.time.nosc.us");
            return client.getTime();
        } catch (Exception ex) {

        } finally {
            if (client != null)
                client.disconnect();
        }
        return 0;
    }

    public static Calendar DateToCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

}
