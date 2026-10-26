package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.WeightRecordRequestDTO;
import com.pfe.mamacareplus.dto.WeightRecordResponseDTO;
import com.pfe.mamacareplus.service.WeightRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/weight-records")
@CrossOrigin(origins = "*")
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    public WeightRecordController(WeightRecordService weightRecordService) {
        this.weightRecordService = weightRecordService;
    }

    @GetMapping
    public List<WeightRecordResponseDTO> getAllWeightRecords() {
        return weightRecordService.getAllWeightRecords();
    }

    @GetMapping("/{weightRecordId}")
    public WeightRecordResponseDTO getWeightRecordById(@PathVariable Integer weightRecordId) {
        return weightRecordService.getWeightRecordById(weightRecordId);
    }

    @GetMapping("/pregnancy-profile/{pregnancyProfileId}")
    public List<WeightRecordResponseDTO> getWeightRecordsByPregnancyProfileId(
            @PathVariable Integer pregnancyProfileId
    ) {
        return weightRecordService.getWeightRecordsByPregnancyProfileId(pregnancyProfileId);
    }

    @PostMapping
    public WeightRecordResponseDTO createWeightRecord(@Valid @RequestBody WeightRecordRequestDTO request) {
        return weightRecordService.createWeightRecord(request);
    }

    @PutMapping("/{weightRecordId}")
    public WeightRecordResponseDTO updateWeightRecord(
            @PathVariable Integer weightRecordId,
            @Valid @RequestBody WeightRecordRequestDTO request
    ) {
        return weightRecordService.updateWeightRecord(weightRecordId, request);
    }

    @DeleteMapping("/{weightRecordId}")
    public ResponseEntity<Void> deleteWeightRecord(@PathVariable Integer weightRecordId) {
        weightRecordService.deleteWeightRecord(weightRecordId);
        return ResponseEntity.noContent().build();
    }
}