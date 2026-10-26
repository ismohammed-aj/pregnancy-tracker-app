package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.MedicationReminderRequestDTO;
import com.pfe.mamacareplus.dto.MedicationReminderResponseDTO;
import com.pfe.mamacareplus.service.MedicationReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medication-reminders")
@CrossOrigin(origins = "*")
public class MedicationReminderController {

    private final MedicationReminderService medicationReminderService;

    public MedicationReminderController(MedicationReminderService medicationReminderService) {
        this.medicationReminderService = medicationReminderService;
    }

    @PostMapping
    public MedicationReminderResponseDTO createReminder(
            @Valid @RequestBody MedicationReminderRequestDTO request
    ) {
        return medicationReminderService.createReminder(request);
    }

    @GetMapping("/user/{userId}")
    public List<MedicationReminderResponseDTO> getRemindersByUserId(@PathVariable Integer userId) {
        return medicationReminderService.getRemindersByUserId(userId);
    }

    @GetMapping("/femme/{femmeId}")
    public List<MedicationReminderResponseDTO> getRemindersByFemmeId(@PathVariable Integer femmeId) {
        return medicationReminderService.getRemindersByFemmeId(femmeId);
    }

    @GetMapping("/today/{userId}")
    public List<MedicationReminderResponseDTO> getTodayRemindersByUserId(@PathVariable Integer userId) {
        return medicationReminderService.getTodayRemindersByUserId(userId);
    }

    @GetMapping("/today/femme/{femmeId}")
    public List<MedicationReminderResponseDTO> getTodayRemindersByFemmeId(@PathVariable Integer femmeId) {
        return medicationReminderService.getTodayRemindersByFemmeId(femmeId);
    }

    @PutMapping("/{id}")
    public MedicationReminderResponseDTO updateReminder(
            @PathVariable Integer id,
            @Valid @RequestBody MedicationReminderRequestDTO request
    ) {
        return medicationReminderService.updateReminder(id, request);
    }

    @PatchMapping("/{id}/done")
    public MedicationReminderResponseDTO markReminderDone(@PathVariable Integer id) {
        return medicationReminderService.markDone(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Integer id) {
        medicationReminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }
}
