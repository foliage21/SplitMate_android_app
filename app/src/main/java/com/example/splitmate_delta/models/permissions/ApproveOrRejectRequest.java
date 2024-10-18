package com.example.splitmate_delta.models.permissions;

public class ApproveOrRejectRequest {
    private int userId;
    private String macAddress;
    private boolean approve;

    public ApproveOrRejectRequest(int userId, String macAddress, boolean approve) {
        this.userId = userId;
        this.macAddress = macAddress;
        this.approve = approve;
    }

}