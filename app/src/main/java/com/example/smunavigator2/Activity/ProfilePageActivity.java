package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Adapter.FollowersAdapter;
import com.example.smunavigator2.Adapter.PostsAdapter;
import com.example.smunavigator2.R;
import com.example.smunavigator2.ViewModel.ProfileViewModel;
import com.example.smunavigator2.databinding.ActivityProfilePageBinding;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class ProfilePageActivity extends AppCompatActivity {

    private ActivityProfilePageBinding binding;

    private ChipNavigationBar bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.progressBar.setVisibility(View.GONE);
        binding.nestedScrollView.setVisibility(View.VISIBLE);


        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getProfileModelLiveData().observe(this, profileModel -> {
            if (profileModel != null) {
                Log.d("ProfileDebug", "Data loaded: " + profileModel);
                Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show();
                binding.nestedScrollView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);

                binding.nameText.setText("Michael Test");
                binding.departmentText.setText("Dept Test");
                binding.aboutText.setText("This is test about.");

                binding.nameText.setText(profileModel.profileName);
                binding.departmentText.setText(profileModel.department != null ? profileModel.department : "No department");
                binding.aboutText.setText(profileModel.about != null ? profileModel.about : "");
                binding.followersTxt.setText(profileModel.followersNum);
                binding.followingTxt.setText(profileModel.followingNum);
                binding.likesTxt.setText(profileModel.likes);

                Glide.with(ProfilePageActivity.this)
                        .load(profileModel.profileImage)
                        .into(binding.profileImg);

                LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                binding.followersList.setLayoutManager(horizontalLayoutManager);

                binding.followersList.setAdapter(new FollowersAdapter(profileModel.followers));


                binding.postList.setLayoutManager(new GridLayoutManager(this, 3));
                binding.postList.setAdapter(new PostsAdapter(profileModel.posts));
            }
        });

        // Setup Bottom Navigation
        setupBottomNav(R.id.profile);


    }

    private void setupBottomNav(int selectedItemId) {
        bottomNav = binding.navigationBar; // using view binding
        bottomNav.setItemSelected(selectedItemId, true);

        bottomNav.setOnItemSelectedListener(id -> {
            if (id == selectedItemId) return; // Already on this activity

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
