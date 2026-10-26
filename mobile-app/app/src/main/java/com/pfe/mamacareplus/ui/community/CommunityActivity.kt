package com.pfe.mamacareplus.ui.community

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivityCommunityBinding
import com.pfe.mamacareplus.model.response.community.CommentResponse
import com.pfe.mamacareplus.model.response.community.CommunityPostResponse
import com.pfe.mamacareplus.ui.applyCompactOutlineButton
import com.pfe.mamacareplus.ui.applyCompactPrimaryButton
import com.pfe.mamacareplus.ui.applyListCardStyle
import com.pfe.mamacareplus.ui.applyNestedCardStyle
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.applyPremiumInputFeedback
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.main.setMainContentView
import com.pfe.mamacareplus.viewmodel.CommentUiState
import com.pfe.mamacareplus.viewmodel.CommentViewModel
import com.pfe.mamacareplus.viewmodel.CommunityUiState
import com.pfe.mamacareplus.viewmodel.CommunityViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class CommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityBinding
    private val communityViewModel: CommunityViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()

    private var selectedPostId: Int? = null
    private var selectedCommentsContainer: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setMainContentView(binding.root, activeTab = null)

        setupListeners()
        observeCommunity()
        observeComments()

        communityViewModel.loadPosts()
    }

    private fun setupListeners() {
        binding.tilPostContent.bindLiveValidation(binding.etPostContent) { value ->
            value.trim().isNotBlank()
        }
        binding.tilImageUrl.applyPremiumInputFeedback(binding.etImageUrl)
        binding.btnCreatePost.applyModernPressAnimation()
        binding.btnCreatePost.setOnClickListener {
            val content = binding.etPostContent.text.toString().trim()
            val imageUrl = binding.etImageUrl.text.toString().trim()

            if (content.isBlank()) {
                binding.tilPostContent.error = "Contenu requis"
                return@setOnClickListener
            }

            communityViewModel.createPost(
                content = content,
                imageUrl = imageUrl.ifBlank { null }
            )

            binding.etPostContent.text?.clear()
            binding.etImageUrl.text?.clear()
        }
    }

    private fun observeCommunity() {
        lifecycleScope.launch {
            communityViewModel.communityState.collect { state ->
                when (state) {
                    is CommunityUiState.Loading -> {
                        binding.tvCommunityMessage.text = "Chargement des publications..."
                    }

                    is CommunityUiState.Empty -> {
                        binding.tvCommunityMessage.text = "Aucune publication pour le moment. Soyez la première à partager."
                        binding.listPosts.removeAllViews()
                    }

                    is CommunityUiState.Success -> {
                        renderPosts(state.posts)
                    }

                    is CommunityUiState.Error -> {
                        binding.tvCommunityMessage.text = state.message
                    }
                }
            }
        }
    }

    private fun observeComments() {
        lifecycleScope.launch {
            commentViewModel.commentState.collect { state ->
                when (state) {
                    is CommentUiState.Loading -> {
                        selectedCommentsContainer?.removeAllViews()
                        selectedCommentsContainer?.addView(simpleText("Chargement des commentaires..."))
                    }

                    is CommentUiState.Success -> {
                        selectedCommentsContainer?.let { container ->
                            renderComments(container, state.comments)
                        }
                    }

                    is CommentUiState.Error -> {
                        selectedCommentsContainer?.removeAllViews()
                        selectedCommentsContainer?.addView(simpleText(state.message))
                    }
                }
            }
        }
    }

    private fun renderPosts(posts: List<CommunityPostResponse>) {
        binding.listPosts.removeAllViews()
        binding.tvCommunityMessage.text = "${posts.size} publication(s) dans la communauté"

        posts.forEach { post ->
            binding.listPosts.addView(createPostView(post))
        }
    }

    private fun createPostView(post: CommunityPostResponse): View {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            applyListCardStyle()
        }

        val username = TextView(this).apply {
            text = post.username ?: "Utilisatrice MamaCare"
            textSize = 16f
            setTextColor(Color.parseColor("#D6336C"))
            setTypeface(null, Typeface.BOLD)
        }

        val content = TextView(this).apply {
            text = post.content
            textSize = 15f
            setTextColor(Color.parseColor("#2D1B24"))
            setPadding(0, 10, 0, 8)
        }

        val createdAt = TextView(this).apply {
            text = "Créé le : ${post.createdAt ?: "Date inconnue"}"
            textSize = 12f
            setTextColor(Color.parseColor("#7A5C69"))
        }

        val imageUrl = TextView(this).apply {
            text = post.imageUrl?.let { "Image : $it" } ?: ""
            textSize = 12f
            setTextColor(Color.parseColor("#6F5B63"))
            setPadding(0, 6, 0, 8)
        }

        val commentInputLayout = TextInputLayout(this).apply {
            hint = "Écrire un commentaire..."
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor = getColor(R.color.card_background)
            setBoxCornerRadii(dpFloat(16f), dpFloat(16f), dpFloat(16f), dpFloat(16f))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }

        val commentInput = TextInputEditText(commentInputLayout.context).apply {
            minHeight = dp(54)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine(false)
            setTextColor(getColor(R.color.input_text))
            setHintTextColor(getColor(R.color.input_placeholder))
        }

        commentInputLayout.addView(commentInput)
        commentInputLayout.applyPremiumInputFeedback(commentInput, invalidWhenBlank = true) { value ->
            value.trim().isNotBlank()
        }

        val commentsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 0)
        }

        val addCommentButton = MaterialButton(this).apply {
            text = "Ajouter un commentaire"
            applyCompactPrimaryButton(topMarginDp = 10)
            applyModernPressAnimation()
            setOnClickListener {
                val comment = commentInput.text.toString().trim()

                if (comment.isBlank()) {
                    commentInputLayout.error = "Commentaire requis"
                    return@setOnClickListener
                }

                selectedPostId = post.postId
                selectedCommentsContainer = commentsContainer

                commentViewModel.createComment(post.postId, comment)
                commentInput.text?.clear()
            }
        }

        val loadCommentsButton = MaterialButton(this).apply {
            text = "Afficher les commentaires"
            applyCompactOutlineButton(topMarginDp = 8)
            applyModernPressAnimation()
            setOnClickListener {
                selectedPostId = post.postId
                selectedCommentsContainer = commentsContainer
                commentViewModel.loadComments(post.postId)
            }
        }

        val deleteButton = MaterialButton(this).apply {
            text = "Supprimer la publication"
            applyCompactOutlineButton(topMarginDp = 8, destructive = true)
            applyModernPressAnimation()
            setOnClickListener {
                communityViewModel.deletePost(post.postId)
                Toast.makeText(this@CommunityActivity, "Publication supprimée", Toast.LENGTH_SHORT).show()
            }
        }

        container.addView(username)
        container.addView(content)
        container.addView(createdAt)

        if (!post.imageUrl.isNullOrBlank()) {
            container.addView(imageUrl)
        }

        container.addView(commentInputLayout)
        container.addView(addCommentButton)
        container.addView(loadCommentsButton)
        container.addView(commentsContainer)
        container.addView(deleteButton)

        return container
    }

    private fun renderComments(container: LinearLayout, comments: List<CommentResponse>) {
        container.removeAllViews()

        if (comments.isEmpty()) {
            container.addView(simpleText("Aucun commentaire pour le moment."))
            return
        }

        comments.forEach { comment ->
            val commentView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                applyNestedCardStyle()
            }

            val user = TextView(this).apply {
                text = comment.username ?: "Utilisatrice"
                textSize = 13f
                setTextColor(Color.parseColor("#D6336C"))
                setTypeface(null, Typeface.BOLD)
            }

            val content = TextView(this).apply {
                text = comment.content
                textSize = 14f
                setTextColor(Color.parseColor("#2D1B24"))
                setPadding(0, 4, 0, 4)
            }

            val delete = MaterialButton(this).apply {
                text = "Supprimer le commentaire"
                applyCompactOutlineButton(topMarginDp = 8, destructive = true)
                applyModernPressAnimation()
                setOnClickListener {
                    commentViewModel.deleteComment(comment.postId, comment.commentId)
                }
            }

            commentView.addView(user)
            commentView.addView(content)
            commentView.addView(delete)

            container.addView(commentView)
        }
    }

    private fun simpleText(message: String): TextView {
        return TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(Color.parseColor("#7A5C69"))
            setPadding(0, 8, 0, 8)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun dpFloat(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}
