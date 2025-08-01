package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Adapter.CommitteeAdapter;
import com.example.smunavigator2.Adapter.PostsAdapter;
import com.example.smunavigator2.Domain.Committee;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.example.smunavigator2.Activity.CityGuideActivity;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // ✅ Initialize App Check (SafetyNet)
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );



        // Set up View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initCommittee();
        initSocialFeed();

        // Apply entrance animations
        applyIntroAnimations();

        ImageView notificationButton = findViewById(R.id.notificationBell);
        notificationButton.setOnClickListener(v -> {
            // Example: Open a NotificationActivity
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        View badgeView = findViewById(R.id.badgeView);

        // Check Firebase for unread notifications
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    badgeView.setVisibility(View.VISIBLE); // 🔴 show red dot
                } else {
                    badgeView.setVisibility(View.GONE); // hide red dot
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                badgeView.setVisibility(View.GONE);
            }
        });


        // Navigate to Campus
        binding.campusSection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Campus clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, CampusActivity.class));
        });


        // Navigate to CityGuide
        binding.cityGuideSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CityGuideActivity.class);
            intent.putExtra("id", "city"); // or any relevant ID
            intent.putExtra("title", "City Guide");
            startActivity(intent);
        });

        // Navigate to Events (Semester Calendar)
        binding.eventsSection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Semester Events clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, SemesterEventsActivity.class));
        });

        //Navigate to Map
        binding.liveMapSection.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening Live Campus Map", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, GoogleMapActivity.class);
            startActivity(intent);
        });

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                for (Signature signature : info.signingInfo.getApkContentsSigners()) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                    Log.d("KeyHash", "KeyHash: " + keyHash);
                }
            }
        } catch (Exception e) {
            Log.e("KyHash", "Error", e);
        }



        // Set up Recommended RecyclerView
        binding.recommendedView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );


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

        loadUserGreeting(); // 👈 Add this after setContentView and other setups
    }

    private void initCommittee() {
        String node = getLanguageBasedNode("committee"); // will resolve to committeeEn or committeeKr
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(node);

        binding.progressBarCommittee.setVisibility(View.VISIBLE); // ← Add a ProgressBar with this ID in XML

        ArrayList<Committee> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Committee item = issue.getValue(Committee.class);
                        Log.d("CommitteeData", "Loaded: " + item.getTitle());

                        if (item != null) {
                            list.add(item);
                        }
                    }

                    binding.recommendedView.setLayoutManager(
                            new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false)
                    );
                    CommitteeAdapter adapter = new CommitteeAdapter(list);
                    binding.recommendedView.setAdapter(adapter);

                    if (!list.isEmpty())
                    {
                        Toast.makeText(MainActivity.this, "No committees available.", Toast.LENGTH_SHORT).show();

                    }
                }

                binding.progressBarCommittee.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarCommittee.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Failed to load committees.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void initSocialFeed() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("profiles");
        binding.progressBarSocial.setVisibility(View.VISIBLE);

        ArrayList<Post> feedList = new ArrayList<>();

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedList.clear();

                for (DataSnapshot profileSnap : snapshot.getChildren()) {
                    if (profileSnap.hasChild("posts")) { // ✅ check first
                        DataSnapshot postsSnap = profileSnap.child("posts");

                        for (DataSnapshot postSnap : postsSnap.getChildren()) {
                            String imageUrl = postSnap.child("imageUrl").getValue(String.class);

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                feedList.add(new Post(
                                        List.of(imageUrl),
                                        "", // caption (optional)
                                        profileSnap.getKey(), // userId
                                        System.currentTimeMillis()
                                ));
                            }
                        }
                    }
                }

                binding.socialFeedRecycler.setLayoutManager(
                        new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false)
                );

                PostsAdapter adapter = new PostsAdapter(feedList, post -> {
                    Toast.makeText(MainActivity.this, "Clicked post", Toast.LENGTH_SHORT).show();
                });

                binding.socialFeedRecycler.setAdapter(adapter);
                binding.progressBarSocial.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("initSocialFeed", "Failed to load posts", error.toException());
                binding.progressBarSocial.setVisibility(View.GONE);
            }
        });
    }

    private void loadUserGreeting() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Log.d("MainActivity", "No user logged in.");
            return;
        }

        DatabaseReference profileRef = database.getReference("profiles").child(uid);
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("profileName").getValue(String.class);
                    String imageUrl = snapshot.child("profileImage").getValue(String.class);

                    // Update greeting text
                    if (name != null) {
                        String greeting = getString(R.string.hi_user, name);
                        binding.textView14.setText(greeting);
                    }

                    // Update profile image
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.profile)
                                .into(binding.imageView);
                    }
                } else {
                    Log.d("MainActivity", "Profile not found in database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Failed to load profile: " + error.getMessage());
            }
        });
    }

    private String getLanguageBasedNode(String baseNode) {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals("ko")) {
            return baseNode + "Kr";
        } else {
            return baseNode + "En";
        }
    }



    private void applyIntroAnimations() {

        animateMenuCards();

        // Animate profile picture
        binding.imageView.setAlpha(0f);
        binding.imageView.setTranslationY(-50f);
        binding.imageView.animate()
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
            }else if (id == R.id.post) {intent = new Intent(this, UploadPostActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }
}