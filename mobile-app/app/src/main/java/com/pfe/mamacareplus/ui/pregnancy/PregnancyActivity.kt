package com.pfe.mamacareplus.ui.pregnancy

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityPregnancyBinding
import com.pfe.mamacareplus.model.response.pregnancy.BabyDevelopmentResponse
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.ui.main.MainTab
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.ui.profile.ProfileActivity
import com.pfe.mamacareplus.viewmodel.BabyDevelopmentUiState
import com.pfe.mamacareplus.viewmodel.BabyDevelopmentViewModel
import com.pfe.mamacareplus.viewmodel.PregnancyUiState
import com.pfe.mamacareplus.viewmodel.PregnancyViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PregnancyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPregnancyBinding
    private lateinit var weekSelector: WeekSelector
    private lateinit var babySizeCard: BabySizeCard
    private lateinit var comparisonSelector: ComparisonSelector
    private lateinit var imageStrip: BabyDevelopmentImageStrip
    private lateinit var developmentTimeline: DevelopmentTimeline

    private val viewModel: PregnancyViewModel by viewModels()
    private val babyDevelopmentViewModel: BabyDevelopmentViewModel by viewModels()
    private val weekData by lazy { buildWeeks() }

    private var selectedWeek = 24
    private var profileWeek = 24
    private var selectedMode = ComparisonMode.FRUIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPregnancyBinding.inflate(layoutInflater)
        setMainContentView(binding.root, MainTab.BABY)

        setupComponents()
        setupNavigation()
        observePregnancy()
        observeBabyDevelopment()

        renderWeek()
        viewModel.loadPregnancyProfile()
    }

    private fun setupComponents() {
        weekSelector = WeekSelector(this)
        babySizeCard = BabySizeCard(this)
        comparisonSelector = ComparisonSelector(this)
        imageStrip = BabyDevelopmentImageStrip(this)
        developmentTimeline = DevelopmentTimeline(this)

        binding.weekSelectorContainer.addView(weekSelector)
        binding.babySizeContainer.addView(babySizeCard)
        binding.comparisonSelectorContainer.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(comparisonSelector)
            addView(imageStrip, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8.dp
            })
        })
        binding.timelineContainer.addView(developmentTimeline)

        comparisonSelector.bind(selectedMode) { mode ->
            selectedMode = mode
            renderWeek()
        }

        binding.babyDevelopmentContent.playSoftEntrance()
    }

    private fun setupNavigation() {
        binding.btnBabyBack.applyModernPressAnimation()
        binding.btnBabyProfile.applyModernPressAnimation()
        binding.btnBabyBack.setOnClickListener { finish() }
        binding.btnBabyProfile.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
    }

    private fun observePregnancy() {
        lifecycleScope.launch {
            viewModel.pregnancyState.collect { state ->
                when (state) {
                    is PregnancyUiState.Loading -> {
                        binding.tvPregnancyMessage.text = "Chargement de votre profil de grossesse..."
                    }

                    is PregnancyUiState.Success -> {
                        profileWeek = state.profile.currentWeek.coerceIn(1, 40)
                        selectedWeek = profileWeek
                        val progress = state.profile.progressPercentage.roundToInt().coerceIn(0, 100)
                        binding.tvPregnancyMessage.text =
                            "Semaine ${state.profile.currentWeek} - $progress% complet - ${state.profile.remainingDays} jours restants"
                        renderWeek()
                        babyDevelopmentViewModel.loadDevelopmentByWeek(selectedWeek)
                    }

                    is PregnancyUiState.Error -> {
                        binding.tvPregnancyMessage.text =
                            "Mode aperçu. Complétez votre profil de grossesse pour synchroniser votre semaine exacte."
                        profileWeek = selectedWeek
                        renderWeek()
                    }
                }
            }
        }
    }

    private fun observeBabyDevelopment() {
        lifecycleScope.launch {
            babyDevelopmentViewModel.babyDevelopmentState.collect { state ->
                when (state) {
                    is BabyDevelopmentUiState.Idle -> Unit
                    is BabyDevelopmentUiState.Loading -> {
                        binding.tvPregnancyMessage.text = "Chargement du développement de la semaine $selectedWeek..."
                    }

                    is BabyDevelopmentUiState.Success -> {
                        if (state.development.weekNumber == selectedWeek) {
                            renderWeek(state.development)
                        }
                    }

                    is BabyDevelopmentUiState.Error -> {
                        renderWeek()
                    }
                }
            }
        }
    }

    private fun renderWeek(development: BabyDevelopmentResponse? = null) {
        val base = weekData[selectedWeek - 1]
        val data = mergeDevelopment(base, development)
        val dayStart = ((selectedWeek - 1) * 7 + 1).coerceAtLeast(1)
        val dayEnd = (selectedWeek * 7).coerceAtMost(280)

        binding.tvCurrentWeekLabel.text = "Jour $dayStart-$dayEnd - Semaine $selectedWeek"
        binding.tvPregnancyTitle.text = "Développement du bébé"

        weekSelector.bind(selectedWeek) { week ->
            selectedWeek = week
            renderWeek()
            babyDevelopmentViewModel.loadDevelopmentByWeek(week)
        }

        babySizeCard.bind(data, selectedMode)
        imageStrip.bind(weekData.map { it.itemFor(selectedMode) }, selectedWeek) { item ->
            selectedWeek = item.week
            renderWeek()
            babyDevelopmentViewModel.loadDevelopmentByWeek(item.week)
        }
        renderStats(data)
        renderInsights(data)
        renderTimeline()
    }

    private fun mergeDevelopment(
        base: BabyWeekData,
        development: BabyDevelopmentResponse?
    ): BabyWeekData {
        if (development == null) return base
        val backendComparison = development.comparisonName?.takeIf { it.isNotBlank() }
            ?: development.babySize?.takeIf { it.isNotBlank() }
        return base.copy(
            fruitComparison = backendComparison ?: base.fruitComparison,
            weightGrams = development.babyWeightGrams ?: base.weightGrams,
            lengthCm = development.babyLengthCm ?: base.lengthCm,
            development = development.description?.takeIf { it.isNotBlank() } ?: base.development,
            motherTip = development.advice?.takeIf { it.isNotBlank() } ?: base.motherTip
        )
    }

    private fun renderStats(data: BabyWeekData) {
        binding.statsContainer.removeAllViews()

        val rows = listOf(
            listOf(
                Triple(formatLength(data.lengthCm), "Taille", "Estimation de la tête aux talons"),
                Triple(formatWeight(data.weightGrams), "Poids", "Moyenne hebdomadaire indicative")
            ),
            listOf(
                Triple(data.stage, "Étape de développement", "Croissance principale cette semaine"),
                Triple(data.movementLevel, "Mouvements", "Ce que vous pouvez ressentir")
            )
        )

        rows.forEachIndexed { index, rowItems ->
            binding.statsContainer.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) topMargin = 10.dp
                }

                rowItems.forEachIndexed { itemIndex, item ->
                    addView(GrowthStatCard(this@PregnancyActivity).apply {
                        bind(item.first, item.second, item.third)
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            if (itemIndex == 0) marginEnd = 5.dp else marginStart = 5.dp
                        }
                    })
                }
            })
        }
    }

    private fun renderInsights(data: BabyWeekData) {
        binding.developmentInsightsContainer.removeAllViews()
        val cards = listOf(
            "Ce qui se développe cette semaine" to data.development,
            "Progression des organes du bébé" to data.organs,
            "Conseils pour la maman" to data.motherTip,
            "Nutrition recommandée" to data.nutrition,
            "Explication des symptômes" to data.symptoms
        )

        cards.forEach { (title, body) ->
            binding.developmentInsightsContainer.addView(DevelopmentCard(this).apply {
                bind(title, body)
            })
        }
    }

    private fun renderTimeline() {
        val start = (selectedWeek - 2).coerceAtLeast(1)
        val end = (selectedWeek + 2).coerceAtMost(40)
        developmentTimeline.bind(weekData.subList(start - 1, end))
    }

    private fun buildWeeks(): List<BabyWeekData> {
        val fruits = listOf(
            "petite graine", "graine de sésame", "graine de chia", "graine de pavot", "pépin de pomme",
            "petit pois", "myrtille", "framboise", "cerise", "fraise",
            "figue", "citron vert", "prune", "citron", "pomme", "avocat",
            "poire", "poivron", "mangue", "banane", "carotte", "papaye",
            "pamplemousse", "épi de maïs", "rutabaga", "courgette", "chou-fleur",
            "aubergine", "courge butternut", "chou", "noix de coco", "courge",
            "ananas", "cantaloup", "melon miel", "melon",
            "laitue romaine", "poireau", "mini pastèque", "pastèque"
        )
        val animals = listOf(
            "point de coccinelle", "coccinelle", "abeille", "petit papillon", "oeuf de colibri",
            "petit escargot", "petit hippocampe", "petite grenouille", "souris des champs", "petit hamster",
            "jeune lézard", "petit moineau", "cochon d'Inde", "bébé hérisson", "patte de chaton",
            "petit chaton", "petit lapin", "petit caneton", "petit chiot", "petite tortue",
            "petit furet", "jeune chaton", "petit bébé loutre", "sabot d'agneau nouveau-né", "petit suricate",
            "petit manchot", "bébé lapin", "petit renardeau", "jeune chat", "petit chiot",
            "jeune lièvre", "petit koala", "bébé phoque", "petit agneau", "jeune beagle",
            "petit terrier", "faon nouveau-né", "petit chat", "chevreau nouveau-né", "agneau nouveau-né"
        )
        val objects = listOf(
            "tête d'épingle", "petite perle", "grain de riz", "bouton de chemise", "petite perle nacrée",
            "pierre de bague", "bouchon de bouteille", "grande pièce", "porte-clés", "souris d'ordinateur",
            "balle de tennis", "tasse", "appareil photo compact", "étui à lunettes", "petit livre",
            "étui à banane", "télécommande", "bouteille d'eau", "petite pochette", "épi de maïs",
            "boîte repas", "parapluie de voyage", "petit bol", "mini tablette", "pain",
            "coussin", "petit sac à dos", "serviette roulée", "petit panier", "couverture pliée",
            "body de nouveau-né", "oreiller doux", "paquet de couches", "couverture bébé", "oreiller de berceau",
            "couverture d'emmaillotage", "sac de maternité", "porte-bébé", "petite pastèque", "citrouille"
        )
        val weights = listOf(
            0.0, 0.0, 0.1, 0.2, 1.0, 1.5, 2.0, 3.0, 4.0, 7.0,
            14.0, 23.0, 43.0, 70.0, 100.0, 140.0, 190.0, 240.0, 300.0, 330.0,
            360.0, 430.0, 500.0, 600.0, 660.0, 760.0, 875.0, 1000.0, 1150.0, 1320.0,
            1500.0, 1700.0, 1900.0, 2150.0, 2400.0, 2650.0, 2850.0, 3100.0, 3300.0, 3500.0
        )
        val lengths = listOf(
            0.1, 0.1, 0.15, 0.2, 0.3, 0.6, 1.0, 1.6, 2.3, 3.1,
            4.1, 5.4, 7.4, 8.7, 10.1, 11.6, 13.0, 14.2, 15.3, 25.6,
            26.7, 27.8, 28.9, 30.0, 34.6, 35.6, 36.6, 37.6, 38.6, 39.9,
            41.1, 42.4, 43.7, 45.0, 46.2, 47.4, 48.6, 49.8, 50.7, 51.2
        )

        return (1..40).map { week ->
            val stage = stageForWeek(week)
            BabyWeekData(
                week = week,
                fruitComparison = fruits[week - 1],
                animalComparison = animals[week - 1],
                objectComparison = objects[week - 1],
                lengthCm = lengths[week - 1],
                weightGrams = weights[week - 1],
                stage = stage,
                movementLevel = movementForWeek(week),
                development = developmentForWeek(week),
                organs = organsForWeek(week),
                motherTip = tipForWeek(week),
                nutrition = nutritionForWeek(week),
                symptoms = symptomsForWeek(week),
                visualRes = visualForWeek(week),
                fruitImageUrl = BabyDevelopmentImageCatalog.imageUrlFor(
                    week,
                    ComparisonMode.FRUIT,
                    fruits[week - 1]
                ),
                animalImageUrl = BabyDevelopmentImageCatalog.imageUrlFor(
                    week,
                    ComparisonMode.ANIMAL,
                    animals[week - 1]
                ),
                objectImageUrl = BabyDevelopmentImageCatalog.imageUrlFor(
                    week,
                    ComparisonMode.OBJECT,
                    objects[week - 1]
                )
            )
        }
    }

    private fun stageForWeek(week: Int): String {
        return when (week) {
            in 1..4 -> "Fondations"
            in 5..8 -> "Formation précoce"
            in 9..12 -> "Premier trimestre"
            in 13..16 -> "Traits qui se précisent"
            in 17..20 -> "Mouvements en développement"
            in 21..24 -> "Éveil des sens"
            in 25..28 -> "Renforcement"
            in 29..32 -> "Croissance du cerveau"
            in 33..36 -> "Respiration en entraînement"
            else -> "Bientôt prêt"
        }
    }

    private fun movementForWeek(week: Int): String {
        return when (week) {
            in 1..12 -> "Calme"
            in 13..18 -> "Petits frémissements"
            in 19..24 -> "Coups légers"
            in 25..32 -> "Rythme actif"
            else -> "Étirements forts"
        }
    }

    private fun developmentForWeek(week: Int): String {
        return when (week) {
            in 1..4 -> "Les premières cellules s'organisent et préparent les bases du placenta et de l'embryon."
            in 5..8 -> "De petites structures se forment rapidement, dont la zone du coeur, les bourgeons des membres et les traits du visage."
            in 9..12 -> "La silhouette du bébé devient plus claire pendant que les doigts, les orteils et les premiers réflexes se développent."
            in 13..16 -> "La croissance devient plus visible. Les os se renforcent et les détails du visage se précisent."
            in 17..20 -> "Les mouvements deviennent plus faciles à percevoir et le bébé commence à s'étirer et à se tourner."
            in 21..24 -> "Les sens deviennent plus actifs, avec l'audition et les rythmes veille-sommeil qui se développent progressivement."
            in 25..28 -> "Le bébé gagne en force et en graisse corporelle pendant que les poumons et le cerveau poursuivent leur maturation."
            in 29..32 -> "Les connexions du cerveau évoluent rapidement et les mouvements peuvent devenir plus réguliers."
            in 33..36 -> "Le bébé s'entraîne aux mouvements respiratoires, prend du poids et se prépare aux dernières semaines."
            else -> "Le bébé prend son poids final, coordonne ses réflexes et se prépare à la naissance."
        }
    }

    private fun organsForWeek(week: Int): String {
        return when (week) {
            in 1..8 -> "Le coeur précoce, le tube neural et le placenta se développent. Cette étape est délicate et rapide."
            in 9..16 -> "Les organes principaux sont présents et continuent de se préciser. Les systèmes digestif et urinaire commencent à s'exercer."
            in 17..24 -> "L'audition, les couches de la peau et les structures pulmonaires progressent pendant que le bébé devient plus réactif."
            in 25..32 -> "Les poumons, le cerveau et le système nerveux mûrissent pendant que le bébé constitue ses réserves d'énergie."
            else -> "Les poumons et le cerveau terminent leur maturation. Les réflexes du bébé deviennent mieux coordonnés."
        }
    }

    private fun tipForWeek(week: Int): String {
        return when (week) {
            in 1..12 -> "Reposez-vous quand c'est possible, gardez vos rendez-vous et contactez votre médecin ou sage-femme en cas de saignement, douleur forte ou fièvre."
            in 13..24 -> "La marche douce, l'hydratation et un sommeil confortable peuvent aider votre corps à s'adapter."
            in 25..32 -> "Surveillez les mouvements selon les conseils de votre soignant et demandez de l'aide s'ils semblent réduits ou inhabituels."
            else -> "Préparez votre sac de maternité, confirmez vos rendez-vous et appelez votre équipe médicale en cas de contractions, perte de liquide ou signes d'alerte."
        }
    }

    private fun nutritionForWeek(week: Int): String {
        return when (week) {
            in 1..12 -> "Privilégiez les aliments riches en folates, les repas simples, l'eau et les vitamines prénatales recommandées par votre soignant."
            in 13..24 -> "Ajoutez des sources de fer, de calcium et de protéines dans la journée, avec de l'eau à portée de main."
            in 25..32 -> "Des repas équilibrés avec protéines, fibres et bonnes graisses peuvent soutenir l'énergie et la digestion."
            else -> "Gardez des repas faciles à digérer et riches en nutriments, et demandez conseil à votre équipe médicale en cas de restriction."
        }
    }

    private fun symptomsForWeek(week: Int): String {
        return when (week) {
            in 1..12 -> "Nausées, fatigue et sensibilité des seins peuvent être fréquentes. Des symptômes forts nécessitent un avis médical."
            in 13..24 -> "Des douleurs ligamentaires, des changements d'appétit et les premiers mouvements peuvent apparaître."
            in 25..32 -> "La pression dans le dos, le reflux et les changements de sommeil peuvent augmenter avec la croissance du bébé."
            else -> "La pression pelvienne, les contractions de Braxton Hicks et la fatigue peuvent être plus fortes. Vérifiez toujours les signes d'alerte avec votre équipe médicale."
        }
    }

    private fun visualForWeek(week: Int): Int {
        return when (week) {
            in 1..8 -> R.drawable.baby_dev_seed
            in 9..16 -> R.drawable.baby_dev_avocado
            in 17..24 -> R.drawable.baby_dev_corn
            in 25..32 -> R.drawable.baby_dev_melon
            else -> R.drawable.baby_dev_pumpkin
        }
    }

    private fun formatWeight(weight: Double): String {
        return if (weight >= 1000.0) {
            String.format("%.2f kg", weight / 1000.0)
        } else if (weight % 1.0 == 0.0) {
            "${weight.toInt()} g"
        } else {
            String.format("%.1f g", weight)
        }
    }

    private fun formatLength(length: Double): String {
        return if (length % 1.0 == 0.0) {
            "${length.toInt()} cm"
        } else {
            String.format("%.1f cm", length)
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
