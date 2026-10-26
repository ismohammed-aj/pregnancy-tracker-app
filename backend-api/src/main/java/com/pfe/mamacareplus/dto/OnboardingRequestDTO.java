package com.pfe.mamacareplus.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class OnboardingRequestDTO {

    private Integer birthYear;
    private Integer heightCm;
    @JsonProperty("weightKg")
    @JsonAlias({"weight", "poids", "currentWeight"})
    private Double weightKg;
    private String currentStatus;
    private LocalDate lastPeriodDate;
    private LocalDate dueDate;
    private Boolean firstPregnancy;
    private String medicalFollowUp;
    private String tryingDuration;
    private String cycleTracking;
    private String previousPregnancyProblem;
    private List<String> previousPregnancyProblemTypes;
    private List<String> currentWarningSymptoms;
    private List<String> chronicDiseases;
    private Boolean takesMedication;
    private String medicationName;
    private LocalTime medicationTime;
    private String folicAcid;
    private String smokingStatus;
    private Boolean remindersEnabled;
    private LocalTime preferredReminderTime;
    private String wantsAppointmentReminder;
    private LocalDateTime nextAppointmentDateTime;

    public OnboardingRequestDTO() {}

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public LocalDate getLastPeriodDate() { return lastPeriodDate; }
    public void setLastPeriodDate(LocalDate lastPeriodDate) { this.lastPeriodDate = lastPeriodDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Boolean getFirstPregnancy() { return firstPregnancy; }
    public void setFirstPregnancy(Boolean firstPregnancy) { this.firstPregnancy = firstPregnancy; }

    public String getMedicalFollowUp() { return medicalFollowUp; }
    public void setMedicalFollowUp(String medicalFollowUp) { this.medicalFollowUp = medicalFollowUp; }

    public String getTryingDuration() { return tryingDuration; }
    public void setTryingDuration(String tryingDuration) { this.tryingDuration = tryingDuration; }

    public String getCycleTracking() { return cycleTracking; }
    public void setCycleTracking(String cycleTracking) { this.cycleTracking = cycleTracking; }

    public String getPreviousPregnancyProblem() { return previousPregnancyProblem; }
    public void setPreviousPregnancyProblem(String previousPregnancyProblem) {
        this.previousPregnancyProblem = previousPregnancyProblem;
    }

    public List<String> getPreviousPregnancyProblemTypes() { return previousPregnancyProblemTypes; }
    public void setPreviousPregnancyProblemTypes(List<String> previousPregnancyProblemTypes) {
        this.previousPregnancyProblemTypes = previousPregnancyProblemTypes;
    }

    public List<String> getCurrentWarningSymptoms() { return currentWarningSymptoms; }
    public void setCurrentWarningSymptoms(List<String> currentWarningSymptoms) {
        this.currentWarningSymptoms = currentWarningSymptoms;
    }

    public List<String> getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(List<String> chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public Boolean getTakesMedication() { return takesMedication; }
    public void setTakesMedication(Boolean takesMedication) { this.takesMedication = takesMedication; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public LocalTime getMedicationTime() { return medicationTime; }
    public void setMedicationTime(LocalTime medicationTime) { this.medicationTime = medicationTime; }

    public String getFolicAcid() { return folicAcid; }
    public void setFolicAcid(String folicAcid) { this.folicAcid = folicAcid; }

    public String getSmokingStatus() { return smokingStatus; }
    public void setSmokingStatus(String smokingStatus) { this.smokingStatus = smokingStatus; }

    public Boolean getRemindersEnabled() { return remindersEnabled; }
    public void setRemindersEnabled(Boolean remindersEnabled) { this.remindersEnabled = remindersEnabled; }

    public LocalTime getPreferredReminderTime() { return preferredReminderTime; }
    public void setPreferredReminderTime(LocalTime preferredReminderTime) { this.preferredReminderTime = preferredReminderTime; }

    public String getWantsAppointmentReminder() { return wantsAppointmentReminder; }
    public void setWantsAppointmentReminder(String wantsAppointmentReminder) { this.wantsAppointmentReminder = wantsAppointmentReminder; }

    public LocalDateTime getNextAppointmentDateTime() { return nextAppointmentDateTime; }
    public void setNextAppointmentDateTime(LocalDateTime nextAppointmentDateTime) { this.nextAppointmentDateTime = nextAppointmentDateTime; }
}
