package com.example.smunavigator2.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smunavigator2.Domain.CategoryModel;
import com.example.smunavigator2.Domain.StoreModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResultsRepository {

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public LiveData<List<CategoryModel>> loadSubCategory() {
        MutableLiveData<List<CategoryModel>> listData = new MutableLiveData<>();

        firebaseDatabase.getReference("SubCategory")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<CategoryModel> lists = new ArrayList<>();
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            CategoryModel item = childSnapshot.getValue(CategoryModel.class);
                            if (item != null) {
                                lists.add(item);
                            }
                        }
                        listData.setValue(lists);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listData.setValue(new ArrayList<>());
                    }
                });

        return listData;
    }

    public LiveData<List<StoreModel>> loadPlaces(String nodeName) {
        MutableLiveData<List<StoreModel>> listData = new MutableLiveData<>();

        firebaseDatabase.getReference(nodeName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<StoreModel> places = new ArrayList<>();

                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            for (DataSnapshot storeSnapshot : categorySnapshot.getChildren()) {
                                try {
                                    Object rawOpeningHours = storeSnapshot.child("opening_hours").getValue();

                                    // Fix string to list if needed
                                    if (rawOpeningHours instanceof String) {
                                        List<String> fixedList = new ArrayList<>();
                                        fixedList.add((String) rawOpeningHours);
                                        storeSnapshot.getRef().child("opening_hours").setValue(fixedList);
                                    }

                                    StoreModel store = storeSnapshot.getValue(StoreModel.class);
                                    if (store != null) {
                                        places.add(store);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace(); // log and skip bad item
                                }
                            }
                        }

                        listData.setValue(places);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listData.setValue(new ArrayList<>());
                    }
                });

        return listData;
    }
}