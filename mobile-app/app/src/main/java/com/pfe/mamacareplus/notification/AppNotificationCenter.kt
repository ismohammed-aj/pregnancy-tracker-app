package com.pfe.mamacareplus.notification

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.model.response.appointment.AppointmentResponse
import com.pfe.mamacareplus.model.response.medication.MedicationReminderResponse
import com.pfe.mamacareplus.model.response.task.TaskResponse
import com.pfe.mamacareplus.repository.appointment.AppointmentRepository
import com.pfe.mamacareplus.repository.medication.MedicationReminderRepository
import com.pfe.mamacareplus.repository.task.TaskRepository
import com.pfe.mamacareplus.ui.appointment.AppointmentActivity
import com.pfe.mamacareplus.ui.article.ArticlesActivity
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.tasks.TasksActivity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AppNotificationCenter(private val context: Context) {

    private val store = NotificationStore(context.applicationContext)
    private val medicationRepository = MedicationReminderRepository(context.applicationContext)
    private val appointmentRepository = AppointmentRepository(context.applicationContext)
    private val taskRepository = TaskRepository(context.applicationContext)

    suspend fun unreadCount(): Int {
        return loadItems().count { !it.read }
    }

    suspend fun loadItems(): List<AppNotificationItem> {
        val medicationItems = medicationRepository.getTodayReminders()
            .getOrElse { emptyList() }
            .map { it.toNotificationItem() }

        val appointmentItems = appointmentRepository.getAppointments()
            .getOrElse { emptyList() }
            .filter { it.status.lowercase() != "cancelled" && it.status.lowercase() != "canceled" }
            .map { it.toNotificationItem() }

        val taskItems = taskRepository.getTasks()
            .getOrElse { emptyList() }
            .filter { !it.isCompleted }
            .map { it.toNotificationItem() }

        val healthTipItems = listOf(dailyHealthTipItem())

        return (medicationItems + appointmentItems + taskItems + healthTipItems)
            .filterNot { store.isCleared(it.id) }
            .map { it.copy(read = it.read || store.isRead(it.id)) }
            .sortedWith(compareBy<AppNotificationItem> { it.dateTime ?: LocalDateTime.MAX }.thenBy { it.title })
            .take(40)
    }

    suspend fun showNotificationDialog(activity: Activity, onChanged: () -> Unit) {
        val initialItems = loadItems()
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val listContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleRow = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20.dp, 18.dp, 20.dp, 10.dp)
        }

        titleRow.addView(TextView(activity).apply {
            text = "Notifications"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(DARK_TEXT)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        val markAllButton = MaterialButton(activity).apply {
            text = "Tout lire"
            textSize = 12f
            minHeight = 36.dp
            cornerRadius = 14.dp
            applyModernPressAnimation()
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setTextColor(ACCENT)
            strokeColor = android.content.res.ColorStateList.valueOf(SOFT_BORDER)
            strokeWidth = 1.dp
            setOnClickListener {
                val visibleIds = currentVisibleIds(listContainer)
                store.markAllRead(visibleIds)
                renderGroupedNotifications(activity, listContainer, markItemsRead(initialItems), dialog, onChanged)
                onChanged()
            }
        }
        titleRow.addView(markAllButton)

        val subtitle = TextView(activity).apply {
            text = "Médicaments, rendez-vous, tâches et conseils"
            textSize = 13.5f
            setTextColor(MUTED_TEXT)
            setPadding(20.dp, 0, 20.dp, 12.dp)
        }

        val scrollView = ScrollView(activity).apply {
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(listContainer)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                notificationListHeight(activity)
            )
        }

        val closeButton = MaterialButton(activity).apply {
            text = "Fermer"
            minHeight = 44.dp
            cornerRadius = 16.dp
            applyModernPressAnimation()
            backgroundTintList = android.content.res.ColorStateList.valueOf(ACCENT)
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                44.dp
            ).apply {
                setMargins(20.dp, 10.dp, 20.dp, 18.dp)
            }
            setOnClickListener { dialog.dismiss() }
        }

        val root = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedBackground(Color.WHITE, 28.dp, SOFT_BORDER, 1.dp)
            setPadding(0, 0, 0, 0)
            addView(titleRow)
            addView(subtitle)
            addView(scrollView)
            addView(closeButton)
        }

        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnShowListener {
            dialog.window?.setLayout(notificationDialogWidth(activity), ViewGroup.LayoutParams.WRAP_CONTENT)
            root.alpha = 0f
            root.translationY = 18.dp.toFloat()
            root.animate().alpha(1f).translationY(0f).setDuration(180L).start()
        }

        renderGroupedNotifications(activity, listContainer, initialItems, dialog, onChanged)
        dialog.show()
    }

    private fun renderGroupedNotifications(
        activity: Activity,
        listContainer: LinearLayout,
        items: List<AppNotificationItem>,
        dialog: Dialog,
        onChanged: () -> Unit
    ) {
        listContainer.removeAllViews()

        if (items.isEmpty()) {
            listContainer.addView(createEmptyState(activity))
            return
        }

        val groups = groupItems(items)
        var cardIndex = 0
        NotificationGroup.values().forEach { group ->
            val groupItems = groups[group].orEmpty()
            if (groupItems.isEmpty()) return@forEach

            listContainer.addView(createSectionHeader(activity, group.title))
            groupItems.forEach { item ->
                val card = createNotificationCard(activity, item, dialog, onChanged)
                card.alpha = 0f
                card.translationY = 16.dp.toFloat()
                listContainer.addView(card)
                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay((cardIndex * 35L).coerceAtMost(240L))
                    .setDuration(220L)
                    .start()
                cardIndex += 1
            }
        }
    }

    private fun createNotificationCard(
        activity: Activity,
        item: AppNotificationItem,
        dialog: Dialog,
        onChanged: () -> Unit
    ): View {
        val style = styleFor(item)
        val card = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            tag = item.id
            setPadding(14.dp, 14.dp, 14.dp, 12.dp)
            background = notificationCardBackground(item, style)
            elevation = if (item.read) 2.dp.toFloat() else 5.dp.toFloat()
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dp, 0, 16.dp, 12.dp)
            }
            setOnClickListener {
                store.markRead(item.id)
                onChanged()
                openRelatedScreen(activity, item)
                dialog.dismiss()
            }
        }

        attachSwipeAndPress(card, item, onChanged)

        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(FrameLayout(activity).apply {
            background = roundedBackground(style.iconBackground, 18.dp, Color.TRANSPARENT, 0)
            layoutParams = LinearLayout.LayoutParams(44.dp, 44.dp).apply {
                marginEnd = 12.dp
            }
            addView(ImageView(activity).apply {
                setImageResource(item.iconRes)
                setColorFilter(style.accent)
                layoutParams = FrameLayout.LayoutParams(23.dp, 23.dp, Gravity.CENTER)
            })
        })

        header.addView(LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            addView(TextView(activity).apply {
                text = item.title
                textSize = 15.5f
                setTypeface(null, if (item.read) Typeface.NORMAL else Typeface.BOLD)
                setTextColor(DARK_TEXT)
                maxLines = 2
            })

            addView(TextView(activity).apply {
                text = item.timeLabel
                textSize = 12.5f
                setTextColor(style.accent)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 4.dp, 0, 0)
            })
        })

        header.addView(FrameLayout(activity).apply {
            visibility = if (item.read) View.INVISIBLE else View.VISIBLE
            background = roundedBackground(style.accent, 8.dp, Color.TRANSPARENT, 0)
            layoutParams = LinearLayout.LayoutParams(10.dp, 10.dp).apply {
                marginStart = 8.dp
            }
        })

        card.addView(header)

        card.addView(TextView(activity).apply {
            text = item.description
            textSize = 13.5f
            setTextColor(MUTED_TEXT)
            setLineSpacing(3f, 1f)
            setPadding(56.dp, 8.dp, 0, 0)
        })

        val footer = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(56.dp, 10.dp, 0, 0)
        }

        footer.addView(TextView(activity).apply {
            text = item.type.label
            textSize = 11.5f
            setTypeface(null, Typeface.BOLD)
            setTextColor(style.accent)
            gravity = Gravity.CENTER
            background = roundedBackground(style.badgeBackground, 12.dp, Color.TRANSPARENT, 0)
            setPadding(10.dp, 4.dp, 10.dp, 4.dp)
        })

        footer.addView(TextView(activity).apply {
            text = if (item.read) "Lu" else "Non lu"
            textSize = 11.5f
            setTextColor(if (item.read) MUTED_TEXT else DARK_TEXT)
            setPadding(10.dp, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        footer.addView(MaterialButton(activity).apply {
            text = if (item.read) "Effacer" else "Lire"
            textSize = 11.5f
            minHeight = 34.dp
            cornerRadius = 13.dp
            applyModernPressAnimation()
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setTextColor(style.accent)
            strokeColor = android.content.res.ColorStateList.valueOf(style.badgeBackground)
            strokeWidth = 1.dp
            setOnClickListener {
                if (item.read) {
                    clearCard(card, item, onChanged)
                } else {
                    store.markRead(item.id)
                    onChanged()
                    card.background = notificationCardBackground(item.copy(read = true), style)
                    text = "Effacer"
                }
            }
        })

        card.addView(footer)
        return card
    }

    private fun createSectionHeader(activity: Activity, title: String): View {
        return TextView(activity).apply {
            text = title
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(MUTED_TEXT)
            letterSpacing = 0f
            setPadding(20.dp, 16.dp, 20.dp, 9.dp)
        }
    }

    private fun createEmptyState(activity: Activity): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(26.dp, 42.dp, 26.dp, 46.dp)

            addView(FrameLayout(activity).apply {
                background = roundedBackground(SOFT_CYAN, 30.dp, Color.TRANSPARENT, 0)
                layoutParams = LinearLayout.LayoutParams(76.dp, 76.dp)
                addView(ImageView(activity).apply {
                    setImageResource(R.drawable.ic_notifications_24)
                    setColorFilter(ACCENT)
                    layoutParams = FrameLayout.LayoutParams(34.dp, 34.dp, Gravity.CENTER)
                })
            })

            addView(TextView(activity).apply {
                text = "Aucune notification"
                textSize = 20f
                setTypeface(null, Typeface.BOLD)
                setTextColor(DARK_TEXT)
                gravity = Gravity.CENTER
                setPadding(0, 18.dp, 0, 0)
            })

            addView(TextView(activity).apply {
                text = "Les rappels de médicaments, rendez-vous et tâches apparaîtront ici."
                textSize = 14f
                setTextColor(MUTED_TEXT)
                gravity = Gravity.CENTER
                setLineSpacing(4f, 1f)
                setPadding(12.dp, 8.dp, 12.dp, 0)
            })
        }
    }

    private fun attachSwipeAndPress(card: View, item: AppNotificationItem, onChanged: () -> Unit) {
        var startX = 0f
        var swiping = false

        card.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    swiping = false
                    view.animate().scaleX(0.985f).scaleY(0.985f).setDuration(90L).start()
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val delta = event.rawX - startX
                    if (kotlin.math.abs(delta) > 18.dp) {
                        swiping = true
                        view.translationX = delta.coerceIn(-96.dp.toFloat(), 96.dp.toFloat())
                        view.alpha = 1f - (kotlin.math.abs(delta) / 260.dp).coerceAtMost(0.35f)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(110L).start()
                    val delta = event.rawX - startX
                    if (swiping && kotlin.math.abs(delta) > 72.dp) {
                        clearCard(view, item, onChanged)
                        true
                    } else {
                        view.animate().translationX(0f).alpha(1f).setDuration(140L).start()
                        view.performClick()
                        true
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).translationX(0f).alpha(1f).setDuration(140L).start()
                    false
                }

                else -> false
            }
        }
    }

    private fun clearCard(card: View, item: AppNotificationItem, onChanged: () -> Unit) {
        store.clear(item.id)
        card.animate()
            .translationX(card.width.toFloat().coerceAtLeast(220.dp.toFloat()))
            .alpha(0f)
            .setDuration(190L)
            .withEndAction {
                (card.parent as? LinearLayout)?.removeView(card)
                onChanged()
            }
            .start()
    }

    private fun openRelatedScreen(activity: Activity, item: AppNotificationItem) {
        val destination = when (item.type) {
            NotificationType.MEDICATION, NotificationType.TASK -> TasksActivity::class.java
            NotificationType.APPOINTMENT -> AppointmentActivity::class.java
            NotificationType.HEALTH_TIP -> ArticlesActivity::class.java
        }

        activity.startActivity(
            Intent(activity, destination).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        )
    }

    private fun groupItems(items: List<AppNotificationItem>): Map<NotificationGroup, List<AppNotificationItem>> {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        return items.groupBy { item ->
            val date = item.dateTime?.toLocalDate()
            when {
                date == null || date == today -> NotificationGroup.TODAY
                date == tomorrow -> NotificationGroup.TOMORROW
                date.isAfter(tomorrow) -> NotificationGroup.UPCOMING
                else -> NotificationGroup.EARLIER
            }
        }
    }

    private fun currentVisibleIds(listContainer: LinearLayout): List<String> {
        return (0 until listContainer.childCount).mapNotNull { index ->
            listContainer.getChildAt(index).tag as? String
        }
    }

    private fun markItemsRead(items: List<AppNotificationItem>): List<AppNotificationItem> {
        return items.map { it.copy(read = true) }
    }

    private fun styleFor(item: AppNotificationItem): NotificationStyle {
        return when (item.priority) {
            NotificationPriority.MEDICATION -> NotificationStyle(
                accent = ACCENT,
                iconBackground = SOFT_CYAN,
                cardBackground = if (item.read) Color.WHITE else Color.parseColor("#F3FDFF"),
                badgeBackground = Color.parseColor("#DFF8FB")
            )
            NotificationPriority.APPOINTMENT -> NotificationStyle(
                accent = Color.parseColor("#7A6FF0"),
                iconBackground = Color.parseColor("#F0EEFF"),
                cardBackground = if (item.read) Color.WHITE else Color.parseColor("#F8F7FF"),
                badgeBackground = Color.parseColor("#E9E6FF")
            )
            NotificationPriority.WARNING -> NotificationStyle(
                accent = Color.parseColor("#E87461"),
                iconBackground = Color.parseColor("#FFF0EC"),
                cardBackground = if (item.read) Color.WHITE else Color.parseColor("#FFF8F5"),
                badgeBackground = Color.parseColor("#FFE4DC")
            )
            NotificationPriority.INFO -> NotificationStyle(
                accent = Color.parseColor("#2EAD6B"),
                iconBackground = Color.parseColor("#EAF8F1"),
                cardBackground = if (item.read) Color.WHITE else Color.parseColor("#F7FCFA"),
                badgeBackground = Color.parseColor("#DFF5E9")
            )
        }
    }

    private fun notificationCardBackground(item: AppNotificationItem, style: NotificationStyle): GradientDrawable {
        return roundedBackground(style.cardBackground, 20.dp, if (item.read) SOFT_BORDER else style.badgeBackground, 1.dp)
    }

    private fun roundedBackground(fill: Int, radius: Int, stroke: Int, strokeWidth: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = radius.toFloat()
            if (strokeWidth > 0) setStroke(strokeWidth, stroke)
        }
    }

    private fun notificationDialogWidth(activity: Activity): Int {
        val screenWidth = activity.resources.displayMetrics.widthPixels
        return (screenWidth - 24.dp).coerceAtMost(430.dp)
    }

    private fun notificationListHeight(activity: Activity): Int {
        val screenHeight = activity.resources.displayMetrics.heightPixels
        return (screenHeight * 0.64f).toInt().coerceIn(320.dp, 590.dp)
    }

    private fun MedicationReminderResponse.toNotificationItem(): AppNotificationItem {
        val time = parseTime(reminderTime)
        return AppNotificationItem(
            id = "medication-$id",
            title = title,
            description = "$dosage à ${formatTime(time)}",
            timeLabel = relativeTimeLabel(time?.let { LocalDateTime.of(LocalDate.now(), it) }),
            iconRes = R.drawable.ic_medicine_24,
            dateTime = time?.let { LocalDateTime.of(LocalDate.now(), it) },
            read = done,
            type = NotificationType.MEDICATION,
            priority = NotificationPriority.MEDICATION
        )
    }

    private fun AppointmentResponse.toNotificationItem(): AppNotificationItem {
        val dateTime = parseDateTime(appointmentDate)
        return AppNotificationItem(
            id = "appointment-$appointmentId",
            title = title,
            description = listOfNotNull(doctorName, location, notes).joinToString(" | ").ifBlank {
                "Rappel de rendez-vous"
            },
            timeLabel = relativeTimeLabel(dateTime),
            iconRes = R.drawable.ic_calendar_24,
            dateTime = dateTime,
            read = status.lowercase() == "completed",
            type = NotificationType.APPOINTMENT,
            priority = if (dateTime?.let { isSoon(it) } == true) {
                NotificationPriority.WARNING
            } else {
                NotificationPriority.APPOINTMENT
            }
        )
    }

    private fun TaskResponse.toNotificationItem(): AppNotificationItem {
        val dateTime = dueDate?.let(::parseDateTime)
        return AppNotificationItem(
            id = "task-$taskId",
            title = title,
            description = description ?: "Rappel de tâche",
            timeLabel = relativeTimeLabel(dateTime),
            iconRes = if (dateTime?.let { isSoon(it) } == true) R.drawable.ic_warning_24 else R.drawable.ic_task_24,
            dateTime = dateTime,
            read = isCompleted,
            type = NotificationType.TASK,
            priority = if (dateTime?.let { isSoon(it) } == true) {
                NotificationPriority.WARNING
            } else {
                NotificationPriority.INFO
            }
        )
    }

    private fun dailyHealthTipItem(): AppNotificationItem {
        val dateTime = LocalDate.now().atTime(9, 0)
        return AppNotificationItem(
            id = "health-tip-${LocalDate.now()}",
            title = "Conseil santé du jour",
            description = "Hydratez-vous et reposez-vous quand votre corps le demande. Les petits gestes de soin comptent aujourd'hui.",
            timeLabel = "Aujourd'hui",
            iconRes = R.drawable.ic_health_heart_24,
            dateTime = dateTime,
            read = false,
            type = NotificationType.HEALTH_TIP,
            priority = NotificationPriority.INFO
        )
    }

    private fun isSoon(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        val hours = Duration.between(now, dateTime).toHours()
        return !dateTime.isBefore(now) && hours <= 24L
    }

    private fun relativeTimeLabel(dateTime: LocalDateTime?): String {
        if (dateTime == null) return "Aucune date"
        val date = dateTime.toLocalDate()
        val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        val today = LocalDate.now()
        return when (date) {
            today -> "Aujourd'hui à $time"
            today.plusDays(1) -> "Demain à $time"
            else -> dateTime.format(DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.FRENCH))
        }
    }

    private fun parseDateTime(value: String): LocalDateTime? {
        return runCatching { LocalDateTime.parse(value) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDate.parse(value.take(10)).atStartOfDay() }.getOrNull()
    }

    private fun parseTime(value: String): LocalTime? {
        return runCatching { LocalTime.parse(value.take(8)) }.getOrNull()
            ?: runCatching { LocalTime.parse(value.take(5)) }.getOrNull()
    }

    private fun formatTime(time: LocalTime?): String {
        return time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--"
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()

    companion object {
        private val ACCENT = Color.parseColor("#16B8C7")
        private val SOFT_CYAN = Color.parseColor("#E6FAFC")
        private val SOFT_BORDER = Color.parseColor("#D9F1F3")
        private val DARK_TEXT = Color.parseColor("#24333A")
        private val MUTED_TEXT = Color.parseColor("#75868D")
    }
}

data class AppNotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeLabel: String,
    val iconRes: Int,
    val dateTime: LocalDateTime?,
    val read: Boolean,
    val type: NotificationType,
    val priority: NotificationPriority
)

enum class NotificationType(val label: String) {
    MEDICATION("Médicament"),
    APPOINTMENT("Rendez-vous"),
    TASK("Tâche"),
    HEALTH_TIP("Conseil santé")
}

enum class NotificationPriority {
    MEDICATION,
    APPOINTMENT,
    WARNING,
    INFO
}

enum class NotificationGroup(val title: String) {
    TODAY("Aujourd'hui"),
    TOMORROW("Demain"),
    UPCOMING("À venir"),
    EARLIER("Plus tôt")
}

private data class NotificationStyle(
    val accent: Int,
    val iconBackground: Int,
    val cardBackground: Int,
    val badgeBackground: Int
)
