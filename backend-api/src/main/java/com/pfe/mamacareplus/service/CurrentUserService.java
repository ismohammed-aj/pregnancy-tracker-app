package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.User;
import com.pfe.mamacareplus.repository.FemmeRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final FemmeRepository femmeRepository;

    public CurrentUserService(FemmeRepository femmeRepository) {
        this.femmeRepository = femmeRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User)) {
            throw new RuntimeException("Invalid authenticated user");
        }

        return (User) principal;
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public Femme getCurrentFemme() {
        User currentUser = getCurrentUser();

        return femmeRepository.findByUserUserId(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Femme profile not found for current user"));
    }

    public Integer getCurrentFemmeId() {
        return getCurrentFemme().getFemmeId();
    }

    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("ADMIN");
    }

    public void checkFemmeAccess(Integer femmeId) {
        if (isAdmin()) {
            return;
        }

        Integer currentFemmeId = getCurrentFemmeId();

        if (!currentFemmeId.equals(femmeId)) {
            throw new RuntimeException("Access denied: you cannot access another femme data");
        }
    }
}