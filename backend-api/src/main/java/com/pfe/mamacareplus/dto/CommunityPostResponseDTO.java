package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;

public class CommunityPostResponseDTO {

    private Integer postId;
    private Integer femmeId;
    private String username;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommunityPostResponseDTO() {}

    public CommunityPostResponseDTO(
            Integer postId,
            Integer femmeId,
            String username,
            String content,
            String imageUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.postId = postId;
        this.femmeId = femmeId;
        this.username = username;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getPostId() {
        return postId;
    }

    public Integer getFemmeId() {
        return femmeId;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}