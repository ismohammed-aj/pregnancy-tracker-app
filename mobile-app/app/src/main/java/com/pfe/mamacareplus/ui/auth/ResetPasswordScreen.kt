package com.pfe.mamacareplus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.databinding.ActivityResetPasswordBinding
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.viewmodel.ForgotPasswordViewModel
import com.pfe.mamacareplus.viewmodel.PasswordResetUiState
import kotlinx.coroutines.launch

class ResetPasswordScreen : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private lateinit var email: String
    private lateinit var code: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra(ForgotPasswordEmailScreen.EXTRA_EMAIL).orEmpty()
        code = intent.getStringExtra(VerifyResetCodeScreen.EXTRA_CODE).orEmpty()
        if (email.isBlank() || code.isBlank()) {
            Toast.makeText(this, "Demande de réinitialisation invalide", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUiMotion()
        setupListeners()
        observeResetState()
    }

    private fun setupUiMotion() {
        binding.root.playSoftEntrance()
        binding.btnResetBack.applyModernPressAnimation()
        binding.btnResetPassword.applyModernPressAnimation()
        binding.tilNewPassword.bindLiveValidation(binding.etNewPassword) {
            it.length >= 6
        }
        binding.tilConfirmPassword.bindLiveValidation(binding.etConfirmPassword) {
            it.length >= 6 && it == binding.etNewPassword.text.toString()
        }
    }

    private fun setupListeners() {
        binding.btnResetBack.setOnClickListener {
            finish()
        }

        binding.btnResetPassword.setOnClickListener {
            val password = binding.etNewPassword.text.toString()
            val confirmation = binding.etConfirmPassword.text.toString()

            binding.tilNewPassword.error = null
            binding.tilConfirmPassword.error = null

            if (password.isBlank()) {
                binding.tilNewPassword.error = "Nouveau mot de passe requis"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.tilNewPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
                return@setOnClickListener
            }

            if (confirmation.isBlank()) {
                binding.tilConfirmPassword.error = "Confirmation requise"
                return@setOnClickListener
            }

            if (password != confirmation) {
                binding.tilConfirmPassword.error = "Les mots de passe ne correspondent pas"
                return@setOnClickListener
            }

            viewModel.resetPassword(email, code, password)
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
                        Toast.makeText(
                            this@ResetPasswordScreen,
                            "Mot de passe modifié avec succès",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(
                            Intent(this@ResetPasswordScreen, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        viewModel.resetState()
                        finish()
                    }
                    is PasswordResetUiState.Error -> {
                        setLoading(false)
                        binding.tilNewPassword.error = state.message
                        Toast.makeText(this@ResetPasswordScreen, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnResetPassword.isEnabled = !isLoading
        binding.etNewPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
        binding.btnResetPassword.text = if (isLoading) "Modification..." else "Modifier le mot de passe"
    }
}
