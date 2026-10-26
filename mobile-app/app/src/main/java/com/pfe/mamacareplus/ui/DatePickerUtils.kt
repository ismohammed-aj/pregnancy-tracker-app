package com.pfe.mamacareplus.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DatePickerUtils {

    fun showIsoDatePicker(
        context: Context,
        currentValue: String?,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        onDateSelected: (String) -> Unit
    ) {
        val initialDate = parseDate(currentValue) ?: maxDate?.coerceAtMost(LocalDate.now()) ?: LocalDate.now()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(selected.format(DateTimeFormatter.ISO_LOCAL_DATE))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).apply {
            minDate?.let { datePicker.minDate = it.toEpochDay() * MILLIS_PER_DAY }
            maxDate?.let { datePicker.maxDate = it.toEpochDay() * MILLIS_PER_DAY }
            setOnShowListener {
                getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(ACCENT_COLOR)
                getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(ACCENT_COLOR)
            }
            show()
        }
    }

    private fun parseDate(value: String?): LocalDate? {
        return value?.trim()?.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it.take(10), DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        }
    }

    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    private val ACCENT_COLOR = Color.parseColor("#16B8C7")
}
