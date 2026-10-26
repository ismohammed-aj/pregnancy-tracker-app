package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.PregnancyProfileResponseDTO;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.PregnancyProfile;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.PregnancyProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PregnancyProfileServiceTest {

    @Mock
    private PregnancyProfileRepository pregnancyProfileRepository;

    @Mock
    private FemmeRepository femmeRepository;

    @Mock
    private CurrentUserService currentUserService;

    private PregnancyProfileService pregnancyProfileService;

    @BeforeEach
    void setUp() {
        pregnancyProfileService = new PregnancyProfileService(
                pregnancyProfileRepository,
                femmeRepository,
                currentUserService
        );
    }

    @Test
    void incoherentDueDateDoesNotShowFinishedPregnancyAtWeekNine() {
        LocalDate lastPeriodDate = LocalDate.now().minusWeeks(8);
        LocalDate wrongDueDate = LocalDate.now();
        PregnancyProfile profile = pregnancyProfile(lastPeriodDate, wrongDueDate);

        when(pregnancyProfileRepository.findById(3)).thenReturn(Optional.of(profile));

        PregnancyProfileResponseDTO response = pregnancyProfileService.getProfileById(3);

        assertEquals(9, response.getCurrentWeek());
        assertEquals(lastPeriodDate.plusDays(280), response.getDueDate());
        assertTrue(response.getRemainingDays() > 200);
        assertTrue(response.getProgressPercentage() > 15.0);
        assertTrue(response.getProgressPercentage() < 30.0);
    }

    private PregnancyProfile pregnancyProfile(LocalDate startDate, LocalDate dueDate) {
        Femme femme = new Femme();
        femme.setFemmeId(7);
        femme.setUsername("Test");

        PregnancyProfile profile = new PregnancyProfile();
        profile.setPregnancyProfileId(3);
        profile.setFemme(femme);
        profile.setStartDate(startDate);
        profile.setDueDate(dueDate);
        return profile;
    }
}
