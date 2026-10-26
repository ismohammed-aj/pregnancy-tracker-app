package com.pfe.mamacareplus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FemmeResponseDTO {

    private Integer femmeId;
    private Integer userId;
    private String email;
    private String username;
    private LocalDate birthDate;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FemmeResponseDTO() {}

    public FemmeResponseDTO(
            Integer femmeId,
            Integer userId,
            String email,
            String username,
            LocalDate birthDate,
            String avatar,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.femmeId = femmeId;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.birthDate = birthDate;
        this.avatar = avatar;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getFemmeId() { return femmeId; }
    public Integer getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getAvatar() { return avatar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}