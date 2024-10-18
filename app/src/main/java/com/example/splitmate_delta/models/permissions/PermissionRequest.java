package com.example.splitmate_delta.models.permissions;

public class PermissionRequest {
    private int userId;
    private String macAddress;

    public PermissionRequest(int userId, String macAddress) {
        this.userId = userId;
        this.macAddress = macAddress;
    }

}