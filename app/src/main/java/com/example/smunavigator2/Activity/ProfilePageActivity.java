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
import com.example.smunavigator2.Adapter.PostsAdapter;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.Utils.FollowUtils;
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

public class ProfilePageActivity extends AppCompatActivity implements PostsAdapter.OnPostClickListener {

    /** Intent extra: whose profile to show. Missing = the signed-in user. */
    public static final String EXTRA_USER_ID = "userId";

    private ActivityProfilePageBinding binding;
    private PostsAdapter postsAdapter;

    private String myUid;
    private String viewedUid;
    private boolean isOwnProfile;
    private boolean followedByMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityProfilePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.postList.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(new ArrayList<>(), this);
        binding.postList.setAdapter(postsAdapter);

        // Tap a count to see who follows this profile, or who it follows
        binding.followersColumn.setOnClickListener(v -> startActivity(PeopleActivity.intent(this, PeopleActivity.MODE_FOLLOWERS, viewedUid)));
        binding.followingColumn.setOnClickListener(v -> startActivity(PeopleActivity.intent(this, PeopleActivity.MODE_FOLLOWING, viewedUid)));

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String requestedUid = getIntent().getStringExtra(EXTRA_USER_ID);
        isOwnProfile = requestedUid == null || requestedUid.equals(myUid);
        viewedUid = isOwnProfile ? myUid : requestedUid;
        setupFollowButton();

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getProfileModelLiveData(viewedUid).observe(this, profileModel -> {
            binding.progressBar.setVisibility(View.GONE);
            if (profileModel == null) return;
            followedByMe = profileModel.followedByMe;
            updateFollowButtonText();

            Log.d("ProfileDebug", "Data loaded: " + profileModel);

            binding.nameText.setText(profileModel.profileName != null ? profileModel.profileName : "");
            binding.departmentText.setText(profileModel.department != null ? profileModel.department : "");
            binding.aboutText.setText(profileModel.about != null ? profileModel.about : "");

            binding.followersTxt.setText(String.valueOf(profileModel.followersNum));
            binding.followingTxt.setText(String.valueOf(profileModel.followingNum));

            Glide.with(this)
                    .load(profileModel.profileImage)
                    .into(binding.profileImg);

            List<Post> postList = profileModel.posts != null
                    ? new ArrayList<>(profileModel.posts.values()) : new ArrayList<>();
            postList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp())); // newest first
            binding.postsCountTxt.setText(String.valueOf(postList.size()));

            postsAdapter.updatePosts(postList);
        });

        binding.settingsIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.menu_profile_dropdown, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_edit_profile) {
                    startActivity(new Intent(this, ProfileSetupActivity.class).putExtra(ProfileSetupActivity.EXTRA_EDITING, true));
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

    // Own profile: "Edit profile". Someone else's: Follow / Following, and no settings menu.
    private void setupFollowButton() {
        if (isOwnProfile) {
            binding.followBtn.setText(R.string.edit_profile);
            binding.followBtn.setOnClickListener(v -> startActivity(new Intent(this, ProfileSetupActivity.class).putExtra(ProfileSetupActivity.EXTRA_EDITING, true)));
        } else {
            binding.settingsIcon.setVisibility(View.GONE);
            binding.followBtn.setOnClickListener(v -> toggleFollow());
            updateFollowButtonText();
        }
    }

    private void updateFollowButtonText() {
        if (!isOwnProfile) binding.followBtn.setText(followedByMe ? R.string.following_state : R.string.follow);
    }

    private void toggleFollow() {
        binding.followBtn.setEnabled(false);
        FollowUtils.setFollowing(myUid, viewedUid, !followedByMe).addOnCompleteListener(task -> {
            binding.followBtn.setEnabled(true);
            if (!task.isSuccessful()) {
                Toast.makeText(this, R.string.follow_failed, Toast.LENGTH_SHORT).show();
            }
            // On success the profile listener fires and updates the button and counts
        });
    }

    @Override
    public void onPostClick(Post post) {
        Toast.makeText(this, "Clicked post by userId: " + post.getUserId(), Toast.LENGTH_SHORT).show();
    }

    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = binding.navigationBar;
        bottomNav.setItemSelected(selectedItemId, true);

        bottomNav.setOnItemSelectedListener(id -> {
            // On someone else's profile, the Profile tab goes back to your own
            if (id == selectedItemId && isOwnProfile) return;

            Intent intent = null;
            if (id == R.id.home) intent = new Intent(this, MainActivity.class);
            else if (id == R.id.explore) intent = new Intent(this, ExploreActivity.class);
            else if (id == R.id.favorite) intent = new Intent(this, FavoritesActivity.class);
            else if (id == R.id.profile) intent = new Intent(this, ProfilePageActivity.class);
            else if (id == R.id.post) intent = new Intent(this, UploadPostActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }
}