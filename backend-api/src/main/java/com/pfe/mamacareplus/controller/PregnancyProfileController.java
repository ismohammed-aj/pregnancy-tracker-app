package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.PregnancyProfileRequestDTO;
import com.pfe.mamacareplus.dto.PregnancyProfileResponseDTO;
import com.pfe.mamacareplus.service.PregnancyProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/pregnancy-profiles")
@CrossOrigin(origins = "*")
public class PregnancyProfileController {

    private final PregnancyProfileService pregnancyProfileService;

    public PregnancyProfileController(PregnancyProfileService pregnancyProfileService) {
        this.pregnancyProfileService = pregnancyProfileService;
    }

    @GetMapping
    public List<PregnancyProfileResponseDTO> getAllProfiles() {
        return pregnancyProfileService.getAllProfiles();
    }

    @GetMapping("/{profileId}")
    public PregnancyProfileResponseDTO getProfileById(@PathVariable Integer profileId) {
        return pregnancyProfileService.getProfileById(profileId);
    }

    @GetMapping("/femme/{femmeId}")
    public PregnancyProfileResponseDTO getProfileByFemmeId(@PathVariable Integer femmeId) {
        return pregnancyProfileService.getProfileByFemmeId(femmeId);
    }

    @PostMapping
    public PregnancyProfileResponseDTO createProfile(@Valid @RequestBody PregnancyProfileRequestDTO request) {
        return pregnancyProfileService.createProfile(request);
    }

    @PutMapping("/{profileId}")
    public PregnancyProfileResponseDTO updateProfile(
            @PathVariable Integer profileId,
            @Valid @RequestBody PregnancyProfileRequestDTO request
    ) {
        return pregnancyProfileService.updateProfile(profileId, request);
    }

    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Integer profileId) {
        pregnancyProfileService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}