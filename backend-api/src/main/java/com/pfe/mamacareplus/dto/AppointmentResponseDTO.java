package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppointmentResponseDTO {
    private Integer appointmentId;
    @NotNull(message = "Femme ID is required")
    private Integer femmeId;
    private String username;
    @NotBlank(message = "Title is required")
    private String title;
    @NotNull(message = "Appointment date is required")
    private LocalDateTime appointmentDate;
    private String location;
    private String doctorName;
    private String notes;
    private String status;
    private LocalDateTime createdAt;

    public AppointmentResponseDTO() {}

    public AppointmentResponseDTO(
            Integer appointmentId,
            Integer femmeId,
            String username,
            String title,
            LocalDateTime appointmentDate,
            String location,
            String doctorName,
            String notes,
            String status,
            LocalDateTime createdAt
    ) {
        this.appointmentId = appointmentId;
        this.femmeId = femmeId;
        this.username = username;
        this.title = title;
        this.appointmentDate = appointmentDate;
        this.location = location;
        this.doctorName = doctorName;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getAppointmentId() { return appointmentId; }
    public Integer getFemmeId() { return femmeId; }
    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public String getLocation() { return location; }
    public String getDoctorName() { return doctorName; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}