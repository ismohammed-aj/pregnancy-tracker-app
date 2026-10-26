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
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingService.class);
    private static final String ONBOARDING_TYPE = "ONBOARDING";
    private static final String STATUS_PREGNANT = "Je suis enceinte";
    private static final String INVALID_HEIGHT_MESSAGE = "Veuillez saisir une taille valide en cm.";
    private static final String INVALID_WEIGHT_MESSAGE = "Veuillez saisir un poids valide en kg.";
    private static final String INITIAL_WEIGHT_NOTE = "Poids initial saisi pendant l'onboarding MamaCarePlus.";
    private static final long PREGNANCY_DURATION_DAYS = 280L;
    private static final long MIN_REASONABLE_PREGNANCY_DAYS = 240L;
    private static final long MAX_REASONABLE_PREGNANCY_DAYS = 320L;

    private final CurrentUserService currentUserService;
    private final PregnancyProfileRepository pregnancyProfileRepository;
    private final MedicationReminderRepository medicationReminderRepository;
    private final AppointmentRepository appointmentRepository;
    private final WeightRecordRepository weightRecordRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuestionnaireResponseRepository questionnaireResponseRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

    public OnboardingService(
            CurrentUserService currentUserService,
            PregnancyProfileRepository pregnancyProfileRepository,
            MedicationReminderRepository medicationReminderRepository,
            AppointmentRepository appointmentRepository,
            WeightRecordRepository weightRecordRepository,
            QuestionnaireRepository questionnaireRepository,
            QuestionRepository questionRepository,
            OptionRepository optionRepository,
            QuestionnaireResponseRepository questionnaireResponseRepository,
            RiskAssessmentRepository riskAssessmentRepository
    ) {
        this.currentUserService = currentUserService;
        this.pregnancyProfileRepository = pregnancyProfileRepository;
        this.medicationReminderRepository = medicationReminderRepository;
        this.appointmentRepository = appointmentRepository;
        this.weightRecordRepository = weightRecordRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.questionnaireResponseRepository = questionnaireResponseRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    @Transactional(readOnly = true)
    public OnboardingResponseDTO getCurrentOnboarding() {
        Femme femme = currentUserService.getCurrentFemme();
        Optional<QuestionnaireResponse> latestResponse =
                questionnaireResponseRepository.findFirstByFemme_FemmeIdAndQuestionnaire_TypeIgnoreCaseOrderByCompletedAtDesc(
                        femme.getFemmeId(),
                        ONBOARDING_TYPE
                );

        if (latestResponse.isEmpty()) {
            OnboardingResponseDTO response = new OnboardingResponseDTO();
            response.setSuccess(true);
            response.setFemmeId(femme.getFemmeId());
            response.setOnboardingCompleted(false);
            response.setMessage("Aucun onboarding trouve.");
            return response;
        }

        RiskAssessment assessment = riskAssessmentRepository
                .findByQuestionnaireResponseResponseId(latestResponse.get().getResponseId())
                .orElse(null);

        return mapToResponse(latestResponse.get(), assessment, false, false, false);
    }

    @Transactional
    public OnboardingResponseDTO completeOnboarding(OnboardingRequestDTO request) {
        Femme femme = currentUserService.getCurrentFemme();
        logger.info(
                "Debut sauvegarde onboarding: femmeId={}, userId={}, email={}, request={}",
                femme.getFemmeId(),
                getUserId(femme),
                getUserEmail(femme),
                summarizeRequest(request)
        );
        validateRequest(request);

        Integer riskScore = calculateRiskScore(request);
        String riskLevel = resolveRiskLevel(riskScore);
        logger.info(
                "Score onboarding calcule: femmeId={}, riskScore={}, riskLevel={}",
                femme.getFemmeId(),
                riskScore,
                riskLevel
        );

        QuestionnaireResponse savedResponse = saveQuestionnaireResponse(femme, request);
        RiskAssessment riskAssessment = saveRiskAssessment(savedResponse, riskScore, riskLevel);

        PregnancyProfile pregnancyProfile = syncPregnancyProfile(femme, request);
        boolean pregnancyProfileUpdated = pregnancyProfile != null;
        createOrUpdateInitialWeightRecordIfNeeded(pregnancyProfile, request);
        boolean medicationReminderCreated = createMedicationReminderIfNeeded(femme, request);
        boolean appointmentCreated = createAppointmentIfNeeded(femme, request);

        OnboardingResponseDTO response = mapToResponse(
                savedResponse,
                riskAssessment,
                pregnancyProfileUpdated,
                medicationReminderCreated,
                appointmentCreated
        );
        response.setSuccess(true);
        response.setMessage("Onboarding termin\u00e9");
        logger.info(
                "Onboarding sauvegarde: femmeId={}, responseId={}, pregnancyProfileUpdated={}, medicationReminderCreated={}, appointmentCreated={}",
                femme.getFemmeId(),
                savedResponse.getResponseId(),
                pregnancyProfileUpdated,
                medicationReminderCreated,
                appointmentCreated
        );
        return response;
    }

    private QuestionnaireResponse saveQuestionnaireResponse(Femme femme, OnboardingRequestDTO request) {
        Questionnaire questionnaire = ensureOnboardingQuestionnaire();
        Map<String, Question> questions = getOnboardingQuestions(questionnaire);

        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setFemme(femme);
        response.setQuestionnaire(questionnaire);

        addNumberAnswer(response, questions.get("birthYear"), request.getBirthYear(), scoreBirthYear(request.getBirthYear()));
        addNumberAnswer(response, questions.get("heightCm"), request.getHeightCm(), 0);
        addNumberAnswer(response, questions.get("weightKg"), request.getWeightKg(), 0);
        addSingleOptionAnswer(response, questions.get("currentStatus"), request.getCurrentStatus());
        addTextAnswer(response, questions.get("lastPeriodDate"), request.getLastPeriodDate());
        addTextAnswer(response, questions.get("dueDate"), resolveDueDate(request));
        addSingleOptionAnswer(response, questions.get("firstPregnancy"), booleanLabel(resolveFirstPregnancy(request)));
        addSingleOptionAnswer(response, questions.get("medicalFollowUp"), request.getMedicalFollowUp());
        addSingleOptionAnswer(response, questions.get("tryingDuration"), request.getTryingDuration());
        addSingleOptionAnswer(response, questions.get("cycleTracking"), request.getCycleTracking());
        addSingleOptionAnswer(response, questions.get("previousPregnancyProblem"), request.getPreviousPregnancyProblem());
        addListOptionAnswers(response, questions.get("previousPregnancyProblemTypes"), request.getPreviousPregnancyProblemTypes());
        addListOptionAnswers(response, questions.get("currentWarningSymptoms"), request.getCurrentWarningSymptoms());
        addListOptionAnswers(response, questions.get("chronicDiseases"), request.getChronicDiseases());
        addSingleOptionAnswer(response, questions.get("takesMedication"), booleanLabel(request.getTakesMedication()));
        addTextAnswer(response, questions.get("medicationName"), request.getMedicationName());
        addTextAnswer(response, questions.get("medicationTime"), request.getMedicationTime());
        addSingleOptionAnswer(response, questions.get("folicAcid"), request.getFolicAcid());
        addSingleOptionAnswer(response, questions.get("smokingStatus"), request.getSmokingStatus());
        addSingleOptionAnswer(response, questions.get("remindersEnabled"), booleanLabel(request.getRemindersEnabled()));
        addTextAnswer(response, questions.get("preferredReminderTime"), request.getPreferredReminderTime());
        addSingleOptionAnswer(response, questions.get("wantsAppointmentReminder"), request.getWantsAppointmentReminder());
        addTextAnswer(response, questions.get("nextAppointmentDateTime"), request.getNextAppointmentDateTime());

        return questionnaireResponseRepository.save(response);
    }

    private RiskAssessment saveRiskAssessment(QuestionnaireResponse response, Integer riskScore, String riskLevel) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setFemme(response.getFemme());
        assessment.setQuestionnaireResponse(response);
        assessment.setTotalScore(riskScore);
        assessment.setRiskLevel(riskLevel);
        assessment.setDescription(resolveRiskDescription(riskLevel));
        assessment.setRecommendation(resolveRecommendation(riskLevel));
        return riskAssessmentRepository.save(assessment);
    }

    private Questionnaire ensureOnboardingQuestionnaire() {
        Questionnaire questionnaire = questionnaireRepository
                .findFirstByTypeIgnoreCaseOrderByQuestionnaireIdAsc(ONBOARDING_TYPE)
                .orElseGet(Questionnaire::new);

        questionnaire.setTitle("Onboarding MamaCarePlus");
        questionnaire.setDescription("Questionnaire initial pour personnaliser le suivi grossesse et sante.");
        questionnaire.setType(ONBOARDING_TYPE);
        questionnaire.setLevel("INITIAL");
        Questionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);

        for (QuestionSpec spec : onboardingQuestionSpecs()) {
            Question question = questionRepository
                    .findByQuestionnaireQuestionnaireIdAndFieldKey(savedQuestionnaire.getQuestionnaireId(), spec.fieldKey())
                    .orElseGet(Question::new);
            question.setQuestionnaire(savedQuestionnaire);
            question.setFieldKey(spec.fieldKey());
            question.setText(spec.text());
            question.setType(spec.type());
            question.setCategory(spec.category());
            question.setDisplayOrder(spec.displayOrder());
            question.setActive(true);
            Question savedQuestion = questionRepository.save(question);

            int optionOrder = 1;
            for (String optionLabel : spec.options()) {
                ensureOption(savedQuestion, optionLabel, scoreForOption(spec.fieldKey(), optionLabel), optionOrder++);
            }
        }

        return savedQuestionnaire;
    }

    private Map<String, Question> getOnboardingQuestions(Questionnaire questionnaire) {
        return questionRepository
                .findByQuestionnaireQuestionnaireIdOrderByDisplayOrderAscQuestionIdAsc(questionnaire.getQuestionnaireId())
                .stream()
                .filter(question -> question.getFieldKey() != null)
                .collect(
                        LinkedHashMap::new,
                        (map, question) -> map.put(question.getFieldKey(), question),
                        LinkedHashMap::putAll
                );
    }

    private void addNumberAnswer(QuestionnaireResponse response, Question question, Number value, Integer score) {
        if (question == null || value == null) {
            return;
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setNumberAnswer(value.doubleValue());
        answer.setScore(score != null ? score : 0);
        response.addAnswer(answer);
    }

    private void addTextAnswer(QuestionnaireResponse response, Question question, Object value) {
        if (question == null || value == null || clean(String.valueOf(value)).isBlank()) {
            return;
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setTextAnswer(clean(String.valueOf(value)));
        answer.setScore(0);
        response.addAnswer(answer);
    }

    private void addSingleOptionAnswer(QuestionnaireResponse response, Question question, String value) {
        if (question == null || isBlank(value)) {
            return;
        }

        String label = clean(value);
        Option option = ensureOption(question, label, scoreForOption(question.getFieldKey(), label), nextOptionOrder(question));
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setSelectedOption(option);
        answer.setScore(option.getScoreValue() != null ? option.getScoreValue() : 0);
        response.addAnswer(answer);
    }

    private void addListOptionAnswers(QuestionnaireResponse response, Question question, List<String> values) {
        if (question == null || values == null || values.isEmpty()) {
            return;
        }

        values.stream()
                .filter(Objects::nonNull)
                .map(this::clean)
                .filter(value -> !value.isBlank())
                .distinct()
                .forEach(value -> addSingleOptionAnswer(response, question, value));
    }

    private Option ensureOption(Question question, String label, Integer score, Integer displayOrder) {
        Option option = optionRepository
                .findByQuestionQuestionIdAndLabelIgnoreCase(question.getQuestionId(), label)
                .orElseGet(Option::new);

        option.setQuestion(question);
        option.setLabel(label);
        option.setScoreValue(score != null ? score : 0);
        option.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        return optionRepository.save(option);
    }

    private int nextOptionOrder(Question question) {
        return optionRepository.findByQuestionQuestionIdOrderByDisplayOrderAscOptionIdAsc(question.getQuestionId())
                .stream()
                .map(Option::getDisplayOrder)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private void validateRequest(OnboardingRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Onboarding request is required");
        }

        if (request.getBirthYear() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birth year is required");
        }

        int currentYear = LocalDate.now().getYear();
        if (request.getBirthYear() < 1900 || request.getBirthYear() > currentYear) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birth year is invalid");
        }

        validateHeight(request.getHeightCm());
        validateWeight(request.getWeightKg());

        if (isBlank(request.getCurrentStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current status is required");
        }

        if (isPregnant(request) && request.getLastPeriodDate() == null && request.getDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last period date or due date is required for pregnancy tracking");
        }

        if (isPregnant(request)) {
            LocalDate startDate = resolveStartDate(request);
            LocalDate dueDate = resolveDueDate(request);

            if (startDate == null || dueDate == null || !dueDate.isAfter(startDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pregnancy dates are invalid");
            }
        }

        if (Boolean.TRUE.equals(request.getTakesMedication())) {
            if (isBlank(request.getMedicationName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medication name is required");
            }
            if (request.getMedicationTime() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medication time is required");
            }
        }

        if (Boolean.TRUE.equals(request.getRemindersEnabled()) && request.getPreferredReminderTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preferred reminder time is required");
        }

        if ("Oui".equalsIgnoreCase(clean(request.getWantsAppointmentReminder())) && request.getNextAppointmentDateTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date is required");
        }
    }

    private void validateHeight(Integer heightCm) {
        if (heightCm == null || heightCm < 100 || heightCm > 230) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_HEIGHT_MESSAGE);
        }
    }

    private void validateWeight(Double weightKg) {
        if (weightKg == null || weightKg < 30 || weightKg > 250) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_WEIGHT_MESSAGE);
        }
    }

    private PregnancyProfile syncPregnancyProfile(Femme femme, OnboardingRequestDTO request) {
        if (!isPregnant(request)) {
            return null;
        }

        LocalDate startDate = resolveStartDate(request);
        LocalDate dueDate = resolveDueDate(request);

        if (startDate == null || dueDate == null || !dueDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pregnancy dates are invalid");
        }

        PregnancyProfile profile = pregnancyProfileRepository.findByFemmeFemmeId(femme.getFemmeId())
                .orElseGet(PregnancyProfile::new);

        profile.setFemme(femme);
        profile.setStartDate(startDate);
        profile.setDueDate(dueDate);
        if (isBlank(profile.getHealthStatus())) {
            profile.setHealthStatus("Suivi grossesse");
        }
        profile.setNotes("Onboarding MamaCarePlus: profil grossesse synchronise.");

        PregnancyProfile savedProfile = pregnancyProfileRepository.save(profile);
        return savedProfile == null ? profile : savedProfile;
    }

    private boolean createOrUpdateInitialWeightRecordIfNeeded(PregnancyProfile profile, OnboardingRequestDTO request) {
        if (profile == null || request.getWeightKg() == null) {
            return false;
        }

        Optional<WeightRecord> existingRecord = findInitialWeightRecord(profile);
        WeightRecord record = existingRecord.orElseGet(WeightRecord::new);
        record.setPregnancyProfile(profile);
        record.setWeightKg(BigDecimal.valueOf(request.getWeightKg()).setScale(2, RoundingMode.HALF_UP));
        record.setBmi(calculateBmi(request.getWeightKg(), request.getHeightCm()));
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        record.setNotes(INITIAL_WEIGHT_NOTE);

        weightRecordRepository.save(record);
        return true;
    }

    private Optional<WeightRecord> findInitialWeightRecord(PregnancyProfile profile) {
        if (profile.getPregnancyProfileId() == null) {
            return Optional.empty();
        }

        return weightRecordRepository.findFirstByPregnancyProfilePregnancyProfileIdAndNotesOrderByRecordDateDesc(
                profile.getPregnancyProfileId(),
                INITIAL_WEIGHT_NOTE
        );
    }

    private BigDecimal calculateBmi(Double weightKg, Integer heightCm) {
        if (weightKg == null || heightCm == null || heightCm <= 0) {
            return null;
        }

        double heightMeters = heightCm / 100.0;
        return BigDecimal.valueOf(weightKg / (heightMeters * heightMeters))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean createMedicationReminderIfNeeded(Femme femme, OnboardingRequestDTO request) {
        if (!Boolean.TRUE.equals(request.getTakesMedication())) {
            return false;
        }

        MedicationReminder reminder = new MedicationReminder();
        reminder.setFemme(femme);
        reminder.setMedicationName(clean(request.getMedicationName()));
        reminder.setDosage("A preciser");
        reminder.setFrequency("Daily");
        reminder.setReminderTime(request.getMedicationTime());
        reminder.setStartDate(LocalDate.now());
        reminder.setNotes("Cree depuis l'onboarding MamaCarePlus.");
        reminder.setActive(true);
        reminder.setDone(false);

        medicationReminderRepository.save(reminder);
        return true;
    }

    private boolean createAppointmentIfNeeded(Femme femme, OnboardingRequestDTO request) {
        if (!"Oui".equalsIgnoreCase(clean(request.getWantsAppointmentReminder()))) {
            return false;
        }

        Appointment appointment = new Appointment();
        appointment.setFemme(femme);
        appointment.setTitle("Rendez-vous medical");
        appointment.setAppointmentDate(request.getNextAppointmentDateTime());
        appointment.setNotes("Cree depuis l'onboarding MamaCarePlus.");
        appointment.setStatus("pending");

        appointmentRepository.save(appointment);
        return true;
    }

    private Integer calculateRiskScore(OnboardingRequestDTO request) {
        int score = 0;
        if (request == null) {
            return score;
        }

        score += scoreBirthYear(request.getBirthYear());
        score += sumOptionScores("chronicDiseases", request.getChronicDiseases());
        score += sumOptionScores("currentWarningSymptoms", request.getCurrentWarningSymptoms());
        score += sumOptionScores("previousPregnancyProblemTypes", request.getPreviousPregnancyProblemTypes());
        score += scoreForOption("smokingStatus", request.getSmokingStatus());
        score += scoreForOption("medicalFollowUp", request.getMedicalFollowUp());
        score += Boolean.TRUE.equals(request.getTakesMedication()) ? 1 : 0;
        score += scoreForOption("folicAcid", request.getFolicAcid());
        score += Boolean.TRUE.equals(resolveFirstPregnancy(request)) ? 1 : 0;
        return score;
    }

    private Integer scoreBirthYear(Integer birthYear) {
        if (birthYear == null) {
            return 0;
        }

        int age = LocalDate.now().getYear() - birthYear;
        if (age < 18) {
            return 3;
        }
        if (age >= 35 && age <= 39) {
            return 2;
        }
        if (age >= 40) {
            return 3;
        }
        return 0;
    }

    private int sumOptionScores(String fieldKey, List<String> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }

        return values.stream()
                .filter(Objects::nonNull)
                .mapToInt(value -> scoreForOption(fieldKey, value))
                .sum();
    }

    private Integer scoreForOption(String fieldKey, String value) {
        if (isBlank(fieldKey) || isBlank(value)) {
            return 0;
        }

        String normalized = normalize(value);
        return switch (fieldKey) {
            case "chronicDiseases" -> {
                if (normalized.contains("diabete")) yield 3;
                if (normalized.contains("hypertension")) yield 3;
                if (normalized.contains("maladie cardiaque")) yield 3;
                if (normalized.contains("maladie renale")) yield 3;
                if (normalized.contains("thyroide")) yield 2;
                if (normalized.contains("anemie")) yield 1;
                if (normalized.contains("asthme")) yield 1;
                yield 0;
            }
            case "currentWarningSymptoms" -> {
                if (normalized.contains("saignements")) yield 4;
                if (normalized.contains("douleurs fortes")) yield 3;
                if (normalized.contains("maux de tete importants")) yield 3;
                if (normalized.contains("gonflement important")) yield 3;
                if (normalized.contains("fievre")) yield 2;
                if (normalized.contains("vomissements severes")) yield 2;
                if (normalized.contains("diminution des mouvements du bebe")) yield 4;
                yield 0;
            }
            case "previousPregnancyProblemTypes" -> {
                if (normalized.contains("fausse couche")) yield 2;
                if (normalized.contains("accouchement premature")) yield 3;
                if (normalized.contains("hypertension pendant la grossesse")) yield 3;
                if (normalized.contains("diabete gestationnel")) yield 2;
                if (normalized.contains("saignements importants")) yield 3;
                if (normalized.contains("cesarienne")) yield 1;
                yield 0;
            }
            case "smokingStatus" -> {
                if ("oui".equals(normalized)) yield 2;
                if (normalized.contains("arrete recemment")) yield 1;
                yield 0;
            }
            case "medicalFollowUp" -> {
                if ("non".equals(normalized)) yield 3;
                if ("pas encore".equals(normalized)) yield 2;
                yield 0;
            }
            case "takesMedication" -> "oui".equals(normalized) ? 1 : 0;
            case "folicAcid" -> ("non".equals(normalized) || normalized.contains("je ne sais pas")) ? 1 : 0;
            case "firstPregnancy" -> "oui".equals(normalized) ? 1 : 0;
            default -> 0;
        };
    }

    private String resolveRiskLevel(Integer score) {
        if (score <= 3) {
            return "Profil stable";
        }
        if (score <= 7) {
            return "Vigilance mod\u00e9r\u00e9e";
        }
        if (score <= 11) {
            return "Suivi m\u00e9dical recommand\u00e9";
        }
        return "Consultation prioritaire recommand\u00e9e";
    }

    private String resolveRiskDescription(String riskLevel) {
        return switch (riskLevel) {
            case "Vigilance mod\u00e9r\u00e9e" -> "Quelques elements demandent une surveillance attentive.";
            case "Suivi m\u00e9dical recommand\u00e9" -> "Un suivi medical est recommande pour clarifier les signes ou antecedents.";
            case "Consultation prioritaire recommand\u00e9e" -> "Des signes importants justifient une consultation prioritaire.";
            default -> "Aucun facteur de risque majeur detecte dans les reponses.";
        };
    }

    private String resolveRecommendation(String riskLevel) {
        return switch (riskLevel) {
            case "Vigilance mod\u00e9r\u00e9e" -> "Continuez le suivi et signalez tout changement a votre professionnel de sante.";
            case "Suivi m\u00e9dical recommand\u00e9" -> "Planifiez ou confirmez un avis medical pour adapter votre suivi.";
            case "Consultation prioritaire recommand\u00e9e" -> "Contactez rapidement un professionnel de sante ou un service d'urgence si les signes persistent.";
            default -> "Gardez vos rendez-vous habituels et mettez a jour vos informations si votre situation change.";
        };
    }

    private OnboardingResponseDTO mapToResponse(
            QuestionnaireResponse questionnaireResponse,
            RiskAssessment riskAssessment,
            boolean pregnancyProfileUpdated,
            boolean medicationReminderCreated,
            boolean appointmentCreated
    ) {
        Map<String, List<Answer>> answersByField = groupAnswersByField(questionnaireResponse);
        OnboardingResponseDTO response = new OnboardingResponseDTO();
        response.setSuccess(true);
        response.setFemmeId(questionnaireResponse.getFemme().getFemmeId());
        response.setBirthYear(numberAsInteger(firstNumber(answersByField, "birthYear")));
        response.setHeightCm(numberAsInteger(firstNumber(answersByField, "heightCm")));
        response.setWeightKg(firstNumber(answersByField, "weightKg"));
        response.setCurrentStatus(firstValue(answersByField, "currentStatus"));
        response.setLastPeriodDate(parseDateValue(firstValue(answersByField, "lastPeriodDate")));
        response.setDueDate(resolveResponseDueDate(answersByField));
        response.setFirstPregnancy(booleanFromLabel(firstValue(answersByField, "firstPregnancy")));
        response.setMedicalFollowUp(firstValue(answersByField, "medicalFollowUp"));
        response.setTryingDuration(firstValue(answersByField, "tryingDuration"));
        response.setCycleTracking(firstValue(answersByField, "cycleTracking"));
        response.setPreviousPregnancyProblem(firstValue(answersByField, "previousPregnancyProblem"));
        response.setPreviousPregnancyProblemTypes(listValues(answersByField, "previousPregnancyProblemTypes"));
        response.setCurrentWarningSymptoms(listValues(answersByField, "currentWarningSymptoms"));
        response.setChronicDiseases(listValues(answersByField, "chronicDiseases"));
        response.setTakesMedication(booleanFromLabel(firstValue(answersByField, "takesMedication")));
        response.setMedicationName(firstValue(answersByField, "medicationName"));
        response.setMedicationTime(parseTimeValue(firstValue(answersByField, "medicationTime")));
        response.setFolicAcid(firstValue(answersByField, "folicAcid"));
        response.setSmokingStatus(firstValue(answersByField, "smokingStatus"));
        response.setRemindersEnabled(booleanFromLabel(firstValue(answersByField, "remindersEnabled")));
        response.setPreferredReminderTime(parseTimeValue(firstValue(answersByField, "preferredReminderTime")));
        response.setWantsAppointmentReminder(firstValue(answersByField, "wantsAppointmentReminder"));
        response.setNextAppointmentDateTime(parseDateTimeValue(firstValue(answersByField, "nextAppointmentDateTime")));
        response.setRiskScore(riskAssessment == null ? 0 : riskAssessment.getTotalScore());
        response.setRiskLevel(riskAssessment == null ? "Profil stable" : riskAssessment.getRiskLevel());
        response.setOnboardingCompleted(true);
        response.setPregnancyProfileUpdated(pregnancyProfileUpdated);
        response.setMedicationReminderCreated(medicationReminderCreated);
        response.setAppointmentCreated(appointmentCreated);
        response.setMessage("Profil onboarding charge.");
        return response;
    }

    private Map<String, List<Answer>> groupAnswersByField(QuestionnaireResponse response) {
        Map<String, List<Answer>> grouped = new LinkedHashMap<>();
        response.getAnswers().forEach(answer -> {
            String fieldKey = answer.getQuestion().getFieldKey();
            if (!isBlank(fieldKey)) {
                grouped.computeIfAbsent(fieldKey, key -> new ArrayList<>()).add(answer);
            }
        });
        return grouped;
    }

    private String firstValue(Map<String, List<Answer>> answersByField, String fieldKey) {
        return answersByField.getOrDefault(fieldKey, List.of())
                .stream()
                .findFirst()
                .map(this::answerValue)
                .orElse(null);
    }

    private List<String> listValues(Map<String, List<Answer>> answersByField, String fieldKey) {
        return answersByField.getOrDefault(fieldKey, List.of())
                .stream()
                .map(this::answerValue)
                .filter(value -> !isBlank(value))
                .toList();
    }

    private Double firstNumber(Map<String, List<Answer>> answersByField, String fieldKey) {
        return answersByField.getOrDefault(fieldKey, List.of())
                .stream()
                .findFirst()
                .map(Answer::getNumberAnswer)
                .orElse(null);
    }

    private String answerValue(Answer answer) {
        if (answer.getSelectedOption() != null) {
            return answer.getSelectedOption().getLabel();
        }
        return answer.getTextAnswer();
    }

    private Integer numberAsInteger(Double value) {
        return value == null ? null : value.intValue();
    }

    private LocalDate parseDateValue(String value) {
        return isBlank(value) ? null : LocalDate.parse(value.substring(0, Math.min(10, value.length())));
    }

    private LocalTime parseTimeValue(String value) {
        return isBlank(value) ? null : LocalTime.parse(value.substring(0, Math.min(8, value.length())));
    }

    private LocalDateTime parseDateTimeValue(String value) {
        return isBlank(value) ? null : LocalDateTime.parse(value);
    }

    private LocalDate resolveResponseDueDate(Map<String, List<Answer>> answersByField) {
        LocalDate dueDate = parseDateValue(firstValue(answersByField, "dueDate"));
        if (dueDate != null) {
            return dueDate;
        }

        LocalDate lastPeriodDate = parseDateValue(firstValue(answersByField, "lastPeriodDate"));
        return lastPeriodDate == null ? null : lastPeriodDate.plusDays(PREGNANCY_DURATION_DAYS);
    }

    private Boolean booleanFromLabel(String value) {
        if (isBlank(value)) {
            return null;
        }
        return "Oui".equalsIgnoreCase(clean(value));
    }

    private String booleanLabel(Boolean value) {
        if (value == null) {
            return null;
        }
        return Boolean.TRUE.equals(value) ? "Oui" : "Non";
    }

    private boolean isPregnant(OnboardingRequestDTO request) {
        return STATUS_PREGNANT.equalsIgnoreCase(clean(request.getCurrentStatus()));
    }

    private Integer getUserId(Femme femme) {
        return femme.getUser() == null ? null : femme.getUser().getUserId();
    }

    private String getUserEmail(Femme femme) {
        return femme.getUser() == null ? null : femme.getUser().getEmail();
    }

    private String summarizeRequest(OnboardingRequestDTO request) {
        if (request == null) {
            return "null";
        }

        return "birthYear=" + request.getBirthYear()
                + ", heightCm=" + request.getHeightCm()
                + ", weightKg=" + request.getWeightKg()
                + ", currentStatus=" + request.getCurrentStatus()
                + ", previousPregnancyProblem=" + request.getPreviousPregnancyProblem()
                + ", previousPregnancyProblemTypes=" + safeList(request.getPreviousPregnancyProblemTypes())
                + ", currentWarningSymptoms=" + safeList(request.getCurrentWarningSymptoms())
                + ", chronicDiseases=" + safeList(request.getChronicDiseases())
                + ", medicalFollowUp=" + request.getMedicalFollowUp()
                + ", smokingStatus=" + request.getSmokingStatus()
                + ", folicAcid=" + request.getFolicAcid()
                + ", takesMedication=" + request.getTakesMedication()
                + ", firstPregnancy=" + request.getFirstPregnancy()
                + ", remindersEnabled=" + request.getRemindersEnabled()
                + ", wantsAppointmentReminder=" + request.getWantsAppointmentReminder();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private Boolean resolveFirstPregnancy(OnboardingRequestDTO request) {
        if (Boolean.TRUE.equals(request.getFirstPregnancy())) {
            return true;
        }
        String previousPregnancyProblem = clean(request.getPreviousPregnancyProblem());
        if (normalize(previousPregnancyProblem).contains("premiere grossesse")) {
            return true;
        }
        if (!isBlank(previousPregnancyProblem)) {
            return false;
        }
        return request.getFirstPregnancy();
    }

    private LocalDate resolveStartDate(OnboardingRequestDTO request) {
        if (request.getLastPeriodDate() != null) {
            return request.getLastPeriodDate();
        }
        return request.getDueDate() == null ? null : request.getDueDate().minusDays(PREGNANCY_DURATION_DAYS);
    }

    private LocalDate resolveDueDate(OnboardingRequestDTO request) {
        LocalDate lastPeriodDate = request.getLastPeriodDate();
        LocalDate dueDate = request.getDueDate();

        if (lastPeriodDate != null) {
            LocalDate expectedDueDate = lastPeriodDate.plusDays(PREGNANCY_DURATION_DAYS);
            if (dueDate == null) {
                return expectedDueDate;
            }

            if (!isReasonableDueDateForStart(lastPeriodDate, dueDate)) {
                logger.warn(
                        "DPA incoherente ignoree: lastPeriodDate={}, dueDate={}, expectedDueDate={}",
                        lastPeriodDate,
                        dueDate,
                        expectedDueDate
                );
                return expectedDueDate;
            }

            return dueDate;
        }

        return dueDate;
    }

    private boolean isReasonableDueDateForStart(LocalDate startDate, LocalDate dueDate) {
        long pregnancyDays = ChronoUnit.DAYS.between(startDate, dueDate);
        return pregnancyDays >= MIN_REASONABLE_PREGNANCY_DAYS
                && pregnancyDays <= MAX_REASONABLE_PREGNANCY_DAYS;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{Mn}+", "");
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private List<QuestionSpec> onboardingQuestionSpecs() {
        return List.of(
                spec("birthYear", "Annee de naissance", "number", "identity", 10),
                spec("heightCm", "Taille en centimetres", "number", "identity", 20),
                spec("weightKg", "Poids actuel en kilogrammes", "number", "identity", 30),
                spec("currentStatus", "Situation actuelle", "single_choice", "profile", 40,
                        "Je suis enceinte", "J'essaie de tomber enceinte", "Je veux suivre ma sante feminine"),
                spec("lastPeriodDate", "Date des dernieres regles", "date", "pregnancy", 50),
                spec("dueDate", "Date prevue d'accouchement", "date", "pregnancy", 60),
                spec("firstPregnancy", "Premiere grossesse", "single_choice", "pregnancy", 70, "Oui", "Non"),
                spec("medicalFollowUp", "Suivi medical", "single_choice", "pregnancy", 80, "Oui", "Non", "Pas encore"),
                spec("tryingDuration", "Duree des essais", "single_choice", "fertility", 90, "Moins de 3 mois", "3 a 6 mois", "6 a 12 mois", "Plus de 12 mois"),
                spec("cycleTracking", "Suivi du cycle", "single_choice", "fertility", 100, "Oui", "Non"),
                spec("previousPregnancyProblem", "Antecedents de grossesse", "single_choice", "history", 110, "Non", "Oui", "C'est ma premiere grossesse"),
                spec("previousPregnancyProblemTypes", "Types d'antecedents", "multiple_choice", "history", 120,
                        "Fausse couche", "Accouchement premature", "Hypertension pendant la grossesse", "Diabete gestationnel", "Saignements importants", "Cesarienne"),
                spec("currentWarningSymptoms", "Symptomes actuels", "multiple_choice", "warning", 130,
                        "Aucun", "Saignements", "Douleurs fortes", "Maux de tete importants", "Gonflement important", "Fievre", "Vomissements severes", "Diminution des mouvements du bebe"),
                spec("chronicDiseases", "Maladies chroniques", "multiple_choice", "health", 140,
                        "Aucune", "Diabete", "Hypertension", "Maladie cardiaque", "Maladie renale", "Thyroide", "Anemie", "Asthme"),
                spec("takesMedication", "Prend un traitement", "single_choice", "health", 150, "Oui", "Non"),
                spec("medicationName", "Nom du traitement", "text", "health", 160),
                spec("medicationTime", "Heure du traitement", "text", "health", 170),
                spec("folicAcid", "Acide folique", "single_choice", "health", 180, "Oui", "Non", "Je ne sais pas"),
                spec("smokingStatus", "Tabagisme", "single_choice", "health", 190, "Non", "Oui", "J'ai arrete recemment"),
                spec("remindersEnabled", "Rappels actives", "single_choice", "reminders", 200, "Oui", "Non"),
                spec("preferredReminderTime", "Heure preferee de rappel", "text", "reminders", 210),
                spec("wantsAppointmentReminder", "Souhaite un rappel rendez-vous", "single_choice", "appointments", 220, "Oui", "Plus tard", "Non"),
                spec("nextAppointmentDateTime", "Date et heure du prochain rendez-vous", "text", "appointments", 230)
        );
    }

    private QuestionSpec spec(String key, String text, String type, String category, int order, String... options) {
        return new QuestionSpec(key, text, type, category, order, List.of(options));
    }

    private record QuestionSpec(
            String fieldKey,
            String text,
            String type,
            String category,
            int displayOrder,
            List<String> options
    ) {}
}
