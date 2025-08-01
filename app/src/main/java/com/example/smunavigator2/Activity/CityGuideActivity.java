package com.example.smunavigator2.Activity;

import static com.example.smunavigator2.Activity.BaseActivity.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.PopularStoreAdapter;
import com.example.smunavigator2.Adapter.SubCategoryAdapter;
import com.example.smunavigator2.Adapter.NearestStoreAdapter;
import com.example.smunavigator2.Domain.CategoryModel;
import com.example.smunavigator2.Domain.CityLocation;
import com.example.smunavigator2.Domain.Location;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.ViewModel.ResultViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class CityGuideActivity extends AppCompatActivity {

    private RecyclerView subCategoryRecyclerView, popularRecyclerView, nearestRecyclerView;
    private EditText searchInput;
    private ResultViewModel categoryViewModel;

    //private SubCategoryAdapter subCategoryAdapter;
    private PopularStoreAdapter popularAdapter;
    private NearestStoreAdapter nearestAdapter;

    private Spinner categorySpinner;
    private TextView nearestEmptyMessage;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String LANGUAGE_KEY = "selectedLanguage";

    private FloatingActionButton languageFab;
    private boolean isEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_guide);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        languageFab = findViewById(R.id.languageFab);
        categorySpinner = findViewById(R.id.locationSp); // ✅ spinner initialized
        isEnglish = getLanguagePreference();

        categoryViewModel = new ViewModelProvider(this).get(ResultViewModel.class);
        updateLanguageUI();
        initCityLocation(); // ✅ correct method call

        languageFab.setOnClickListener(v -> {
            isEnglish = !isEnglish;
            saveLanguagePreference(isEnglish);
            updateLanguageUI();
            Toast.makeText(this, isEnglish ? "Switched to English" : "한국어로 변경됨", Toast.LENGTH_SHORT).show();
        });

        setupBottomNav(R.id.explore);

        subCategoryRecyclerView = findViewById(R.id.recyclerViewCategory);
        subCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        searchInput = findViewById(R.id.searchInput);

        /* searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (subCategoryAdapter != null) {
                    subCategoryAdapter.filter(s.toString());
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        }); */

        popularRecyclerView = findViewById(R.id.popularRecyclerView);
        nearestRecyclerView = findViewById(R.id.nearestRecyclerView);

        popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nearestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        popularAdapter = new PopularStoreAdapter(new ArrayList<>());
        nearestAdapter = new NearestStoreAdapter(new ArrayList<>());
        popularRecyclerView.setAdapter(popularAdapter);
        nearestRecyclerView.setAdapter(nearestAdapter);

        nearestEmptyMessage = findViewById(R.id.nearestEmptyTextView);
    }

    private void updateLanguageUI() {
        languageFab.setImageResource(isEnglish ? R.drawable.ic_english : R.drawable.ic_korean);
        loadPlaces(isEnglish ? "placesEn" : "placesKo", "All City Sections");
    }

    private void initCityLocation() { // ✅ fixed signature
        DatabaseReference myRef = database.getReference("CityLocation");
        ArrayList<CityLocation> categoryList = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        CityLocation location = issue.getValue(CityLocation.class);
                        if (location != null) {
                            categoryList.add(location);
                        }
                    }

                    ArrayAdapter<CityLocation> adapter = new ArrayAdapter<>(CityGuideActivity.this, R.layout.sp_item, categoryList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);

                    categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            CityLocation selected = categoryList.get(position);
                            filterByCategory(selected.getLoc());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CityGuideActivity.this, "Failed to load city categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterCitySections(String loc) {
        switch (loc) {
            case "Coffee":
            case "Convenience":
            case "Accomodation":
            case "Mart":
            case "Restaurant":
                loadPlaces(isEnglish ? "placesEn" : "placesKo", loc);
                break;
            case "All City Sections":
            default:
                loadPlaces(isEnglish ? "placesEn" : "placesKo", "All City Sections");
                break;
        }
    }

    private void filterByCategory(String selectedCategory) {
        String nodeName = isEnglish ? "placesEn" : "placesKo";
        categoryViewModel.getPlaces(nodeName).observe(this, stores -> {
            List<StoreModel> popular = new ArrayList<>();
            List<StoreModel> nearest = new ArrayList<>();

            for (StoreModel store : stores) {
                if (selectedCategory.equals("All City Sections") || store.getCategory().equalsIgnoreCase(selectedCategory)) {
                    popular.add(store);
                    if (distanceFromSMU(store.getLatitude(), store.getLongitude()) <= 2.0) {
                        nearest.add(store);
                    }
                }
            }

            popularAdapter = new PopularStoreAdapter(popular);
            nearestAdapter = new NearestStoreAdapter(nearest);
            popularRecyclerView.setAdapter(popularAdapter);
            nearestRecyclerView.setAdapter(nearestAdapter);
        });
    }

    private void loadPlaces(String nodeName, String selectedCategory) {
        categoryViewModel.getPlaces(nodeName).observe(this, stores -> {
            List<StoreModel> popular = new ArrayList<>();
            List<StoreModel> nearest = new ArrayList<>();

            for (StoreModel store : stores) {
                if (selectedCategory.equals("All City Sections") || store.getCategory().equalsIgnoreCase(selectedCategory)) {
                    popular.add(store);
                    if (distanceFromSMU(store.getLatitude(), store.getLongitude()) <= 2.0) {
                        nearest.add(store);
                    }
                }
            }

            popularAdapter = new PopularStoreAdapter(popular);
            nearestAdapter = new NearestStoreAdapter(nearest);
            popularRecyclerView.setAdapter(popularAdapter);
            nearestRecyclerView.setAdapter(nearestAdapter);

            if (nearest.isEmpty()) {
                nearestEmptyMessage.setVisibility(View.VISIBLE);
            } else {
                nearestEmptyMessage.setVisibility(View.GONE);
            }

        });
    }

    /*private void initSubCategoriesFromFirebase() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Category");
        ArrayList<CategoryModel> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        CategoryModel category = issue.getValue(CategoryModel.class);
                        if (category != null) {
                            list.add(category);
                        }
                    }

                    subCategoryAdapter = new SubCategoryAdapter();
                    subCategoryAdapter.setLanguage(isEnglish ? "en" : "ko");
                    subCategoryAdapter.setData(list);
                    subCategoryRecyclerView.setAdapter(subCategoryAdapter);

                    subCategoryAdapter.setOnItemClickListener(item ->
                            Toast.makeText(CityGuideActivity.this, "Clicked: " +
                                    (isEnglish ? item.getNameEn() : item.getNameKo()), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CityGuideActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private double distanceFromSMU(double lat, double lng) {
        double smuLat = 37.1662;
        double smuLng = 128.1696;
        double earthRadius = 6371;

        double dLat = Math.toRadians(lat - smuLat);
        double dLng = Math.toRadians(lng - smuLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(smuLat)) * Math.cos(Math.toRadians(lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return earthRadius * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = findViewById(R.id.navigationBar);
        bottomNav.setItemSelected(selectedItemId, true);
        bottomNav.setOnItemSelectedListener(id -> {
            if (id == selectedItemId) return;
            Intent intent = null;
            if (id == R.id.home) intent = new Intent(this, MainActivity.class);
            else if (id == R.id.explore) intent = new Intent(this, ExploreActivity.class);
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

    private void saveLanguagePreference(boolean isEnglish) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(LANGUAGE_KEY, isEnglish).apply();
    }

    private boolean getLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(LANGUAGE_KEY, false);
    }
}