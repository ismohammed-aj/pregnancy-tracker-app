package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;

public class AppointmentRequestDTO {

    private Integer femmeId;
    private String title;
    private LocalDateTime appointmentDate;
    private String location;
    private String doctorName;
    private String notes;
    private String status;

    public AppointmentRequestDTO() {}

    public Integer getFemmeId() { return femmeId; }
    public void setFemmeId(Integer femmeId) { this.femmeId = femmeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}