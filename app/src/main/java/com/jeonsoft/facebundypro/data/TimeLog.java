package com.jeonsoft.facebundypro.data;

/**
 * Created by Wendell Wayne on 1/13/2015.
 */
public class TimeLog {
    private int id;
    private String accesscode;
    private String time;
    private String type;
    private String filename;
    private int edition;
    private double gpsLatitude;
    private double gpsLongitude;
    private String gpsLocation;

    public TimeLog(int id, String accesscode, String time, String type, String filename, int edition, double latitude, double longitude) {
        this.accesscode = accesscode;
        this.time = time;
        this.type = type;
        this.filename = filename;
        this.edition = edition;
        this.gpsLatitude = latitude;
        this.gpsLongitude = longitude;
        this.id = id;
    }

    public void setGpsLatitude(double latitude) {
        this.gpsLatitude = latitude;
    }

    public void setGpsLongitude(double longitude) {
        this.gpsLongitude = longitude;
    }

    public double getGpsLatitude() {
        return gpsLatitude;
    }

    public double getGpsLongitude() {
        return gpsLongitude;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccesscode(String accesscode) {
        this.accesscode = accesscode;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }

    public int getId() {
        return id;
    }

    public String getAccessCode() {
        return accesscode;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getFilename() {
        return filename;
    }

    public int getEdition() {
        return edition;
    }

    @Override
    public String toString() {
        return accesscode + ", " + time + ", " + type + ", " + filename + ", " + String.valueOf(edition);
    }
}
