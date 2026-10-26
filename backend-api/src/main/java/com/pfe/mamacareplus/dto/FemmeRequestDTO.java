package com.pfe.mamacareplus.dto;

import java.time.LocalDate;

public class FemmeRequestDTO {

    private Integer userId;
    private String username;
    private LocalDate birthDate;
    private String avatar;

    public FemmeRequestDTO() {}

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
