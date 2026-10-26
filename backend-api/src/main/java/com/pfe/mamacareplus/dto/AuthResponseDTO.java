package com.pfe.mamacareplus.dto;

public class AuthResponseDTO {

    private String token;
    private Integer userId;
    private String email;
    private String role;
    private Integer femmeId;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, Integer userId, String email, String role, Integer femmeId) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.femmeId = femmeId;
    }

    public String getToken() {
        return token;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Integer getFemmeId() {
        return femmeId;
    }
}