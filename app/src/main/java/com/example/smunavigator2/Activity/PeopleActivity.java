package com.example.smunavigator2.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smunavigator2.Adapter.PeopleAdapter;
import com.example.smunavigator2.Adapter.PeopleAdapter.Person;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.FollowUtils;
import com.example.smunavigator2.Utils.ModerationUtils;
import com.example.smunavigator2.databinding.ActivityPeopleBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Find people by name or department, or list a profile's followers / following. */
public class PeopleActivity extends AppCompatActivity {

    public static final String MODE_SEARCH = "search";
    public static final String MODE_FOLLOWERS = "followers";
    public static final String MODE_FOLLOWING = "following";

    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_USER_ID = "userId";

    public static Intent intent(Context context, String mode, String userId) {
        return new Intent(context, PeopleActivity.class)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_USER_ID, userId);
    }

    private ActivityPeopleBinding binding;
    private PeopleAdapter adapter;
    private String mode;
    private String myUid;
    private final List<Person> allPeople = new ArrayList<>();
    private final Set<String> myFollowing = new HashSet<>();

    private DatabaseReference myFollowingRef;
    private ValueEventListener myFollowingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPeopleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        myUid = user.getUid();
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_SEARCH;
        String userId = getIntent().getStringExtra(EXTRA_USER_ID);
        if (userId == null) userId = myUid;

        binding.backBtn.setOnClickListener(v -> finish());
        binding.peopleTitle.setText(MODE_FOLLOWERS.equals(mode) ? R.string.followers
                : MODE_FOLLOWING.equals(mode) ? R.string.followings : R.string.find_people);

        adapter = new PeopleAdapter(myUid, this::toggleFollow);
        binding.peopleRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.peopleRecycler.setAdapter(adapter);

        if (MODE_SEARCH.equals(mode)) {
            binding.searchInput.setVisibility(View.VISIBLE);
            binding.searchInput.requestFocus();
            binding.searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override public void afterTextChanged(Editable s) { showFiltered(); }
            });
        }

        ModerationUtils.watchBlocked();
        loadPeople(userId);
        watchMyFollowing();
    }

    // One read of all profiles; search filters it locally, the lists keep only followers / following
    private void loadPeople(String userId) {
        FirebaseDatabase.getInstance().getReference("profiles").get().addOnCompleteListener(task -> {
            if (isFinishing() || isDestroyed()) return;
            binding.peopleProgress.setVisibility(View.GONE);
            if (!task.isSuccessful()) {
                Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            DataSnapshot profiles = task.getResult();
            Set<String> keep = null;
            if (!MODE_SEARCH.equals(mode)) {
                keep = new HashSet<>();
                for (DataSnapshot child : profiles.child(userId).child(mode).getChildren()) keep.add(child.getKey());
            }

            allPeople.clear();
            for (DataSnapshot profile : profiles.getChildren()) {
                String uid = profile.getKey();
                if (uid == null || (keep != null && !keep.contains(uid))) continue;
                if (MODE_SEARCH.equals(mode) && uid.equals(myUid)) continue; // don't find yourself
                if (ModerationUtils.isBlocked(uid)) continue; // or people you blocked
                String name = profile.child("profileName").getValue(String.class);
                if (TextUtils.isEmpty(name)) continue; // accounts that never finished setup
                allPeople.add(new Person(uid, name,
                        profile.child("department").getValue(String.class),
                        profile.child("profileImage").getValue(String.class)));
            }
            allPeople.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
            showFiltered();
        });
    }

    private void showFiltered() {
        String query = binding.searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<Person> shown = new ArrayList<>();
        for (Person p : allPeople) {
            if (query.isEmpty()
                    || p.name.toLowerCase(Locale.ROOT).contains(query)
                    || (p.department != null && p.department.toLowerCase(Locale.ROOT).contains(query))) {
                shown.add(p);
            }
        }
        adapter.setPeople(shown);

        boolean empty = shown.isEmpty() && binding.peopleProgress.getVisibility() != View.VISIBLE;
        binding.emptyPeopleTxt.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.emptyPeopleTxt.setText(MODE_FOLLOWERS.equals(mode) ? R.string.no_followers_yet
                : MODE_FOLLOWING.equals(mode) ? R.string.not_following_anyone : R.string.no_people_found);
    }

    // Live, so buttons stay right after following from here or from a profile
    private void watchMyFollowing() {
        myFollowingRef = FirebaseDatabase.getInstance().getReference("profiles").child(myUid).child("following");
        myFollowingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myFollowing.clear();
                for (DataSnapshot child : snapshot.getChildren()) myFollowing.add(child.getKey());
                adapter.setFollowing(myFollowing);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myFollowingRef.addValueEventListener(myFollowingListener);
    }

    private void toggleFollow(Person person, boolean follow) {
        FollowUtils.setFollowing(myUid, person.uid, follow).addOnFailureListener(e ->
                Toast.makeText(this, R.string.follow_failed, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myFollowingRef != null) myFollowingRef.removeEventListener(myFollowingListener);
    }
}
