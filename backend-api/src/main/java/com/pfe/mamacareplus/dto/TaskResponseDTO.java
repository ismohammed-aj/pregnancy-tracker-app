package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;

public class TaskResponseDTO {

    private Integer taskId;
    private Integer femmeId;
    private String username;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Boolean isCompleted;
    private LocalDateTime createdAt;

    public TaskResponseDTO() {}

    public TaskResponseDTO(
            Integer taskId,
            Integer femmeId,
            String username,
            String title,
            String description,
            LocalDateTime dueDate,
            Boolean isCompleted,
            LocalDateTime createdAt
    ) {
        this.taskId = taskId;
        this.femmeId = femmeId;
        this.username = username;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public Integer getTaskId() { return taskId; }
    public Integer getFemmeId() { return femmeId; }
    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Boolean getIsCompleted() { return isCompleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}