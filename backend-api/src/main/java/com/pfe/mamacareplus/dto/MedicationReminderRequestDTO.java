package com.pfe.mamacareplus.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class MedicationReminderRequestDTO {

    private Integer femmeId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String daysOfWeek;
    private LocalTime reminderTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private Boolean active;
    private Boolean done;

    public MedicationReminderRequestDTO() {}

    public Integer getFemmeId() { return femmeId; }
    public void setFemmeId(Integer femmeId) { this.femmeId = femmeId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getDone() { return done; }
    public void setDone(Boolean done) { this.done = done; }
}
