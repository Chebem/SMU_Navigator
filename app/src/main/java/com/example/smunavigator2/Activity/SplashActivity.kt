package com.example.smunavigator2.Activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smunavigator2.R
import com.example.smunavigator2.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the splash screen fullscreen (remove status bar overlay)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Inflate the layout using ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start MainActivity on button click
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Prevent going back to SplashActivity
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.snu_mascot2) // your .gif file in res/drawable
            .into(binding.gifMascot)

    }
}
