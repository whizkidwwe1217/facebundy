package com.jeonsoft.facebundypro.data;

/**
 * Created by WendellWayne on 6/30/2015.
 */
public class ItineraryProjectTask {
    public int id;
    public String task;
    public String notes;
    public int projectId;
    public String accessCode;
    public String timeIn;

    public ItineraryProjectTask(int id, String accessCode, String timeIn, String task, String notes, int projectId) {
        this.id = id;
        this.accessCode = accessCode;
        this.timeIn = timeIn;
        this.task = task;
        this.notes = notes;
        this.projectId = projectId;
    }
}
