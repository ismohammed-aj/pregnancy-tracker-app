package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.PregnancyProfileRequestDTO;
import com.pfe.mamacareplus.dto.PregnancyProfileResponseDTO;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.PregnancyProfile;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.PregnancyProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PregnancyProfileService {

    private static final long PREGNANCY_DURATION_DAYS = 280L;
    private static final long MIN_REASONABLE_PREGNANCY_DAYS = 240L;
    private static final long MAX_REASONABLE_PREGNANCY_DAYS = 320L;

    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final FemmeRepository femmeRepository;
    private final CurrentUserService currentUserService;

    public PregnancyProfileService(
            PregnancyProfileRepository pregnancyProfileRepository,
            FemmeRepository femmeRepository,
            CurrentUserService currentUserService
    ) {
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.femmeRepository = femmeRepository;
        this.currentUserService = currentUserService;
    }

    public List<PregnancyProfileResponseDTO> getAllProfiles() {
        if (!currentUserService.isAdmin()) {
            Integer currentFemmeId = currentUserService.getCurrentFemmeId();

            return pregnancyProfileRepository.findByFemmeFemmeId(currentFemmeId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return pregnancyProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PregnancyProfileResponseDTO getProfileById(Integer profileId) {
        PregnancyProfile profile = findProfileById(profileId);
        currentUserService.checkFemmeAccess(profile.getFemme().getFemmeId());

        return mapToResponse(profile);
    }

    public PregnancyProfileResponseDTO getProfileByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);

        PregnancyProfile profile = pregnancyProfileRepository.findByFemmeFemmeId(femmeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregnancy profile not found for this femme"));

        return mapToResponse(profile);
    }

    public PregnancyProfileResponseDTO createProfile(PregnancyProfileRequestDTO request) {
        validateCreateRequest(request);
        currentUserService.checkFemmeAccess(request.getFemmeId());

        if (pregnancyProfileRepository.existsByFemmeFemmeId(request.getFemmeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This femme already has a pregnancy profile");
        }

        Femme femme = femmeRepository.findById(request.getFemmeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found"));

        PregnancyProfile profile = new PregnancyProfile();
        profile.setFemme(femme);
        profile.setStartDate(request.getStartDate());
        profile.setDueDate(request.getDueDate());
        profile.setHealthStatus(request.getHealthStatus());
        profile.setNotes(request.getNotes());

        return mapToResponse(pregnancyProfileRepository.save(profile));
    }

    public PregnancyProfileResponseDTO updateProfile(Integer profileId, PregnancyProfileRequestDTO request) {
        PregnancyProfile profile = findProfileById(profileId);
        currentUserService.checkFemmeAccess(profile.getFemme().getFemmeId());

        LocalDate newStartDate = request.getStartDate() != null ? request.getStartDate() : profile.getStartDate();
        LocalDate newDueDate = request.getDueDate() != null ? request.getDueDate() : profile.getDueDate();

        if (!newDueDate.isAfter(newStartDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be after start date");
        }

        if (request.getStartDate() != null) {
            profile.setStartDate(request.getStartDate());
        }

        if (request.getDueDate() != null) {
            profile.setDueDate(request.getDueDate());
        }

        if (request.getHealthStatus() != null) {
            profile.setHealthStatus(request.getHealthStatus());
        }

        if (request.getNotes() != null) {
            profile.setNotes(request.getNotes());
        }

        return mapToResponse(pregnancyProfileRepository.save(profile));
    }

    public void deleteProfile(Integer profileId) {
        PregnancyProfile profile = findProfileById(profileId);
        currentUserService.checkFemmeAccess(profile.getFemme().getFemmeId());

        pregnancyProfileRepository.delete(profile);
    }

    private PregnancyProfile findProfileById(Integer profileId) {
        return pregnancyProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregnancy profile not found"));
    }

    private void validateCreateRequest(PregnancyProfileRequestDTO request) {
        if (request.getFemmeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Femme ID is required");
        }

        if (request.getStartDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date is required");
        }

        if (request.getDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date is required");
        }

        if (!request.getDueDate().isAfter(request.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date must be after start date");
        }
    }

    private PregnancyProfileResponseDTO mapToResponse(PregnancyProfile profile) {
        LocalDate startDate = profile.getStartDate();
        LocalDate dueDate = resolveCalculationDueDate(startDate, profile.getDueDate());

        return new PregnancyProfileResponseDTO(
                profile.getPregnancyProfileId(),
                profile.getFemme().getFemmeId(),
                profile.getFemme().getUsername(),
                startDate,
                dueDate,
                calculateCurrentWeek(startDate),
                calculateCurrentMonth(startDate),
                calculateRemainingDays(dueDate),
                calculateProgressPercentage(startDate, dueDate),
                profile.getHealthStatus(),
                profile.getNotes(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private Integer calculateCurrentWeek(LocalDate startDate) {
        if (startDate == null) {
            return 1;
        }

        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        int week = (int) (days / 7) + 1;

        if (week < 1) return 1;
        if (week > 42) return 42;

        return week;
    }

    private Integer calculateCurrentMonth(LocalDate startDate) {
        int week = calculateCurrentWeek(startDate);

        if (week <= 4) return 1;
        if (week <= 8) return 2;
        if (week <= 13) return 3;
        if (week <= 17) return 4;
        if (week <= 22) return 5;
        if (week <= 27) return 6;
        if (week <= 31) return 7;
        if (week <= 35) return 8;
        return 9;
    }

    private Integer calculateRemainingDays(LocalDate dueDate) {
        if (dueDate == null) {
            return 0;
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        return (int) Math.max(days, 0);
    }

    private Double calculateProgressPercentage(LocalDate startDate, LocalDate dueDate) {
        if (startDate == null || dueDate == null) {
            return 0.0;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, dueDate);
        long passedDays = ChronoUnit.DAYS.between(startDate, LocalDate.now());

        if (totalDays <= 0) return 0.0;

        double progress = (passedDays * 100.0) / totalDays;

        if (progress < 0) return 0.0;
        if (progress > 100) return 100.0;

        return Math.round(progress * 100.0) / 100.0;
    }

    private LocalDate resolveCalculationDueDate(LocalDate startDate, LocalDate dueDate) {
        if (startDate == null) {
            return dueDate;
        }

        if (dueDate == null) {
            return startDate.plusDays(PREGNANCY_DURATION_DAYS);
        }

        long pregnancyDays = ChronoUnit.DAYS.between(startDate, dueDate);
        if (pregnancyDays < MIN_REASONABLE_PREGNANCY_DAYS
                || pregnancyDays > MAX_REASONABLE_PREGNANCY_DAYS) {
            return startDate.plusDays(PREGNANCY_DURATION_DAYS);
        }

        return dueDate;
    }
}
