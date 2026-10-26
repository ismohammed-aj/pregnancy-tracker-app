ALTER TABLE IF EXISTS articles
    ADD COLUMN IF NOT EXISTS video_url VARCHAR(600),
    ADD COLUMN IF NOT EXISTS video_title VARCHAR(200);
@@

ALTER TABLE IF EXISTS baby_developments
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(600),
    ADD COLUMN IF NOT EXISTS comparison_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS comparison_name VARCHAR(120);
@@

CREATE TABLE IF NOT EXISTS medication_reminders (
    id SERIAL PRIMARY KEY,
    femme_id INTEGER NOT NULL,
    medication_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    days_of_week VARCHAR(100),
    reminder_time TIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    notes TEXT,
    active BOOLEAN DEFAULT TRUE,
    done BOOLEAN DEFAULT FALSE,
    last_completed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT medication_reminders_femme_id_fk
        FOREIGN KEY (femme_id) REFERENCES femmes(femme_id) ON DELETE CASCADE
);
@@

ALTER TABLE medication_reminders
    ADD COLUMN IF NOT EXISTS femme_id INTEGER,
    ADD COLUMN IF NOT EXISTS medication_name VARCHAR(150),
    ADD COLUMN IF NOT EXISTS dosage VARCHAR(100),
    ADD COLUMN IF NOT EXISTS frequency VARCHAR(50),
    ADD COLUMN IF NOT EXISTS days_of_week VARCHAR(100),
    ADD COLUMN IF NOT EXISTS reminder_time TIME,
    ADD COLUMN IF NOT EXISTS start_date DATE,
    ADD COLUMN IF NOT EXISTS end_date DATE,
    ADD COLUMN IF NOT EXISTS notes TEXT,
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS done BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS last_completed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
@@

CREATE INDEX IF NOT EXISTS idx_medication_reminders_femme_id
    ON medication_reminders(femme_id);
@@

CREATE INDEX IF NOT EXISTS idx_medication_reminders_active_dates
    ON medication_reminders(femme_id, active, start_date, end_date);
@@

CREATE TABLE IF NOT EXISTS password_reset_codes (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
@@

CREATE INDEX IF NOT EXISTS idx_password_reset_codes_email
    ON password_reset_codes(email);
@@

CREATE INDEX IF NOT EXISTS idx_password_reset_codes_lookup
    ON password_reset_codes(email, code, used);
@@

ALTER TABLE IF EXISTS questions
    ADD COLUMN IF NOT EXISTS field_key VARCHAR(100),
    ADD COLUMN IF NOT EXISTS category VARCHAR(100),
    ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
@@

ALTER TABLE IF EXISTS options
    ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;
@@

ALTER TABLE IF EXISTS answers
    ADD COLUMN IF NOT EXISTS number_answer DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS score INTEGER DEFAULT 0;
@@

ALTER TABLE IF EXISTS questionnaire_responses
    DROP COLUMN IF EXISTS total_score,
    DROP COLUMN IF EXISTS risk_level,
    DROP COLUMN IF EXISTS classification_type;
@@

DO $$
BEGIN
    IF to_regclass('public.femmes') IS NOT NULL
       AND to_regclass('public.questionnaire_responses') IS NOT NULL THEN
        CREATE TABLE IF NOT EXISTS risk_assessments (
            risk_assessment_id SERIAL PRIMARY KEY,
            femme_id INTEGER NOT NULL,
            questionnaire_response_id INTEGER,
            total_score INTEGER NOT NULL DEFAULT 0,
            risk_level VARCHAR(100) NOT NULL,
            description TEXT,
            recommendation TEXT,
            evaluated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT risk_assessments_femme_id_fk
                FOREIGN KEY (femme_id) REFERENCES femmes(femme_id) ON DELETE CASCADE,
            CONSTRAINT risk_assessments_response_id_fk
                FOREIGN KEY (questionnaire_response_id) REFERENCES questionnaire_responses(response_id) ON DELETE SET NULL
        );
    END IF;
END $$;
@@

ALTER TABLE IF EXISTS risk_assessments
    ADD COLUMN IF NOT EXISTS questionnaire_response_id INTEGER;
@@

DO $$
BEGIN
    IF to_regclass('public.risk_assessments') IS NOT NULL
       AND EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'risk_assessments'
              AND column_name = 'response_id'
       ) THEN
        EXECUTE 'UPDATE risk_assessments
                 SET questionnaire_response_id = response_id
                 WHERE questionnaire_response_id IS NULL
                   AND response_id IS NOT NULL';
        EXECUTE 'ALTER TABLE risk_assessments DROP COLUMN response_id';
    END IF;
