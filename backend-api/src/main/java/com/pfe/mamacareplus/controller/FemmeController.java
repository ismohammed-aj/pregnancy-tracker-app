package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.ChangePasswordRequestDTO;
import com.pfe.mamacareplus.dto.FemmeRequestDTO;
import com.pfe.mamacareplus.dto.FemmeResponseDTO;
import com.pfe.mamacareplus.dto.MessageResponseDTO;
import com.pfe.mamacareplus.service.FemmeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/femmes")
@CrossOrigin(origins = "*")
public class FemmeController {

    private final FemmeService femmeService;

    public FemmeController(FemmeService femmeService) {
        this.femmeService = femmeService;
    }

    @GetMapping
    public List<FemmeResponseDTO> getAllFemmes() {
        return femmeService.getAllFemmes();
    }

    @GetMapping("/{femmeId}")
    public FemmeResponseDTO getFemmeById(@PathVariable Integer femmeId) {
        return femmeService.getFemmeById(femmeId);
    }

    @GetMapping("/user/{userId}")
    public FemmeResponseDTO getFemmeByUserId(@PathVariable Integer userId) {
        return femmeService.getFemmeByUserId(userId);
    }

    @PostMapping
    public FemmeResponseDTO createFemme(@Valid @RequestBody FemmeRequestDTO request) {
        return femmeService.createFemme(request);
    }

    @PutMapping("/{femmeId}")
    public FemmeResponseDTO updateFemme(
            @PathVariable Integer femmeId,
            @Valid @RequestBody FemmeRequestDTO request
    ) {
        return femmeService.updateFemme(femmeId, request);
    }

    @PostMapping(value = "/{femmeId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FemmeResponseDTO uploadAvatar(
            @PathVariable Integer femmeId,
            @RequestPart("avatar") MultipartFile avatar
    ) {
        return femmeService.uploadAvatar(femmeId, avatar);
    }

    @PutMapping("/{femmeId}/change-password")
    public MessageResponseDTO changePassword(
            @PathVariable Integer femmeId,
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {
        return femmeService.changePassword(femmeId, request);
    }

    @DeleteMapping("/{femmeId}")
    public ResponseEntity<Void> deleteFemme(@PathVariable Integer femmeId) {
        femmeService.deleteFemme(femmeId);
        return ResponseEntity.noContent().build();
    }
}
