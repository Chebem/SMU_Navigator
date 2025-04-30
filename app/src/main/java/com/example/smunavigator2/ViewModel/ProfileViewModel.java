package com.example.smunavigator2.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.Repository.ProfileRepository;

public class ProfileViewModel extends ViewModel {
    private ProfileRepository repository;
    private  LiveData<ProfileModel> profileModelLiveData;

    public ProfileViewModel() {
        repository = new ProfileRepository();
        profileModelLiveData= repository.getProfileLiveData();
        }

    public LiveData <ProfileModel> getProfileModelLiveData() {
        return profileModelLiveData;
    }
}
