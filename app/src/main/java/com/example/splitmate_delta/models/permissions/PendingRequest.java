package com.example.splitmate_delta.models.permissions;

public class PendingRequest {
    private int id;
    private String deviceName;
    private String macAddress;
    private int userId;
    private String permissionStatus;


    public int getId() {
        return id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getUserId() {
        return userId;
    }

    public String getPermissionStatus() {
        return permissionStatus;
    }

}