package com.pfe.mamacareplus.dto;

public class UserResponseDTO {

    private Integer userId;
    private String email;
    private String role;
    private Boolean isActive;
    private Boolean isVerified;

    public UserResponseDTO() {}

    public UserResponseDTO(Integer userId, String email, String role, Boolean isActive, Boolean isVerified) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.isVerified = isVerified;
    }

    public Integer getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getIsVerified() { return isVerified; }
}