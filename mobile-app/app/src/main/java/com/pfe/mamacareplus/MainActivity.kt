package com.pfe.mamacareplus

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pfe.mamacareplus.databinding.ActivityMainBinding
import com.pfe.mamacareplus.ui.auth.LoginActivity
import com.pfe.mamacareplus.ui.auth.RegisterActivity
import com.pfe.mamacareplus.ui.common.applyModernPressAnimation
import com.pfe.mamacareplus.ui.common.playSoftEntrance
import com.pfe.mamacareplus.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.playSoftEntrance()
        binding.btnLogin.applyModernPressAnimation()
        binding.btnRegister.applyModernPressAnimation()
        binding.btnHome.applyModernPressAnimation()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}
