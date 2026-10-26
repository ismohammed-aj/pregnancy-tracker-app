package com.pfe.mamacareplus.ui.questionnaire

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityQuestionnaireBinding
import com.pfe.mamacareplus.model.onboarding.OnboardingQuestion
import com.pfe.mamacareplus.model.onboarding.QuestionCondition
import com.pfe.mamacareplus.model.onboarding.QuestionType
import com.pfe.mamacareplus.model.request.onboarding.OnboardingRequest
import com.pfe.mamacareplus.model.response.onboarding.OnboardingResponse
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback
import com.pfe.mamacareplus.ui.home.HomeActivity
import com.pfe.mamacareplus.viewmodel.OnboardingViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class QuestionnaireActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionnaireBinding
    private val viewModel: OnboardingViewModel by viewModels()

    private val answers = mutableMapOf<String, Any?>()
    private var currentQuestionIndex = 0
    private var finalResponse: OnboardingResponse? = null
    private val pregnancyDurationDays = 280L

    private val primaryColor = Color.parseColor("#D96B8C")
    private val strongPinkColor = Color.parseColor("#D6336C")
    private val darkTextColor = Color.parseColor("#2D1B24")
    private val secondaryTextColor = Color.parseColor("#8A7180")
    private val softPinkColor = Color.parseColor("#FFF0F6")
    private val borderColor = Color.parseColor("#F0C9D8")
    private val cardColor = Color.parseColor("#FFFFFC")
    private val paleCardColor = Color.parseColor("#F8F3F6")

    private val allQuestions = buildOnboardingQuestions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStaticUi()
        setupListeners()
        observeState()
        renderCurrentQuestion(animated = false)
    }

    private fun setupStaticUi() {
        binding.tvQuestionnaireTitle.gravity = Gravity.CENTER
        binding.tvQuestionnaireMessage.gravity = Gravity.CENTER
        binding.btnPreviousQuestion.visibility = View.GONE
        binding.questionnaireBottomBar.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        binding.btnQuestionnaireBack.setOnClickListener {
            handleBack()
        }

        binding.btnPreviousQuestion.setOnClickListener {
            handleBack()
        }

        binding.btnSkipQuestion.setOnClickListener {
            currentQuestion()?.let { question ->
                answers.remove(question.fieldName)
                goNext()
            }
        }

        binding.btnNextQuestion.setOnClickListener {
            handleNext()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.onboardingState.collect { state ->
                if (state.isLoading) {
                    showSaving()
                    return@collect
                }

                state.errorMessage?.let { message ->
                    Toast.makeText(this@QuestionnaireActivity, message, Toast.LENGTH_LONG).show()
                    renderCurrentQuestion(animated = false)
                    viewModel.resetState()
                    return@collect
                }

                if (state.showRiskResult) {
                    finalResponse = state.response
                    showRiskResultScreen(
                        riskScore = state.riskScore ?: 0,
                        riskLevel = state.riskLevel ?: "Profil stable",
                        response = state.response
                    )
                    viewModel.resetState()
                }
            }
        }
    }

    private fun handleBack() {
        if (finalResponse != null) {
            goHome()
            return
        }

        if (currentQuestionIndex > 0) {
            currentQuestionIndex -= 1
            renderCurrentQuestion()
        } else {
            finish()
        }
    }

    private fun handleNext() {
        val question = currentQuestion() ?: return

        if (!hasAnswer(question)) {
            Toast.makeText(this, validationMessage(question), Toast.LENGTH_SHORT).show()
            return
        }

        goNext()
    }

    private fun goNext() {
        val questions = visibleQuestions()
        if (currentQuestionIndex >= questions.lastIndex) {
            submitOnboarding()
            return
        }

        currentQuestionIndex += 1
        renderCurrentQuestion()
    }

    private fun submitOnboarding() {
        val request = buildRequest()
        if (request == null) {
            Toast.makeText(this, "Complétez les champs requis avant de continuer.", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("ONBOARDING_PAYLOAD", "weightKg=${request.weightKg}")
        viewModel.completeOnboarding(request)
    }

    private fun renderCurrentQuestion(animated: Boolean = true) {
        finalResponse = null
        val questions = visibleQuestions()
        if (questions.isEmpty()) return

        if (currentQuestionIndex >= questions.size) {
            currentQuestionIndex = questions.lastIndex
        }

        val question = questions[currentQuestionIndex]
        val questionNumber = currentQuestionIndex + 1
        val total = questions.size

        binding.tvQuestionnaireTitle.text = question.title
        binding.tvQuestionnaireMessage.text = question.description
        binding.tvProgressBadge.text = "$questionNumber/$total"
        binding.progressQuestionnaire.max = total
        binding.progressQuestionnaire.progress = questionNumber
        binding.questionnaireBottomBar.visibility = View.VISIBLE

        binding.btnQuestionnaireBack.alpha = if (currentQuestionIndex > 0) 1f else 0.65f
        binding.btnSkipQuestion.visibility = if (question.required) View.GONE else View.VISIBLE
        binding.btnNextQuestion.text = if (currentQuestionIndex == total - 1) "Terminer" else "Suivant"
        updateNextButton(question)

        binding.listQuestionnaires.removeAllViews()
        binding.listQuestionnaires.addView(createQuestionContent(question, questionNumber, total))

        if (animated) {
            binding.listQuestionnaires.alpha = 0f
            binding.listQuestionnaires.translationX = 24.dp.toFloat()
            binding.listQuestionnaires.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(180L)
                .start()
        }
    }

    private fun createQuestionContent(question: OnboardingQuestion, questionNumber: Int, total: Int): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            addView(createIconCard(question))
            addSpace(this, 18)
            addView(createPhaseChip(question.phase, questionNumber, total))
            addSpace(this, 22)
            addView(createAnswerView(question))
        }
    }

    private fun createIconCard(question: OnboardingQuestion): View {
        return FrameLayout(this).apply {
            background = roundedBackground(softPinkColor, borderColor, 28.dp.toFloat())
            elevation = 3f
            layoutParams = LinearLayout.LayoutParams(86.dp, 86.dp)
            addView(ImageView(this@QuestionnaireActivity).apply {
                setImageResource(iconFor(question))
                imageTintList = ColorStateList.valueOf(strongPinkColor)
                layoutParams = FrameLayout.LayoutParams(42.dp, 42.dp, Gravity.CENTER)
            })
        }
    }

    private fun createPhaseChip(phase: String, questionNumber: Int, total: Int): View {
        return TextView(this).apply {
            text = "$phase  •  $questionNumber sur $total"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(strongPinkColor)
            gravity = Gravity.CENTER
            setPadding(14.dp, 7.dp, 14.dp, 7.dp)
            background = roundedBackground(Color.WHITE, borderColor, 18.dp.toFloat())
        }
    }

    private fun createAnswerView(question: OnboardingQuestion): View {
        return when (question.type) {
            QuestionType.SINGLE_CHOICE -> createChoiceList(question, multi = false)
            QuestionType.MULTI_CHOICE -> createChoiceList(question, multi = true)
            QuestionType.TEXT_INPUT -> createTextInput(question)
            QuestionType.NUMBER_INPUT -> createNumberInput(question)
            QuestionType.DATE_PICKER -> createPickerCard(question, "Choisir une date") { showDatePicker(question) }
            QuestionType.TIME_PICKER -> createPickerCard(question, "Choisir une heure") { showTimePicker(question) }
            QuestionType.DATE_TIME_PICKER -> createPickerCard(question, "Choisir date et heure") { showDateTimePicker(question) }
            QuestionType.YEAR_PICKER -> createPickerCard(question, "Choisir l'année") { showYearPicker(question) }
        }
    }

    private fun createChoiceList(question: OnboardingQuestion, multi: Boolean): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            question.options.forEach { option ->
                addView(createChoiceCard(question, option, multi))
            }
        }
    }

    private fun createChoiceCard(question: OnboardingQuestion, option: String, multi: Boolean): View {
        val selected = if (multi) {
            selectedMultiValues(question).contains(option)
        } else {
            answers[question.fieldName] == option
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = 66.dp
            setPadding(18.dp, 14.dp, 18.dp, 14.dp)
            background = if (selected) {
                roundedBackground(primaryColor, primaryColor, 20.dp.toFloat())
            } else {
                roundedBackground(if (multi) Color.WHITE else paleCardColor, borderColor, 20.dp.toFloat())
            }
            elevation = if (selected) 4f else 1f
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12.dp)
            }

            addView(ImageView(this@QuestionnaireActivity).apply {
                setImageResource(if (selected) R.drawable.ic_check_circle_24 else R.drawable.ic_radio_empty_24)
                imageTintList = ColorStateList.valueOf(if (selected) Color.WHITE else strongPinkColor)
                layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp)
            })

            addView(TextView(this@QuestionnaireActivity).apply {
                text = option
                textSize = 16f
                setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(if (selected) Color.WHITE else darkTextColor)
                setLineSpacing(3f, 1f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(14.dp, 0, 0, 0)
                }
            })

            setOnClickListener {
                animate()
                    .scaleX(0.98f)
                    .scaleY(0.98f)
                    .setDuration(70L)
                    .withEndAction {
                        scaleX = 1f
                        scaleY = 1f
                        if (multi) {
                            toggleMultiAnswer(question, option)
                        } else {
                            answers[question.fieldName] = option
                        }
                        renderCurrentQuestion()
                    }
                    .start()
            }
        }
    }

    private fun createTextInput(question: OnboardingQuestion): View {
        return createInputLayout(question, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
    }

    private fun createNumberInput(question: OnboardingQuestion): View {
        val inputTypeValue = if (isWeightQuestion(question)) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        } else {
            InputType.TYPE_CLASS_NUMBER
        }

        return createInputLayout(
            question,
            inputTypeValue
        )
    }

    private fun createInputLayout(question: OnboardingQuestion, inputTypeValue: Int): View {
        val inputLayout = TextInputLayout(this).apply {
            hint = question.title
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor = Color.WHITE
            boxStrokeColor = strongPinkColor
            setBoxCornerRadii(18.dp.toFloat(), 18.dp.toFloat(), 18.dp.toFloat(), 18.dp.toFloat())
            startIconDrawable = getDrawable(iconFor(question))
            setStartIconTintList(ColorStateList.valueOf(strongPinkColor))
            hintTextColor = ColorStateList.valueOf(secondaryTextColor)
            suffixText = question.unit
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val editText = TextInputEditText(inputLayout.context).apply {
            setText(answers[question.fieldName]?.toString().orEmpty())
            inputType = inputTypeValue
            minHeight = 58.dp
            textSize = 16f
            setTextColor(darkTextColor)
            setHintTextColor(secondaryTextColor)
        }

        inputLayout.addView(editText)
        inputLayout.applyPremiumInputFeedback(editText, invalidWhenBlank = question.required) { value ->
            when {
                isHeightQuestion(question) -> OnboardingInputParser.isValidHeightCm(value)
                isWeightQuestion(question) -> OnboardingInputParser.isValidWeightKg(value)
                else -> value.isNotBlank() || !question.required
            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString()?.trim().orEmpty()
                if (value.isBlank()) {
                    answers.remove(question.fieldName)
                } else {
                    answers[question.fieldName] = value
                }
                inputLayout.error = when {
                    isHeightQuestion(question) && !OnboardingInputParser.isValidHeightCm(value) ->
                        "Veuillez saisir une taille valide en cm."
                    isWeightQuestion(question) && !OnboardingInputParser.isValidWeightKg(value) ->
                        "Veuillez saisir un poids valide en kg."
                    else -> null
                }
                updateNextButton(question)
            }
        })
        return inputLayout
    }

    private fun createPickerCard(question: OnboardingQuestion, placeholder: String, onClick: () -> Unit): View {
        val value = answers[question.fieldName]?.toString()
        val hasValue = !value.isNullOrBlank()

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            minimumHeight = 68.dp
            setPadding(18.dp, 14.dp, 18.dp, 14.dp)
            background = roundedBackground(if (hasValue) softPinkColor else Color.WHITE, borderColor, 20.dp.toFloat())
            elevation = 2f
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            addView(ImageView(this@QuestionnaireActivity).apply {
                setImageResource(iconFor(question))
                imageTintList = ColorStateList.valueOf(strongPinkColor)
                layoutParams = LinearLayout.LayoutParams(26.dp, 26.dp)
            })

            addView(TextView(this@QuestionnaireActivity).apply {
                text = if (hasValue) displayValue(question, value!!) else placeholder
                textSize = 16f
                setTypeface(null, if (hasValue) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(if (hasValue) darkTextColor else secondaryTextColor)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(14.dp, 0, 0, 0)
                }
            })

            addView(ImageView(this@QuestionnaireActivity).apply {
                setImageResource(R.drawable.ic_chevron_right_24)
                imageTintList = ColorStateList.valueOf(strongPinkColor)
                layoutParams = LinearLayout.LayoutParams(22.dp, 22.dp)
            })

            setOnClickListener { onClick() }
        }
    }

    private fun showYearPicker(question: OnboardingQuestion) {
        val currentYear = LocalDate.now().year
        val initialYear = (answers[question.fieldName] as? Int) ?: (currentYear - 28)
        val picker = NumberPicker(this).apply {
            minValue = currentYear - 60
            maxValue = currentYear - 12
            value = initialYear.coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle("Année de naissance")
            .setView(picker)
            .setPositiveButton("Valider") { _, _ ->
                answers[question.fieldName] = picker.value
                renderCurrentQuestion()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDatePicker(question: OnboardingQuestion) {
        val initialDate = parseDate(answers[question.fieldName]?.toString())
            ?: defaultDateFor(question)
        DatePickerDialog(
            this,
            { _, year, month, day ->
                answers[question.fieldName] = LocalDate.of(year, month + 1, day)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
                renderCurrentQuestion()
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).show()
    }

    private fun defaultDateFor(question: OnboardingQuestion): LocalDate {
        if (question.fieldName == "dueDate") {
            return parseDate(answers["lastPeriodDate"]?.toString())
                ?.plusDays(pregnancyDurationDays)
                ?: LocalDate.now().plusDays(pregnancyDurationDays)
        }

        return LocalDate.now()
    }

    private fun showTimePicker(question: OnboardingQuestion) {
        val initialTime = parseTime(answers[question.fieldName]?.toString()) ?: LocalTime.of(9, 0)
        TimePickerDialog(
            this,
            { _, hour, minute ->
                answers[question.fieldName] = LocalTime.of(hour, minute)
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                renderCurrentQuestion()
            },
            initialTime.hour,
            initialTime.minute,
            true
        ).show()
    }

    private fun showDateTimePicker(question: OnboardingQuestion) {
        val initialDateTime = parseDateTime(answers[question.fieldName]?.toString()) ?: LocalDateTime.now().plusDays(7)
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = LocalDate.of(year, month + 1, day)
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        answers[question.fieldName] = LocalDateTime.of(date, LocalTime.of(hour, minute))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                        renderCurrentQuestion()
                    },
                    initialDateTime.hour,
                    initialDateTime.minute,
                    true
                ).show()
            },
            initialDateTime.year,
            initialDateTime.monthValue - 1,
            initialDateTime.dayOfMonth
        ).show()
    }

    private fun showSaving() {
        binding.tvQuestionnaireTitle.text = "Sauvegarde"
        binding.tvQuestionnaireMessage.text = "Nous préparons votre profil personnalisé."
        binding.tvProgressBadge.text = "OK"
        binding.progressQuestionnaire.max = 100
        binding.progressQuestionnaire.progress = 100
        binding.questionnaireBottomBar.visibility = View.GONE
        binding.listQuestionnaires.removeAllViews()
        binding.listQuestionnaires.addView(createStatusCard("Sauvegarde de votre onboarding..."))
    }

    private fun showRiskResultScreen(riskScore: Int, riskLevel: String, response: OnboardingResponse?) {
        binding.tvQuestionnaireTitle.text = "Votre profil est prêt"
        binding.tvQuestionnaireMessage.text = finalDescription(riskLevel)
        binding.tvProgressBadge.text = "100%"
        binding.progressQuestionnaire.max = 100
        binding.progressQuestionnaire.progress = 100
        binding.questionnaireBottomBar.visibility = View.GONE
        binding.listQuestionnaires.removeAllViews()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(createIconCard(
            OnboardingQuestion(
                id = "final",
                phase = "Profil",
                title = "",
                description = "",
                type = QuestionType.SINGLE_CHOICE,
                fieldName = "final"
            )
        ))
        addSpace(container, 18)
        container.addView(TextView(this).apply {
            text = riskLevel
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(strongPinkColor)
            gravity = Gravity.CENTER
        })
        addSpace(container, 12)
        container.addView(createSummaryCard(riskScore, riskLevel, response))
        addSpace(container, 14)
        container.addView(createDisclaimerCard())
        addSpace(container, 18)
        container.addView(MaterialButton(this).apply {
            text = "Continuer"
            backgroundTintList = ColorStateList.valueOf(strongPinkColor)
            setTextColor(Color.WHITE)
            cornerRadius = 14.dp
            minHeight = 48.dp
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                48.dp
            )
            setOnClickListener { goHome() }
        })

        binding.listQuestionnaires.addView(container)
    }

    private fun createSummaryCard(score: Int, riskLevel: String, response: OnboardingResponse?): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp, 18.dp, 20.dp, 18.dp)
            background = roundedBackground(Color.WHITE, borderColor, 20.dp.toFloat())
            elevation = 3f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(summaryLine("Score de vigilance", score.toString()))
            addView(summaryLine("Niveau", riskLevel))
            addView(summaryLine("Profil grossesse", if (response?.pregnancyProfileUpdated == true) "Mis à jour" else "Non requis"))
            addView(summaryLine("Rappel médicament", if (response?.medicationReminderCreated == true) "Créé" else "Non requis"))
            addView(summaryLine("Rendez-vous", if (response?.appointmentCreated == true) "Créé" else "Non requis"))
        }
    }

    private fun summaryLine(label: String, value: String): View {
        return TextView(this).apply {
            text = "$label : $value"
            textSize = 15f
            setTextColor(darkTextColor)
            setPadding(0, 5.dp, 0, 5.dp)
        }
    }

    private fun createDisclaimerCard(): View {
        return TextView(this).apply {
            text = "Cette évaluation ne remplace pas un avis médical. Elle sert uniquement à adapter les rappels et recommandations générales."
            textSize = 14f
            setTextColor(secondaryTextColor)
            setLineSpacing(4f, 1f)
            gravity = Gravity.CENTER
            setPadding(18.dp, 16.dp, 18.dp, 16.dp)
            background = roundedBackground(softPinkColor, Color.TRANSPARENT, 18.dp.toFloat())
        }
    }

    private fun createStatusCard(message: String): View {
        return TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(secondaryTextColor)
            gravity = Gravity.CENTER
            setPadding(24.dp, 28.dp, 24.dp, 28.dp)
            background = roundedBackground(Color.WHITE, borderColor, 20.dp.toFloat())
        }
    }

    private fun updateNextButton(question: OnboardingQuestion) {
        val enabled = hasAnswer(question)
        binding.btnNextQuestion.isEnabled = enabled
        binding.btnNextQuestion.alpha = if (enabled) 1f else 0.55f
    }

    private fun hasAnswer(question: OnboardingQuestion): Boolean {
        val value = answers[question.fieldName]
        if (value == null) return false

        return when (question.type) {
            QuestionType.MULTI_CHOICE -> selectedMultiValues(question).isNotEmpty()
            QuestionType.NUMBER_INPUT -> when {
                isHeightQuestion(question) -> OnboardingInputParser.isValidHeightCm(value)
                isWeightQuestion(question) -> OnboardingInputParser.isValidWeightKg(value)
                else -> value.toString().isNotBlank()
            }
            else -> value.toString().isNotBlank()
        }
    }

    private fun validationMessage(question: OnboardingQuestion): String {
        return when (question.type) {
            QuestionType.MULTI_CHOICE -> "Sélectionnez au moins une réponse."
            QuestionType.NUMBER_INPUT -> when {
                isHeightQuestion(question) -> "Veuillez saisir une taille valide en cm."
                isWeightQuestion(question) -> "Veuillez saisir un poids valide en kg."
                else -> "Entrez une valeur numérique valide."
            }
            QuestionType.YEAR_PICKER -> "Choisissez votre année de naissance."
            QuestionType.DATE_PICKER -> "Choisissez une date."
            QuestionType.TIME_PICKER -> "Choisissez une heure."
            QuestionType.DATE_TIME_PICKER -> "Choisissez une date et une heure."
            else -> "Sélectionnez une réponse pour continuer."
        }
    }

    private fun isWeightQuestion(question: OnboardingQuestion): Boolean {
        return question.fieldName == "weightKg"
    }

    private fun isHeightQuestion(question: OnboardingQuestion): Boolean {
        return question.fieldName == "heightCm"
    }

    private fun toggleMultiAnswer(question: OnboardingQuestion, option: String) {
        val values = selectedMultiValues(question).toMutableSet()
        val neutralOptions = setOf("Aucune", "Aucun")

        if (neutralOptions.contains(option)) {
            if (values.contains(option)) {
                values.clear()
            } else {
                values.clear()
                values.add(option)
            }
        } else {
            values.removeAll(neutralOptions)
            if (!values.add(option)) {
                values.remove(option)
            }
        }

        if (values.isEmpty()) {
            answers.remove(question.fieldName)
        } else {
            answers[question.fieldName] = values.toList()
        }
    }

    private fun selectedMultiValues(question: OnboardingQuestion): List<String> {
        val value = answers[question.fieldName]
        return when (value) {
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    private fun visibleQuestions(): List<OnboardingQuestion> {
        return allQuestions.filter { question ->
            if (question.fieldName == "previousPregnancyProblemTypes") {
                answers["currentStatus"] == "Je suis enceinte" &&
                    answers["previousPregnancyProblem"]?.toString().equals("Oui", ignoreCase = true)
            } else {
                question.condition?.matches(answers) ?: true
            }
        }
    }

    private fun currentQuestion(): OnboardingQuestion? {
        return visibleQuestions().getOrNull(currentQuestionIndex)
    }

    private fun buildRequest(): OnboardingRequest? {
        val birthYear = when (val value = answers["birthYear"]) {
            is Int -> value
            else -> value?.toString()?.toIntOrNull()
        }
        val heightCm = OnboardingInputParser.parseValidHeightCm(answers["heightCm"]) ?: return null
        val weightKg = OnboardingInputParser.parseValidWeightKg(answers["weightKg"]) ?: return null
        val currentStatus = answers["currentStatus"]?.toString() ?: return null
        val isPregnant = currentStatus == "Je suis enceinte"
        val previousPregnancyProblem = answers["previousPregnancyProblem"]?.toString()
        val takesMedication = yesNoToBoolean(answers["takesMedication"]?.toString())
        val remindersEnabled = yesNoToBoolean(answers["remindersEnabled"]?.toString())
        val wantsAppointmentReminder = answers["wantsAppointmentReminder"]?.toString()
        val wantsAppointment = wantsAppointmentReminder.equals("Oui", ignoreCase = true)

        return OnboardingRequest(
            birthYear = birthYear ?: return null,
            heightCm = heightCm,
            weightKg = weightKg,
            currentStatus = currentStatus,
            lastPeriodDate = answers["lastPeriodDate"]?.toString().takeIf { isPregnant },
            dueDate = answers["dueDate"]?.toString().takeIf { isPregnant },
            firstPregnancy = isFirstPregnancy(previousPregnancyProblem).takeIf { isPregnant },
            medicalFollowUp = answers["medicalFollowUp"]?.toString().takeIf { isPregnant },
            previousPregnancyProblem = previousPregnancyProblem.takeIf { isPregnant },
            previousPregnancyProblemTypes = selectedMultiValuesForField("previousPregnancyProblemTypes")
                .takeIf { isPregnant && previousPregnancyProblem.equals("Oui", ignoreCase = true) }
                ?: emptyList(),
            currentWarningSymptoms = selectedMultiValuesForField("currentWarningSymptoms").takeIf { isPregnant }
                ?: emptyList(),
            chronicDiseases = selectedMultiValuesForField("chronicDiseases"),
            takesMedication = takesMedication,
            medicationName = answers["medicationName"]?.toString().takeIf { takesMedication == true },
            medicationTime = answers["medicationTime"]?.toString().takeIf { takesMedication == true },
            folicAcid = answers["folicAcid"]?.toString(),
            smokingStatus = answers["smokingStatus"]?.toString(),
            remindersEnabled = remindersEnabled,
            preferredReminderTime = answers["preferredReminderTime"]?.toString().takeIf { remindersEnabled == true },
            wantsAppointmentReminder = wantsAppointmentReminder,
            nextAppointmentDateTime = answers["nextAppointmentDateTime"]?.toString().takeIf { wantsAppointment }
        )
    }

    private fun selectedMultiValuesForField(fieldName: String): List<String> {
        val value = answers[fieldName]
        return when (value) {
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    private fun yesNoToBoolean(value: String?): Boolean? {
        return when {
            value.equals("Oui", ignoreCase = true) -> true
            value.equals("Non", ignoreCase = true) -> false
            else -> null
        }
    }

    private fun isFirstPregnancy(value: String?): Boolean {
        return value.equals("C'est ma première grossesse", ignoreCase = true) ||
            value.equals("C’est ma première grossesse", ignoreCase = true)
    }

    private fun finalDescription(riskLevel: String): String {
        return when (riskLevel) {
            "Profil stable" -> "Votre profil ne montre pas de facteur de vigilance important. Votre suivi personnalisé est prêt."
            "Vigilance modérée" -> "Certains éléments nécessitent une attention régulière. Continuez à suivre vos rappels et conseils personnalisés."
            "Suivi médical recommandé" -> "Votre profil contient plusieurs facteurs qui peuvent nécessiter un suivi médical plus attentif. Pensez à consulter un professionnel de santé."
            else -> "Certains symptômes ou facteurs nécessitent une attention rapide. Nous vous recommandons de contacter un professionnel de santé."
        }
    }

    private fun displayValue(question: OnboardingQuestion, value: String): String {
        return when (question.type) {
            QuestionType.TIME_PICKER -> parseTime(value)?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: value
            QuestionType.DATE_TIME_PICKER -> parseDateTime(value)?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) ?: value
            QuestionType.DATE_PICKER -> parseDate(value)?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: value
            else -> value
        }
    }

    private fun parseDate(value: String?): LocalDate? {
        return runCatching {
            value?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it.take(10), DateTimeFormatter.ISO_LOCAL_DATE) }
        }.getOrNull()
    }

    private fun parseTime(value: String?): LocalTime? {
        return runCatching {
            value?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it.take(8), DateTimeFormatter.ISO_LOCAL_TIME) }
        }.getOrNull()
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        return runCatching {
            value?.takeIf { it.isNotBlank() }?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        }.getOrNull()
    }

    private fun iconFor(question: OnboardingQuestion): Int {
        return when (question.fieldName) {
            "birthYear" -> R.drawable.ic_person_24
            "heightCm", "weightKg" -> R.drawable.ic_weight_24
            "currentStatus", "firstPregnancy", "previousPregnancyProblem", "previousPregnancyProblemTypes" -> R.drawable.ic_maternity_health
            "lastPeriodDate", "dueDate", "nextAppointmentDateTime" -> R.drawable.ic_calendar_24
            "medicalFollowUp" -> R.drawable.ic_doctor_24
            "currentWarningSymptoms", "chronicDiseases", "folicAcid", "smokingStatus" -> R.drawable.ic_health_heart_24
            "takesMedication", "medicationName", "medicationTime" -> R.drawable.ic_medicine_24
            "remindersEnabled", "preferredReminderTime" -> R.drawable.ic_notifications_24
            else -> R.drawable.ic_question_24
        }
    }

    private fun roundedBackground(fillColor: Int, strokeColor: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(fillColor)
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(1.dp, strokeColor)
            }
        }
    }

    private fun addSpace(container: LinearLayout, heightDp: Int) {
        container.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightDp.dp
            )
        })
    }

    private fun goHome() {
        startActivity(
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun buildOnboardingQuestions(): List<OnboardingQuestion> {
        val pregnant = QuestionCondition("currentStatus", listOf("Je suis enceinte"))
        val takesMedication = QuestionCondition("takesMedication", listOf("Oui"))
        val remindersEnabled = QuestionCondition("remindersEnabled", listOf("Oui"))
        val wantsAppointment = QuestionCondition("wantsAppointmentReminder", listOf("Oui"))

        return listOf(
            OnboardingQuestion(
                id = "q1_birth_year",
                phase = "Profil personnel",
                title = "Quelle est votre année de naissance ?",
                description = "Cela nous aide à personnaliser les recommandations selon votre âge.",
                type = QuestionType.YEAR_PICKER,
                fieldName = "birthYear"
            ),
            OnboardingQuestion(
                id = "q2_height",
                phase = "Profil personnel",
                title = "Quelle est votre taille ?",
                description = "Cette information aide à suivre l'évolution du poids pendant la grossesse.",
                type = QuestionType.NUMBER_INPUT,
                fieldName = "heightCm",
                unit = "cm"
            ),
            OnboardingQuestion(
                id = "q3_weight",
                phase = "Profil personnel",
                title = "Quel est votre poids actuel ?",
                description = "Le suivi du poids permet de mieux accompagner votre grossesse.",
                type = QuestionType.NUMBER_INPUT,
                fieldName = "weightKg",
                unit = "kg"
            ),
            OnboardingQuestion(
                id = "q4_status",
                phase = "Situation actuelle",
                title = "Quelle est votre situation actuelle ?",
                description = "Nous adapterons le suivi selon votre objectif.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf(
                    "Je suis enceinte",
                    "J'essaie de tomber enceinte",
                    "Je veux suivre ma santé féminine"
                ),
                fieldName = "currentStatus"
            ),
            OnboardingQuestion(
                id = "q5_last_period",
                phase = "Grossesse",
                title = "Quelle est la date de vos dernières règles ?",
                description = "Cette date permet d'estimer la semaine de grossesse et la date prévue d'accouchement.",
                type = QuestionType.DATE_PICKER,
                fieldName = "lastPeriodDate",
                condition = pregnant
            ),
            OnboardingQuestion(
                id = "q6_due_date",
                phase = "Grossesse",
                title = "Connaissez-vous votre date prévue d'accouchement ?",
                description = "Si vous la connaissez déjà, nous l'utiliserons pour améliorer le suivi.",
                type = QuestionType.DATE_PICKER,
                fieldName = "dueDate",
                required = false,
                condition = pregnant
            ),
            OnboardingQuestion(
                id = "q7_previous_pregnancy_problem",
                phase = "Vigilance grossesse",
                title = "Avez-vous déjà eu un problème pendant une grossesse précédente ?",
                description = "Certaines complications passées peuvent nécessiter un suivi plus attentif.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Non", "Oui", "C’est ma première grossesse"),
                fieldName = "previousPregnancyProblem",
                condition = pregnant
            ),
            OnboardingQuestion(
                id = "q7a_previous_pregnancy_problem_types",
                phase = "Vigilance grossesse",
                title = "Quel type de problème avez-vous eu ?",
                description = "Cette information nous aide à personnaliser votre niveau de vigilance.",
                type = QuestionType.MULTI_CHOICE,
                options = listOf(
                    "Fausse couche",
                    "Accouchement prématuré",
                    "Hypertension pendant la grossesse",
                    "Diabète gestationnel",
                    "Césarienne",
                    "Saignements importants",
                    "Autre"
                ),
                fieldName = "previousPregnancyProblemTypes",
                condition = QuestionCondition("previousPregnancyProblem", listOf("Oui"))
            ),
            OnboardingQuestion(
                id = "q8_current_warning_symptoms",
                phase = "Vigilance grossesse",
                title = "Avez-vous actuellement l’un de ces symptômes ?",
                description = "Certains symptômes peuvent nécessiter une consultation médicale rapide.",
                type = QuestionType.MULTI_CHOICE,
                options = listOf(
                    "Aucun",
                    "Douleurs fortes",
                    "Saignements",
                    "Maux de tête importants",
                    "Gonflement important",
                    "Fièvre",
                    "Vomissements sévères",
                    "Diminution des mouvements du bébé",
                    "Autre"
                ),
                fieldName = "currentWarningSymptoms",
                condition = pregnant
            ),
            OnboardingQuestion(
                id = "q8_medical_follow_up",
                phase = "Vigilance grossesse",
                title = "Votre grossesse est-elle suivie par un médecin ou une sage-femme ?",
                description = "Le suivi médical aide à prévenir les complications.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Oui", "Non", "Pas encore"),
                fieldName = "medicalFollowUp",
                condition = pregnant
            ),
            OnboardingQuestion(
                id = "q9_chronic_diseases",
                phase = "Santé",
                title = "Avez-vous une maladie chronique ?",
                description = "Certaines maladies demandent un suivi plus régulier pendant la grossesse.",
                type = QuestionType.MULTI_CHOICE,
                options = listOf(
                    "Aucune",
                    "Diabète",
                    "Hypertension",
                    "Thyroïde",
                    "Anémie",
                    "Asthme",
                    "Maladie cardiaque",
                    "Maladie rénale",
                    "Autre"
                ),
                fieldName = "chronicDiseases"
            ),
            OnboardingQuestion(
                id = "q10_takes_medication",
                phase = "Santé",
                title = "Prenez-vous actuellement des médicaments ?",
                description = "Nous pouvons aussi vous aider à créer des rappels pour vos traitements.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Oui", "Non"),
                fieldName = "takesMedication"
            ),
            OnboardingQuestion(
                id = "q10a_medication_name",
                phase = "Santé",
                title = "Quel médicament prenez-vous ?",
                description = "Ajoutez le nom du médicament pour créer un rappel.",
                type = QuestionType.TEXT_INPUT,
                fieldName = "medicationName",
                condition = takesMedication
            ),
            OnboardingQuestion(
                id = "q10b_medication_time",
                phase = "Santé",
                title = "Quand devez-vous le prendre ?",
                description = "Nous utiliserons cette heure pour créer un rappel.",
                type = QuestionType.TIME_PICKER,
                fieldName = "medicationTime",
                condition = takesMedication
            ),
            OnboardingQuestion(
                id = "q11_folic_acid",
                phase = "Santé",
                title = "Prenez-vous de l’acide folique ?",
                description = "L’acide folique est souvent recommandé avant et pendant le début de grossesse.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Oui", "Non", "Je ne sais pas"),
                fieldName = "folicAcid"
            ),
            OnboardingQuestion(
                id = "q12_smoking",
                phase = "Santé",
                title = "Fumez-vous actuellement ?",
                description = "Cette information permet d'adapter les conseils de prévention.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Non", "Oui", "J’ai arrêté récemment"),
                fieldName = "smokingStatus"
            ),
            OnboardingQuestion(
                id = "q13_reminders",
                phase = "Notifications",
                title = "Souhaitez-vous recevoir des rappels ?",
                description = "Vous pouvez recevoir des rappels pour les médicaments, rendez-vous et conseils.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Oui", "Non"),
                fieldName = "remindersEnabled"
            ),
            OnboardingQuestion(
                id = "q13a_preferred_time",
                phase = "Notifications",
                title = "À quelle heure préférez-vous recevoir vos rappels ?",
                description = "Choisissez une heure confortable pour vous.",
                type = QuestionType.TIME_PICKER,
                fieldName = "preferredReminderTime",
                condition = remindersEnabled
            ),
            OnboardingQuestion(
                id = "q14_appointment",
                phase = "Notifications",
                title = "Souhaitez-vous enregistrer votre prochain rendez-vous médical ?",
                description = "Nous pouvons vous aider à ne pas oublier votre prochain contrôle.",
                type = QuestionType.SINGLE_CHOICE,
                options = listOf("Oui", "Plus tard"),
                fieldName = "wantsAppointmentReminder"
            ),
            OnboardingQuestion(
                id = "q14a_appointment_date",
                phase = "Notifications",
                title = "Date du rendez-vous",
                description = "Choisissez la date et l'heure de votre prochain contrôle.",
                type = QuestionType.DATE_TIME_PICKER,
                fieldName = "nextAppointmentDateTime",
                condition = wantsAppointment
            )
        )
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
