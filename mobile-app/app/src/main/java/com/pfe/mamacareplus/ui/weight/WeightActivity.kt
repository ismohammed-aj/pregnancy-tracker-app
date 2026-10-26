package com.pfe.mamacareplus.ui.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityWeightBinding
import com.pfe.mamacareplus.model.response.weight.WeightRecordResponse
import com.pfe.mamacareplus.ui.applyCompactOutlineButton
import com.pfe.mamacareplus.ui.applyListCardStyle
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.ui.main.MainTab
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.ui.questionnaire.OnboardingInputParser
import com.pfe.mamacareplus.viewmodel.WeightUiState
import com.pfe.mamacareplus.viewmodel.WeightViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.launch
import kotlin.math.abs

class WeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeightBinding
    private val viewModel: WeightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightBinding.inflate(layoutInflater)
        setMainContentView(binding.root, MainTab.HEALTH)

        setupUiPolish()
        setupListeners()
        observeWeightRecords()

        viewModel.loadWeightRecords()
    }

    private fun setupUiPolish() {
        binding.weightContainer.playSoftEntrance()
        binding.btnAddWeight.applyModernPressAnimation()
        binding.tilWeightKg.bindLiveValidation(binding.etWeightKg) { value ->
            OnboardingInputParser.parseValidWeightKg(value.trim()) != null
        }
        binding.tilBmi.applyPremiumInputFeedback(binding.etBmi, invalidWhenBlank = false) { value ->
            value.replace(",", ".").toDoubleOrNull()?.let { it > 0.0 } == true
        }
        binding.tilRecordDate.applyPremiumInputFeedback(binding.etRecordDate, invalidWhenBlank = false) { value ->
            parseRecordDate(value.trim()) != null
        }
        binding.tilWeightNotes.applyPremiumInputFeedback(binding.etWeightNotes)
    }

    private fun setupListeners() {
        binding.btnAddWeight.setOnClickListener {
            val weightText = binding.etWeightKg.text.toString().trim()
            val bmiText = binding.etBmi.text.toString().trim()
            val date = binding.etRecordDate.text.toString().trim()
            val notes = binding.etWeightNotes.text.toString().trim()

            if (weightText.isBlank()) {
                binding.etWeightKg.error = "Poids requis"
                return@setOnClickListener
            }

            val weightKg = OnboardingInputParser.parseValidWeightKg(weightText)
            if (weightKg == null) {
                binding.etWeightKg.error = "Veuillez saisir un poids valide en kg."
                return@setOnClickListener
            }

            val bmi = bmiText.replace(",", ".").toDoubleOrNull()

            viewModel.createWeightRecord(
                weightKg = weightKg,
                bmi = bmi,
                recordDate = date.ifBlank { null },
                notes = notes.ifBlank { null }
            )

            binding.etWeightKg.text?.clear()
            binding.etBmi.text?.clear()
            binding.etRecordDate.text?.clear()
            binding.etWeightNotes.text?.clear()
        }
    }

    private fun observeWeightRecords() {
        lifecycleScope.launch {
            viewModel.weightState.collect { state ->
                when (state) {
                    is WeightUiState.Loading -> {
                        binding.tvWeightMessage.text = "Chargement des mesures..."
                    }

                    is WeightUiState.Success -> {
                        renderWeightRecords(state.records)
                    }

                    is WeightUiState.Error -> {
                        binding.tvWeightMessage.text = state.message
                    }
                }
            }
        }
    }

    private fun renderWeightRecords(records: List<WeightRecordResponse>) {
        binding.listWeightRecords.removeAllViews()
        val weeklyPoints = buildWeeklyPoints(records)
        renderWeightSummary(weeklyPoints)
        renderWeightChart(weeklyPoints)

        if (records.isEmpty()) {
            binding.tvWeightMessage.text = "Aucune mesure pour le moment."
            return
        }

        binding.tvWeightMessage.text = "Vous avez ${records.size} mesure(s) de poids."

        records.forEach { record ->
            binding.listWeightRecords.addView(createWeightRecordView(record))
        }
    }

    private fun buildWeeklyPoints(records: List<WeightRecordResponse>): List<WeeklyWeightPoint> {
        return records
            .map { record ->
                val date = parseRecordDate(record.recordDate) ?: LocalDate.now()
                ChartPoint(record = record, date = date)
            }
            .groupBy { it.weekStart }
            .values
            .map { weeklyPoints ->
                weeklyPoints.maxByOrNull { it.date } ?: weeklyPoints.first()
            }
            .sortedBy { it.date }
            .mapIndexed { index, point ->
                WeeklyWeightPoint(
                    weekLabel = "S${index + 1}",
                    dateLabel = formatShortDate(point.date),
                    weightKg = point.record.weightKg
                )
            }
    }

    private fun renderWeightSummary(points: List<WeeklyWeightPoint>) {
        val latest = points.lastOrNull()
        val first = points.firstOrNull()
        val delta = if (latest != null && first != null) latest.weightKg - first.weightKg else 0.0

        binding.tvLatestWeight.text = latest?.let { "${formatWeight(it.weightKg)} kg" } ?: "0 kg"
        binding.tvWeightDelta.text = formatDelta(delta)
        binding.tvWeightWeeks.text = points.size.toString()
    }

    private fun renderWeightChart(points: List<WeeklyWeightPoint>) {
        binding.weightChartContainer.removeAllViews()

        if (points.isEmpty()) {
            binding.weightChartContainer.addView(createChartEmptyView())
            return
        }

        binding.weightChartContainer.addView(
            WeeklyWeightChartView(this).apply {
                setPoints(points)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        )
    }

    private fun createChartEmptyView(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            addView(TextView(this@WeightActivity).apply {
                text = "0 kg"
                textSize = 30f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#D6336C"))
                gravity = Gravity.CENTER
            })

            addView(TextView(this@WeightActivity).apply {
                text = "Ajoutez votre première mesure pour créer le diagramme."
                textSize = 14f
                setTextColor(Color.parseColor("#6F5B63"))
                gravity = Gravity.CENTER
                setLineSpacing(3f, 1f)
                setPadding(18.dp, 10.dp, 18.dp, 0)
            })
        }
    }

    private fun createWeightRecordView(record: WeightRecordResponse): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            applyListCardStyle(bottomMarginDp = 10)
            setPadding(14.dp, 12.dp, 14.dp, 12.dp)
        }

        val weight = TextView(this).apply {
            text = "${formatWeight(record.weightKg)} kg"
            textSize = 18f
            setTextColor(Color.parseColor("#D6336C"))
            setTypeface(null, Typeface.BOLD)
        }

        val bmi = TextView(this).apply {
            text = "IMC : ${record.bmi ?: "Non défini"}"
            textSize = 13f
            setTextColor(Color.parseColor("#6F5B63"))
            setPadding(0, 5.dp, 0, 0)
        }

        val date = TextView(this).apply {
            text = "Date : ${record.recordDate}"
            textSize = 13f
            setTextColor(Color.parseColor("#6F5B63"))
            setPadding(0, 4.dp, 0, 0)
        }

        val notes = TextView(this).apply {
            text = "Notes : ${record.notes ?: "Aucune note"}"
            textSize = 13f
            setTextColor(Color.parseColor("#6F5B63"))
            setPadding(0, 4.dp, 0, 4.dp)
        }

        val deleteButton = MaterialButton(this).apply {
            text = "Supprimer"
            applyCompactOutlineButton(topMarginDp = 8, destructive = true)
            minWidth = 0
            minimumWidth = 0
            minHeight = 34.dp
            minimumHeight = 34.dp
            insetTop = 0
            insetBottom = 0
            cornerRadius = 11.dp
            textSize = 12f
            setPadding(12.dp, 0, 12.dp, 0)
            applyModernPressAnimation()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                34.dp
            ).apply {
                topMargin = 6.dp
            }
            setOnClickListener {
                viewModel.deleteWeightRecord(record.weightRecordId)
                Toast.makeText(this@WeightActivity, "Mesure supprimée", Toast.LENGTH_SHORT).show()
            }
        }

        container.addView(weight)
        container.addView(bmi)
        container.addView(date)
        container.addView(notes)
        container.addView(deleteButton)

        return container
    }

    private fun parseRecordDate(value: String): LocalDate? {
        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun formatShortDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }

    private fun formatWeight(weight: Double): String {
        return if (weight % 1.0 == 0.0) {
            weight.toInt().toString()
        } else {
            String.format("%.1f", weight)
        }
    }

    private fun formatDelta(delta: Double): String {
        if (abs(delta) < 0.05) return "0 kg"

        val sign = if (delta > 0) "+" else "-"
        return "$sign${formatWeight(abs(delta))} kg"
    }

    private data class ChartPoint(
        val record: WeightRecordResponse,
        val date: LocalDate
    ) {
        val weekStart: LocalDate
            get() = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    private data class WeeklyWeightPoint(
        val weekLabel: String,
        val dateLabel: String,
        val weightKg: Double
    )

    private class WeeklyWeightChartView(context: Context) : View(context) {

        private val points = mutableListOf<WeeklyWeightPoint>()
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D6336C")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22D6336C")
            style = Paint.Style.FILL
        }
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D6336C")
            style = Paint.Style.FILL
        }
        private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F1D8E1")
            strokeWidth = 1.5f
        }
        private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A7180")
            textSize = sp(12f)
            textAlign = Paint.Align.CENTER
        }
        private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2D1B24")
            textSize = sp(12f)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        fun setPoints(newPoints: List<WeeklyWeightPoint>) {
            points.clear()
            points.addAll(newPoints)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (points.isEmpty()) return

            val left = 20f
            val right = width - 18f
            val top = 34f
            val bottom = height - 48f
            val chartHeight = bottom - top
            val minWeight = points.minOf { it.weightKg }
            val maxWeight = points.maxOf { it.weightKg }
            val range = (maxWeight - minWeight).takeIf { it > 0.0 } ?: 1.0

            repeat(4) { index ->
                val y = top + (chartHeight * index / 3f)
                canvas.drawLine(left, y, right, y, gridPaint)
            }

            val coordinates = points.mapIndexed { index, point ->
                val x = if (points.size == 1) {
                    (left + right) / 2f
                } else {
                    left + ((right - left) * index / (points.size - 1))
                }
                val y = bottom - (((point.weightKg - minWeight) / range).toFloat() * chartHeight)
                x to y
            }

            val linePath = Path()
            val fillPath = Path()
            coordinates.forEachIndexed { index, (x, y) ->
                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, bottom)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo(coordinates.last().first, bottom)
            fillPath.close()

            canvas.drawPath(fillPath, fillPaint)
            canvas.drawPath(linePath, linePaint)

            coordinates.forEachIndexed { index, (x, y) ->
                val point = points[index]
                canvas.drawCircle(x, y, 8f, dotStrokePaint)
                canvas.drawCircle(x, y, 6f, dotPaint)
                canvas.drawText("${formatChartWeight(point.weightKg)} kg", x, y - 14f, valuePaint)
                canvas.drawText(point.weekLabel, x, bottom + 22f, labelPaint)
                canvas.drawText(point.dateLabel, x, bottom + 40f, labelPaint)
            }
        }

        private fun formatChartWeight(weight: Double): String {
            return if (weight % 1.0 == 0.0) {
                weight.toInt().toString()
            } else {
                String.format("%.1f", weight)
            }
        }

        private fun sp(value: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                resources.displayMetrics
            )
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
