package com.jeonsoft.facebundypro.data;

/**
 * Created by WendellWayne on 6/26/2015.
 */
public class ItineraryProject {
    public int id;
    public String timeIn;
    public String accessCode;
    public String timeOut;
    public double latitude;
    public double longitude;
    public String location;
    public String project;
    public int status;
    public String dateCreated;

    public ItineraryProject(int id, String accessCode, String project,  String timeIn, String timeOut, double latitude, double longitude, String location) {
        this(id, accessCode, project, timeIn, timeOut, latitude, longitude, location, "");
    }

    public ItineraryProject(int id, String accessCode, String project,  String timeIn, String timeOut, double latitude, double longitude, String location, String dateCreated) {
        this.id = id;
        this.timeIn = timeIn;
        this.project = project;
        this.accessCode = accessCode;
        this.timeOut = timeOut;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.dateCreated = dateCreated;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
