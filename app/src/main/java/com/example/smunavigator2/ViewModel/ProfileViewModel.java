package com.example.smunavigator2.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.Repository.ProfileRepository;

public class ProfileViewModel extends ViewModel {
    private LiveData<ProfileModel> profileModelLiveData;

    /** Profile of {@code userId} (null = signed-in user); loaded once and kept across rotations. */
    public LiveData<ProfileModel> getProfileModelLiveData(String userId) {
        if (profileModelLiveData == null) {
            profileModelLiveData = new ProfileRepository(userId).getProfileLiveData();
        }
        return profileModelLiveData;
    }
}
