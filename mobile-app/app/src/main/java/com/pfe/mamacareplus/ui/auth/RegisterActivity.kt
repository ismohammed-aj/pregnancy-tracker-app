package com.pfe.mamacareplus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.databinding.ActivityRegisterBinding
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.viewmodel.AuthUiState
import com.pfe.mamacareplus.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiMotion()
        setupListeners()
        observeAuthState()
    }

    private fun setupUiMotion() {
        binding.root.playSoftEntrance()
        binding.btnRegisterBack.applyModernPressAnimation()
        binding.btnRegisterSubmit.applyModernPressAnimation()
        binding.tvGoToLogin.applyModernPressAnimation(0.98f)
        binding.tilFullName.bindLiveValidation(binding.etFullName) {
            it.trim().isNotBlank()
        }
        binding.tilRegisterEmail.bindLiveValidation(binding.etRegisterEmail) {
            Patterns.EMAIL_ADDRESS.matcher(it.trim()).matches()
        }
        binding.tilRegisterPassword.bindLiveValidation(binding.etRegisterPassword) {
            it.length >= 6
        }
    }

    private fun setupListeners() {
        binding.btnRegisterBack.setOnClickListener {
            finish()
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnRegisterSubmit.setOnClickListener {
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()
            val username = binding.etFullName.text.toString().trim()

            if (username.isBlank()) {
                binding.etFullName.error = "Prénom requis"
                return@setOnClickListener
            }

            if (email.isBlank()) {
                binding.etRegisterEmail.error = "Adresse email requise"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etRegisterEmail.error = "Format email invalide"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.etRegisterPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
                return@setOnClickListener
            }

            viewModel.register(
                email = email,
                password = password,
                username = username,
                birthDate = null,
            )
        }

    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        binding.btnRegisterSubmit.isEnabled = true
                        binding.btnRegisterSubmit.text = "Créer un compte"
                    }

                    is AuthUiState.Loading -> {
                        binding.btnRegisterSubmit.isEnabled = false
                        binding.btnRegisterSubmit.text = "Création..."
                    }

                    is AuthUiState.Success -> {
                        binding.btnRegisterSubmit.isEnabled = true
                        binding.btnRegisterSubmit.text = "Créer un compte"
                        Toast.makeText(
                            this@RegisterActivity,
                            "Compte créé. Connectez-vous avec votre nouveau compte.",
                            Toast.LENGTH_LONG,
                        ).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }

                    is AuthUiState.Error -> {
                        binding.btnRegisterSubmit.isEnabled = true
                        binding.btnRegisterSubmit.text = "Créer un compte"
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }
}
