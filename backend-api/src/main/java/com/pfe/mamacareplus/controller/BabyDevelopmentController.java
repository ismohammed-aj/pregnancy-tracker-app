package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.BabyDevelopmentRequestDTO;
import com.pfe.mamacareplus.dto.BabyDevelopmentResponseDTO;
import com.pfe.mamacareplus.service.BabyDevelopmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/baby-developments")
@CrossOrigin(origins = "*")
public class BabyDevelopmentController {

    private final BabyDevelopmentService babyDevelopmentService;

    public BabyDevelopmentController(BabyDevelopmentService babyDevelopmentService) {
        this.babyDevelopmentService = babyDevelopmentService;
    }

    @GetMapping
    public List<BabyDevelopmentResponseDTO> getAllBabyDevelopments() {
        return babyDevelopmentService.getAllBabyDevelopments();
    }

    @GetMapping("/{babyDevelopmentId}")
    public BabyDevelopmentResponseDTO getBabyDevelopmentById(@PathVariable Integer babyDevelopmentId) {
        return babyDevelopmentService.getBabyDevelopmentById(babyDevelopmentId);
    }

    @GetMapping("/week/{weekNumber}")
    public BabyDevelopmentResponseDTO getBabyDevelopmentByWeek(@PathVariable Integer weekNumber) {
        return babyDevelopmentService.getBabyDevelopmentByWeek(weekNumber);
    }

    @PostMapping
    public BabyDevelopmentResponseDTO createBabyDevelopment(@RequestBody BabyDevelopmentRequestDTO request) {
        return babyDevelopmentService.createBabyDevelopment(request);
    }

    @PutMapping("/{babyDevelopmentId}")
    public BabyDevelopmentResponseDTO updateBabyDevelopment(
            @PathVariable Integer babyDevelopmentId,
            @RequestBody BabyDevelopmentRequestDTO request
    ) {
        return babyDevelopmentService.updateBabyDevelopment(babyDevelopmentId, request);
    }

    @DeleteMapping("/{babyDevelopmentId}")
    public ResponseEntity<Void> deleteBabyDevelopment(@PathVariable Integer babyDevelopmentId) {
        babyDevelopmentService.deleteBabyDevelopment(babyDevelopmentId);
        return ResponseEntity.noContent().build();
    }
}