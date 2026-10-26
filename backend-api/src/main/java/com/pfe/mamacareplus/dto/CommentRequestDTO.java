package com.pfe.mamacareplus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CommentRequestDTO {

    @NotNull(message = "Post ID is required")
    private Integer postId;
    @NotBlank(message = "Comment content is required")
    private String content;

    public CommentRequestDTO() {}

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}