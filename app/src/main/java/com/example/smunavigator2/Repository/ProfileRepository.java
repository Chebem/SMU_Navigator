package com.example.smunavigator2.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.Domain.ProfileModel.Follower;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.Utils.PostParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileRepository {
    private final DatabaseReference profileRef;
    private final MutableLiveData<ProfileModel> profileLiveData;

    private final String myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public ProfileRepository() {
        this(null);
    }

    /** Loads {@code userId}'s profile; null means the signed-in user. */
    public ProfileRepository(String userId) {
        profileRef = FirebaseDatabase.getInstance("https://smu-navigator-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("profiles")
                .child(userId != null ? userId : myUid);
        profileLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<ProfileModel> getProfileLiveData() {
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE_DEBUG", "Data snapshot: " + snapshot);

                // Read fields one by one: some old profiles store numbers as text, which crashes getValue(ProfileModel.class)
                ProfileModel profile = new ProfileModel();
                profile.profileName = snapshot.child("profileName").getValue(String.class);
                profile.profileImage = snapshot.child("profileImage").getValue(String.class);
                profile.department = snapshot.child("department").getValue(String.class);
                profile.about = snapshot.child("about").getValue(String.class);

                // Parse posts as Map<String, Post>
                Map<String, Post> postMap = new HashMap<>();
                for (DataSnapshot postSnap : snapshot.child("posts").getChildren()) {
                    Post post = PostParser.parse(postSnap);
                    if (post != null) {
                        post.setPostId(postSnap.getKey());
                        if (post.getUserId() == null) post.setUserId(snapshot.getKey());
                        postMap.put(postSnap.getKey(), post);
                    }
                }
                profile.posts = postMap;

                // Parse followers as List
                ArrayList<Follower> followerList = new ArrayList<>();
                if (snapshot.child("followers").exists()) {
                    for (DataSnapshot followerSnap : snapshot.child("followers").getChildren()) {
                        Follower follower = followerSnap.getValue(Follower.class);
                        if (follower != null) {
                            followerList.add(follower);
                        }
                    }
                }
                profile.followers = followerList;

                // Counts come from the lists themselves, so they can't drift
                profile.followersNum = (int) snapshot.child("followers").getChildrenCount();
                profile.followingNum = (int) snapshot.child("following").getChildrenCount();
                profile.followedByMe = snapshot.child("followers").hasChild(myUid);

                profileLiveData.setValue(profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_ERROR", "Failed to load profile: " + error.getMessage());
            }
        });

        return profileLiveData;
    }
}