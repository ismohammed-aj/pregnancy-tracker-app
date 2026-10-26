package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.MedicationReminderRequestDTO;
import com.pfe.mamacareplus.dto.MedicationReminderResponseDTO;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.MedicationReminder;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.MedicationReminderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class MedicationReminderService {

    private final MedicationReminderRepository medicationReminderRepository;
    private final FemmeRepository femmeRepository;
    private final CurrentUserService currentUserService;

    public MedicationReminderService(
            MedicationReminderRepository medicationReminderRepository,
            FemmeRepository femmeRepository,
            CurrentUserService currentUserService
    ) {
        this.medicationReminderRepository = medicationReminderRepository;
        this.femmeRepository = femmeRepository;
        this.currentUserService = currentUserService;
    }

    public List<MedicationReminderResponseDTO> getRemindersByUserId(Integer userId) {
        Femme femme = findFemmeByUserId(userId);
        currentUserService.checkFemmeAccess(femme.getFemmeId());

        return medicationReminderRepository.findByFemmeUserUserIdOrderByReminderTimeAsc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<MedicationReminderResponseDTO> getRemindersByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);

        return medicationReminderRepository.findByFemmeFemmeIdOrderByReminderTimeAsc(femmeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<MedicationReminderResponseDTO> getTodayRemindersByUserId(Integer userId) {
        Femme femme = findFemmeByUserId(userId);
        return getTodayRemindersByFemmeId(femme.getFemmeId());
    }

    public List<MedicationReminderResponseDTO> getTodayRemindersByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);
        LocalDate today = LocalDate.now();

        return medicationReminderRepository.findActiveForDate(femmeId, today)
                .stream()
                .filter(reminder -> isScheduledForDate(reminder, today))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public MedicationReminderResponseDTO createReminder(MedicationReminderRequestDTO request) {
        validateCreateRequest(request);
        currentUserService.checkFemmeAccess(request.getFemmeId());

        Femme femme = femmeRepository.findById(request.getFemmeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found"));

        MedicationReminder reminder = new MedicationReminder();
        reminder.setFemme(femme);
        applyRequest(reminder, request, true);

        return mapToResponse(medicationReminderRepository.save(reminder));
    }

    @Transactional
    public MedicationReminderResponseDTO updateReminder(Integer id, MedicationReminderRequestDTO request) {
        MedicationReminder reminder = findReminderById(id);
        currentUserService.checkFemmeAccess(reminder.getFemme().getFemmeId());

        if (request.getFemmeId() != null && !request.getFemmeId().equals(reminder.getFemme().getFemmeId())) {
            currentUserService.checkFemmeAccess(request.getFemmeId());
            Femme femme = femmeRepository.findById(request.getFemmeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found"));
            reminder.setFemme(femme);
        }

        applyRequest(reminder, request, false);
        validateDateRange(reminder.getStartDate(), reminder.getEndDate());

        return mapToResponse(medicationReminderRepository.save(reminder));
    }

    @Transactional
    public MedicationReminderResponseDTO markDone(Integer id) {
        MedicationReminder reminder = findReminderById(id);
        currentUserService.checkFemmeAccess(reminder.getFemme().getFemmeId());

        reminder.setDone(true);
        reminder.setLastCompletedAt(LocalDateTime.now());

        return mapToResponse(medicationReminderRepository.save(reminder));
    }

    @Transactional
    public void deleteReminder(Integer id) {
        MedicationReminder reminder = findReminderById(id);
        currentUserService.checkFemmeAccess(reminder.getFemme().getFemmeId());

        medicationReminderRepository.delete(reminder);
    }

    private MedicationReminder findReminderById(Integer id) {
        return medicationReminderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication reminder not found"));
    }

    private Femme findFemmeByUserId(Integer userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }

        return femmeRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found for user"));
    }

    private void applyRequest(MedicationReminder reminder, MedicationReminderRequestDTO request, boolean create) {
        if (request.getMedicationName() != null) {
            reminder.setMedicationName(cleanRequiredText(request.getMedicationName(), "Medication name"));
        } else if (create) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medication name is required");
        }

        if (request.getDosage() != null) {
            reminder.setDosage(cleanRequiredText(request.getDosage(), "Dosage"));
        } else if (create) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dosage is required");
        }

        if (request.getFrequency() != null) {
            reminder.setFrequency(cleanRequiredText(request.getFrequency(), "Frequency"));
        } else if (create) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Frequency is required");
        }

        if (request.getDaysOfWeek() != null) {
            reminder.setDaysOfWeek(request.getDaysOfWeek().trim());
        }

        if (request.getReminderTime() != null) {
            reminder.setReminderTime(request.getReminderTime());
        } else if (create) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reminder time is required");
        }

        if (request.getStartDate() != null) {
            reminder.setStartDate(request.getStartDate());
        } else if (create) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date is required");
        }

        if (request.getEndDate() != null || create) {
            reminder.setEndDate(request.getEndDate());
        }

        if (request.getNotes() != null) {
            reminder.setNotes(request.getNotes().trim());
        }

        if (request.getActive() != null) {
            reminder.setActive(request.getActive());
        } else if (create) {
            reminder.setActive(true);
        }

        if (request.getDone() != null) {
            reminder.setDone(request.getDone());
            if (!request.getDone()) {
                reminder.setLastCompletedAt(null);
            }
        } else if (create) {
            reminder.setDone(false);
        }
    }

    private void validateCreateRequest(MedicationReminderRequestDTO request) {
        if (request.getFemmeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Femme ID is required");
        }

        validateDateRange(request.getStartDate(), request.getEndDate());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
    }

    private String cleanRequiredText(String value, String fieldName) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return cleaned;
    }

    private boolean isScheduledForDate(MedicationReminder reminder, LocalDate date) {
        String frequency = normalize(reminder.getFrequency());
        if (!frequency.contains("SPECIFIC") && !frequency.contains("DAYS") && !frequency.contains("JOURS")) {
            return true;
        }

        String daysOfWeek = reminder.getDaysOfWeek();
        if (daysOfWeek == null || daysOfWeek.isBlank()) {
            return true;
        }

        String normalizedDays = normalize(daysOfWeek);
        DayOfWeek day = date.getDayOfWeek();
        String englishDay = day.name();
        String englishShortDay = englishDay.substring(0, 3);
        String frenchDay = frenchDayName(day);

        return normalizedDays.contains(englishDay)
                || normalizedDays.contains(englishShortDay)
                || normalizedDays.contains(frenchDay);
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String frenchDayName(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "LUNDI";
            case TUESDAY -> "MARDI";
            case WEDNESDAY -> "MERCREDI";
            case THURSDAY -> "JEUDI";
            case FRIDAY -> "VENDREDI";
            case SATURDAY -> "SAMEDI";
            case SUNDAY -> "DIMANCHE";
        };
    }

    private MedicationReminderResponseDTO mapToResponse(MedicationReminder reminder) {
        return new MedicationReminderResponseDTO(
                reminder.getId(),
                reminder.getFemme().getFemmeId(),
                reminder.getFemme().getUsername(),
                reminder.getMedicationName(),
                reminder.getDosage(),
                reminder.getFrequency(),
                reminder.getDaysOfWeek(),
                reminder.getReminderTime(),
                reminder.getStartDate(),
                reminder.getEndDate(),
                reminder.getNotes(),
                reminder.getActive(),
                reminder.getDone(),
                reminder.getLastCompletedAt(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt()
        );
    }
}
