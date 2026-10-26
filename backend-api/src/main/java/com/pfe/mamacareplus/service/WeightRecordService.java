package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.WeightRecordRequestDTO;
import com.pfe.mamacareplus.dto.WeightRecordResponseDTO;
import com.pfe.mamacareplus.entity.PregnancyProfile;
import com.pfe.mamacareplus.entity.WeightRecord;
import com.pfe.mamacareplus.repository.PregnancyProfileRepository;
import com.pfe.mamacareplus.repository.WeightRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WeightRecordService {

    private final WeightRecordRepository weightRecordRepository;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final CurrentUserService currentUserService;

    public WeightRecordService(
            WeightRecordRepository weightRecordRepository,
            PregnancyProfileRepository pregnancyProfileRepository,
            CurrentUserService currentUserService
    ) {
        this.weightRecordRepository = weightRecordRepository;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.currentUserService = currentUserService;
    }

    public List<WeightRecordResponseDTO> getAllWeightRecords() {
        if (!currentUserService.isAdmin()) {
            Integer currentFemmeId = currentUserService.getCurrentFemmeId();

            return weightRecordRepository.findAll()
                    .stream()
                    .filter(weightRecord ->
                            weightRecord.getPregnancyProfile()
                                    .getFemme()
                                    .getFemmeId()
                                    .equals(currentFemmeId)
                    )
                    .map(this::mapToResponse)
                    .toList();
        }

        return weightRecordRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public WeightRecordResponseDTO getWeightRecordById(Integer weightRecordId) {
        WeightRecord weightRecord = findWeightRecordById(weightRecordId);
        currentUserService.checkFemmeAccess(
                weightRecord.getPregnancyProfile().getFemme().getFemmeId()
        );

        return mapToResponse(weightRecord);
    }

    public List<WeightRecordResponseDTO> getWeightRecordsByPregnancyProfileId(Integer pregnancyProfileId) {
        PregnancyProfile pregnancyProfile = pregnancyProfileRepository.findById(pregnancyProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregnancy profile not found"));

        currentUserService.checkFemmeAccess(pregnancyProfile.getFemme().getFemmeId());

        return weightRecordRepository
                .findByPregnancyProfilePregnancyProfileIdOrderByRecordDateDesc(pregnancyProfileId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public WeightRecordResponseDTO createWeightRecord(WeightRecordRequestDTO request) {
        validateRequest(request);

        PregnancyProfile pregnancyProfile = pregnancyProfileRepository.findById(request.getPregnancyProfileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pregnancy profile not found"));

        currentUserService.checkFemmeAccess(pregnancyProfile.getFemme().getFemmeId());

        WeightRecord weightRecord = new WeightRecord();
        weightRecord.setPregnancyProfile(pregnancyProfile);
        weightRecord.setWeightKg(request.getWeightKg());
        weightRecord.setBmi(request.getBmi());
        weightRecord.setRecordDate(request.getRecordDate());
        weightRecord.setNotes(request.getNotes());

        return mapToResponse(weightRecordRepository.save(weightRecord));
    }

    public WeightRecordResponseDTO updateWeightRecord(Integer weightRecordId, WeightRecordRequestDTO request) {
        WeightRecord weightRecord = findWeightRecordById(weightRecordId);
        currentUserService.checkFemmeAccess(
                weightRecord.getPregnancyProfile().getFemme().getFemmeId()
        );

        if (request.getWeightKg() != null) {
            if (request.getWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weight must be positive");
            }
            weightRecord.setWeightKg(request.getWeightKg());
        }

        if (request.getBmi() != null) {
            weightRecord.setBmi(request.getBmi());
        }

        if (request.getRecordDate() != null) {
            weightRecord.setRecordDate(request.getRecordDate());
        }

        if (request.getNotes() != null) {
            weightRecord.setNotes(request.getNotes());
        }

        return mapToResponse(weightRecordRepository.save(weightRecord));
    }

    public void deleteWeightRecord(Integer weightRecordId) {
        WeightRecord weightRecord = findWeightRecordById(weightRecordId);
        currentUserService.checkFemmeAccess(
                weightRecord.getPregnancyProfile().getFemme().getFemmeId()
        );

        weightRecordRepository.delete(weightRecord);
    }

    private WeightRecord findWeightRecordById(Integer weightRecordId) {
        return weightRecordRepository.findById(weightRecordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weight record not found"));
    }

    private void validateRequest(WeightRecordRequestDTO request) {
        if (request.getPregnancyProfileId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pregnancy profile ID is required");
        }

        if (request.getWeightKg() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weight is required");
        }

        if (request.getWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weight must be positive");
        }
    }

    private WeightRecordResponseDTO mapToResponse(WeightRecord weightRecord) {
        return new WeightRecordResponseDTO(
                weightRecord.getWeightRecordId(),
                weightRecord.getPregnancyProfile().getPregnancyProfileId(),
                weightRecord.getWeightKg(),
                weightRecord.getBmi(),
                weightRecord.getRecordDate(),
                weightRecord.getNotes(),
                weightRecord.getCreatedAt()
        );
    }
}