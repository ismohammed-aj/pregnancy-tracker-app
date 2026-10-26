package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.AppointmentRequestDTO;
import com.pfe.mamacareplus.dto.AppointmentResponseDTO;
import com.pfe.mamacareplus.entity.Appointment;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.repository.AppointmentRepository;
import com.pfe.mamacareplus.repository.FemmeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final FemmeRepository femmeRepository;
    private final CurrentUserService currentUserService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            FemmeRepository femmeRepository,
            CurrentUserService currentUserService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.femmeRepository = femmeRepository;
        this.currentUserService = currentUserService;
    }

    public List<AppointmentResponseDTO> getAllAppointments() {
        if (!currentUserService.isAdmin()) {
            Integer currentFemmeId = currentUserService.getCurrentFemmeId();

            return appointmentRepository.findByFemmeFemmeIdOrderByAppointmentDateAsc(currentFemmeId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AppointmentResponseDTO getAppointmentById(Integer appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);
        currentUserService.checkFemmeAccess(appointment.getFemme().getFemmeId());

        return mapToResponse(appointment);
    }

    public List<AppointmentResponseDTO> getAppointmentsByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);

        return appointmentRepository.findByFemmeFemmeIdOrderByAppointmentDateAsc(femmeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentResponseDTO> getAppointmentsByFemmeIdAndStatus(Integer femmeId, String status) {
        currentUserService.checkFemmeAccess(femmeId);

        return appointmentRepository.findByFemmeFemmeIdAndStatusOrderByAppointmentDateAsc(femmeId, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {
        validateCreateRequest(request);
        currentUserService.checkFemmeAccess(request.getFemmeId());

        Femme femme = femmeRepository.findById(request.getFemmeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found"));

        Appointment appointment = new Appointment();
        appointment.setFemme(femme);
        appointment.setTitle(request.getTitle());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setLocation(request.getLocation());
        appointment.setDoctorName(request.getDoctorName());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(normalizeStatus(request.getStatus()));

        return mapToResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponseDTO updateAppointment(Integer appointmentId, AppointmentRequestDTO request) {
        Appointment appointment = findAppointmentById(appointmentId);
        currentUserService.checkFemmeAccess(appointment.getFemme().getFemmeId());

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            appointment.setTitle(request.getTitle());
        }

        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }

        if (request.getLocation() != null) {
            appointment.setLocation(request.getLocation());
        }

        if (request.getDoctorName() != null) {
            appointment.setDoctorName(request.getDoctorName());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            appointment.setStatus(normalizeStatus(request.getStatus()));
        }

        return mapToResponse(appointmentRepository.save(appointment));
    }

    public AppointmentResponseDTO updateStatus(Integer appointmentId, String status) {
        Appointment appointment = findAppointmentById(appointmentId);
        currentUserService.checkFemmeAccess(appointment.getFemme().getFemmeId());

        appointment.setStatus(normalizeStatus(status));
        return mapToResponse(appointmentRepository.save(appointment));
    }

    public void deleteAppointment(Integer appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);
        currentUserService.checkFemmeAccess(appointment.getFemme().getFemmeId());

        appointmentRepository.delete(appointment);
    }

    private Appointment findAppointmentById(Integer appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private void validateCreateRequest(AppointmentRequestDTO request) {
        if (request.getFemmeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Femme ID is required");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        if (request.getAppointmentDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date is required");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "pending";
        }

        String normalized = status.trim().toLowerCase();

        if (!normalized.equals("pending")
                && !normalized.equals("completed")
                && !normalized.equals("cancelled")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be pending, completed, or cancelled");
        }

        return normalized;
    }

    private AppointmentResponseDTO mapToResponse(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getAppointmentId(),
                appointment.getFemme().getFemmeId(),
                appointment.getFemme().getUsername(),
                appointment.getTitle(),
                appointment.getAppointmentDate(),
                appointment.getLocation(),
                appointment.getDoctorName(),
                appointment.getNotes(),
                appointment.getStatus(),
                appointment.getCreatedAt()
        );
    }
}