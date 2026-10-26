package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.OnboardingRequestDTO;
import com.pfe.mamacareplus.dto.OnboardingResponseDTO;
import com.pfe.mamacareplus.entity.Answer;
import com.pfe.mamacareplus.entity.Appointment;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.MedicationReminder;
import com.pfe.mamacareplus.entity.Option;
import com.pfe.mamacareplus.entity.PregnancyProfile;
import com.pfe.mamacareplus.entity.Question;
import com.pfe.mamacareplus.entity.Questionnaire;
import com.pfe.mamacareplus.entity.QuestionnaireResponse;
import com.pfe.mamacareplus.entity.RiskAssessment;
import com.pfe.mamacareplus.entity.WeightRecord;
import com.pfe.mamacareplus.repository.AppointmentRepository;
import com.pfe.mamacareplus.repository.MedicationReminderRepository;
import com.pfe.mamacareplus.repository.OptionRepository;
import com.pfe.mamacareplus.repository.PregnancyProfileRepository;
import com.pfe.mamacareplus.repository.QuestionRepository;
import com.pfe.mamacareplus.repository.QuestionnaireRepository;
import com.pfe.mamacareplus.repository.QuestionnaireResponseRepository;
import com.pfe.mamacareplus.repository.RiskAssessmentRepository;
import com.pfe.mamacareplus.repository.WeightRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private PregnancyProfileRepository pregnancyProfileRepository;

    @Mock
    private MedicationReminderRepository medicationReminderRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private WeightRecordRepository weightRecordRepository;

    @Mock
    private QuestionnaireRepository questionnaireRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private QuestionnaireResponseRepository questionnaireResponseRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    private final List<Question> savedQuestions = new ArrayList<>();
    private final List<Option> savedOptions = new ArrayList<>();

    private OnboardingService onboardingService;
    private Femme femme;
    private int questionIdSequence;
    private int optionIdSequence;

    @BeforeEach
    void setUp() {
        savedQuestions.clear();
        savedOptions.clear();
        questionIdSequence = 100;
        optionIdSequence = 1000;

        onboardingService = new OnboardingService(
                currentUserService,
                pregnancyProfileRepository,
                medicationReminderRepository,
                appointmentRepository,
                weightRecordRepository,
                questionnaireRepository,
                questionRepository,
                optionRepository,
                questionnaireResponseRepository,
                riskAssessmentRepository
        );

        femme = new Femme();
        femme.setFemmeId(7);

        when(currentUserService.getCurrentFemme()).thenReturn(femme);
        stubNormalizedQuestionnairePersistence();
    }

    @Test
    void pregnantWithoutProblemKeepsStableProfileAndSavesOnboarding() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setHeightCm(165);
        request.setWeightKg(60.0);
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertTrue(response.getSuccess());
        assertEquals("Onboarding termin\u00e9", response.getMessage());
        assertTrue(response.getOnboardingCompleted());
        assertTrue(response.getPregnancyProfileUpdated());
        assertEquals(0, response.getRiskScore());
        assertEquals("Profil stable", response.getRiskLevel());
        assertEquals("Non", response.getPreviousPregnancyProblem());
        assertEquals(List.of("Aucun"), response.getCurrentWarningSymptoms());

        QuestionnaireResponse savedResponse = captureSavedQuestionnaireResponse();
        assertSame(femme, savedResponse.getFemme());
        assertEquals(165.0, answerFor(savedResponse, "heightCm").getNumberAnswer(), 0.0);
        assertEquals(60.0, answerFor(savedResponse, "weightKg").getNumberAnswer(), 0.0);
        assertEquals("Aucun", answerValue(answerFor(savedResponse, "currentWarningSymptoms")));

        RiskAssessment savedRisk = captureSavedRiskAssessment();
        assertSame(femme, savedRisk.getFemme());
        assertSame(savedResponse, savedRisk.getQuestionnaireResponse());
        assertEquals(0, savedRisk.getTotalScore());
        assertEquals("Profil stable", savedRisk.getRiskLevel());

        verify(medicationReminderRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());

        ArgumentCaptor<WeightRecord> weightRecordCaptor = ArgumentCaptor.forClass(WeightRecord.class);
        verify(weightRecordRepository).save(weightRecordCaptor.capture());
        WeightRecord initialWeight = weightRecordCaptor.getValue();
        assertSame(femme, initialWeight.getPregnancyProfile().getFemme());
        assertEquals(0, new BigDecimal("60.00").compareTo(initialWeight.getWeightKg()));
        assertEquals(0, new BigDecimal("22.04").compareTo(initialWeight.getBmi()));
        assertEquals("Poids initial saisi pendant l'onboarding MamaCarePlus.", initialWeight.getNotes());
    }

    @Test
    void decimalWeightIsSavedWithoutAffectingRiskScore() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setWeightKg(60.5);
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertEquals(0, response.getRiskScore());
        assertEquals("Profil stable", response.getRiskLevel());

        QuestionnaireResponse savedResponse = captureSavedQuestionnaireResponse();
        assertEquals(60.5, answerFor(savedResponse, "weightKg").getNumberAnswer(), 0.0);
    }

    @Test
    void inconsistentDueDateFromDatePickerTodayUsesLastPeriodDateDueDate() {
        LocalDate lastPeriodDate = LocalDate.now().minusWeeks(8);

        OnboardingRequestDTO request = basePregnantRequest();
        request.setLastPeriodDate(lastPeriodDate);
        request.setDueDate(LocalDate.now());
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        onboardingService.completeOnboarding(request);

        ArgumentCaptor<PregnancyProfile> pregnancyCaptor = ArgumentCaptor.forClass(PregnancyProfile.class);
        verify(pregnancyProfileRepository).save(pregnancyCaptor.capture());
        assertEquals(lastPeriodDate.plusDays(280), pregnancyCaptor.getValue().getDueDate());
    }

    @Test
    void invalidWeightReturnsFriendlyMessage() {
        assertWeightRejected(null);
        assertWeightRejected(20.0);
        assertWeightRejected(300.0);
    }

    @Test
    void invalidHeightReturnsFriendlyMessage() {
        assertHeightRejected(null);
        assertHeightRejected(99);
        assertHeightRejected(231);
    }

    @Test
    void nullableOptionalRiskInputsDoNotCrashAndAreSavedAsEmptyLists() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setPreviousPregnancyProblem(null);
        request.setPreviousPregnancyProblemTypes(null);
        request.setCurrentWarningSymptoms(null);
        request.setChronicDiseases(null);
        request.setMedicalFollowUp(null);
        request.setSmokingStatus(null);
        request.setFolicAcid(null);
        request.setTakesMedication(null);
        request.setFirstPregnancy(null);
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertTrue(response.getSuccess());
        assertEquals(0, response.getRiskScore());
        assertEquals("Profil stable", response.getRiskLevel());
        assertEquals(List.of(), response.getPreviousPregnancyProblemTypes());
        assertEquals(List.of(), response.getCurrentWarningSymptoms());
        assertEquals(List.of(), response.getChronicDiseases());

        QuestionnaireResponse savedResponse = captureSavedQuestionnaireResponse();
        assertTrue(answersFor(savedResponse, "previousPregnancyProblemTypes").isEmpty());
        assertTrue(answersFor(savedResponse, "currentWarningSymptoms").isEmpty());
        assertTrue(answersFor(savedResponse, "chronicDiseases").isEmpty());
    }

    @Test
    void pregnantWithHypertensionAndSmokingGetsModerateVigilance() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setHeightCm(170);
        request.setWeightKg(70.5);
        request.setChronicDiseases(List.of("Hypertension"));
        request.setSmokingStatus("Oui");
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertEquals(5, response.getRiskScore());
        assertEquals("Vigilance mod\u00e9r\u00e9e", response.getRiskLevel());
        assertEquals(5, captureSavedRiskAssessment().getTotalScore());
    }

    @Test
    void hypertensionAndDiabetesIncreaseRiskScoreWithoutPregnancyProfile() {
        OnboardingRequestDTO request = baseRequest("Je veux suivre ma sant\u00e9 f\u00e9minine");
        request.setChronicDiseases(List.of("Hypertension", "Diab\u00e8te"));

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertTrue(response.getSuccess());
        assertEquals(6, response.getRiskScore());
        assertEquals("Vigilance mod\u00e9r\u00e9e", response.getRiskLevel());
        assertFalse(response.getPregnancyProfileUpdated());
        verify(pregnancyProfileRepository, never()).save(any());
    }

    @Test
    void bleedingSymptomRaisesScore() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setCurrentWarningSymptoms(List.of("Saignements"));
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertEquals(4, response.getRiskScore());
        assertEquals("Vigilance mod\u00e9r\u00e9e", response.getRiskLevel());
    }

    @Test
    void decreasedBabyMovementRaisesScore() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setCurrentWarningSymptoms(List.of("Diminution des mouvements du b\u00e9b\u00e9"));
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertEquals(4, response.getRiskScore());
        assertEquals("Vigilance mod\u00e9r\u00e9e", response.getRiskLevel());
    }

    @Test
    void bleedingAndDecreasedBabyMovementProduceMedicalFollowUpRisk() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setCurrentWarningSymptoms(List.of("Saignements", "Diminution des mouvements du b\u00e9b\u00e9"));
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertEquals(8, response.getRiskScore());
        assertEquals("Suivi m\u00e9dical recommand\u00e9", response.getRiskLevel());
    }

    @Test
    void firstPregnancyAddsOnlyOnePoint() {
        OnboardingRequestDTO request = basePregnantRequest();
        request.setPreviousPregnancyProblem("C\u2019est ma premi\u00e8re grossesse");
        request.setFirstPregnancy(true);
        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertEquals(1, response.getRiskScore());
        assertEquals("Profil stable", response.getRiskLevel());
    }

    @Test
    void priorityRiskPathCreatesMedicationReminderAppointmentAndStoresProblemLists() {
        LocalTime medicationTime = LocalTime.of(8, 30);
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusDays(5).withNano(0);

        OnboardingRequestDTO request = basePregnantRequest();
        request.setBirthYear(LocalDate.now().getYear() - 40);
        request.setPreviousPregnancyProblem("Oui");
        request.setPreviousPregnancyProblemTypes(List.of(
                "Accouchement pr\u00e9matur\u00e9",
                "Diab\u00e8te gestationnel"
        ));
        request.setCurrentWarningSymptoms(List.of("Saignements", "Diminution des mouvements du b\u00e9b\u00e9"));
        request.setChronicDiseases(List.of("Diab\u00e8te", "Hypertension"));
        request.setMedicalFollowUp("Pas encore");
        request.setTakesMedication(true);
        request.setMedicationName("Fer");
        request.setMedicationTime(medicationTime);
        request.setSmokingStatus("Oui");
        request.setFolicAcid("Non");
        request.setRemindersEnabled(true);
        request.setPreferredReminderTime(LocalTime.of(9, 0));
        request.setWantsAppointmentReminder("Oui");
        request.setNextAppointmentDateTime(appointmentDateTime);

        when(pregnancyProfileRepository.findByFemmeFemmeId(7)).thenReturn(Optional.empty());

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertTrue(response.getMedicationReminderCreated());
        assertTrue(response.getAppointmentCreated());
        assertEquals("Consultation prioritaire recommand\u00e9e", response.getRiskLevel());
        assertTrue(response.getRiskScore() >= 12);
        assertEquals(List.of("Accouchement pr\u00e9matur\u00e9", "Diab\u00e8te gestationnel"),
                response.getPreviousPregnancyProblemTypes());
        assertEquals(List.of("Saignements", "Diminution des mouvements du b\u00e9b\u00e9"),
                response.getCurrentWarningSymptoms());

        ArgumentCaptor<PregnancyProfile> pregnancyCaptor = ArgumentCaptor.forClass(PregnancyProfile.class);
        verify(pregnancyProfileRepository).save(pregnancyCaptor.capture());
        assertEquals("Suivi grossesse", pregnancyCaptor.getValue().getHealthStatus());

        RiskAssessment riskAssessment = captureSavedRiskAssessment();
        assertEquals("Consultation prioritaire recommand\u00e9e", riskAssessment.getRiskLevel());

        ArgumentCaptor<MedicationReminder> medicationCaptor = ArgumentCaptor.forClass(MedicationReminder.class);
        verify(medicationReminderRepository).save(medicationCaptor.capture());
        MedicationReminder reminder = medicationCaptor.getValue();
        assertSame(femme, reminder.getFemme());
        assertEquals("Fer", reminder.getMedicationName());
        assertEquals(medicationTime, reminder.getReminderTime());
        assertTrue(reminder.getActive());

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());
        Appointment appointment = appointmentCaptor.getValue();
        assertSame(femme, appointment.getFemme());
        assertEquals("Rendez-vous medical", appointment.getTitle());
        assertEquals(appointmentDateTime, appointment.getAppointmentDate());
        assertEquals("pending", appointment.getStatus());
    }

    @Test
    void tryingToConceivePathSkipsPregnancyObjectsAndKeepsStableRisk() {
        OnboardingRequestDTO request = baseRequest("J'essaie de tomber enceinte");
        request.setTryingDuration("3 \u00e0 6 mois");
        request.setCycleTracking("Oui");

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertTrue(response.getOnboardingCompleted());
        assertFalse(response.getPregnancyProfileUpdated());
        assertFalse(response.getMedicationReminderCreated());
        assertFalse(response.getAppointmentCreated());
        assertEquals(0, response.getRiskScore());
        assertEquals("Profil stable", response.getRiskLevel());
        assertEquals("3 \u00e0 6 mois", response.getTryingDuration());
        assertEquals("Oui", response.getCycleTracking());

        verify(pregnancyProfileRepository, never()).findByFemmeFemmeId(any());
        verify(pregnancyProfileRepository, never()).save(any());
        verify(weightRecordRepository, never()).save(any());
        verify(medicationReminderRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void healthTrackingPathStoresSimpleProfileAndSkipsConditionalObjects() {
        OnboardingRequestDTO request = baseRequest("Je veux suivre ma sant\u00e9 f\u00e9minine");

        OnboardingResponseDTO response = onboardingService.completeOnboarding(request);

        assertTrue(response.getOnboardingCompleted());
        assertFalse(response.getPregnancyProfileUpdated());
        assertFalse(response.getMedicationReminderCreated());
        assertFalse(response.getAppointmentCreated());
        assertEquals("Je veux suivre ma sant\u00e9 f\u00e9minine", response.getCurrentStatus());
        assertEquals(0, response.getRiskScore());
        assertEquals("Profil stable", response.getRiskLevel());

        QuestionnaireResponse savedResponse = captureSavedQuestionnaireResponse();
        assertEquals("Aucune", answerValue(answerFor(savedResponse, "chronicDiseases")));
        assertEquals("Non", answerValue(answerFor(savedResponse, "takesMedication")));

        verify(pregnancyProfileRepository, never()).findByFemmeFemmeId(any());
        verify(pregnancyProfileRepository, never()).save(any());
        verify(weightRecordRepository, never()).save(any());
        verify(medicationReminderRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    private void assertWeightRejected(Double weightKg) {
        OnboardingRequestDTO request = baseRequest("Je veux suivre ma sant\u00e9 f\u00e9minine");
        request.setWeightKg(weightKg);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> onboardingService.completeOnboarding(request)
        );

        assertEquals("Veuillez saisir un poids valide en kg.", exception.getReason());
        verify(questionnaireResponseRepository, never()).save(any());
        verify(riskAssessmentRepository, never()).save(any());
    }

    private void assertHeightRejected(Integer heightCm) {
        OnboardingRequestDTO request = baseRequest("Je veux suivre ma sant\u00e9 f\u00e9minine");
        request.setHeightCm(heightCm);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> onboardingService.completeOnboarding(request)
        );

        assertEquals("Veuillez saisir une taille valide en cm.", exception.getReason());
        verify(questionnaireResponseRepository, never()).save(any());
        verify(riskAssessmentRepository, never()).save(any());
    }

    private void stubNormalizedQuestionnairePersistence() {
        lenient().when(questionnaireRepository.findFirstByTypeIgnoreCaseOrderByQuestionnaireIdAsc(anyString()))
                .thenReturn(Optional.empty());

        lenient().when(questionnaireRepository.save(any(Questionnaire.class))).thenAnswer(invocation -> {
            Questionnaire questionnaire = invocation.getArgument(0);
            if (questionnaire.getQuestionnaireId() == null) {
                questionnaire.setQuestionnaireId(42);
            }
            return questionnaire;
        });

        lenient().when(questionRepository.findByQuestionnaireQuestionnaireIdAndFieldKey(anyInt(), anyString()))
                .thenAnswer(invocation -> {
                    Integer questionnaireId = invocation.getArgument(0);
                    String fieldKey = invocation.getArgument(1);
                    return savedQuestions.stream()
                            .filter(question -> Objects.equals(question.getQuestionnaire().getQuestionnaireId(), questionnaireId))
                            .filter(question -> Objects.equals(question.getFieldKey(), fieldKey))
                            .findFirst();
                });

        lenient().when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question question = invocation.getArgument(0);
            if (question.getQuestionId() == null) {
                question.setQuestionId(questionIdSequence++);
            }
            savedQuestions.removeIf(existing -> Objects.equals(existing.getQuestionId(), question.getQuestionId()));
            savedQuestions.add(question);
            return question;
        });

        lenient().when(questionRepository.findByQuestionnaireQuestionnaireIdOrderByDisplayOrderAscQuestionIdAsc(anyInt()))
                .thenAnswer(invocation -> {
                    Integer questionnaireId = invocation.getArgument(0);
                    return savedQuestions.stream()
                            .filter(question -> Objects.equals(question.getQuestionnaire().getQuestionnaireId(), questionnaireId))
                            .sorted(Comparator
                                    .comparing(Question::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                                    .thenComparing(Question::getQuestionId))
                            .toList();
                });

        lenient().when(optionRepository.findByQuestionQuestionIdAndLabelIgnoreCase(any(), anyString()))
                .thenAnswer(invocation -> {
                    Integer questionId = invocation.getArgument(0);
                    String label = invocation.getArgument(1);
                    return savedOptions.stream()
                            .filter(option -> Objects.equals(option.getQuestion().getQuestionId(), questionId))
                            .filter(option -> option.getLabel().equalsIgnoreCase(label))
                            .findFirst();
                });

        lenient().when(optionRepository.findByQuestionQuestionIdOrderByDisplayOrderAscOptionIdAsc(any()))
                .thenAnswer(invocation -> {
                    Integer questionId = invocation.getArgument(0);
                    return savedOptions.stream()
                            .filter(option -> Objects.equals(option.getQuestion().getQuestionId(), questionId))
                            .sorted(Comparator
                                    .comparing(Option::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                                    .thenComparing(Option::getOptionId))
                            .toList();
                });

        lenient().when(optionRepository.save(any(Option.class))).thenAnswer(invocation -> {
            Option option = invocation.getArgument(0);
            if (option.getOptionId() == null) {
                option.setOptionId(optionIdSequence++);
            }
            savedOptions.removeIf(existing -> Objects.equals(existing.getOptionId(), option.getOptionId()));
            savedOptions.add(option);
            return option;
        });

        lenient().when(questionnaireResponseRepository.save(any(QuestionnaireResponse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private QuestionnaireResponse captureSavedQuestionnaireResponse() {
        ArgumentCaptor<QuestionnaireResponse> responseCaptor = ArgumentCaptor.forClass(QuestionnaireResponse.class);
        verify(questionnaireResponseRepository).save(responseCaptor.capture());
        return responseCaptor.getValue();
    }

    private RiskAssessment captureSavedRiskAssessment() {
        ArgumentCaptor<RiskAssessment> riskCaptor = ArgumentCaptor.forClass(RiskAssessment.class);
        verify(riskAssessmentRepository).save(riskCaptor.capture());
        return riskCaptor.getValue();
    }

    private Answer answerFor(QuestionnaireResponse response, String fieldKey) {
        return answersFor(response, fieldKey).stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing answer for " + fieldKey));
    }

    private List<Answer> answersFor(QuestionnaireResponse response, String fieldKey) {
        return response.getAnswers().stream()
                .filter(answer -> Objects.equals(answer.getQuestion().getFieldKey(), fieldKey))
                .toList();
    }

    private String answerValue(Answer answer) {
        if (answer.getSelectedOption() != null) {
            return answer.getSelectedOption().getLabel();
        }
        return answer.getTextAnswer();
    }

    private OnboardingRequestDTO basePregnantRequest() {
        OnboardingRequestDTO request = baseRequest("Je suis enceinte");
        request.setLastPeriodDate(LocalDate.now().minusWeeks(10));
        request.setPreviousPregnancyProblem("Non");
        request.setPreviousPregnancyProblemTypes(List.of());
        request.setCurrentWarningSymptoms(List.of("Aucun"));
        request.setMedicalFollowUp("Oui");
        request.setFirstPregnancy(false);
        return request;
    }

    private OnboardingRequestDTO baseRequest(String currentStatus) {
        OnboardingRequestDTO request = new OnboardingRequestDTO();
        request.setBirthYear(LocalDate.now().getYear() - 28);
        request.setHeightCm(165);
        request.setWeightKg(68.0);
        request.setCurrentStatus(currentStatus);
        request.setChronicDiseases(List.of("Aucune"));
        request.setTakesMedication(false);
        request.setFolicAcid("Oui");
        request.setSmokingStatus("Non");
        request.setRemindersEnabled(false);
        request.setWantsAppointmentReminder("Plus tard");
        return request;
    }
}
