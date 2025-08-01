package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Adapter.FollowersAdapter;
import com.example.smunavigator2.Adapter.PostsAdapter;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.ViewModel.ProfileViewModel;
import com.example.smunavigator2.databinding.ActivityProfilePageBinding;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfilePageActivity extends AppCompatActivity implements PostsAdapter.OnPostClickListener {

    private ActivityProfilePageBinding binding;
    private ChipNavigationBar bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ App Check with Play Integrity
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );

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

                // Fallback values if null or empty
                binding.nameText.setText(profileModel.profileName != null && !profileModel.profileName.isEmpty()
                        ? profileModel.profileName : "Michael Test");

                binding.departmentText.setText(profileModel.department != null && !profileModel.department.isEmpty()
                        ? profileModel.department : "Department of Computer Science");

                binding.aboutText.setText(profileModel.about != null && !profileModel.about.isEmpty()
                        ? profileModel.about : "This is test about.");

                binding.followersTxt.setText(String.valueOf(profileModel.followersNum));
                binding.followingTxt.setText(String.valueOf(profileModel.followingNum));
                binding.likesTxt.setText(String.valueOf(profileModel.likes));

                Glide.with(ProfilePageActivity.this)
                        .load(profileModel.profileImage)
                        .into(binding.profileImg);

                // Followers
                binding.followersList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                binding.followersList.setAdapter(new FollowersAdapter(
                        profileModel.followers != null ? profileModel.followers : new ArrayList<>()
                ));

                binding.postList.setLayoutManager(new LinearLayoutManager(this));
                // Convert Map<String, Post> to List<Post>
                // NEW ✅
                List<Post> postList = new ArrayList<>();
                if (profileModel.posts != null) {
                    for (Map.Entry<String, ProfileModel.Post> entry : profileModel.posts.entrySet()) {
                        ProfileModel.Post oldPost = entry.getValue();
                        Post newPost = new Post(
                                oldPost.getImageUrls(),
                                oldPost.getCaption(),
                                oldPost.getUserId(),
                                oldPost.getTimestamp()
                        );
                        postList.add(newPost);
                    }
                }

                PostsAdapter adapter = new PostsAdapter(postList, this);
                binding.postList.setAdapter(adapter);
                adapter.notifyDataSetChanged(); // 🔄 force UI refresh
            }
        });

        binding.settingsIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ProfilePageActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_profile_dropdown, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit_profile) {
                    startActivity(new Intent(this, ProfileSetupActivity.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        setupBottomNav(R.id.profile);
    }


    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = binding.navigationBar;
        bottomNav.setItemSelected(selectedItemId, true);

        bottomNav.setOnItemSelectedListener(id -> {
            if (id == selectedItemId) return;

            Intent intent = null;
            if (id == R.id.home) intent = new Intent(this, MainActivity.class);
            else if (id == R.id.explore) intent = new Intent(this, CampusActivity.class);
            else if (id == R.id.favorite) intent = new Intent(this, FavoritesActivity.class);
            else if (id == R.id.profile) intent = new Intent(this, ProfilePageActivity.class);
            else if (id == R.id.post) {intent = new Intent(this, UploadPostActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }



    @Override
    public void onPostClick(Post post) {
        Toast.makeText(this, "Clicked post by userId: " + post.getUserId(), Toast.LENGTH_SHORT).show();
    }
}