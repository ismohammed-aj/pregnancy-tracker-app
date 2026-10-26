package com.pfe.mamacareplus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pfe.mamacareplus.databinding.ActivityVerifyResetCodeBinding
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.bindLiveValidation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.viewmodel.ForgotPasswordViewModel
import com.pfe.mamacareplus.viewmodel.PasswordResetUiState
import kotlinx.coroutines.launch

class VerifyResetCodeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyResetCodeBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyResetCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra(ForgotPasswordEmailScreen.EXTRA_EMAIL).orEmpty()
        if (email.isBlank()) {
            Toast.makeText(this, "Email manquant", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUiMotion()
        setupListeners()
        observeResetState()
    }

    private fun setupUiMotion() {
        binding.root.playSoftEntrance()
        binding.btnVerifyBack.applyModernPressAnimation()
        binding.btnVerifyCode.applyModernPressAnimation()
        binding.tilResetCode.bindLiveValidation(binding.etResetCode) {
            it.matches(Regex("\\d{6}"))
        }
    }

    private fun setupListeners() {
        binding.btnVerifyBack.setOnClickListener {
            finish()
        }

        binding.btnVerifyCode.setOnClickListener {
            val code = binding.etResetCode.text.toString().trim()

            binding.tilResetCode.error = null

            if (code.isBlank()) {
                binding.tilResetCode.error = "Code requis"
                return@setOnClickListener
            }

            if (!code.matches(Regex("\\d{6}"))) {
                binding.tilResetCode.error = "Le code doit contenir 6 chiffres"
                return@setOnClickListener
            }

            viewModel.verifyCode(email, code)
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
                        val code = binding.etResetCode.text.toString().trim()
                        Toast.makeText(this@VerifyResetCodeScreen, state.message, Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(this@VerifyResetCodeScreen, ResetPasswordScreen::class.java).apply {
                                putExtra(ForgotPasswordEmailScreen.EXTRA_EMAIL, email)
                                putExtra(EXTRA_CODE, code)
                            }
                        )
                        viewModel.resetState()
                    }
                    is PasswordResetUiState.Error -> {
                        setLoading(false)
                        binding.tilResetCode.error = state.message
                        Toast.makeText(this@VerifyResetCodeScreen, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnVerifyCode.isEnabled = !isLoading
        binding.etResetCode.isEnabled = !isLoading
        binding.btnVerifyCode.text = if (isLoading) "Vérification..." else "Vérifier le code"
    }

    companion object {
        const val EXTRA_CODE = "extra_code"
    }
}