END $$;
@@

CREATE INDEX IF NOT EXISTS idx_risk_assessments_femme_id
    ON risk_assessments(femme_id);
@@

CREATE INDEX IF NOT EXISTS idx_risk_assessments_evaluated_at
    ON risk_assessments(femme_id, evaluated_at DESC);
@@

CREATE UNIQUE INDEX IF NOT EXISTS idx_risk_assessments_response_unique
    ON risk_assessments(questionnaire_response_id)
    WHERE questionnaire_response_id IS NOT NULL;
@@

CREATE INDEX IF NOT EXISTS idx_questions_questionnaire_field_key
    ON questions(questionnaire_id, field_key);
@@

CREATE INDEX IF NOT EXISTS idx_answers_response_question
    ON answers(response_id, question_id);
@@

INSERT INTO questionnaires(title, description, type, level, created_at)
SELECT 'Onboarding MamaCarePlus',
       'Questionnaire initial pour personnaliser le suivi grossesse et sante.',
       'ONBOARDING',
       'INITIAL',
       CURRENT_TIMESTAMP
WHERE to_regclass('public.questionnaires') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM questionnaires WHERE lower(type) = 'onboarding'
  );
@@

INSERT INTO questions(questionnaire_id, text, type, field_key, category, display_order, active, created_at)
SELECT q.questionnaire_id,
       spec.text,
       spec.type,
       spec.field_key,
       spec.category,
       spec.display_order,
       TRUE,
       CURRENT_TIMESTAMP
FROM questionnaires q
CROSS JOIN (
    VALUES
        ('birthYear', 'Annee de naissance', 'number', 'identity', 10),
        ('heightCm', 'Taille en centimetres', 'number', 'identity', 20),
        ('weightKg', 'Poids actuel en kilogrammes', 'number', 'identity', 30),
        ('currentStatus', 'Situation actuelle', 'single_choice', 'profile', 40),
        ('lastPeriodDate', 'Date des dernieres regles', 'date', 'pregnancy', 50),
        ('dueDate', 'Date prevue d''accouchement', 'date', 'pregnancy', 60),
        ('firstPregnancy', 'Premiere grossesse', 'single_choice', 'pregnancy', 70),
        ('medicalFollowUp', 'Suivi medical', 'single_choice', 'pregnancy', 80),
        ('tryingDuration', 'Duree des essais', 'single_choice', 'fertility', 90),
        ('cycleTracking', 'Suivi du cycle', 'single_choice', 'fertility', 100),
        ('previousPregnancyProblem', 'Antecedents de grossesse', 'single_choice', 'history', 110),
        ('previousPregnancyProblemTypes', 'Types d''antecedents', 'multiple_choice', 'history', 120),
        ('currentWarningSymptoms', 'Symptomes actuels', 'multiple_choice', 'warning', 130),
        ('chronicDiseases', 'Maladies chroniques', 'multiple_choice', 'health', 140),
        ('takesMedication', 'Prend un traitement', 'single_choice', 'health', 150),
        ('medicationName', 'Nom du traitement', 'text', 'health', 160),
        ('medicationTime', 'Heure du traitement', 'text', 'health', 170),
        ('folicAcid', 'Acide folique', 'single_choice', 'health', 180),
        ('smokingStatus', 'Tabagisme', 'single_choice', 'health', 190),
        ('remindersEnabled', 'Rappels actives', 'single_choice', 'reminders', 200),
        ('preferredReminderTime', 'Heure preferee de rappel', 'text', 'reminders', 210),
        ('wantsAppointmentReminder', 'Souhaite un rappel rendez-vous', 'single_choice', 'appointments', 220),
        ('nextAppointmentDateTime', 'Date et heure du prochain rendez-vous', 'text', 'appointments', 230)
) AS spec(field_key, text, type, category, display_order)
WHERE lower(q.type) = 'onboarding'
  AND NOT EXISTS (
      SELECT 1
      FROM questions existing
      WHERE existing.questionnaire_id = q.questionnaire_id
        AND existing.field_key = spec.field_key
  );
