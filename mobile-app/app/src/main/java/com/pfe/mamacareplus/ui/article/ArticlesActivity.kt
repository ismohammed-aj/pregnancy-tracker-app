package com.pfe.mamacareplus.ui.article

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.databinding.ActivityArticlesBinding
import com.pfe.mamacareplus.model.response.article.ArticleResponse
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.ui.main.MainTab
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.viewmodel.ArticleUiState
import com.pfe.mamacareplus.viewmodel.ArticleViewModel
import kotlinx.coroutines.launch

class ArticlesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticlesBinding
    private val viewModel: ArticleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticlesBinding.inflate(layoutInflater)
        setMainContentView(binding.root, MainTab.ARTICLES)

        binding.articlesContainer.playSoftEntrance()
        observeArticles()
        viewModel.loadArticles()
    }

    private fun observeArticles() {
        lifecycleScope.launch {
            viewModel.articleState.collect { state ->
                when (state) {
                    is ArticleUiState.Loading -> {
                        binding.tvArticlesMessage.text = "Chargement des conseils..."
                    }

                    is ArticleUiState.Empty -> {
                        binding.tvArticlesMessage.text = "Aucun conseil disponible pour le moment."
                        binding.sectionsContainer.removeAllViews()
                    }

                    is ArticleUiState.Success -> {
                        binding.tvArticlesMessage.text = ""
                        renderSections(state.articles)
                    }

                    is ArticleUiState.Error -> {
                        binding.tvArticlesMessage.text = state.message
                        binding.sectionsContainer.removeAllViews()
                    }
                }
            }
        }
    }

    private fun renderSections(articles: List<ArticleResponse>) {
        binding.sectionsContainer.removeAllViews()

        val sections = listOf(
            SectionSpec(
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
            SectionSpec(
                title = "Conseils grossesse",
                articles = articles.pickByKeys(
                    "article_mental_respiration",
                    "article_sante_poids",
                    "article_conseils_valise",
                    "article_conseils_retour",
                    "article_activite_dos"
                )
            ),
            SectionSpec(
                title = "Développement du bébé",
                articles = articles.pickByKeys(
                    "article_bebe_developpement",
                    "article_bebe_mouvements",
                    "article_rdv_echographie"
                )
            ),
            SectionSpec(
                title = "Nutrition et hydratation",
                articles = articles.pickByKeys(
                    "article_nutrition_assiette",
                    "article_nutrition_micronutrients",
                    "article_nutrition_nausees"
                )
            ),
            SectionSpec(
                title = "Vidéos les plus recherchées",
                badges = mapOf(
                    "article_rdv_consultation" to "Vidéo",
                    "article_mental_soutien" to "Vidéo"
                ),
                articles = articles.pickByKeys(
                    "article_rdv_consultation",
                    "article_mental_soutien"
                )
            )
        )

        sections.forEach { section ->
            val items = section.articles.map { article ->
                ArticleCardItem(article, section.badges[article.localKey()])
            }
            if (items.isNotEmpty()) {
                binding.sectionsContainer.addView(ArticleSection(this).apply {
                    bind(section.title, items)
                })
            }
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

    private data class SectionSpec(
        val title: String,
        val articles: List<ArticleResponse>,
        val badges: Map<String, String> = emptyMap()
    )
}
