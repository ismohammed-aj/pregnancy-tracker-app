package com.pfe.mamacareplus.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        startActivity(Intent(this, InteractiveWelcomeActivity::class.java))
        overridePendingTransition(R.anim.onboarding_enter, R.anim.onboarding_exit)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.onboarding_background_top)
        window.navigationBarColor = getColor(R.color.onboarding_background_bottom)

        prepareIntroState()
        playIntroAnimation()
        handler.postDelayed(navigateRunnable, 2600)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }

    private fun prepareIntroState() {
        binding.logoContainer.alpha = 0f
        binding.logoContainer.scaleX = 0.78f
        binding.logoContainer.scaleY = 0.78f
        binding.appName.alpha = 0f
        binding.appName.translationY = 16f
        binding.tagline.alpha = 0f
        binding.tagline.translationY = 18f
    }

    private fun playIntroAnimation() {
        binding.logoContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(760)
            .setInterpolator(OvershootInterpolator(1.35f))
            .start()

        binding.appName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(260)
            .setDuration(560)
            .setInterpolator(DecelerateInterpolator())
            .start()

        binding.tagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(430)
            .setDuration(560)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}
