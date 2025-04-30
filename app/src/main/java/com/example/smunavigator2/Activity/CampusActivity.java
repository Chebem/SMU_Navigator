package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.CategoryAdapter;
import com.example.smunavigator2.Adapter.ConvenienceAdapter;
import com.example.smunavigator2.Adapter.OthersAdapter;
import com.example.smunavigator2.Adapter.RecommendedAdapter;
import com.example.smunavigator2.Domain.Category;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.Domain.Location;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ActivityCampusBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Locale;

public class CampusActivity extends BaseActivity {

    private ActivityCampusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Initialize ViewBinding
        binding = ActivityCampusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Setup bottom navigation
        setupBottomNav(R.id.explore);

        // 🔽 Init Spinner with Firebase data
        initLocation();

        //Banner Function
        //initBanner();;

        initCategory();

        //Recommended Function
        initRecommended();
        initDormitory();
        initConvenience();
        initOthers();
    }
    private void initRecommended() {
        String node = getCurrentLanguage(); // "popular_en" or "popular_ko"
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        Log.d("FirebaseLangNode", "Fetching data from: " + node);

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
                        binding.recyclerViewFaculty.setLayoutManager(new LinearLayoutManager(CampusActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter<RecommendedAdapter.Viewholder> adapter = new RecommendedAdapter(list);
                        binding.recyclerViewFaculty.setAdapter(adapter);
                    }
                }
                binding.progressBarFaculty.setVisibility(View.GONE); // ✅ Corrected to progressBarFaculty
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarFaculty.setVisibility(View.GONE);
            }
        });
    }

    private void initDormitory() {
        String node = getCurrentLanguage(); // "popular_en" or "popular_ko"
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        Log.d("FirebaseLangNode", "Fetching data from: " + node);

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
                        binding.recyclerViewDorm.setLayoutManager(new LinearLayoutManager(CampusActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter<RecommendedAdapter.Viewholder> adapter = new RecommendedAdapter(list);
                        binding.recyclerViewDorm .setAdapter(adapter);
                    }
                }
                binding.progressBarDorm.setVisibility(View.GONE); // ✅ Corrected to progressBarFaculty
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarDorm.setVisibility(View.GONE);
            }
        });
    }

    private void initConvenience() {
        String node = getCurrentLanguage(); // "popular_en" or "popular_ko"
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        Log.d("FirebaseLangNode", "Fetching data from: " + node);

        binding.progressBarConvenience.setVisibility(View.VISIBLE);

        ArrayList<ItemDomain> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(ItemDomain.class));
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewConvenience.setLayoutManager(new LinearLayoutManager(CampusActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter<ConvenienceAdapter.Viewholder> adapter = new ConvenienceAdapter(list);
                        binding.recyclerViewConvenience .setAdapter(adapter);
                    }
                }
                binding.progressBarConvenience.setVisibility(View.GONE); // ✅ Corrected to progressBarFaculty
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarConvenience.setVisibility(View.GONE);
            }
        });
    }


    private void initOthers() {
        String node = getCurrentLanguage(); // "popular_en" or "popular_ko"
        DatabaseReference myRef = database.getReference(node);
        Log.d("FirebaseLangNode", "Node used: " + node);
        Log.d("FirebaseLangNode", "Fetching data from: " + node);

        binding.progressBarViewOther.setVisibility(View.VISIBLE);

        ArrayList<ItemDomain> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot issue : snapshot.getChildren()) {
                        list.add(issue.getValue(ItemDomain.class));
                    }
                    if (!list.isEmpty()) {
                        binding.recyclerViewOthers.setLayoutManager(new LinearLayoutManager(CampusActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter<OthersAdapter.Viewholder> adapter = new OthersAdapter(list);
                        binding.recyclerViewOthers.setAdapter(adapter);
                    }
                }
                binding.progressBarViewOther.setVisibility(View.GONE); //
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarViewOther.setVisibility(View.GONE);
            }
        });
    }


    private String getCurrentLanguage() {
        String lang = Locale.getDefault().getLanguage(); // "en", "ko", etc.
        return lang.equals("ko") ? "PopularKo" : "PopularEn";
    }


    private void initCategory()
    {
        DatabaseReference myRef= database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category>  list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue:snapshot.getChildren())
                    {
                        list.add(issue.getValue(Category.class));
                    }
                    if(list.size()>0)
                    {
                        binding.recyclerViewCategory.setLayoutManager(new LinearLayoutManager(CampusActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter adapter= new CategoryAdapter(list);
                        binding.recyclerViewCategory.setAdapter(adapter);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
                

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //location dropdown function
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

                    Log.d("FirebaseLocation", "Total locations: " + list.size());

                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(
                            CampusActivity.this,
                            R.layout.sp_item,
                            list
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.locationSp.setAdapter(adapter);

                    // ✅ Spinner item selection logic
                    binding.locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Location selected = (Location) parent.getItemAtPosition(position);
                            filterCampusSections(selected.getLoc());
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

    // 🎯 This method filters section views based on selected Spinner item
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
                // Show all
                binding.recyclerViewFaculty.setVisibility(View.VISIBLE);
                binding.recyclerViewDorm.setVisibility(View.VISIBLE);
                binding.recyclerViewConvenience.setVisibility(View.VISIBLE);
                binding.recyclerViewOthers.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Helper method to show one RecyclerView and hide the rest
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


    //bottom Navigation function
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

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }


   /* private void banners(ArrayList<SliderItems> items )
    {
        binding.viewPagerSilder.setAdapter(new SliderAdapter(items,binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setoffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer= new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);

    }


    private void initBanner()
    {
        DatabaseReference myRef = database.getReference("Banner");
        binding.progresBarBanner.setVisibility(View.VISIBLE);
        ArrayList<SliderItems> items = new ArrayList<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    for (DataSnapshot issue : snapshot.getChildren())
                    {
                        items.add(issue.getValue(SliderItems.class));

                    }
                banners(items);
                binding.progressBarBanner.setVisibility(View.GONE);
            }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }

        });
    }*/
}
