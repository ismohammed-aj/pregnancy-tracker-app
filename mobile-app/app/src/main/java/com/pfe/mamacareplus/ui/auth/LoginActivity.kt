package com.pfe.mamacareplus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.databinding.ActivityLoginBinding
import com.pfe.mamacareplus.repository.onboarding.OnboardingRepository
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.ui.home.HomeActivity
import com.pfe.mamacareplus.ui.questionnaire.QuestionnaireActivity
import com.pfe.mamacareplus.viewmodel.AuthUiState
import com.pfe.mamacareplus.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private val onboardingRepository by lazy { OnboardingRepository(applicationContext) }
    private var routingAfterLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiMotion()
        setupListeners()
        observeAuthState()
    }

    private fun setupUiMotion() {
        binding.root.playSoftEntrance()
        binding.btnLoginSubmit.applyModernPressAnimation()
        binding.tvGoToRegister.applyModernPressAnimation(0.98f)
        binding.tvForgotPassword.applyModernPressAnimation(0.98f)
        binding.tilLoginEmail.bindLiveValidation(binding.etEmail) {
            Patterns.EMAIL_ADDRESS.matcher(it.trim()).matches()
        }
        binding.tilLoginPassword.bindLiveValidation(binding.etPassword) {
            it.isNotBlank()
        }
    }

    private fun setupListeners() {
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordEmailScreen::class.java))
        }

        binding.btnLoginSubmit.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank()) {
                binding.etEmail.error = "Adresse email requise"
                return@setOnClickListener
            }

            if (password.isBlank()) {
                binding.etPassword.error = "Mot de passe requis"
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        binding.btnLoginSubmit.isEnabled = true
                    }

                    is AuthUiState.Loading -> {
                        binding.btnLoginSubmit.isEnabled = false
                        binding.btnLoginSubmit.text = "Connexion..."
                    }

                    is AuthUiState.Success -> {
                        if (!routingAfterLogin) {
                            routingAfterLogin = true
                            Toast.makeText(this@LoginActivity, "Connexion réussie", Toast.LENGTH_SHORT).show()
                            routeAfterLogin()
                        }
                    }

                    is AuthUiState.Error -> {
                        binding.btnLoginSubmit.isEnabled = true
                        binding.btnLoginSubmit.text = "Connexion"

                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun routeAfterLogin() {
        binding.btnLoginSubmit.isEnabled = false
        binding.btnLoginSubmit.text = "Vérification..."

        lifecycleScope.launch {
            val onboardingCompleted = onboardingRepository
                .isOnboardingCompleted()
                .getOrElse { false }

            val nextActivity = if (onboardingCompleted) {
                HomeActivity::class.java
            } else {
                QuestionnaireActivity::class.java
            }

            startActivity(
                Intent(this@LoginActivity, nextActivity).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }
    }
}
