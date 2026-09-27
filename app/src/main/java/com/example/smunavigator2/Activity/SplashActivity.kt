package com.example.smunavigator2.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import com.bumptech.glide.Glide
import com.example.smunavigator2.R
import com.example.smunavigator2.Utils.PushUtils
import com.example.smunavigator2.databinding.ActivitySplashBinding
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen mode
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize App Check with Play Integrity
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Log.d("SplashActivity", "App Check initialized")

        // Opened by tapping a push: go straight to the notice or profile, with Home underneath for Back
        if (auth.currentUser != null && openPushTarget()) return

        //  Load mascot animation
        Glide.with(this)
            .asGif()
            .load(R.drawable.smu_mascot2)
            .into(binding.gifMascot)

        // Get Started button click listener
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun openPushTarget(): Boolean {
        val extras = intent.extras ?: return false
        val data = extras.keySet().associateWith { extras.get(it)?.toString() ?: "" }
        val target = PushUtils.targetIntent(this, data) ?: return false
        TaskStackBuilder.create(this)
            .addNextIntent(Intent(this, MainActivity::class.java))
            .addNextIntent(target)
            .startActivities()
        finish()
        return true
    }
}
