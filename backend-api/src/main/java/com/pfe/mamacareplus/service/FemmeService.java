package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.ChangePasswordRequestDTO;
import com.pfe.mamacareplus.dto.FemmeRequestDTO;
import com.pfe.mamacareplus.dto.FemmeResponseDTO;
import com.pfe.mamacareplus.dto.MessageResponseDTO;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.User;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FemmeService {

    private final FemmeRepository femmeRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public FemmeService(
            FemmeRepository femmeRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            PasswordEncoder passwordEncoder
    ) {
        this.femmeRepository = femmeRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<FemmeResponseDTO> getAllFemmes() {
        if (!currentUserService.isAdmin()) {
            Integer currentFemmeId = currentUserService.getCurrentFemmeId();

            return femmeRepository.findById(currentFemmeId)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return femmeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FemmeResponseDTO getFemmeById(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);
        return mapToResponse(findFemmeById(femmeId));
    }

    public FemmeResponseDTO getFemmeByUserId(Integer userId) {
        User currentUser = currentUserService.getCurrentUser();

        if (!currentUserService.isAdmin() && !currentUser.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Femme femme = femmeRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme profile not found for this user"));

        return mapToResponse(femme);
    }

    public FemmeResponseDTO createFemme(FemmeRequestDTO request) {
        validateRequest(request);

        User currentUser = currentUserService.getCurrentUser();

        if (!currentUserService.isAdmin() && !currentUser.getUserId().equals(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        if (femmeRepository.existsByUserUserId(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This user already has a femme profile");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Femme femme = new Femme();
        femme.setUser(user);
        femme.setUsername(request.getUsername());
        femme.setBirthDate(request.getBirthDate());
        femme.setAvatar(request.getAvatar());

        return mapToResponse(femmeRepository.save(femme));
    }

    public FemmeResponseDTO updateFemme(Integer femmeId, FemmeRequestDTO request) {
        currentUserService.checkFemmeAccess(femmeId);

        Femme femme = findFemmeById(femmeId);

        if (request.getUsername() != null) {
            if (request.getUsername().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
            }
            femme.setUsername(request.getUsername());
        }

        if (request.getBirthDate() != null) {
            femme.setBirthDate(request.getBirthDate());
        }

        if (request.getAvatar() != null) {
            femme.setAvatar(request.getAvatar());
        }

        return mapToResponse(femmeRepository.save(femme));
    }

    public FemmeResponseDTO uploadAvatar(Integer femmeId, MultipartFile avatar) {
        currentUserService.checkFemmeAccess(femmeId);

        if (avatar == null || avatar.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar image is required");
        }

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar must be an image");
        }

        Femme femme = findFemmeById(femmeId);

        try {
            Path uploadDir = Path.of("uploads", "avatars").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String extension = resolveExtension(avatar.getOriginalFilename(), contentType);
            String filename = "avatar_" + femmeId + "_" + UUID.randomUUID() + extension;
            Path target = uploadDir.resolve(filename).normalize();

            if (!target.startsWith(uploadDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid avatar filename");
            }

            Files.copy(avatar.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            femme.setAvatar("/uploads/avatars/" + filename);
            return mapToResponse(femmeRepository.save(femme));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save avatar image");
        }
    }

    public MessageResponseDTO changePassword(Integer femmeId, ChangePasswordRequestDTO request) {
        currentUserService.checkFemmeAccess(femmeId);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match");
        }

        Femme femme = findFemmeById(femmeId);
        User user = femme.getUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponseDTO("Password changed successfully");
    }

    public void deleteFemme(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);

        Femme femme = findFemmeById(femmeId);
        femmeRepository.delete(femme);
    }

    private Femme findFemmeById(Integer femmeId) {
        return femmeRepository.findById(femmeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Femme not found"));
    }

    private void validateRequest(FemmeRequestDTO request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (extension.matches("\\.(jpg|jpeg|png|webp|gif)")) {
                    return extension;
                }
            }
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private FemmeResponseDTO mapToResponse(Femme femme) {
        return new FemmeResponseDTO(
                femme.getFemmeId(),
                femme.getUser().getUserId(),
                femme.getUser().getEmail(),
                femme.getUsername(),
                femme.getBirthDate(),
                femme.getAvatar(),
                femme.getCreatedAt(),
                femme.getUpdatedAt()
        );
    }
}
