package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Set up View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply entrance animations
        applyIntroAnimations();

        // Navigate to Campus
        binding.campusSection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Campus clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, CampusActivity.class));
        });

        // Navigate to City Guide
        binding.cityGuideSection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "City Guide clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, CityGuideActivity.class));
        });

        // Set up Recommended RecyclerView
        binding.recommendedView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        // Navigate to Events
      /*  binding.eventsSection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Events clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, EventsActivity.class));
        });*/


        // Setup Bottom Navigation
        setupBottomNav(R.id.home);

        // ✅ Correctly Load GIFs into ImageViews
        Glide.with(this)
                .asGif()
                .load(R.drawable.bus) // your animated bus schedule gif
                .into((ImageView) findViewById(R.id.busScheduleIcon));

        Glide.with(this)
                .asGif()
                .load(R.drawable.school) // your animated campus gif
                .into((ImageView) findViewById(R.id.campusIcon));

        Glide.with(this)
                .asGif()
                .load(R.drawable.building) // your animated city guide gif
                .into((ImageView) findViewById(R.id.cityGuideIcon));

        Glide.with(this)
                .asGif()
                .load(R.drawable.calendar) // your animated events gif
                .into((ImageView) findViewById(R.id.eventsIcon));

        Glide.with(this)
                .asGif()
                .load(R.drawable.worldwide) // your animated chat gif
                .into((ImageView) findViewById(R.id.smutalkIcon));
    }

    private void applyIntroAnimations() {

        animateMenuCards();

        // Animate profile picture
        binding.imageView4.setAlpha(0f);
        binding.imageView4.setTranslationY(-50f);
        binding.imageView4.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(100)
                .setDuration(500)
                .start();

        // Animate "Hi, Alex"
        binding.textView14.setAlpha(0f);
        binding.textView14.setTranslationX(-50f);
        binding.textView14.animate()
                .alpha(1f)
                .translationX(0f)
                .setStartDelay(300)
                .setDuration(600)
                .start();

        // Animate tagline
        binding.textView15.setAlpha(0f);
        binding.textView15.setTranslationY(30f);
        binding.textView15.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(600)
                .start();

        // Animate search bar
        binding.editTextText.setAlpha(0f);
        binding.editTextText.setTranslationY(50f);
        binding.editTextText.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(800)
                .setDuration(600)
                .start();
    }

    private void animateMenuCards() {
        int delay = 0;
        int duration = 300;

        View[] cards = {
                binding.campusSection,
                binding.cityGuideSection,
                binding.eventsSection,
                //binding.smuDeptSection,
                binding.smuTalkSection,
                //binding.profileSection
        };

        for (View card : cards) {
            card.setAlpha(0f);
            card.setTranslationY(30f);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .start();
            delay += 150;
        }
    }


    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = binding.navigationBar;
        bottomNav.setItemSelected(selectedItemId, true);

        bottomNav.setOnItemSelectedListener(id -> {
            if (id == selectedItemId) return;

            Intent intent = null;

            if (id == R.id.home) {
                intent = new Intent(this, MainActivity.class);
            } else if (id == R.id.explore) {
                intent = new Intent(this, ExploreActivity.class);
            } else if (id == R.id.favorite) {
                intent = new Intent(this, FavoritesActivity.class);
            } else if (id == R.id.profile) {
                intent = new Intent(this, ProfilePageActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }
}