@@

INSERT INTO options(question_id, label, score_value, display_order)
SELECT question.question_id,
       option_spec.label,
       option_spec.score_value,
       option_spec.display_order
FROM questions question
JOIN questionnaires questionnaire
  ON questionnaire.questionnaire_id = question.questionnaire_id
CROSS JOIN LATERAL (
    VALUES
        ('currentStatus', 'Je suis enceinte', 0, 1),
        ('currentStatus', 'J''essaie de tomber enceinte', 0, 2),
        ('currentStatus', 'Je veux suivre ma sante feminine', 0, 3),
        ('firstPregnancy', 'Oui', 1, 1),
        ('firstPregnancy', 'Non', 0, 2),
        ('medicalFollowUp', 'Oui', 0, 1),
        ('medicalFollowUp', 'Non', 3, 2),
        ('medicalFollowUp', 'Pas encore', 2, 3),
        ('tryingDuration', 'Moins de 3 mois', 0, 1),
        ('tryingDuration', '3 a 6 mois', 0, 2),
        ('tryingDuration', '6 a 12 mois', 0, 3),
        ('tryingDuration', 'Plus de 12 mois', 0, 4),
        ('cycleTracking', 'Oui', 0, 1),
        ('cycleTracking', 'Non', 0, 2),
        ('previousPregnancyProblem', 'Non', 0, 1),
        ('previousPregnancyProblem', 'Oui', 0, 2),
        ('previousPregnancyProblem', 'C''est ma premiere grossesse', 0, 3),
        ('previousPregnancyProblemTypes', 'Fausse couche', 2, 1),
        ('previousPregnancyProblemTypes', 'Accouchement premature', 3, 2),
        ('previousPregnancyProblemTypes', 'Hypertension pendant la grossesse', 3, 3),
        ('previousPregnancyProblemTypes', 'Diabete gestationnel', 2, 4),
        ('previousPregnancyProblemTypes', 'Saignements importants', 3, 5),
        ('previousPregnancyProblemTypes', 'Cesarienne', 1, 6),
        ('currentWarningSymptoms', 'Aucun', 0, 1),
        ('currentWarningSymptoms', 'Saignements', 4, 2),
        ('currentWarningSymptoms', 'Douleurs fortes', 3, 3),
        ('currentWarningSymptoms', 'Maux de tete importants', 3, 4),
        ('currentWarningSymptoms', 'Gonflement important', 3, 5),
        ('currentWarningSymptoms', 'Fievre', 2, 6),
        ('currentWarningSymptoms', 'Vomissements severes', 2, 7),
        ('currentWarningSymptoms', 'Diminution des mouvements du bebe', 4, 8),
        ('chronicDiseases', 'Aucune', 0, 1),
        ('chronicDiseases', 'Diabete', 3, 2),
        ('chronicDiseases', 'Hypertension', 3, 3),
        ('chronicDiseases', 'Maladie cardiaque', 3, 4),
        ('chronicDiseases', 'Maladie renale', 3, 5),
        ('chronicDiseases', 'Thyroide', 2, 6),
        ('chronicDiseases', 'Anemie', 1, 7),
        ('chronicDiseases', 'Asthme', 1, 8),
        ('takesMedication', 'Oui', 1, 1),
        ('takesMedication', 'Non', 0, 2),
        ('folicAcid', 'Oui', 0, 1),
        ('folicAcid', 'Non', 1, 2),
        ('folicAcid', 'Je ne sais pas', 1, 3),
        ('smokingStatus', 'Non', 0, 1),
        ('smokingStatus', 'Oui', 2, 2),
        ('smokingStatus', 'J''ai arrete recemment', 1, 3),
        ('remindersEnabled', 'Oui', 0, 1),
        ('remindersEnabled', 'Non', 0, 2),
        ('wantsAppointmentReminder', 'Oui', 0, 1),
        ('wantsAppointmentReminder', 'Plus tard', 0, 2),
        ('wantsAppointmentReminder', 'Non', 0, 3)
) AS option_spec(field_key, label, score_value, display_order)
WHERE lower(questionnaire.type) = 'onboarding'
  AND question.field_key = option_spec.field_key
  AND NOT EXISTS (
      SELECT 1
      FROM options existing
      WHERE existing.question_id = question.question_id
        AND lower(existing.label) = lower(option_spec.label)
  );
