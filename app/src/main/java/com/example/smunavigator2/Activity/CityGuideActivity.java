package com.example.smunavigator2.Activity;

import static com.example.smunavigator2.Activity.BaseActivity.database;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.smunavigator2.Utils.DistanceUtils;
import com.example.smunavigator2.Utils.FilterButtons;
import com.example.smunavigator2.ViewModel.ResultViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CityGuideActivity extends AppCompatActivity {

    private RecyclerView popularRecyclerView;
    private RecyclerView nearestRecyclerView;
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

    // Nearest list: measured from the student when possible, otherwise from campus
    private final List<StoreModel> allStores = new ArrayList<>();
    private String selectedCategory = "All City Sections";
    private double originLat = DistanceUtils.CAMPUS_LAT;
    private double originLng = DistanceUtils.CAMPUS_LNG;
    private boolean originIsUser = false;

    private static final int RADIUS_ALL = 0;
    private static final int[] RADII_METERS = {1000, 2000, RADIUS_ALL};
    private int radiusMeters = 2000;
    private LinearLayout distanceFilters;

    private final ActivityResultLauncher<String[]> locationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> updateOrigin());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_guide);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        languageFab = findViewById(R.id.languageFab);
        categorySpinner = findViewById(R.id.locationSp); // spinner initialized
        isEnglish = getLanguagePreference();

        categoryViewModel = new ViewModelProvider(this).get(ResultViewModel.class);
        updateLanguageUI();
        initCityLocation(); //  call method

        languageFab.setOnClickListener(v -> {
            isEnglish = !isEnglish;
            saveLanguagePreference(isEnglish);
            updateLanguageUI();
            Toast.makeText(this, isEnglish ? "Switched to English" : "한국어로 변경됨", Toast.LENGTH_SHORT).show();
        });

        setupBottomNav(R.id.explore);

        RecyclerView subCategoryRecyclerView = findViewById(R.id.recyclerViewCategory);
        subCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        EditText searchInput = findViewById(R.id.searchInput);

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

        setupDistanceFilters();
        locationPermission.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void updateOrigin() {
        DistanceUtils.resolveOrigin(this, (lat, lng, fromUser) -> {
            originLat = lat;
            originLng = lng;
            originIsUser = fromUser;
            renderPlaces();
        });
    }

    private void setupDistanceFilters() {
        distanceFilters = findViewById(R.id.distanceFilters);
        for (int radius : RADII_METERS) {
            FilterButtons.add(distanceFilters, radius, radius == radiusMeters).setOnClickListener(v -> {
                radiusMeters = (int) v.getTag();
                FilterButtons.select(distanceFilters, radiusMeters);
                renderPlaces();
            });
        }
        updateDistanceFilterLabels();
    }

    private void updateDistanceFilterLabels() {
        if (distanceFilters == null) return;
        for (int i = 0; i < distanceFilters.getChildCount(); i++) {
            int radius = RADII_METERS[i];
            ((android.widget.Button) distanceFilters.getChildAt(i)).setText(radius == RADIUS_ALL
                    ? (isEnglish ? "All" : "전체")
                    : DistanceUtils.format(radius));
        }
    }

    private void updateLanguageUI() {
        languageFab.setImageResource(isEnglish ? R.drawable.ic_english : R.drawable.ic_korean);
        updateDistanceFilterLabels();
        loadPlaces(isEnglish ? "placesEn" : "placesKo", "All City Sections");
    }

    private void initCityLocation() { //  fixed signature
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

    private void filterByCategory(String category) {
        selectedCategory = category;
        renderPlaces();
    }

    private void loadPlaces(String nodeName, String category) {
        selectedCategory = category;
        categoryViewModel.getPlaces(nodeName).observe(this, stores -> {
            allStores.clear();
            allStores.addAll(stores);
            renderPlaces();
        });
    }

    private void renderPlaces() {
        List<StoreModel> popular = new ArrayList<>();
        List<StoreModel> nearest = new ArrayList<>();

        for (StoreModel store : allStores) {
            if (selectedCategory.equals("All City Sections") || store.getCategory().equalsIgnoreCase(selectedCategory)) {
                popular.add(store);
                if (radiusMeters == RADIUS_ALL || distanceTo(store) <= radiusMeters) {
                    nearest.add(store);
                }
            }
        }
        nearest.sort(Comparator.comparingDouble(this::distanceTo));

        popularAdapter = new PopularStoreAdapter(popular);
        nearestAdapter = new NearestStoreAdapter(nearest);
        nearestAdapter.setOrigin(originLat, originLng, originIsUser, isEnglish);
        popularRecyclerView.setAdapter(popularAdapter);
        nearestRecyclerView.setAdapter(nearestAdapter);

        nearestEmptyMessage.setVisibility(nearest.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private float distanceTo(StoreModel store) {
        return DistanceUtils.meters(originLat, originLng, store.getLatitude(), store.getLongitude());
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