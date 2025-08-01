package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.ConvenienceAdapter;
import com.example.smunavigator2.Adapter.DormitoryAdapter;
import com.example.smunavigator2.Adapter.OthersAdapter;
import com.example.smunavigator2.Adapter.RecommendedAdapter;
import com.example.smunavigator2.Domain.ConvenienceFacility;
import com.example.smunavigator2.Domain.FacilityModel;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.Domain.Location;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ActivityCampusBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Locale;

public class CampusActivity extends BaseActivity {

    private ActivityCampusBinding binding;
    private Location selectedLocation;

    private FloatingActionButton languageFab;
    private boolean isEnglish;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String LANGUAGE_KEY = "selectedLanguage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCampusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        languageFab = findViewById(R.id.languageFab);
        isEnglish = getLanguagePreference();

        setupRecyclerViews();
        setupBottomNav(R.id.explore);
        initLocation();
        initRecommended();
        initDormitory();
        initConvenience();
        initOthers();

        languageFab.setOnClickListener(v -> {
            isEnglish = !isEnglish;
            saveLanguagePreference(isEnglish);
            updateLanguageUI();
            Toast.makeText(this, isEnglish ? "Switched to English" : "한국어로 변경됨", Toast.LENGTH_SHORT).show();
        });

    }

    private void setupRecyclerViews() {
        binding.recyclerViewFaculty.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFaculty.setAdapter(new RecommendedAdapter(new ArrayList<>()));

        binding.recyclerViewDorm.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewDorm.setAdapter(new DormitoryAdapter(new ArrayList<>()));

        binding.recyclerViewConvenience.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewConvenience.setAdapter(new ConvenienceAdapter(new ArrayList<>()));

        binding.recyclerViewOthers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewOthers.setAdapter(new OthersAdapter(new ArrayList<>()));
    }

    private void initRecommended() {
        String node = getLanguageBasedNode("Popular");
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        binding.progressBarFaculty.setVisibility(View.VISIBLE);

        ArrayList<ItemDomain> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(ItemDomain.class));
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewFaculty.setAdapter(new RecommendedAdapter(list));
                    }
                }
                binding.progressBarFaculty.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarFaculty.setVisibility(View.GONE);
            }
        });
    }

    private void initDormitory() {
        String node = getLanguageBasedNode("dormitories");
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        binding.progressBarDorm.setVisibility(View.VISIBLE);

        ArrayList<ItemDomain> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(ItemDomain.class));
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewDorm.setAdapter(new DormitoryAdapter(list));
                    }
                }
                binding.progressBarDorm.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarDorm.setVisibility(View.GONE);
            }
        });
    }

    private void initConvenience() {
        String node = getLanguageBasedNode("convenienceFacilities");
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        binding.progressBarConvenience.setVisibility(View.VISIBLE);

        ArrayList<ConvenienceFacility> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        ConvenienceFacility item = issue.getValue(ConvenienceFacility.class);
                        if (item != null) {
                            item.setId(issue.getKey());
                            list.add(item);
                        }
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewConvenience.setAdapter(new ConvenienceAdapter(list));
                    }
                }
                binding.progressBarConvenience.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarConvenience.setVisibility(View.GONE);
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }

    private void initOthers() {
        String node = "OtherFacilitiesEn"; // Default to English
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        binding.progressBarViewOther.setVisibility(View.VISIBLE);

        ArrayList<FacilityModel> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FirebaseDebug", "Snapshot exists: " + snapshot.exists());
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        FacilityModel model = issue.getValue(FacilityModel.class);
                        if (model != null) {
                            Log.d("FirebaseModel", "Loaded: " + model.getName());
                            list.add(model);
                        }
                    }
                    if (!list.isEmpty()) {
                        Log.d("initOthers", "List size before setting adapter: " + list.size());
                        binding.recyclerViewOthers.setAdapter(new OthersAdapter(list));
                    }
                }
                binding.progressBarViewOther.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarViewOther.setVisibility(View.GONE);
                Log.e("FirebaseError", "Error loading others: " + error.getMessage());
            }
        });
    }

    protected String getLanguageBasedNode(String base) {
        String lang = Locale.getDefault().getLanguage();
        return lang.equals("ko") ? base + "Ko" : base + "En";
    }

    private void initLocation() {
        DatabaseReference myRef = database.getReference("Location");
        ArrayList<Location> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        Location location = issue.getValue(Location.class);
                        if (location != null) {
                            list.add(location);
                        }
                    }

                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(CampusActivity.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.locationSp.setAdapter(adapter);

                    binding.locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedLocation = list.get(position);
                            filterCampusSections(selectedLocation.getLoc());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseLocation", "Database error: " + error.getMessage());
            }
        });
    }

    private void filterCampusSections(String loc) {
        switch (loc) {
            case "Faculties":
                showOnly(binding.recyclerViewFaculty);
                break;
            case "Dormitories":
                showOnly(binding.recyclerViewDorm);
                break;
            case "Convenience Facilities":
                showOnly(binding.recyclerViewConvenience);
                break;
            case "Other Facilities":
                showOnly(binding.recyclerViewOthers);
                break;
            case "All Campus Sections":
            default:
                binding.recyclerViewFaculty.setVisibility(View.VISIBLE);
                binding.recyclerViewDorm.setVisibility(View.VISIBLE);
                binding.recyclerViewConvenience.setVisibility(View.VISIBLE);
                binding.recyclerViewOthers.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showOnly(View visibleView) {
        View[] views = {
                binding.recyclerViewFaculty,
                binding.recyclerViewDorm,
                binding.recyclerViewConvenience,
                binding.recyclerViewOthers
        };

        for (View view : views) {
            view.setVisibility(view == visibleView ? View.VISIBLE : View.GONE);
        }
    }

    private void initOthersFromNode(String node) {
        DatabaseReference myRef = database.getReference(node);
        binding.progressBarViewOther.setVisibility(View.VISIBLE);

        ArrayList<FacilityModel> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        FacilityModel model = issue.getValue(FacilityModel.class);
                        if (model != null) {
                            list.add(model);
                        }
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewOthers.setAdapter(new OthersAdapter(list));
                    }
                }
                binding.progressBarViewOther.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarViewOther.setVisibility(View.GONE);
                Log.e("FirebaseError", "Error loading others: " + error.getMessage());
            }
        });
    }



    private void updateLanguageUI() {
        languageFab.setImageResource(isEnglish ? R.drawable.ic_english : R.drawable.ic_korean);

        // Re-initialize sections using updated language
        initRecommended();
        initDormitory();
        initConvenience();

        // For 'Other Facilities', language switching is not dynamic yet
        String node = isEnglish ? "OtherFacilitiesEn" : "OtherFacilitiesKo"; // Add Ko version in Firebase if needed
        initOthersFromNode(node);
    }



    private void saveLanguagePreference(boolean isEnglish) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(LANGUAGE_KEY, isEnglish).apply();
    }

    private boolean getLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(LANGUAGE_KEY, false);
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
}