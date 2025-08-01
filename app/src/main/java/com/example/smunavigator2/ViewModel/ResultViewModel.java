package com.example.smunavigator2.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.smunavigator2.Domain.CategoryModel;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.Repository.ResultsRepository;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultViewModel extends ViewModel {

    private final ResultsRepository repository = new ResultsRepository();

    // SubCategory data
    private final MutableLiveData<List<CategoryModel>> subCategory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(true);

    // Store data (for popular and nearest)
    private final MutableLiveData<List<StoreModel>> stores = new MutableLiveData<>();

    // Public LiveData getters
    public LiveData<List<StoreModel>> getPlaces(String nodeName) {
        return repository.loadPlaces(nodeName);
    }

    public LiveData<List<CategoryModel>> getSubCategory() {
        return subCategory;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<StoreModel>> getStores() {
        return stores;
    }

    // Load subcategories
    public void loadSubCategory() {
        loading.setValue(true);
        LiveData<List<CategoryModel>> repoLiveData = repository.loadSubCategory();

        repoLiveData.observeForever(new Observer<List<CategoryModel>>() {
            @Override
            public void onChanged(List<CategoryModel> value) {
                subCategory.setValue(value != null ? value : Collections.emptyList());
                loading.setValue(false);
                repoLiveData.removeObserver(this);
            }
        });
    }

    // Load all store data safely (with fix for opening_hours)
    public void loadAllStores() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("placesEn");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<StoreModel> storeList = new ArrayList<>();

                for (DataSnapshot category : snapshot.getChildren()) {
                    for (DataSnapshot store : category.getChildren()) {
                        try {
                            Object rawOpeningHours = store.child("opening_hours").getValue();

                            if (rawOpeningHours instanceof String) {
                                // Convert to list and update Firebase in-place
                                List<String> fixedList = new ArrayList<>();
                                fixedList.add((String) rawOpeningHours);
                                store.getRef().child("opening_hours").setValue(fixedList);
                            }

                            // Deserialize after fixing
                            StoreModel model = store.getValue(StoreModel.class);
                            if (model != null) {
                                storeList.add(model);
                            }

                        } catch (Exception e) {
                            e.printStackTrace(); // prevent crash and log the issue
                        }
                    }
                }

                stores.setValue(storeList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                stores.setValue(new ArrayList<>());
            }
        });
    }
}