@@

DO $$
DECLARE
    has_old_onboarding_columns BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'femmes'
          AND column_name = 'birth_year'
    )
    INTO has_old_onboarding_columns;

    IF has_old_onboarding_columns
       AND to_regclass('public.questionnaires') IS NOT NULL
       AND to_regclass('public.questions') IS NOT NULL
       AND to_regclass('public.questionnaire_responses') IS NOT NULL
       AND to_regclass('public.answers') IS NOT NULL THEN
        EXECUTE $migration$
            INSERT INTO questionnaire_responses(femme_id, questionnaire_id, completed_at)
            SELECT f.femme_id,
                   q.questionnaire_id,
                   COALESCE(f.updated_at, f.created_at, CURRENT_TIMESTAMP)
            FROM femmes f
            JOIN questionnaires q ON lower(q.type) = 'onboarding'
            WHERE (
                    COALESCE(f.onboarding_completed, FALSE) = TRUE
                    OR f.birth_year IS NOT NULL
                    OR f.height_cm IS NOT NULL
                    OR f.weight_kg IS NOT NULL
                    OR f.current_status IS NOT NULL
                    OR f.risk_score IS NOT NULL
                    OR f.risk_level IS NOT NULL
                  )
              AND NOT EXISTS (
                    SELECT 1
                    FROM questionnaire_responses existing
                    WHERE existing.femme_id = f.femme_id
                      AND existing.questionnaire_id = q.questionnaire_id
              )
        $migration$;

        EXECUTE $migration$
            INSERT INTO answers(response_id, question_id, text_answer, number_answer, score, answered_at)
            SELECT qr.response_id,
                   qu.question_id,
                   values_to_migrate.text_answer,
                   values_to_migrate.number_answer,
                   values_to_migrate.score,
                   qr.completed_at
            FROM femmes f
            JOIN questionnaires q ON lower(q.type) = 'onboarding'
            JOIN questionnaire_responses qr
              ON qr.femme_id = f.femme_id
             AND qr.questionnaire_id = q.questionnaire_id
            CROSS JOIN LATERAL (
                VALUES
                    ('birthYear', NULLIF(f.birth_year::text, ''), f.birth_year::double precision,
                        CASE
                            WHEN f.birth_year IS NULL THEN 0
                            WHEN EXTRACT(YEAR FROM CURRENT_DATE)::integer - f.birth_year < 18 THEN 3
                            WHEN EXTRACT(YEAR FROM CURRENT_DATE)::integer - f.birth_year BETWEEN 35 AND 39 THEN 2
                            WHEN EXTRACT(YEAR FROM CURRENT_DATE)::integer - f.birth_year >= 40 THEN 3
                            ELSE 0
                        END),
                    ('heightCm', NULLIF(f.height_cm::text, ''), f.height_cm::double precision, 0),
                    ('weightKg', NULLIF(f.weight_kg::text, ''), f.weight_kg::double precision, 0),
                    ('currentStatus', f.current_status, NULL::double precision, 0),
                    ('lastPeriodDate', f.last_period_date::text, NULL::double precision, 0),
                    ('dueDate', f.due_date::text, NULL::double precision, 0),
                    ('firstPregnancy', CASE WHEN f.first_pregnancy IS NULL THEN NULL WHEN f.first_pregnancy THEN 'Oui' ELSE 'Non' END, NULL::double precision, CASE WHEN f.first_pregnancy THEN 1 ELSE 0 END),
                    ('medicalFollowUp', f.medical_follow_up, NULL::double precision, 0),
                    ('tryingDuration', f.trying_duration, NULL::double precision, 0),
                    ('cycleTracking', f.cycle_tracking, NULL::double precision, 0),
                    ('previousPregnancyProblem', f.previous_pregnancy_problem, NULL::double precision, 0),
                    ('previousPregnancyProblemTypes', f.previous_pregnancy_problem_types, NULL::double precision, 0),
                    ('currentWarningSymptoms', f.current_warning_symptoms, NULL::double precision, 0),
                    ('chronicDiseases', f.chronic_diseases, NULL::double precision, 0),
                    ('takesMedication', CASE WHEN f.takes_medication IS NULL THEN NULL WHEN f.takes_medication THEN 'Oui' ELSE 'Non' END, NULL::double precision, CASE WHEN f.takes_medication THEN 1 ELSE 0 END),
                    ('medicationName', f.medication_name, NULL::double precision, 0),
                    ('medicationTime', f.medication_time::text, NULL::double precision, 0),
                    ('folicAcid', f.folic_acid, NULL::double precision, 0),
                    ('smokingStatus', f.smoking_status, NULL::double precision, 0),
                    ('remindersEnabled', CASE WHEN f.reminders_enabled IS NULL THEN NULL WHEN f.reminders_enabled THEN 'Oui' ELSE 'Non' END, NULL::double precision, 0),
                    ('preferredReminderTime', f.preferred_reminder_time::text, NULL::double precision, 0),
                    ('wantsAppointmentReminder', f.wants_appointment_reminder, NULL::double precision, 0),
                    ('nextAppointmentDateTime', f.next_appointment_date_time::text, NULL::double precision, 0)
            ) AS values_to_migrate(field_key, text_answer, number_answer, score)
            JOIN questions qu
              ON qu.questionnaire_id = q.questionnaire_id
             AND qu.field_key = values_to_migrate.field_key
            WHERE (values_to_migrate.number_answer IS NOT NULL
                   OR NULLIF(BTRIM(values_to_migrate.text_answer), '') IS NOT NULL)
              AND NOT EXISTS (
                    SELECT 1
                    FROM answers existing
                    WHERE existing.response_id = qr.response_id
                      AND existing.question_id = qu.question_id
              )
        $migration$;

        IF to_regclass('public.risk_assessments') IS NOT NULL THEN
            EXECUTE $migration$
                INSERT INTO risk_assessments(
                    femme_id,
                    questionnaire_response_id,
                    total_score,
                    risk_level,
                    description,
                    recommendation,
                    evaluated_at
                )
                SELECT f.femme_id,
                       qr.response_id,
                       COALESCE(f.risk_score, 0),
                       COALESCE(NULLIF(f.risk_level, ''), 'Profil stable'),
                       'Evaluation migree depuis les anciennes colonnes Femme.',
                       'Consultez les recommandations personnalisees dans MamaCarePlus.',
                       COALESCE(qr.completed_at, f.updated_at, f.created_at, CURRENT_TIMESTAMP)
                FROM femmes f
                JOIN questionnaires q ON lower(q.type) = 'onboarding'
                JOIN questionnaire_responses qr
                  ON qr.femme_id = f.femme_id
                 AND qr.questionnaire_id = q.questionnaire_id
                WHERE (f.risk_score IS NOT NULL OR NULLIF(f.risk_level, '') IS NOT NULL)
                  AND NOT EXISTS (
                        SELECT 1
                        FROM risk_assessments existing
                        WHERE existing.questionnaire_response_id = qr.response_id
                  )
            $migration$;
        END IF;

        IF to_regclass('public.pregnancy_profiles') IS NOT NULL THEN
            EXECUTE $migration$
                INSERT INTO pregnancy_profiles(
                    femme_id,
                    start_date,
                    due_date,
                    health_status,
                    notes,
                    created_at,
                    updated_at
                )
                SELECT f.femme_id,
                       COALESCE(f.last_period_date, f.due_date - INTERVAL '280 days'),
                       COALESCE(f.due_date, f.last_period_date + INTERVAL '280 days'),
                       'Suivi grossesse',
                       'Profil grossesse migre depuis les anciennes colonnes Femme.',
                       COALESCE(f.created_at, CURRENT_TIMESTAMP),
                       COALESCE(f.updated_at, CURRENT_TIMESTAMP)
                FROM femmes f
                WHERE (f.last_period_date IS NOT NULL OR f.due_date IS NOT NULL)
                  AND NOT EXISTS (
                        SELECT 1
                        FROM pregnancy_profiles existing
                        WHERE existing.femme_id = f.femme_id
                  )
            $migration$;
        END IF;

        IF to_regclass('public.weight_records') IS NOT NULL
           AND to_regclass('public.pregnancy_profiles') IS NOT NULL THEN
            EXECUTE $migration$
                INSERT INTO weight_records(
                    pregnancy_profile_id,
                    weight_kg,
                    bmi,
                    record_date,
                    notes,
                    created_at
                )
                SELECT p.pregnancy_profile_id,
                       ROUND(f.weight_kg::numeric, 2),
                       CASE
                           WHEN f.height_cm IS NULL OR f.height_cm <= 0 THEN NULL
                           ELSE ROUND((f.weight_kg / POWER(f.height_cm / 100.0, 2))::numeric, 2)
                       END,
                       COALESCE(f.last_period_date, CURRENT_DATE),
                       'Poids initial migre depuis les anciennes colonnes Femme.',
                       COALESCE(f.created_at, CURRENT_TIMESTAMP)
                FROM femmes f
                JOIN pregnancy_profiles p ON p.femme_id = f.femme_id
                WHERE f.weight_kg IS NOT NULL
                  AND NOT EXISTS (
                        SELECT 1
                        FROM weight_records existing
                        WHERE existing.pregnancy_profile_id = p.pregnancy_profile_id
                          AND existing.notes = 'Poids initial migre depuis les anciennes colonnes Femme.'
                  )
            $migration$;
        END IF;
    END IF;
