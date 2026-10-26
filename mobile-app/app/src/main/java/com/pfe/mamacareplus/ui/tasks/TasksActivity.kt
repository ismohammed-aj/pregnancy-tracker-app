package com.pfe.mamacareplus.ui.tasks

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityTasksBinding
import com.pfe.mamacareplus.model.request.medication.MedicationReminderRequest
import com.pfe.mamacareplus.model.response.medication.MedicationReminderResponse
import com.pfe.mamacareplus.model.response.task.TaskResponse
import com.pfe.mamacareplus.ui.DatePickerUtils
import com.pfe.mamacareplus.ui.common.MamaCareFormDialog
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.viewmodel.TaskUiState
import com.pfe.mamacareplus.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setMainContentView(binding.root, activeTab = null)

        setupListeners()
        observeTasks()

        viewModel.loadTasks()
        focusAddTaskIfRequested()
    }

    private fun focusAddTaskIfRequested() {
        if (!intent.getBooleanExtra(EXTRA_FOCUS_ADD_TASK, false)) return

        binding.root.post {
            binding.root.smoothScrollTo(0, binding.btnAddTask.top)
            showAddTaskDialog()
        }
    }

    private fun setupListeners() {
        hideEmbeddedTaskForm()
        binding.btnAddTask.applyModernPressAnimation()
        binding.btnStateAction.applyModernPressAnimation()

        binding.btnStateAction.setOnClickListener {
            viewModel.loadTasks()
        }

        binding.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun hideEmbeddedTaskForm() {
        binding.tilTaskTitle.visibility = View.GONE
        binding.tilTaskDescription.visibility = View.GONE
        binding.tilTaskDueDate.visibility = View.GONE
        binding.tilTaskDueTime.visibility = View.GONE
        binding.btnAddTask.text = "Ajouter une tâche"
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            viewModel.taskState.collect { state ->
                when (state) {
                    is TaskUiState.Loading -> showLoadingState()
                    is TaskUiState.Success -> renderTasks(state.tasks, state.medicationReminders)
                    is TaskUiState.Error -> showErrorState(state.message)
                }
            }
        }
    }

    private fun renderTasks(
        tasks: List<TaskResponse>,
        medicationReminders: List<MedicationReminderResponse>
    ) {
        binding.listTasks.removeAllViews()

        val pendingCount = tasks.count { !it.isCompleted }
        val completedCount = tasks.count { it.isCompleted }
        val importantCount = tasks.count { isImportantTask(it) }

        binding.tvTasksMessage.text = "Planifiez des étapes simples pour une journée plus calme."
        binding.tvTaskOverview.text = buildString {
            append("$pendingCount en attente")
            append("  |  $completedCount terminée(s)")
            if (importantCount > 0) {
                append("  |  $importantCount importante(s)")
            }
            if (medicationReminders.isNotEmpty()) {
                append("  |  ${medicationReminders.size} médicament(s)")
            }
        }

        if (tasks.isEmpty() && medicationReminders.isEmpty()) {
            showEmptyState()
            return
        }

        hideState()
        binding.listTasks.visibility = View.VISIBLE

        if (medicationReminders.isNotEmpty()) {
            binding.listTasks.addView(sectionHeader("Médicaments du jour", "${medicationReminders.size} rappel(s)"))
            medicationReminders
                .sortedBy { parseLocalTime(it.reminderTime) ?: LocalTime.MAX }
                .forEach { reminder ->
                    binding.listTasks.addView(createMedicationReminderView(reminder))
                }
        }

        if (tasks.isNotEmpty()) {
            binding.listTasks.addView(sectionHeader("Tâches de soin", "$pendingCount en attente"))
            tasks.sortedWith(
                compareBy<TaskResponse> { it.isCompleted }
                    .thenBy { parseTaskDateTime(it.dueDate) ?: LocalDateTime.MAX }
            ).forEach { task ->
                binding.listTasks.addView(createTaskView(task))
            }
        }
    }

    private fun showLoadingState() {
        binding.listTasks.removeAllViews()
        binding.listTasks.visibility = View.GONE
        binding.stateContainer.visibility = View.VISIBLE
        binding.progressState.visibility = View.VISIBLE
        binding.ivStateIcon.visibility = View.GONE
        binding.btnStateAction.visibility = View.GONE
        binding.tvStateTitle.text = "Chargement des tâches"
        binding.tvStateMessage.text = "Veuillez patienter pendant la préparation de votre liste."
        binding.tvTaskOverview.text = "Synchronisation de votre liste..."
        binding.tvTasksMessage.text = "Actualisation des tâches et rappels médicament."
    }

    private fun showErrorState(message: String) {
        binding.listTasks.removeAllViews()
        binding.listTasks.visibility = View.GONE
        binding.stateContainer.visibility = View.VISIBLE
        binding.progressState.visibility = View.GONE
        binding.ivStateIcon.visibility = View.VISIBLE
        binding.ivStateIcon.setImageResource(R.drawable.ic_warning_24)
        binding.btnStateAction.visibility = View.VISIBLE
        binding.tvStateTitle.text = "Impossible de charger les tâches"
        binding.tvStateMessage.text = message
        binding.tvTaskOverview.text = "Problème de connexion"
        binding.tvTasksMessage.text = "Vos tâches existantes n'ont pas été modifiées."
    }

    private fun showEmptyState() {
        binding.listTasks.visibility = View.GONE
        binding.stateContainer.visibility = View.VISIBLE
        binding.progressState.visibility = View.GONE
        binding.ivStateIcon.visibility = View.VISIBLE
        binding.ivStateIcon.setImageResource(R.drawable.ic_task_24)
        binding.btnStateAction.visibility = View.GONE
        binding.tvStateTitle.text = "Aucune tâche de soin"
        binding.tvStateMessage.text = "Ajoutez un petit rappel pour vos rendez-vous, repos, hydratation ou soins."
        binding.tvTaskOverview.text = "0 en attente  |  prêt à planifier"
    }

    private fun hideState() {
        binding.stateContainer.visibility = View.GONE
        binding.progressState.visibility = View.GONE
        binding.btnStateAction.visibility = View.GONE
    }

    private fun sectionHeader(title: String, detail: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(10))

            addView(TextView(this@TasksActivity).apply {
                text = title
                textSize = 16f
                setTextColor(COLOR_TEXT)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(this@TasksActivity).apply {
                text = detail
                textSize = 11f
                setTextColor(COLOR_MUTED)
                setTypeface(null, Typeface.BOLD)
                setPadding(dp(10), dp(5), dp(10), dp(5))
                background = roundedBackground(COLOR_CHIP_SOFT, COLOR_CARD_STROKE, 18)
            })
        }
    }

    private fun createMedicationReminderView(reminder: MedicationReminderResponse): View {
        val container = cardContainer(
            fillColor = COLOR_CARD,
            strokeColor = if (reminder.done) COLOR_SUCCESS_STROKE else COLOR_MINT_STROKE
        )

        container.addView(cardHeader(
            iconRes = R.drawable.ic_medicine_24,
            iconBackground = if (reminder.done) COLOR_SUCCESS_SOFT else COLOR_MINT_SOFT,
            title = reminder.title,
            subtitle = "${reminder.dosage} | ${formatReminderTime(reminder.reminderTime)} | ${formatFrequency(reminder.frequency)}",
            completed = reminder.done
        ))

        val chipRow = chipRow().apply {
            addView(
                chip(
                    text = if (reminder.done) "Terminé" else "En attente",
                    backgroundColor = if (reminder.done) COLOR_SUCCESS_SOFT else COLOR_CHIP_SOFT,
                    textColor = if (reminder.done) COLOR_SUCCESS_TEXT else COLOR_PRIMARY_DARK
                )
            )
            addView(chip("Médicament", COLOR_MINT_SOFT, COLOR_MINT_TEXT))
        }
        container.addView(chipRow)

        reminder.notes?.takeIf { it.isNotBlank() }?.let {
            container.addView(descriptionText(it))
        }

        container.addView(actionButton(
            text = if (reminder.done) "Terminé" else "Marquer terminé",
            iconRes = R.drawable.ic_check_circle_24,
            mode = ButtonMode.Primary,
            enabled = !reminder.done
        ).apply {
            layoutParams = fullWidthButtonParams(topMarginDp = 12)
            setOnClickListener {
                viewModel.completeMedicationReminder(reminder.id)
            }
        })

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(8), 0, 0)
            }
        }

        actions.addView(actionButton("Modifier", R.drawable.ic_edit_24, ButtonMode.Outline).apply {
            layoutParams = rowButtonParams(endMarginDp = 8)
            setOnClickListener { showEditMedicationDialog(reminder) }
        })

        actions.addView(actionButton("Supprimer", R.drawable.ic_delete_24, ButtonMode.Destructive).apply {
            layoutParams = rowButtonParams(startMarginDp = 8)
            setOnClickListener {
                viewModel.deleteMedicationReminder(reminder.id)
                Toast.makeText(this@TasksActivity, "Rappel médicament supprimé", Toast.LENGTH_SHORT).show()
            }
        })

        container.addView(actions)
        return container
    }

    private fun createTaskView(task: TaskResponse): View {
        val dueDate = parseTaskDateTime(task.dueDate)
        val important = isImportantTask(task, dueDate)
        val completed = task.isCompleted

        val container = cardContainer(
            fillColor = when {
                completed -> COLOR_COMPLETED_CARD
                important -> COLOR_IMPORTANT_CARD
                else -> COLOR_CARD
            },
            strokeColor = when {
                completed -> COLOR_SUCCESS_STROKE
                important -> COLOR_IMPORTANT_STROKE
                else -> COLOR_CARD_STROKE
            }
        )

        container.addView(cardHeader(
            iconRes = if (completed) R.drawable.ic_check_circle_24 else if (important) R.drawable.ic_warning_24 else R.drawable.ic_task_24,
            iconBackground = when {
                completed -> COLOR_SUCCESS_SOFT
                important -> COLOR_IMPORTANT_SOFT
                else -> COLOR_CHIP_SOFT
            },
            title = task.title,
            subtitle = task.description?.takeIf { it.isNotBlank() } ?: "Aucune description ajoutée",
            completed = completed
        ))

        val chipRow = chipRow().apply {
            addView(
                chip(
                    text = when {
                        completed -> "Terminée"
                        important -> "Important"
                        else -> "En attente"
                    },
                    backgroundColor = when {
                        completed -> COLOR_SUCCESS_SOFT
                        important -> COLOR_IMPORTANT_SOFT
                        else -> COLOR_CHIP_SOFT
                    },
                    textColor = when {
                        completed -> COLOR_SUCCESS_TEXT
                        important -> COLOR_IMPORTANT_TEXT
                        else -> COLOR_PRIMARY_DARK
                    }
                )
            )
            addView(chip(dueDateLabel(dueDate, completed), COLOR_SOFT_SURFACE, COLOR_MUTED))
        }
        container.addView(chipRow)

        container.addView(metaRow(
            iconRes = if (dueDate == null) R.drawable.ic_calendar_24 else R.drawable.ic_clock_24,
            text = dueDateDetail(dueDate),
            tint = if (important) COLOR_IMPORTANT_TEXT else COLOR_PRIMARY_DARK
        ))

        container.addView(actionButton(
            text = if (completed) "Marquer en attente" else "Marquer terminée",
            iconRes = R.drawable.ic_check_circle_24,
            mode = ButtonMode.Primary
        ).apply {
            layoutParams = fullWidthButtonParams(topMarginDp = 12)
            setOnClickListener {
                if (completed) {
                    viewModel.uncompleteTask(task.taskId)
                } else {
                    viewModel.completeTask(task.taskId)
                }
            }
        })

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(8), 0, 0)
            }
        }

        actions.addView(actionButton("Modifier", R.drawable.ic_edit_24, ButtonMode.Outline).apply {
            layoutParams = rowButtonParams(endMarginDp = 8)
            setOnClickListener { showEditTaskDialog(task) }
        })

        actions.addView(actionButton("Supprimer", R.drawable.ic_delete_24, ButtonMode.Destructive).apply {
            layoutParams = rowButtonParams(startMarginDp = 8)
            setOnClickListener { showDeleteTaskDialog(task) }
        })

        container.addView(actions)
        return container
    }

    private fun cardHeader(
        iconRes: Int,
        iconBackground: Int,
        title: String,
        subtitle: String,
        completed: Boolean
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP

            addView(ImageView(this@TasksActivity).apply {
                setImageResource(iconRes)
                setColorFilter(if (completed) COLOR_SUCCESS_TEXT else COLOR_PRIMARY_DARK)
                background = roundedBackground(iconBackground, Color.TRANSPARENT, 16)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44))
            })

            addView(LinearLayout(this@TasksActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dp(12), 0, 0, 0)
                }

                addView(TextView(this@TasksActivity).apply {
                    text = title
                    textSize = 17f
                    setTextColor(COLOR_TEXT)
                    setTypeface(null, Typeface.BOLD)
                    includeFontPadding = false
                    if (completed) {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        alpha = 0.72f
                    }
                })

                addView(TextView(this@TasksActivity).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(COLOR_MUTED)
                    setPadding(0, dp(6), 0, 0)
                    if (completed) {
                        alpha = 0.72f
                    }
                })
            })
        }
    }

    private fun cardContainer(fillColor: Int, strokeColor: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = roundedBackground(fillColor, strokeColor, 22)
            elevation = dpFloat(2f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
        }
    }

    private fun chipRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(12), 0, 0)
            }
        }
    }

    private fun chip(text: String, backgroundColor: Int, textColor: Int): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColor)
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = roundedBackground(backgroundColor, Color.TRANSPARENT, 18)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, dp(8), 0)
            }
        }
    }

    private fun metaRow(iconRes: Int, text: String, tint: Int): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(12), 0, 0)
            }

            addView(ImageView(this@TasksActivity).apply {
                setImageResource(iconRes)
                setColorFilter(tint)
                layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
            })

            addView(TextView(this@TasksActivity).apply {
                this.text = text
                textSize = 12.5f
                setTextColor(COLOR_MUTED)
                setPadding(dp(8), 0, 0, 0)
            })
        }
    }

    private fun descriptionText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(COLOR_MUTED)
            setPadding(0, dp(12), 0, 0)
        }
    }

    private fun actionButton(
        text: String,
        iconRes: Int,
        mode: ButtonMode,
        enabled: Boolean = true
    ): MaterialButton {
        val textColor = when (mode) {
            ButtonMode.Primary -> Color.WHITE
            ButtonMode.Outline -> COLOR_PRIMARY_DARK
            ButtonMode.Destructive -> COLOR_DESTRUCTIVE
        }

        return MaterialButton(this).apply {
            this.text = text
            isEnabled = enabled
            alpha = if (enabled) 1f else 0.55f
            setAllCaps(false)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 12f
            minHeight = dp(38)
            minimumHeight = dp(38)
            insetTop = 0
            insetBottom = 0
            cornerRadius = dp(12)
            setIconResource(iconRes)
            iconPadding = dp(6)
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            iconTint = ColorStateList.valueOf(textColor)
            setTextColor(textColor)
            rippleColor = ColorStateList.valueOf(COLOR_CHIP_SOFT)
            backgroundTintList = ColorStateList.valueOf(
                when (mode) {
                    ButtonMode.Primary -> COLOR_PRIMARY
                    ButtonMode.Outline, ButtonMode.Destructive -> COLOR_CARD
                }
            )
            strokeWidth = if (mode == ButtonMode.Primary) 0 else dp(1)
            strokeColor = ColorStateList.valueOf(
                if (mode == ButtonMode.Destructive) COLOR_DESTRUCTIVE_STROKE else COLOR_CARD_STROKE
            )
            applyModernPressAnimation()
        }
    }

    private fun fullWidthButtonParams(topMarginDp: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(38)
        ).apply {
            setMargins(0, dp(topMarginDp), 0, 0)
        }
    }

    private fun rowButtonParams(startMarginDp: Int = 0, endMarginDp: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, dp(38), 1f).apply {
            setMargins(dp(startMarginDp), 0, dp(endMarginDp), 0)
        }
    }

    private fun showAddTaskDialog() {
        val titleField = dialogInput("Titre de la tâche", "", required = true)
        val titleInput = titleField.editText
        val descriptionField = dialogInput("Description optionnelle", "")
        val descriptionInput = descriptionField.editText.apply {
            minLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        val dueTimeField = dialogInput("Heure", "")
        val dueTimeInput = dueTimeField.editText.apply {
            configureTimePicker(LocalTime.of(9, 0))
        }
        val dueDateField = dialogInput("Date optionnelle", "")
        val dueDateInput = dueDateField.editText.apply {
            configureDatePicker(minDate = LocalDate.now()) {
                if (dueTimeInput.text.isNullOrBlank()) {
                    dueTimeInput.setText(DEFAULT_TASK_TIME)
                }
            }
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleField.layout)
            addView(descriptionField.layout)
            addView(dueDateField.layout)
            addView(dueTimeField.layout)
        }

        MamaCareFormDialog(
            context = this,
            title = "Ajouter une tâche de soin",
            content = form,
        ) { dialog ->
            val title = titleInput.text.toString().trim()
            if (title.isBlank()) {
                Toast.makeText(this, "Titre de tâche requis", Toast.LENGTH_SHORT).show()
                return@MamaCareFormDialog
            }

            val dueDate = try {
                resolveDueDateForRequest(dueDateInput.text.toString(), dueTimeInput.text.toString())
            } catch (error: IllegalArgumentException) {
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                return@MamaCareFormDialog
            }

            viewModel.createTask(
                title = title,
                description = descriptionInput.text.toString().trim().ifBlank { null },
                dueDate = dueDate
            )
            dialog.dismiss()
        }.show()
    }

    private fun showEditTaskDialog(task: TaskResponse) {
        val currentDueDate = parseTaskDateTime(task.dueDate)
        val titleField = dialogInput("Titre de la tâche", task.title, required = true)
        val titleInput = titleField.editText
        val descriptionField = dialogInput("Description optionnelle", task.description.orEmpty())
        val descriptionInput = descriptionField.editText.apply {
            minLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        val dueDateField = dialogInput(
            "Date optionnelle",
            currentDueDate?.toLocalDate()?.format(DateTimeFormatter.ISO_LOCAL_DATE).orEmpty()
        )
        val dueDateInput = dueDateField.editText.apply {
            configureDatePicker(minDate = null)
        }
        val dueTimeField = dialogInput(
            "Heure",
            currentDueDate?.toLocalTime()?.format(TIME_FORMATTER).orEmpty()
        )
        val dueTimeInput = dueTimeField.editText.apply {
            configureTimePicker(LocalTime.of(9, 0))
        }
        val completedInput = CheckBox(this).apply {
            text = "Terminée"
            isChecked = task.isCompleted
            textSize = 14f
            setTextColor(COLOR_TEXT)
            buttonTintList = ColorStateList.valueOf(COLOR_PRIMARY)
            setPadding(0, dp(8), 0, 0)
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleField.layout)
            addView(descriptionField.layout)
            addView(dueDateField.layout)
            addView(dueTimeField.layout)
            addView(completedInput)
        }

        MamaCareFormDialog(
            context = this,
            title = "Modifier la tâche de soin",
            content = form,
        ) { dialog ->
            val title = titleInput.text.toString().trim()
            if (title.isBlank()) {
                Toast.makeText(this, "Titre de tâche requis", Toast.LENGTH_SHORT).show()
                return@MamaCareFormDialog
            }

            val dueDate = try {
                resolveDueDateForRequest(dueDateInput.text.toString(), dueTimeInput.text.toString())
            } catch (error: IllegalArgumentException) {
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                return@MamaCareFormDialog
            }

            viewModel.updateTask(
                taskId = task.taskId,
                title = title,
                description = descriptionInput.text.toString().trim().ifBlank { null },
                dueDate = dueDate,
                isCompleted = completedInput.isChecked
            )
            dialog.dismiss()
        }.show()
    }

    private fun showDeleteTaskDialog(task: TaskResponse) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer la tâche")
            .setMessage("Supprimer \"${task.title}\" de votre liste de soins ?")
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteTask(task.taskId)
                Toast.makeText(this, "Tâche supprimée", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showEditMedicationDialog(reminder: MedicationReminderResponse) {
        val nameField = dialogInput("Nom du médicament", reminder.medicationName, required = true)
        val nameInput = nameField.editText
        val dosageField = dialogInput("Dosage", reminder.dosage, required = true)
        val dosageInput = dosageField.editText
        val frequencyField = dialogInput("Fréquence", reminder.frequency)
        val frequencyInput = frequencyField.editText
        val timeField = dialogInput("Heure", formatReminderTime(reminder.reminderTime), required = true)
        val timeInput = timeField.editText.apply {
            configureTimePicker(LocalTime.now().withSecond(0).withNano(0))
        }
        val startDateField = dialogInput("Date de début", reminder.startDate, required = true)
        val startDateInput = startDateField.editText.apply {
            configureDatePicker(minDate = null)
        }
        val endDateField = dialogInput("Date de fin optionnelle", reminder.endDate.orEmpty())
        val endDateInput = endDateField.editText.apply {
            configureDatePicker(
                minDateProvider = { parseIsoDate(startDateInput.text.toString()) },
                fallbackDateProvider = { startDateInput.text.toString() }
            )
        }
        val notesField = dialogInput("Notes optionnelles", reminder.notes.orEmpty())
        val notesInput = notesField.editText.apply {
            minLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(nameField.layout)
            addView(dosageField.layout)
            addView(frequencyField.layout)
            addView(timeField.layout)
            addView(startDateField.layout)
            addView(endDateField.layout)
            addView(notesField.layout)
        }

        MamaCareFormDialog(
            context = this,
            title = "Modifier le rappel médicament",
            content = form,
        ) { dialog ->
            val startDate = parseIsoDate(startDateInput.text.toString())
            val endDate = parseIsoDate(endDateInput.text.toString())

            when {
                nameInput.text.isNullOrBlank() -> Toast.makeText(this, "Nom du médicament requis", Toast.LENGTH_SHORT).show()
                dosageInput.text.isNullOrBlank() -> Toast.makeText(this, "Dosage requis", Toast.LENGTH_SHORT).show()
                parseReminderTimeForRequest(timeInput.text.toString()).isBlank() -> Toast.makeText(this, "Heure requise", Toast.LENGTH_SHORT).show()
                startDate == null -> Toast.makeText(this, "Date de début requise", Toast.LENGTH_SHORT).show()
                endDate != null && endDate.isBefore(startDate) -> Toast.makeText(this, "La date de fin ne peut pas précéder la date de début", Toast.LENGTH_SHORT).show()
                else -> {
                    viewModel.updateMedicationReminder(
                        reminderId = reminder.id,
                        request = MedicationReminderRequest(
                            medicationName = nameInput.text.toString().trim(),
                            dosage = dosageInput.text.toString().trim(),
                            frequency = frequencyInput.text.toString().trim().ifBlank { "Quotidien" },
                            daysOfWeek = reminder.daysOfWeek,
                            reminderTime = parseReminderTimeForRequest(timeInput.text.toString()),
                            startDate = startDateInput.text.toString().trim(),
                            endDate = endDateInput.text.toString().trim().takeIf { it.isNotBlank() },
                            notes = notesInput.text.toString().trim().takeIf { it.isNotBlank() },
                            active = reminder.active,
                            done = reminder.done
                        )
                    )
                    dialog.dismiss()
                }
            }
        }.show()
    }

    private fun dialogInput(
        hint: String,
        value: String,
        required: Boolean = false
    ): DialogInput {
        val inputLayout = TextInputLayout(this).apply {
            this.hint = hint
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor = getColor(R.color.dialog_surface_alt)
            setBoxCornerRadii(dpFloat(16f), dpFloat(16f), dpFloat(16f), dpFloat(16f))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(10), 0, 0)
            }
        }

        val editText = TextInputEditText(inputLayout.context).apply {
            setText(value)
            textSize = 14f
            minHeight = dp(54)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setTextColor(COLOR_TEXT)
            setHintTextColor(COLOR_MUTED)
        }

        inputLayout.addView(editText)
        inputLayout.applyPremiumInputFeedback(editText, invalidWhenBlank = required) { text ->
            !required || text.trim().isNotBlank()
        }

        return DialogInput(inputLayout, editText)
    }

    private fun EditText.configureDatePicker(
        minDate: LocalDate? = null,
        minDateProvider: (() -> LocalDate?)? = null,
        fallbackDateProvider: (() -> String)? = null,
        onDateSelected: (() -> Unit)? = null
    ) {
        inputType = InputType.TYPE_NULL
        isFocusable = false
        isClickable = true
        setOnClickListener {
            DatePickerUtils.showIsoDatePicker(
                context = this@TasksActivity,
                currentValue = text.toString().ifBlank { fallbackDateProvider?.invoke().orEmpty() },
                minDate = minDateProvider?.invoke() ?: minDate
            ) {
                setText(it)
                onDateSelected?.invoke()
            }
        }
    }

    private fun EditText.configureTimePicker(fallback: LocalTime) {
        inputType = InputType.TYPE_NULL
        isFocusable = false
        isClickable = true
        setOnClickListener {
            showTimePicker(this, fallback)
        }
    }

    private fun showTimePicker(target: EditText, fallback: LocalTime) {
        val current = parseLocalTime(target.text.toString()) ?: fallback
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                target.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute))
            },
            current.hour,
            current.minute,
            true
        ).show()
    }

    private fun resolveDueDateForRequest(dateValue: String, timeValue: String): String? {
        val dateText = dateValue.trim()
        val timeText = timeValue.trim()

        if (dateText.isBlank() && timeText.isBlank()) {
            return null
        }

        if (dateText.isBlank()) {
            throw IllegalArgumentException("Choisissez une date avant de définir une heure")
        }

        val date = parseIsoDate(dateText)
            ?: throw IllegalArgumentException("Utilisez une date valide")
        val time = parseLocalTime(timeText.ifBlank { DEFAULT_TASK_TIME })
            ?: throw IllegalArgumentException("Utilisez une heure valide")

        return LocalDateTime.of(date, time).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    private fun isImportantTask(task: TaskResponse, dueDate: LocalDateTime? = parseTaskDateTime(task.dueDate)): Boolean {
        return !task.isCompleted && dueDate != null && !dueDate.isAfter(LocalDateTime.now().plusDays(1))
    }

    private fun dueDateLabel(dueDate: LocalDateTime?, completed: Boolean): String {
        if (dueDate == null) {
            return "Sans date"
        }

        val today = LocalDate.now()
        return when {
            !completed && dueDate.isBefore(LocalDateTime.now()) -> "En retard"
            dueDate.toLocalDate() == today -> "Aujourd'hui"
            dueDate.toLocalDate() == today.plusDays(1) -> "Demain"
            else -> dueDate.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))
        }
    }

    private fun dueDateDetail(dueDate: LocalDateTime?): String {
        return dueDate?.let {
            val dayText = when (it.toLocalDate()) {
                LocalDate.now() -> "Aujourd'hui"
                LocalDate.now().plusDays(1) -> "Demain"
                else -> it.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH))
            }
            "$dayText à ${it.toLocalTime().format(TIME_FORMATTER)}"
        } ?: "Aucune date ou heure"
    }

    private fun formatFrequency(value: String): String {
        return when (value.trim().lowercase(Locale.ROOT)) {
            "daily", "quotidien", "every day" -> "Quotidien"
            "weekly", "hebdomadaire" -> "Hebdomadaire"
            "monthly", "mensuel" -> "Mensuel"
            "once", "one time", "une fois" -> "Une fois"
            "specific days" -> "Jours précis"
            else -> value.ifBlank { "Fréquence non définie" }
        }
    }

    private fun formatReminderTime(value: String): String {
        return parseLocalTime(value)?.format(TIME_FORMATTER) ?: value
    }

    private fun parseReminderTimeForRequest(value: String): String {
        val time = parseLocalTime(value) ?: return ""
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    private fun parseTaskDateTime(value: String?): LocalDateTime? {
        val text = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { LocalDateTime.parse(text) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(text.take(19)) }.getOrNull()
    }

    private fun parseLocalTime(value: String): LocalTime? {
        val text = value.trim()
        if (text.isBlank()) {
            return null
        }

        return runCatching { LocalTime.parse(text.take(8)) }.getOrNull()
            ?: runCatching { LocalTime.parse(text.take(5)) }.getOrNull()
    }

    private fun parseIsoDate(value: String): LocalDate? {
        return value.trim().takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it.take(10), DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        }
    }

    private fun roundedBackground(fillColor: Int, strokeColor: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpFloat(radiusDp.toFloat())
            setColor(fillColor)
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun dpFloat(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    private enum class ButtonMode {
        Primary,
        Outline,
        Destructive
    }

    private data class DialogInput(
        val layout: TextInputLayout,
        val editText: TextInputEditText
    )

    companion object {
        const val EXTRA_FOCUS_ADD_TASK = "extra_focus_add_task"

        private const val DEFAULT_TASK_TIME = "09:00"

        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        private val COLOR_TEXT = Color.parseColor("#2F2430")
        private val COLOR_MUTED = Color.parseColor("#847482")
        private val COLOR_CARD = Color.parseColor("#FFFFFC")
        private val COLOR_SOFT_SURFACE = Color.parseColor("#FFF9FC")
        private val COLOR_PRIMARY = Color.parseColor("#D96B8C")
        private val COLOR_PRIMARY_DARK = Color.parseColor("#D6336C")
        private val COLOR_CHIP_SOFT = Color.parseColor("#FFF0F6")
        private val COLOR_CARD_STROKE = Color.parseColor("#F2E0E8")
        private val COLOR_SUCCESS_SOFT = Color.parseColor("#EAF7EE")
        private val COLOR_SUCCESS_STROKE = Color.parseColor("#CFEBD9")
        private val COLOR_SUCCESS_TEXT = Color.parseColor("#2F8F5B")
        private val COLOR_COMPLETED_CARD = Color.parseColor("#FBFFFC")
        private val COLOR_IMPORTANT_SOFT = Color.parseColor("#FFF0E6")
        private val COLOR_IMPORTANT_CARD = Color.parseColor("#FFFBF7")
        private val COLOR_IMPORTANT_STROKE = Color.parseColor("#F6CDB8")
        private val COLOR_IMPORTANT_TEXT = Color.parseColor("#C45F31")
        private val COLOR_MINT_SOFT = Color.parseColor("#EFFAF8")
        private val COLOR_MINT_STROKE = Color.parseColor("#D5EFEC")
        private val COLOR_MINT_TEXT = Color.parseColor("#129B93")
        private val COLOR_DESTRUCTIVE = Color.parseColor("#C24B67")
        private val COLOR_DESTRUCTIVE_STROKE = Color.parseColor("#F2D0DA")
    }
}
