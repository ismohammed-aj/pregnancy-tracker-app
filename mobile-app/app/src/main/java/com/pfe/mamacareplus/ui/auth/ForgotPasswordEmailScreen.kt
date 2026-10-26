package com.pfe.mamacareplus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.databinding.ActivityForgotPasswordEmailBinding
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.viewmodel.ForgotPasswordViewModel
import com.pfe.mamacareplus.viewmodel.PasswordResetUiState
import kotlinx.coroutines.launch

class ForgotPasswordEmailScreen : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordEmailBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUiMotion()
        setupListeners()
        observeResetState()
    }

    private fun setupUiMotion() {
        binding.root.playSoftEntrance()
        binding.btnForgotBack.applyModernPressAnimation()
        binding.btnSendCode.applyModernPressAnimation()
        binding.tilResetEmail.bindLiveValidation(binding.etResetEmail) {
            Patterns.EMAIL_ADDRESS.matcher(it.trim()).matches()
        }
    }

    private fun setupListeners() {
        binding.btnForgotBack.setOnClickListener {
            finish()
        }

        binding.btnSendCode.setOnClickListener {
            val email = binding.etResetEmail.text.toString().trim()

            binding.tilResetEmail.error = null

            if (email.isBlank()) {
                binding.tilResetEmail.error = "Email requis"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilResetEmail.error = "Format email invalide"
                return@setOnClickListener
            }

            viewModel.sendCode(email)
        }
    }

    private fun observeResetState() {
        lifecycleScope.launch {
            viewModel.resetState.collect { state ->
                when (state) {
                    is PasswordResetUiState.Idle -> setLoading(false)
                    is PasswordResetUiState.Loading -> setLoading(true)
                    is PasswordResetUiState.Success -> {
                        setLoading(false)
                        val email = binding.etResetEmail.text.toString().trim()
                        Toast.makeText(this@ForgotPasswordEmailScreen, state.message, Toast.LENGTH_LONG).show()
                        startActivity(
                            Intent(this@ForgotPasswordEmailScreen, VerifyResetCodeScreen::class.java).apply {
                                putExtra(EXTRA_EMAIL, email)
                            }
                        )
                        viewModel.resetState()
                    }
                    is PasswordResetUiState.Error -> {
                        setLoading(false)
                        binding.tilResetEmail.error = state.message
                        Toast.makeText(this@ForgotPasswordEmailScreen, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSendCode.isEnabled = !isLoading
        binding.etResetEmail.isEnabled = !isLoading
        binding.btnSendCode.text = if (isLoading) "Envoi..." else "Envoyer le code"
    }

    companion object {
        const val EXTRA_EMAIL = "extra_email"
    }
}
