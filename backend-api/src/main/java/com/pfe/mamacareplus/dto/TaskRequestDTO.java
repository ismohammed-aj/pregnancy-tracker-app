package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequestDTO {
  
    @NotNull(message = "Femme ID is required")
    private Integer femmeId;
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Boolean isCompleted;

    public TaskRequestDTO() {}

    public Integer getFemmeId() { return femmeId; }
    public void setFemmeId(Integer femmeId) { this.femmeId = femmeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean completed) { isCompleted = completed; }
}