package com.example.splitmate_delta.models.signup;

public class SignupRequest {

    private String name;
    private String password;
    private String email;
    private int houseId;
    private String role;
    private String image;

    // 构造函数包括所有字段
    public SignupRequest(String name, String password, String email, int houseId, String role, String image) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.houseId = houseId;
        this.role = role;
        this.image = image;
    }

    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}