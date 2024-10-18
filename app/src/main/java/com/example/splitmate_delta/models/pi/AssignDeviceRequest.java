package com.example.splitmate_delta.models.pi;

public class AssignDeviceRequest {
    private String uid;
    private int houseId;

    public AssignDeviceRequest(String uid, int houseId) {
        this.uid = uid;
        this.houseId = houseId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }
}