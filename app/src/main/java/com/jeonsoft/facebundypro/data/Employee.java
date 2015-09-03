package com.jeonsoft.facebundypro.data;

/**
 * Created by WendellWayne on 5/12/2015.
 */
public class Employee {
    private String accessCode;
    private String name;
    private String nickName;
    private String lastName;
    private String firstName;
    private int id;

    public Employee(int id, String accessCode, String name, String nickName, String lastName, String firstName) {
        this.id = id;
        this.accessCode = accessCode;
        this.name = name;
        this.nickName = nickName;
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
}
