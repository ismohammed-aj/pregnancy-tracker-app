package com.pfe.mamacareplus.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.api.ApiClient
import com.pfe.mamacareplus.databinding.ActivityHomeBinding
import com.pfe.mamacareplus.model.response.appointment.AppointmentResponse
import com.pfe.mamacareplus.model.response.article.ArticleResponse
import com.pfe.mamacareplus.model.response.pregnancy.BabyDevelopmentResponse
import com.pfe.mamacareplus.model.response.profile.FemmeProfileResponse
import com.pfe.mamacareplus.model.response.profile.PregnancyProfileResponse
import com.pfe.mamacareplus.ui.appointment.AppointmentActivity
import com.pfe.mamacareplus.ui.article.ArticleCardItem
import com.pfe.mamacareplus.ui.article.ArticleSection
import com.pfe.mamacareplus.ui.article.articleImageKey
import com.pfe.mamacareplus.ui.main.MainTab
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.ui.pregnancy.BabyDevelopmentImageCatalog
import com.pfe.mamacareplus.ui.pregnancy.ComparisonMode
import com.pfe.mamacareplus.ui.pregnancy.PregnancyActivity
import com.pfe.mamacareplus.ui.profile.ProfileActivity
import com.pfe.mamacareplus.ui.tasks.TasksActivity
import com.pfe.mamacareplus.viewmodel.AppointmentUiState
import com.pfe.mamacareplus.viewmodel.AppointmentViewModel
import com.pfe.mamacareplus.viewmodel.ArticleUiState
import com.pfe.mamacareplus.viewmodel.ArticleViewModel
import com.pfe.mamacareplus.viewmodel.BabyDevelopmentUiState
import com.pfe.mamacareplus.viewmodel.BabyDevelopmentViewModel
import com.pfe.mamacareplus.viewmodel.PregnancyUiState
import com.pfe.mamacareplus.viewmodel.PregnancyViewModel
import com.pfe.mamacareplus.viewmodel.ProfileUiState
import com.pfe.mamacareplus.viewmodel.ProfileViewModel
import com.pfe.mamacareplus.viewmodel.TaskUiState
import com.pfe.mamacareplus.viewmodel.TaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val profileViewModel: ProfileViewModel by viewModels()
    private val pregnancyViewModel: PregnancyViewModel by viewModels()
    private val babyDevelopmentViewModel: BabyDevelopmentViewModel by viewModels()
    private val taskViewModel: TaskViewModel by viewModels()
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val articleViewModel: ArticleViewModel by viewModels()

    private var latestProfile: FemmeProfileResponse? = null
    private var latestPregnancyProfile: PregnancyProfileResponse? = null
    private var selectedWeek = DEFAULT_WEEK
    private var babyImageRequestVersion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setMainContentView(binding.root, MainTab.HOME)

        setupNavigation()
        observeProfile()
        observePregnancyProfile()
        observeBabyDevelopment()
        observeTasks()
        observeAppointments()
        observeArticles()
        renderPregnancy(null)
        renderBabyDevelopment(null)
        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {
        profileViewModel.loadProfile()
        pregnancyViewModel.loadPregnancyProfile()
        taskViewModel.loadTasks()
        appointmentViewModel.loadAppointments()
        articleViewModel.loadArticles()
    }

    private fun setupNavigation() {
        binding.ivHomeAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.cardPregnancyOverview.configureClick {
            startActivity(Intent(this, PregnancyActivity::class.java))
        }

        binding.cardBabyDevelopment.configureClick {
            startActivity(Intent(this, PregnancyActivity::class.java))
        }

        binding.cardHomeTasks.configureClick {
            startActivity(Intent(this, TasksActivity::class.java))
        }

        binding.cardHomeAppointment.configureClick {
            startActivity(Intent(this, AppointmentActivity::class.java))
        }
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            profileViewModel.profileState.collect { state ->
                if (state is ProfileUiState.Success) {
                    latestProfile = state.profile
                    renderGreeting()
                    loadAvatar(state.profile.avatar)
                }
            }
        }
    }

    private fun observePregnancyProfile() {
        lifecycleScope.launch {
            pregnancyViewModel.pregnancyState.collect { state ->
                latestPregnancyProfile = when (state) {
                    is PregnancyUiState.Success -> state.profile
                    is PregnancyUiState.Error -> null
                    is PregnancyUiState.Loading -> latestPregnancyProfile
                }

                renderGreeting()
                renderPregnancy(latestPregnancyProfile)

                latestPregnancyProfile?.currentWeek?.coerceIn(1, 40)?.let { week ->
                    selectedWeek = week
                    renderBabyDevelopment(null)
                    babyDevelopmentViewModel.loadDevelopmentByWeek(week)
                }
            }
        }
    }

    private fun observeBabyDevelopment() {
        lifecycleScope.launch {
            babyDevelopmentViewModel.babyDevelopmentState.collect { state ->
                when (state) {
                    is BabyDevelopmentUiState.Idle -> Unit
                    is BabyDevelopmentUiState.Loading -> Unit
                    is BabyDevelopmentUiState.Success -> {
                        if (state.development.weekNumber == selectedWeek) {
                            renderBabyDevelopment(state.development)
                        }
                    }
                    is BabyDevelopmentUiState.Error -> renderBabyDevelopment(null)
                }
            }
        }
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            taskViewModel.taskState.collect { state ->
                when (state) {
                    is TaskUiState.Loading -> {
                        binding.tvHomeTasksValue.text = "..."
                        binding.tvHomeTasksLabel.text = "Mes tâches"
                    }
                    is TaskUiState.Success -> {
                        val pendingCount = state.tasks.count { !it.isCompleted }
                        binding.tvHomeTasksValue.text = pendingCount.toString()
                        binding.tvHomeTasksLabel.text = if (pendingCount == 1) {
                            "tâche en attente"
                        } else {
                            "tâches en attente"
                        }
                    }
                    is TaskUiState.Error -> {
                        binding.tvHomeTasksValue.text = "0"
                        binding.tvHomeTasksLabel.text = "Mes tâches"
                    }
                }
            }
        }
    }

    private fun observeAppointments() {
        lifecycleScope.launch {
            appointmentViewModel.appointmentState.collect { state ->
                when (state) {
                    is AppointmentUiState.Loading -> {
                        binding.tvHomeAppointmentValue.text = "..."
                        binding.tvHomeAppointmentLabel.text = "rendez-vous à venir"
                    }
                    is AppointmentUiState.Success -> renderNextAppointment(state.appointments)
                    is AppointmentUiState.Error -> showNoAppointment()
                }
            }
        }
    }

    private fun observeArticles() {
        lifecycleScope.launch {
            articleViewModel.articleState.collect { state ->
                when (state) {
                    is ArticleUiState.Loading -> Unit
                    is ArticleUiState.Success -> renderArticleSections(state.articles)
                    is ArticleUiState.Empty -> binding.homeArticleSections.removeAllViews()
                    is ArticleUiState.Error -> binding.homeArticleSections.removeAllViews()
                }
            }
        }
    }

    private fun renderGreeting() {
        val firstName = latestProfile
            ?.username
            ?.trim()
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }

        binding.tvHomeGreeting.text = firstName?.let { "Bonjour, $it" } ?: "Bonjour"
        binding.tvHomeWelcomeSubtitle.text = latestPregnancyProfile?.let {
            "Semaine ${it.currentWeek} de grossesse"
        } ?: "Votre suivi de grossesse"
    }

    private fun renderPregnancy(profile: PregnancyProfileResponse?) {
        if (profile == null) {
            binding.tvPregnancyWeek.text = "Semaine non définie"
            binding.tvPregnancyProgress.text = "Complétez votre profil pour synchroniser le suivi."
            binding.tvPregnancyDays.text = "Aucune date prévue"
            binding.progressPregnancy.progress = 0
            return
        }

        val progress = profile.progressPercentage.roundToInt().coerceIn(0, 100)
        binding.tvPregnancyWeek.text = "Semaine ${profile.currentWeek}"
        binding.tvPregnancyProgress.text = "$progress% complété"
        binding.tvPregnancyDays.text = "${profile.remainingDays.coerceAtLeast(0)} jours restants"
        binding.progressPregnancy.progress = progress
    }

    private fun renderBabyDevelopment(development: BabyDevelopmentResponse?) {
        val week = development?.weekNumber ?: selectedWeek
        val size = development?.comparisonName
            ?.takeIf { it.isNotBlank() }
            ?: development?.babySize?.takeIf { it.isNotBlank() }
            ?: fallbackBabyComparison(week)
        val length = development?.babyLengthCm
        val weight = development?.babyWeightGrams
        val meta = listOfNotNull(
            length?.let { formatLength(it) },
            weight?.let { formatBabyWeight(it) }
        ).joinToString(" - ")

        binding.tvBabyDevelopmentTitle.text = "Semaine $week"
        binding.tvBabyDevelopmentSubtitle.text = "Votre bébé a environ la taille de : $size"
        binding.tvBabyDevelopmentMeta.text = meta.ifBlank { "Voir les détails" }
        loadBabyImage(
            imageUrl = development?.imageUrl ?: BabyDevelopmentImageCatalog.imageUrlFor(
                week = week.coerceIn(1, 40),
                mode = ComparisonMode.FRUIT,
                comparisonName = size
            ),
            fallbackRes = fallbackBabyImage(week)
        )
    }

    private fun renderNextAppointment(appointments: List<AppointmentResponse>) {
        val now = LocalDateTime.now()
        val upcomingAppointments = appointments
            .filterNot { appointment ->
                appointment.status.equals("completed", true) ||
                    appointment.status.equals("cancelled", true) ||
                    appointment.status.equals("canceled", true)
            }
            .mapNotNull { appointment ->
                parseDateTime(appointment.appointmentDate)
                    ?.takeIf { !it.isBefore(now) }
                    ?.let { appointment to it }
            }
            .sortedBy { it.second }

        if (upcomingAppointments.isEmpty()) {
            showNoAppointment()
            return
        }

        val next = upcomingAppointments.first()
        binding.tvHomeAppointmentValue.text = upcomingAppointments.size.toString()
        binding.tvHomeAppointmentLabel.text = "Prochain: ${next.first.title} - ${formatAppointmentDate(next.second)}"
    }

    private fun showNoAppointment() {
        binding.tvHomeAppointmentValue.text = "0"
        binding.tvHomeAppointmentLabel.text = "Aucun rendez-vous"
    }

    private fun renderArticleSections(articles: List<ArticleResponse>) {
        binding.homeArticleSections.removeAllViews()
        if (articles.isEmpty()) return

        val sections = listOf(
            ArticleHomeSection(
                title = "Tendance",
                badges = mapOf(
                    "article_sante_signes" to "Important",
                    "article_hydration" to "Conseil",
                    "article_sante_sommeil" to "Populaire"
                ),
                articles = articles.pickByKeys(
                    "article_sante_signes",
                    "article_hydration",
                    "article_sante_sommeil",
                    "article_activite_douce"
                )
            ),
            ArticleHomeSection(
                title = "Conseils grossesse",
                articles = articles.pickByKeys(
                    "article_mental_respiration",
                    "article_sante_poids",
                    "article_conseils_valise",
                    "article_conseils_retour",
                    "article_activite_dos"
                )
            ),
            ArticleHomeSection(
                title = "Bébé",
                articles = articles.pickByKeys(
                    "article_bebe_developpement",
                    "article_bebe_mouvements",
                    "article_rdv_echographie"
                )
            ),
            ArticleHomeSection(
                title = "Nutrition & hydratation",
                articles = articles.pickByKeys(
                    "article_nutrition_assiette",
                    "article_nutrition_micronutrients",
                    "article_nutrition_nausees"
                )
            )
        )

        sections.forEach { section ->
            val items = section.articles.map { article ->
                ArticleCardItem(article, section.badges[article.localKey()])
            }
            if (items.isNotEmpty()) {
                binding.homeArticleSections.addView(
                    ArticleSection(this).apply {
                        bind(section.title, items)
                    }
                )
            }
        }
    }

    private fun loadAvatar(avatar: String?) {
        val url = resolveMediaUrl(avatar)

        if (url.isNullOrBlank()) {
            showAvatarPlaceholder()
            return
        }

        if (url.startsWith("content:")) {
            prepareAvatarForPhoto()
            binding.ivHomeAvatar.setImageURI(Uri.parse(url))
            return
        }

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }

            if (bitmap != null) {
                prepareAvatarForPhoto()
                binding.ivHomeAvatar.setImageBitmap(bitmap)
            } else {
                showAvatarPlaceholder()
            }
        }
    }

    private fun loadBabyImage(imageUrl: String?, fallbackRes: Int) {
        val requestVersion = ++babyImageRequestVersion
        binding.ivBabyDevelopment.setImageResource(fallbackRes)

        val resolvedUrl = imageUrl?.trim().orEmpty()
        if (resolvedUrl.isBlank()) return

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    if (resolvedUrl.startsWith(BABY_ASSET_PREFIX)) {
                        assets.open(resolvedUrl.removePrefix(BABY_ASSET_PREFIX)).use {
                            BitmapFactory.decodeStream(it)
                        }
                    } else {
                        URL(resolveMediaUrl(resolvedUrl) ?: resolvedUrl).openStream().use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                }.getOrNull()
            }

            if (requestVersion == babyImageRequestVersion && bitmap != null) {
                binding.ivBabyDevelopment.setImageBitmap(bitmap)
            }
        }
    }

    private fun prepareAvatarForPhoto() {
        binding.ivHomeAvatar.imageTintList = null
        binding.ivHomeAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
        binding.ivHomeAvatar.setPadding(0, 0, 0, 0)
    }

    private fun showAvatarPlaceholder() {
        binding.ivHomeAvatar.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.ivHomeAvatar.setPadding(12.dp, 12.dp, 12.dp, 12.dp)
        binding.ivHomeAvatar.setImageResource(R.drawable.ic_profile_24)
        binding.ivHomeAvatar.imageTintList = ColorStateList.valueOf(COLOR_PRIMARY_DARK)
    }

    private fun resolveMediaUrl(value: String?): String? {
        val cleaned = value?.trim().orEmpty()
        if (cleaned.isBlank()) return null

        return when {
            cleaned.startsWith("http://") || cleaned.startsWith("https://") || cleaned.startsWith("content:") -> cleaned
            cleaned.startsWith("/") -> ApiClient.BASE_URL.trimEnd('/') + cleaned
            else -> ApiClient.BASE_URL + cleaned.trimStart('/')
        }
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        val text = value?.trim()?.takeIf { it.isNotBlank() } ?: return null

        return runCatching { LocalDateTime.parse(text) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(text).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDate.parse(text.take(10)).atStartOfDay() }.getOrNull()
    }

    private fun formatAppointmentDate(value: LocalDateTime): String {
        val day = when (value.toLocalDate()) {
            LocalDate.now() -> "Aujourd'hui"
            LocalDate.now().plusDays(1) -> "Demain"
            else -> value.format(DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH))
        }
        return "$day - ${value.format(DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH))}"
    }

    private fun formatLength(value: Double): String {
        return if (value % 1.0 == 0.0) {
            "${value.toInt()} cm"
        } else {
            String.format(Locale.US, "%.1f cm", value)
        }
    }

    private fun formatBabyWeight(value: Double): String {
        return if (value >= 1000.0) {
            String.format(Locale.US, "%.2f kg", value / 1000.0)
        } else if (value % 1.0 == 0.0) {
            "${value.toInt()} g"
        } else {
            String.format(Locale.US, "%.1f g", value)
        }
    }

    private fun fallbackBabyComparison(week: Int): String {
        return when (week.coerceIn(1, 40)) {
            in 1..8 -> "une petite graine"
            in 9..16 -> "un avocat"
            in 17..24 -> "un épi de maïs"
            in 25..32 -> "un melon"
            else -> "une citrouille"
        }
    }

    private fun fallbackBabyImage(week: Int): Int {
        return when (week.coerceIn(1, 40)) {
            in 1..8 -> R.drawable.baby_dev_seed
            in 9..16 -> R.drawable.baby_dev_avocado
            in 17..24 -> R.drawable.baby_dev_corn
            in 25..32 -> R.drawable.baby_dev_melon
            else -> R.drawable.baby_dev_pumpkin
        }
    }

    private fun List<ArticleResponse>.pickByKeys(vararg keys: String): List<ArticleResponse> {
        val selected = keys.mapNotNull { key ->
            firstOrNull { it.localKey() == key }
        }
        return selected.ifEmpty { take(5) }
    }

    private fun ArticleResponse.localKey(): String {
        return imageUrl.articleImageKey()
    }

    private fun View.configureClick(onClick: () -> Unit) {
        foreground = selectableForeground()
        applyPressAnimation()
        setOnClickListener { onClick() }
    }

    private fun selectableForeground(): android.graphics.drawable.Drawable? {
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray = obtainStyledAttributes(attrs)
        return typedArray.getDrawable(0).also { typedArray.recycle() }
    }

    private fun View.applyPressAnimation() {
        setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.98f).scaleY(0.98f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
            }
            false
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        private const val DEFAULT_WEEK = 24
        private const val BABY_ASSET_PREFIX = "asset://"
        private val COLOR_PRIMARY_DARK = Color.parseColor("#D6336C")
    }

    private data class ArticleHomeSection(
        val title: String,
        val articles: List<ArticleResponse>,
        val badges: Map<String, String> = emptyMap()
    )
}
