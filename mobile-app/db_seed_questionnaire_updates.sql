DO $$
DECLARE
    qid integer;
BEGIN
    UPDATE questions
    SET text = 'Quelle est la date de vos dernieres regles ? (AAAA-MM-JJ ou JJ/MM/AAAA)'
    WHERE question_id = 3;

    UPDATE options SET score_value = 5
    WHERE question_id = 4 AND label = 'Oui';

    UPDATE options SET score_value = 25
    WHERE question_id IN (5, 6, 7, 13, 14, 17, 20, 22) AND label = 'Oui';

    UPDATE options SET score_value = 45
    WHERE question_id IN (8, 9, 10, 11, 12, 15, 16, 18, 19, 21) AND label = 'Oui';

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Votre tension arterielle est-elle superieure a 140/90 ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Votre tension arterielle est-elle superieure a 140/90 ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 45), (qid, 'Je ne sais pas', 10);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Avez-vous des vomissements persistants qui vous empechent de boire ou manger ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Avez-vous des vomissements persistants qui vous empechent de boire ou manger ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 45);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Avez-vous une douleur, rougeur ou gonflement d''un mollet ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Avez-vous une douleur, rougeur ou gonflement d''un mollet ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 45);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Avez-vous eu des convulsions ou une perte de connaissance ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Avez-vous eu des convulsions ou une perte de connaissance ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 45);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Ressentez-vous une tristesse intense, des idees noires ou une anxiete tres forte ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Ressentez-vous une tristesse intense, des idees noires ou une anxiete tres forte ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 25);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Avez-vous des brulures ou douleurs en urinant ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Avez-vous des brulures ou douleurs en urinant ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 25);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM questions
        WHERE questionnaire_id = 1
          AND text = 'Avez-vous des demangeaisons importantes surtout la nuit ?'
    ) THEN
        INSERT INTO questions (questionnaire_id, text, type, created_at)
        VALUES (1, 'Avez-vous des demangeaisons importantes surtout la nuit ?', 'single_choice', NOW())
        RETURNING question_id INTO qid;

        INSERT INTO options (question_id, label, score_value)
        VALUES (qid, 'Non', 0), (qid, 'Oui', 25);
    END IF;
END $$;
