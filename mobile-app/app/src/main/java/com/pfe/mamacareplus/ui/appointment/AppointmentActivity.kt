package com.pfe.mamacareplus.ui.appointment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityAppointmentBinding
import com.pfe.mamacareplus.model.response.appointment.AppointmentResponse
import com.pfe.mamacareplus.ui.common.MamaCareFormDialog
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.ui.main.MainTab
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.viewmodel.AppointmentUiState
import com.pfe.mamacareplus.viewmodel.AppointmentViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

class AppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentBinding
    private val viewModel: AppointmentViewModel by viewModels()
    private val selectedDayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    private var selectedDate: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.from(selectedDate)
    private var currentAppointments: List<AppointmentResponse> = emptyList()
    private var showingListView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setMainContentView(binding.root, MainTab.APPOINTMENTS)

        binding.appointmentsContainer.playSoftEntrance()
        setupListeners()
        observeAppointments()
        renderCalendar()

        viewModel.loadAppointments()
    }

    private fun setupListeners() {
        binding.btnPreviousMonth.applyModernPressAnimation()
        binding.btnNextMonth.applyModernPressAnimation()
        binding.btnListView.applyModernPressAnimation()
        binding.btnAddAppointment.applyModernPressAnimation()

        binding.btnPreviousMonth.setOnClickListener {
            changeMonth(-1)
        }

        binding.btnNextMonth.setOnClickListener {
            changeMonth(1)
        }

        binding.btnListView.setOnClickListener {
            showingListView = !showingListView
            renderAppointments()
        }

        binding.btnAddAppointment.setOnClickListener {
            showAddAppointmentDialog()
        }
    }

    private fun changeMonth(offset: Long) {
        visibleMonth = visibleMonth.plusMonths(offset)
        val selectedDay = min(selectedDate.dayOfMonth, visibleMonth.lengthOfMonth())
        selectedDate = visibleMonth.atDay(selectedDay)
        renderCalendar()
        renderAppointments()
        binding.calendarCard.alpha = 0.72f
        binding.calendarCard.translationY = dpFloat(8f)
        binding.calendarCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220)
            .start()
    }

    private fun observeAppointments() {
        lifecycleScope.launch {
            viewModel.appointmentState.collect { state ->
                when (state) {
                    is AppointmentUiState.Loading -> {
                        binding.tvAppointmentMessage.text = "Chargement des rendez-vous..."
                    }

                    is AppointmentUiState.Success -> {
                        currentAppointments = state.appointments
                        renderCalendar()
                        renderAppointments()
                    }

                    is AppointmentUiState.Error -> {
                        binding.tvAppointmentMessage.text = state.message
                    }
                }
            }
        }
    }

    private fun renderCalendar() {
        binding.tvAppointmentTitle.text = visibleMonth.month
            .getDisplayName(TextStyle.FULL, Locale.FRENCH)
            .replaceFirstChar { it.titlecase(Locale.FRENCH) }
        binding.calendarGrid.removeAllViews()

        val datesWithAppointments = currentAppointments
            .mapNotNull { parseAppointmentDate(it.appointmentDate) }
            .toSet()
        val firstDay = visibleMonth.atDay(1)
        val daysBeforeMonth = firstDay.dayOfWeek.value - 1
        val gridStart = firstDay.minusDays(daysBeforeMonth.toLong())

        repeat(CALENDAR_ROWS) { rowIndex ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(50),
                ).apply {
                    if (rowIndex > 0) topMargin = dp(5)
                }
            }

            repeat(DAYS_IN_WEEK) { columnIndex ->
                val date = gridStart.plusDays((rowIndex * DAYS_IN_WEEK + columnIndex).toLong())
                row.addView(createDayCell(date, datesWithAppointments.contains(date)))
            }

            binding.calendarGrid.addView(row)
        }
    }

    private fun createDayCell(date: LocalDate, hasAppointment: Boolean): View {
        val inVisibleMonth = YearMonth.from(date) == visibleMonth
        val isSelected = date == selectedDate
        val isToday = date == LocalDate.now()

        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            background = selectableBackground()
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener {
                animateDaySelection(this, date)
            }
        }

        val dayNumber = TextView(this).apply {
            text = date.dayOfMonth.toString()
            gravity = Gravity.CENTER
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(
                when {
                    isSelected -> Color.WHITE
                    isToday -> color(R.color.appointment_turquoise_dark)
                    inVisibleMonth -> color(R.color.appointment_text)
                    else -> Color.parseColor("#C8D0D0")
                },
            )
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
            when {
                isSelected -> setBackgroundResource(R.drawable.bg_appointment_selected_day)
                isToday -> setBackgroundResource(R.drawable.bg_appointment_today_day)
            }
        }

        val dot = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(5), dp(5)).apply {
                topMargin = dp(3)
            }
            alpha = if (inVisibleMonth) 1f else 0.35f
            visibility = if (hasAppointment) View.VISIBLE else View.INVISIBLE
            setBackgroundResource(R.drawable.bg_appointment_dot)
        }

        cell.addView(dayNumber)
        cell.addView(dot)
        return cell
    }

    private fun animateDaySelection(dayView: View, date: LocalDate) {
        dayView.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(80)
            .withEndAction {
                selectedDate = date
                visibleMonth = YearMonth.from(date)
                showingListView = false
                dayView.scaleX = 1f
                dayView.scaleY = 1f
                renderCalendar()
                renderAppointments()
                binding.calendarGrid.alpha = 0.82f
                binding.calendarGrid.animate().alpha(1f).setDuration(180).start()
            }
            .start()
    }

    private fun renderAppointments() {
        val appointmentsInMonth = currentAppointments
            .filter {
                parseAppointmentDate(it.appointmentDate)?.let { date -> YearMonth.from(date) == visibleMonth } == true
            }
            .sortedBy { parseAppointmentDateTime(it.appointmentDate) ?: LocalDateTime.MAX }

        val appointmentsForSelectedDay = appointmentsInMonth
            .filter { parseAppointmentDate(it.appointmentDate) == selectedDate }

        binding.calendarCard.visibility = if (showingListView) View.GONE else View.VISIBLE
        binding.btnListView.text = if (showingListView) "Vue calendrier" else "Vue liste"
        binding.tvSelectedDayTitle.text = if (showingListView) {
            "Rendez-vous du mois"
        } else {
            selectedDate.format(selectedDayFormatter)
        }

        binding.tvAppointmentMessage.text = if (appointmentsInMonth.isEmpty()) {
            "Aucun rendez-vous prévu ce mois-ci."
        } else {
            "${appointmentsInMonth.size} rendez-vous prévu(s) ce mois-ci"
        }

        binding.listAppointments.removeAllViews()

        if (showingListView) {
            renderGroupedAppointmentList(appointmentsInMonth)
            return
        }

        if (appointmentsForSelectedDay.isEmpty()) {
            binding.listAppointments.addView(createEmptyState())
            return
        }

        appointmentsForSelectedDay.forEach { appointment ->
            binding.listAppointments.addView(createAppointmentView(appointment))
        }
    }

    private fun renderGroupedAppointmentList(appointments: List<AppointmentResponse>) {
        if (appointments.isEmpty()) {
            binding.listAppointments.addView(createEmptyState())
            return
        }

        appointments.groupBy { parseAppointmentDate(it.appointmentDate) }
            .toSortedMap(compareBy(nullsLast()) { it })
            .forEach { (date, dayAppointments) ->
                binding.listAppointments.addView(createDateHeader(date, dayAppointments.size))
                dayAppointments.forEach { appointment ->
                    binding.listAppointments.addView(createAppointmentView(appointment))
                }
            }
    }

    private fun createDateHeader(date: LocalDate?, count: Int): View {
        return TextView(this).apply {
            text = "${date?.format(selectedDayFormatter) ?: "Date non définie"}  |  $count"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(color(R.color.appointment_turquoise_dark))
            setPadding(0, dp(8), 0, dp(8))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun createAppointmentView(appointment: AppointmentResponse): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(13), dp(12), dp(13))
            setBackgroundResource(R.drawable.bg_appointment_item_card)
            elevation = dpFloat(1f)
            isClickable = true
            background = layeredSelectableBackground(R.drawable.bg_appointment_item_card)
            applyModernPressAnimation()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = dp(12)
            }
            setOnClickListener { showAppointmentActions(appointment) }
        }

        val iconHolder = FrameLayout(this).apply {
            setBackgroundResource(R.drawable.bg_appointment_icon_circle)
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46)).apply {
                marginEnd = dp(13)
            }
        }

        val icon = ImageView(this).apply {
            setImageResource(appointmentIconRes(appointment))
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER)
        }
        iconHolder.addView(icon)

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = appointment.title
            textSize = 15.5f
            setTextColor(color(R.color.appointment_text))
            setTypeface(null, Typeface.BOLD)
            includeFontPadding = false
        }

        val meta = TextView(this).apply {
            text = "${formatAppointmentTime(appointment.appointmentDate)}  |  ${appointment.doctorName ?: "Médecin non renseigné"}"
            textSize = 12.5f
            setTextColor(color(R.color.appointment_muted))
            setPadding(0, dp(7), 0, 0)
            includeFontPadding = false
        }

        val status = TextView(this).apply {
            text = formatStatus(appointment.status)
            textSize = 11.5f
            setTextColor(statusColor(appointment.status))
            setPadding(0, dp(7), 0, 0)
            includeFontPadding = false
        }

        val arrow = ImageView(this).apply {
            setImageResource(R.drawable.ic_chevron_right_24)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                marginStart = dp(8)
            }
        }

        textColumn.addView(title)
        textColumn.addView(meta)
        textColumn.addView(status)
        container.addView(iconHolder)
        container.addView(textColumn)
        container.addView(arrow)

        return container
    }

    private fun createEmptyState(): View {
        return TextView(this).apply {
            text = if (showingListView) {
                "Aucun rendez-vous ce mois-ci."
            } else {
                "Aucun rendez-vous pour ce jour."
            }
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(color(R.color.appointment_muted))
            setPadding(dp(18))
            setBackgroundResource(R.drawable.bg_appointment_item_card)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    private fun showAddAppointmentDialog() {
        showAppointmentFormDialog(appointment = null)
    }

    private fun showEditAppointmentDialog(appointment: AppointmentResponse) {
        showAppointmentFormDialog(appointment)
    }

    private fun showAppointmentFormDialog(appointment: AppointmentResponse?) {
        val isEditing = appointment != null
        val titleField = createDialogInput("Titre du rendez-vous", InputType.TYPE_CLASS_TEXT, required = true)
        val titleInput = titleField.editText.apply {
            setText(appointment?.title.orEmpty())
        }
        val dateField = createDialogInput("Date et heure", InputType.TYPE_CLASS_DATETIME, required = true)
        val dateInput = dateField.editText.apply {
            setText(appointment?.appointmentDate ?: "${selectedDate}T09:00:00")
        }
        val doctorField = createDialogInput("Medecin (optionnel)", InputType.TYPE_CLASS_TEXT, required = false)
        val doctorInput = doctorField.editText.apply {
            setText(appointment?.doctorName.orEmpty())
        }
        val locationField = createDialogInput("Lieu (optionnel)", InputType.TYPE_CLASS_TEXT, required = false)
        val locationInput = locationField.editText.apply {
            setText(appointment?.location.orEmpty())
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleField.layout)
            addView(dateField.layout)
            addView(doctorField.layout)
            addView(locationField.layout)
        }

        MamaCareFormDialog(
            context = this,
            title = if (isEditing) "Modifier le rendez-vous" else "Ajouter un rendez-vous",
            content = form,
        ) { dialog ->
            val title = titleInput.text.toString().trim()
            val date = dateInput.text.toString().trim()

            if (title.isBlank() || date.isBlank()) {
                Toast.makeText(this@AppointmentActivity, "Le titre et la date sont requis", Toast.LENGTH_SHORT).show()
                return@MamaCareFormDialog
            }

            if (appointment == null) {
                viewModel.createAppointment(
                    title = title,
                    appointmentDate = date,
                    location = locationInput.text.toString().trim().ifBlank { null },
                    doctorName = doctorInput.text.toString().trim().ifBlank { null },
                    notes = null,
                )
            } else {
                viewModel.updateAppointment(
                    appointmentId = appointment.appointmentId,
                    title = title,
                    appointmentDate = date,
                    location = locationInput.text.toString().trim().ifBlank { null },
                    doctorName = doctorInput.text.toString().trim().ifBlank { null },
                    notes = appointment.notes,
                    status = appointment.status,
                )
            }
            dialog.dismiss()
        }.show()
    }


    private fun showAppointmentActions(appointment: AppointmentResponse) {
        val items = arrayOf("Modifier", "Marquer comme terminé", "Annuler le rendez-vous", "Supprimer")

        MaterialAlertDialogBuilder(this)
            .setTitle(appointment.title)
            .setMessage("${formatAppointmentDate(appointment.appointmentDate)} à ${formatAppointmentTime(appointment.appointmentDate)}\n${appointment.doctorName ?: "Médecin non renseigné"}")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showEditAppointmentDialog(appointment)
                    1 -> viewModel.markCompleted(appointment.appointmentId)
                    2 -> viewModel.cancelAppointment(appointment.appointmentId)
                    3 -> {
                        viewModel.deleteAppointment(appointment.appointmentId)
                        Toast.makeText(this, "Rendez-vous supprimé", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun appointmentIconRes(appointment: AppointmentResponse): Int {
        val text = listOfNotNull(
            appointment.title,
            appointment.notes,
            appointment.doctorName,
            appointment.location,
        ).joinToString(" ").lowercase(Locale.ENGLISH)

        return when {
            text.contains("ultra") ||
                text.contains("echo") ||
                text.contains("échographie") ||
                text.contains("echographie") -> R.drawable.ic_ultrasound_24

            text.contains("medicine") ||
                text.contains("medic") ||
                text.contains("médic") ||
                text.contains("vitamin") ||
                text.contains("pharmacy") -> R.drawable.ic_medicine_24

            text.contains("check") ||
                text.contains("control") ||
                text.contains("contrôle") ||
                text.contains("consult") ||
                text.contains("bilan") -> R.drawable.ic_checkup_24

            text.contains("doctor") ||
                text.contains("médecin") ||
                text.contains("medecin") ||
                text.contains("dr ") ||
                text.contains("gyne") ||
                text.contains("sage") -> R.drawable.ic_doctor_24

            else -> R.drawable.ic_calendar_24
        }
    }

    private fun createDialogInput(hint: String, inputTypeValue: Int, required: Boolean): DialogInput {
        val inputLayout = TextInputLayout(this).apply {
            this.hint = hint
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor = color(R.color.dialog_surface_alt)
            setBoxCornerRadii(dpFloat(16f), dpFloat(16f), dpFloat(16f), dpFloat(16f))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(10)
            }
        }

        val editText = TextInputEditText(inputLayout.context).apply {
            inputType = inputTypeValue
            textSize = 14f
            minHeight = dp(54)
            setSingleLine(true)
            setTextColor(color(R.color.input_text))
            setHintTextColor(color(R.color.input_placeholder))
        }

        inputLayout.addView(editText)
        inputLayout.applyPremiumInputFeedback(editText, invalidWhenBlank = required) { value ->
            !required || value.trim().isNotBlank()
        }

        return DialogInput(inputLayout, editText)
    }

    private fun parseAppointmentDate(value: String): LocalDate? {
        return parseAppointmentDateTime(value)?.toLocalDate()
            ?: runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
    }

    private fun parseAppointmentDateTime(value: String): LocalDateTime? {
        val trimmed = value.trim()

        return runCatching { LocalDateTime.parse(trimmed) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(trimmed).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDate.parse(trimmed.take(10)).atStartOfDay() }.getOrNull()
    }

    private fun formatAppointmentDate(value: String): String {
        return parseAppointmentDate(value)?.format(selectedDayFormatter) ?: value
    }

    private fun formatAppointmentTime(value: String): String {
        return parseAppointmentDateTime(value)?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Heure non définie"
    }

    private fun formatStatus(status: String): String {
        return when (status.lowercase(Locale.FRENCH)) {
            "completed" -> "Terminé"
            "cancelled", "canceled" -> "Annulé"
            else -> "Planifié"
        }
    }

    private fun statusColor(status: String): Int {
        return when (status.lowercase(Locale.ENGLISH)) {
            "completed" -> color(R.color.appointment_turquoise_dark)
            "cancelled", "canceled" -> Color.parseColor("#C56A6A")
            else -> color(R.color.appointment_turquoise)
        }
    }

    private fun selectableBackground(): android.graphics.drawable.Drawable? {
        val attrs = intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
        val typedArray = obtainStyledAttributes(attrs)
        return typedArray.getDrawable(0).also { typedArray.recycle() }
    }

    private fun layeredSelectableBackground(backgroundRes: Int): android.graphics.drawable.Drawable {
        val base = getDrawable(backgroundRes)!!
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray = obtainStyledAttributes(attrs)
        val ripple = typedArray.getDrawable(0)
        typedArray.recycle()
        return android.graphics.drawable.LayerDrawable(listOfNotNull(base, ripple).toTypedArray())
    }

    private fun color(resId: Int): Int = getColor(resId)

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun dpFloat(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private data class DialogInput(
        val layout: TextInputLayout,
        val editText: TextInputEditText
    )

    companion object {
        private const val CALENDAR_ROWS = 6
        private const val DAYS_IN_WEEK = 7
    }
}
