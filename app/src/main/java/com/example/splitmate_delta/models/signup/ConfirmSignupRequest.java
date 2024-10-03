package com.example.splitmate_delta.models.signup;

public class ConfirmSignupRequest {

    private String username;
    private String confirmationCode;

    public ConfirmSignupRequest(String username, String confirmationCode) {
        this.username = username;
        this.confirmationCode = confirmationCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }
}