package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.BabyDevelopmentRequestDTO;
import com.pfe.mamacareplus.dto.BabyDevelopmentResponseDTO;
import com.pfe.mamacareplus.entity.BabyDevelopment;
import com.pfe.mamacareplus.repository.BabyDevelopmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class BabyDevelopmentService {

    private final BabyDevelopmentRepository babyDevelopmentRepository;

    public BabyDevelopmentService(BabyDevelopmentRepository babyDevelopmentRepository) {
        this.babyDevelopmentRepository = babyDevelopmentRepository;
    }

    public List<BabyDevelopmentResponseDTO> getAllBabyDevelopments() {
        return babyDevelopmentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BabyDevelopment::getWeekNumber))
                .map(this::mapToResponse)
                .toList();
    }

    public BabyDevelopmentResponseDTO getBabyDevelopmentById(Integer babyDevelopmentId) {
        BabyDevelopment babyDevelopment = findBabyDevelopmentById(babyDevelopmentId);
        return mapToResponse(babyDevelopment);
    }

    public BabyDevelopmentResponseDTO getBabyDevelopmentByWeek(Integer weekNumber) {
        validateWeekNumber(weekNumber);

        BabyDevelopment babyDevelopment = babyDevelopmentRepository.findByWeekNumber(weekNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Baby development not found for this week"));

        return mapToResponse(babyDevelopment);
    }

    public BabyDevelopmentResponseDTO createBabyDevelopment(BabyDevelopmentRequestDTO request) {
        validateRequest(request, true);

        if (babyDevelopmentRepository.existsByWeekNumber(request.getWeekNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Baby development already exists for this week");
        }

        BabyDevelopment babyDevelopment = new BabyDevelopment();
        babyDevelopment.setWeekNumber(request.getWeekNumber());
        babyDevelopment.setBabySize(request.getBabySize());
        babyDevelopment.setBabyWeightGrams(request.getBabyWeightGrams());
        babyDevelopment.setBabyLengthCm(request.getBabyLengthCm());
        babyDevelopment.setImageUrl(request.getImageUrl());
        babyDevelopment.setComparisonType(request.getComparisonType());
        babyDevelopment.setComparisonName(request.getComparisonName());
        babyDevelopment.setDescription(request.getDescription());
        babyDevelopment.setAdvice(request.getAdvice());

        return mapToResponse(babyDevelopmentRepository.save(babyDevelopment));
    }

    public BabyDevelopmentResponseDTO updateBabyDevelopment(Integer babyDevelopmentId, BabyDevelopmentRequestDTO request) {
        BabyDevelopment babyDevelopment = findBabyDevelopmentById(babyDevelopmentId);

        if (request.getWeekNumber() != null) {
            validateWeekNumber(request.getWeekNumber());

            if (!request.getWeekNumber().equals(babyDevelopment.getWeekNumber())
                    && babyDevelopmentRepository.existsByWeekNumber(request.getWeekNumber())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Baby development already exists for this week");
            }

            babyDevelopment.setWeekNumber(request.getWeekNumber());
        }

        if (request.getBabySize() != null) {
            babyDevelopment.setBabySize(request.getBabySize());
        }

        if (request.getBabyWeightGrams() != null) {
            validatePositiveDecimal(request.getBabyWeightGrams(), "Baby weight must be positive");
            babyDevelopment.setBabyWeightGrams(request.getBabyWeightGrams());
        }

        if (request.getBabyLengthCm() != null) {
            validatePositiveDecimal(request.getBabyLengthCm(), "Baby length must be positive");
            babyDevelopment.setBabyLengthCm(request.getBabyLengthCm());
        }

        if (request.getImageUrl() != null) {
            babyDevelopment.setImageUrl(request.getImageUrl());
        }

        if (request.getComparisonType() != null) {
            babyDevelopment.setComparisonType(request.getComparisonType());
        }

        if (request.getComparisonName() != null) {
            babyDevelopment.setComparisonName(request.getComparisonName());
        }

        if (request.getDescription() != null) {
            babyDevelopment.setDescription(request.getDescription());
        }

        if (request.getAdvice() != null) {
            babyDevelopment.setAdvice(request.getAdvice());
        }

        return mapToResponse(babyDevelopmentRepository.save(babyDevelopment));
    }

    public void deleteBabyDevelopment(Integer babyDevelopmentId) {
        BabyDevelopment babyDevelopment = findBabyDevelopmentById(babyDevelopmentId);
        babyDevelopmentRepository.delete(babyDevelopment);
    }

    private BabyDevelopment findBabyDevelopmentById(Integer babyDevelopmentId) {
        return babyDevelopmentRepository.findById(babyDevelopmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Baby development not found"));
    }

    private void validateRequest(BabyDevelopmentRequestDTO request, boolean requireWeekNumber) {
        if (requireWeekNumber && request.getWeekNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Week number is required");
        }

        if (request.getWeekNumber() != null) {
            validateWeekNumber(request.getWeekNumber());
        }

        if (request.getBabyWeightGrams() != null) {
            validatePositiveDecimal(request.getBabyWeightGrams(), "Baby weight must be positive");
        }

        if (request.getBabyLengthCm() != null) {
            validatePositiveDecimal(request.getBabyLengthCm(), "Baby length must be positive");
        }
    }

    private void validateWeekNumber(Integer weekNumber) {
        if (weekNumber == null || weekNumber < 1 || weekNumber > 42) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Week number must be between 1 and 42");
        }
    }

    private void validatePositiveDecimal(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private BabyDevelopmentResponseDTO mapToResponse(BabyDevelopment babyDevelopment) {
        return new BabyDevelopmentResponseDTO(
                babyDevelopment.getBabyDevelopmentId(),
                babyDevelopment.getWeekNumber(),
                babyDevelopment.getBabySize(),
                babyDevelopment.getBabyWeightGrams(),
                babyDevelopment.getBabyLengthCm(),
                babyDevelopment.getImageUrl(),
                babyDevelopment.getComparisonType(),
                babyDevelopment.getComparisonName(),
                babyDevelopment.getDescription(),
                babyDevelopment.getAdvice(),
                babyDevelopment.getCreatedAt()
        );
    }
}
