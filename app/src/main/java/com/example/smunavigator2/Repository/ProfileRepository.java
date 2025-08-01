package com.example.smunavigator2.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.Domain.ProfileModel.Follower;
import com.example.smunavigator2.Domain.ProfileModel.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {
    private final DatabaseReference profileRef;
    private final MutableLiveData<ProfileModel> profileLiveData;

    public ProfileRepository() {
        profileRef = FirebaseDatabase.getInstance("https://smu-navigator-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("profiles")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        profileLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<ProfileModel> getProfileLiveData() {
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE_DEBUG", "Data snapshot: " + snapshot);

                ProfileModel profile = snapshot.getValue(ProfileModel.class);
                if (profile == null) profile = new ProfileModel();

                // ✅ Parse posts as Map<String, Post>
                Map<String, Post> postMap = new HashMap<>();
                for (DataSnapshot postSnap : snapshot.child("posts").getChildren()) {
                    Post post = postSnap.getValue(Post.class);
                    if (post != null) {
                        postMap.put(postSnap.getKey(), post);
                    }
                }
                profile.posts = postMap;

                // ✅ Parse followers as List
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