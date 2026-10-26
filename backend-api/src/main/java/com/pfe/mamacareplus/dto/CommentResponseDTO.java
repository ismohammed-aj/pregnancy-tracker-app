package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;

public class CommentResponseDTO {

    private Integer commentId;
    private Integer postId;
    private Integer femmeId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponseDTO() {}

    public CommentResponseDTO(
            Integer commentId,
            Integer postId,
            Integer femmeId,
            String username,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.commentId = commentId;
        this.postId = postId;
        this.femmeId = femmeId;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getCommentId() {
        return commentId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}