END $$;
@@

ALTER TABLE IF EXISTS femmes
    DROP COLUMN IF EXISTS birth_year,
    DROP COLUMN IF EXISTS height_cm,
    DROP COLUMN IF EXISTS weight_kg,
    DROP COLUMN IF EXISTS current_status,
    DROP COLUMN IF EXISTS last_period_date,
    DROP COLUMN IF EXISTS due_date,
    DROP COLUMN IF EXISTS first_pregnancy,
    DROP COLUMN IF EXISTS medical_follow_up,
    DROP COLUMN IF EXISTS trying_duration,
    DROP COLUMN IF EXISTS cycle_tracking,
    DROP COLUMN IF EXISTS previous_pregnancy_problem,
    DROP COLUMN IF EXISTS previous_pregnancy_problem_types,
    DROP COLUMN IF EXISTS current_warning_symptoms,
    DROP COLUMN IF EXISTS chronic_diseases,
    DROP COLUMN IF EXISTS takes_medication,
    DROP COLUMN IF EXISTS medication_name,
    DROP COLUMN IF EXISTS medication_time,
    DROP COLUMN IF EXISTS folic_acid,
    DROP COLUMN IF EXISTS smoking_status,
    DROP COLUMN IF EXISTS reminders_enabled,
    DROP COLUMN IF EXISTS preferred_reminder_time,
    DROP COLUMN IF EXISTS wants_appointment_reminder,
    DROP COLUMN IF EXISTS next_appointment_date_time,
    DROP COLUMN IF EXISTS risk_score,
    DROP COLUMN IF EXISTS risk_level,
    DROP COLUMN IF EXISTS onboarding_completed;
@@
