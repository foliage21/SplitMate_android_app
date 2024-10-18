package com.example.splitmate_delta.models.ble;

public class DeviceStatusUpdate {
    private String userId;
    private String macAddress;
    private String state;
    private String timestamp;

    public DeviceStatusUpdate(String userId, String macAddress, String state, String timestamp) {
        this.userId = userId;
        this.macAddress = macAddress;
        this.state = state;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getState() {
        return state;
    }

    public String getTimestamp() {
        return timestamp;
    }
}