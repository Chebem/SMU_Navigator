package com.example.smunavigator2.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.smunavigator2.Domain.ProfileModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileRepository {

    private DatabaseReference profileRef;
    private MutableLiveData<ProfileModel> profileLiveData;

    public ProfileRepository() {
        profileRef = FirebaseDatabase.getInstance("https://smu-navigator-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("profiles/profile1");
        profileLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<ProfileModel> getProfileLiveData() {
        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE_DEBUG", "Data snapshot: " + snapshot);
                ProfileModel profile = snapshot.getValue(ProfileModel.class);
                profileLiveData.setValue(profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return profileLiveData;
    }
}
