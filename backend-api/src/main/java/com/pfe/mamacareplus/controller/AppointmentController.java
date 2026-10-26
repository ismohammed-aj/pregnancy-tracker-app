package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.AppointmentRequestDTO;
import com.pfe.mamacareplus.dto.AppointmentResponseDTO;
import com.pfe.mamacareplus.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{appointmentId}")
    public AppointmentResponseDTO getAppointmentById(@PathVariable Integer appointmentId) {
        return appointmentService.getAppointmentById(appointmentId);
    }

    @GetMapping("/femme/{femmeId}")
    public List<AppointmentResponseDTO> getAppointmentsByFemmeId(@PathVariable Integer femmeId) {
        return appointmentService.getAppointmentsByFemmeId(femmeId);
    }

    @GetMapping("/femme/{femmeId}/status/{status}")
    public List<AppointmentResponseDTO> getAppointmentsByFemmeIdAndStatus(
            @PathVariable Integer femmeId,
            @PathVariable String status
    ) {
        return appointmentService.getAppointmentsByFemmeIdAndStatus(femmeId, status);
    }

    @PostMapping
    public AppointmentResponseDTO createAppointment(@Valid @RequestBody AppointmentRequestDTO request) {
        return appointmentService.createAppointment(request);
    }

    @PutMapping("/{appointmentId}")
    public AppointmentResponseDTO updateAppointment(
            @PathVariable Integer appointmentId,
            @Valid @RequestBody AppointmentRequestDTO request
    ) {
        return appointmentService.updateAppointment(appointmentId, request);
    }

    @PatchMapping("/{appointmentId}/status/{status}")
    public AppointmentResponseDTO updateStatus(
            @PathVariable Integer appointmentId,
            @PathVariable String status
    ) {
        return appointmentService.updateStatus(appointmentId, status);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Integer appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}