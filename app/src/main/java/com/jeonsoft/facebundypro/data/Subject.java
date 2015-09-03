package com.jeonsoft.facebundypro.data;

/**
 * Created by WendellWayne on 3/14/2015.
 */
public class Subject {
    private String accessCode;

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private byte[] thumbnail;
    private String timestamp;
    private byte[] template;
    private int id;

    public Subject(int id, String accessCode, String timestamp, byte[] template, byte[] thumbnail) {
        this.id = id;
        this.accessCode = accessCode;
        this.template = template;
        this.timestamp = timestamp;
        this.thumbnail = thumbnail;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public byte[] getTemplate() {
        return template;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
