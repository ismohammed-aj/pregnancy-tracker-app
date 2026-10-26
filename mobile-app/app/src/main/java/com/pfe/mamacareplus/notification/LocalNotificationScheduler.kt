package com.pfe.mamacareplus.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pfe.mamacareplus.model.response.medication.MedicationReminderResponse
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class LocalNotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = context.getSharedPreferences("mamacare_scheduled_reminders", Context.MODE_PRIVATE)

    fun scheduleMedicationReminder(reminder: MedicationReminderResponse) {
        ensureChannel(context)
        if (!reminder.active) return

        val triggerAt = nextTriggerAt(reminder) ?: return
        val pendingIntent = pendingIntent(reminder.id, reminder.medicationName)

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent
        )
        remember(reminder)
    }

    fun cancelMedicationReminder(reminderId: Int) {
        alarmManager.cancel(pendingIntent(reminderId, ""))
        forget(reminderId)
    }

    fun rescheduleStoredMedicationReminders() {
        scheduledReminders().forEach { reminder ->
            scheduleMedicationReminder(reminder)
        }
    }

    private fun pendingIntent(reminderId: Int, medicationName: String): PendingIntent {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MedicationReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, medicationName)
        }

        return PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerAt(reminder: MedicationReminderResponse): LocalDateTime? {
        val now = LocalDateTime.now()
        val time = parseTime(reminder.reminderTime) ?: return null
        val startDate = parseDate(reminder.startDate) ?: LocalDate.now()
        val endDate = reminder.endDate?.let(::parseDate)

        for (offset in 0..370) {
            val date = LocalDate.now().plusDays(offset.toLong())
            if (date.isBefore(startDate)) continue
            if (endDate != null && date.isAfter(endDate)) return null
            if (!isScheduledForDate(reminder, date)) continue

            val candidate = LocalDateTime.of(date, time)
            if (candidate.isAfter(now)) return candidate
        }

        return null
    }

    private fun isScheduledForDate(reminder: MedicationReminderResponse, date: LocalDate): Boolean {
        val frequency = reminder.frequency.uppercase(Locale.ROOT)
        if (!frequency.contains("SPECIFIC") && !frequency.contains("DAYS")) {
            return true
        }

        val days = reminder.daysOfWeek?.uppercase(Locale.ROOT).orEmpty()
        if (days.isBlank()) return true

        val day = date.dayOfWeek
        return days.contains(day.name) || days.contains(day.name.take(3)) || days.contains(frenchDay(day))
    }

    private fun frenchDay(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> "LUNDI"
            DayOfWeek.TUESDAY -> "MARDI"
            DayOfWeek.WEDNESDAY -> "MERCREDI"
            DayOfWeek.THURSDAY -> "JEUDI"
            DayOfWeek.FRIDAY -> "VENDREDI"
            DayOfWeek.SATURDAY -> "SAMEDI"
            DayOfWeek.SUNDAY -> "DIMANCHE"
        }
    }

    private fun parseDate(value: String): LocalDate? {
        return runCatching { LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
    }

    private fun parseTime(value: String): LocalTime? {
        return runCatching { LocalTime.parse(value.take(8)) }.getOrNull()
            ?: runCatching { LocalTime.parse(value.take(5)) }.getOrNull()
    }

    private fun remember(reminder: MedicationReminderResponse) {
        val updated = scheduledReminderJson()
            .filter { it.optInt("id") != reminder.id }
            .toMutableList()
        updated.add(reminder.toJson())
        prefs.edit().putStringSet(KEY_REMINDERS, updated.map { it.toString() }.toSet()).apply()
    }

    private fun forget(reminderId: Int) {
        val updated = scheduledReminderJson()
            .filter { it.optInt("id") != reminderId }
            .map { it.toString() }
            .toSet()
        prefs.edit().putStringSet(KEY_REMINDERS, updated).apply()
    }

    private fun scheduledReminders(): List<MedicationReminderResponse> {
        return scheduledReminderJson().mapNotNull { it.toReminder() }
    }

    private fun scheduledReminderJson(): List<JSONObject> {
        return prefs.getStringSet(KEY_REMINDERS, emptySet()).orEmpty().mapNotNull { raw ->
            runCatching { JSONObject(raw) }.getOrNull()
        }
    }

    private fun MedicationReminderResponse.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("femmeId", femmeId)
            .put("username", username)
            .put("medicationName", medicationName)
            .put("dosage", dosage)
            .put("frequency", frequency)
            .put("daysOfWeek", daysOfWeek)
            .put("reminderTime", reminderTime)
            .put("startDate", startDate)
            .put("endDate", endDate)
            .put("notes", notes)
            .put("active", active)
            .put("done", done)
            .put("lastCompletedAt", lastCompletedAt)
            .put("createdAt", createdAt)
            .put("updatedAt", updatedAt)
    }

    private fun JSONObject.toReminder(): MedicationReminderResponse? {
        return runCatching {
            MedicationReminderResponse(
                id = getInt("id"),
                femmeId = optInt("femmeId"),
                username = optString("username").takeIf { it.isNotBlank() && it != "null" },
                medicationName = getString("medicationName"),
                dosage = optString("dosage"),
                frequency = optString("frequency", "Quotidien"),
                daysOfWeek = optString("daysOfWeek").takeIf { it.isNotBlank() && it != "null" },
                reminderTime = getString("reminderTime"),
                startDate = getString("startDate"),
                endDate = optString("endDate").takeIf { it.isNotBlank() && it != "null" },
                notes = optString("notes").takeIf { it.isNotBlank() && it != "null" },
                active = optBoolean("active", true),
                done = optBoolean("done", false),
                lastCompletedAt = optString("lastCompletedAt").takeIf { it.isNotBlank() && it != "null" },
                createdAt = optString("createdAt").takeIf { it.isNotBlank() && it != "null" },
                updatedAt = optString("updatedAt").takeIf { it.isNotBlank() && it != "null" }
            )
        }.getOrNull()
    }

    companion object {
        const val CHANNEL_ID = "mamacare_medication_reminders"
        private const val KEY_REMINDERS = "reminders"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rappels de médicaments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications de rappels de médicaments"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
