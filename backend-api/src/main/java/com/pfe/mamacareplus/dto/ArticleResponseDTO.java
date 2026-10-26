package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;

public class ArticleResponseDTO {

    private Integer articleId;
    private String title;
    private String category;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String videoTitle;
    private Integer readingTimeMinutes;
    private LocalDateTime createdAt;

    public ArticleResponseDTO() {}

    public ArticleResponseDTO(
            Integer articleId,
            String title,
            String category,
            String content,
            String imageUrl,
            String videoUrl,
            String videoTitle,
            Integer readingTimeMinutes,
            LocalDateTime createdAt
    ) {
        this.articleId = articleId;
        this.title = title;
        this.category = category;
        this.content = content;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.videoTitle = videoTitle;
        this.readingTimeMinutes = readingTimeMinutes;
        this.createdAt = createdAt;
    }

    public Integer getArticleId() { return articleId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public String getVideoUrl() { return videoUrl; }
    public String getVideoTitle() { return videoTitle; }
    public Integer getReadingTimeMinutes() { return readingTimeMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
