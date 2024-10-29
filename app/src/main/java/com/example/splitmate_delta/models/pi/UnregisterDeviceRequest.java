package com.example.splitmate_delta.models.pi;

public class UnregisterDeviceRequest {
    private String uid;

    public UnregisterDeviceRequest(String uid) {
        this.uid = uid;
    }

    // Getter and Setter
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}