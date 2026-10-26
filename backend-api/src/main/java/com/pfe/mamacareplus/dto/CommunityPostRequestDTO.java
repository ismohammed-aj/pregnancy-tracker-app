package com.pfe.mamacareplus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class CommunityPostRequestDTO {

    @NotBlank(message = "Post content is required")
    private String content;
    private String imageUrl;

    public CommunityPostRequestDTO() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}