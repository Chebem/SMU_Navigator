package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.NoticeAdapter;
import com.example.smunavigator2.Domain.NoticeModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.FilterButtons;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private RecyclerView noticeRecyclerView;
    private NoticeAdapter adapter;
    private final List<NoticeModel> allNotices = new ArrayList<>();
    private final List<NoticeModel> noticeList = new ArrayList<>(); // filtered, shown in the list
    private boolean isEnglish = true; // Toggle flag

    // Category keys written by the scraper (scraper/scrape_notices.py BOARDS), with EN / KO chip labels
    private static final String[][] CATEGORIES = {
            {"all", "All", "전체"},
            {"general", "General", "일반공지"},
            {"academic", "Academic", "장학·학사"},
            {"events", "Events", "행사안내"},
            {"jobs", "Jobs", "채용공고"},
    };
    private String selectedCategory = "all";
    private LinearLayout categoryFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        setupBottomNav(R.id.explore);

        noticeRecyclerView = findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoticeAdapter(this, noticeList, isEnglish);
        noticeRecyclerView.setAdapter(adapter);

        Button langToggleBtn = findViewById(R.id.langToggleBtn);
        langToggleBtn.setOnClickListener(v -> {
            isEnglish = !isEnglish;
            langToggleBtn.setText(isEnglish ? "EN" : "KR");
            adapter = new NoticeAdapter(this, noticeList, isEnglish);
            noticeRecyclerView.setAdapter(adapter);
            updateFilterLabels();
        });

        setupCategoryFilters();
        fetchNoticesFromFirebase(); // 🔥 live data from Firebase
    }

    private void fetchNoticesFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notices");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allNotices.clear();

                for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                    NoticeModel notice = noticeSnap.getValue(NoticeModel.class);
                    if (notice != null) {
                        allNotices.add(notice);
                    }
                }

                // Newest first: by date (yyyy-MM-dd), then by board number for same-day notices
                allNotices.sort(Comparator
                        .comparing((NoticeModel n) -> n.date != null ? n.date : "")
                        .thenComparing(n -> n.id != null ? n.id.length() : 0)
                        .thenComparing(n -> n.id != null ? n.id : "")
                        .reversed());

                applyCategoryFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExploreActivity.this, "Failed to load notices", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryFilters() {
        categoryFilters = findViewById(R.id.categoryFilters);
        for (String[] category : CATEGORIES) {
            FilterButtons.add(categoryFilters, category[0], category[0].equals(selectedCategory))
                    .setOnClickListener(v -> selectCategory((String) v.getTag()));
        }
        updateFilterLabels();
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        FilterButtons.select(categoryFilters, category);
        applyCategoryFilter();
    }

    private void updateFilterLabels() {
        for (int i = 0; i < categoryFilters.getChildCount(); i++) {
            ((MaterialButton) categoryFilters.getChildAt(i)).setText(isEnglish ? CATEGORIES[i][1] : CATEGORIES[i][2]);
        }
    }

    private void applyCategoryFilter() {
        noticeList.clear();
        for (NoticeModel notice : allNotices) {
            if (matchesCategory(notice, selectedCategory)) noticeList.add(notice);
        }
        adapter.notifyDataSetChanged();
    }

    private static boolean matchesCategory(NoticeModel notice, String category) {
        if (category.equals("all")) return true;
        if (notice.categories != null && Boolean.TRUE.equals(notice.categories.get(category))) return true;
        if (category.equals(notice.category)) return true;
        // Notices saved before categories existed all came from the general board
        return category.equals("general") && notice.category == null && notice.categories == null;
    }

    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = findViewById(R.id.navigationBar);
        bottomNav.setItemSelected(selectedItemId, true);

        bottomNav.setOnItemSelectedListener(id -> {
            if (id == selectedItemId) return;

            Intent intent = null;
            if (id == R.id.home) {
                intent = new Intent(this, MainActivity.class);
            } else if (id == R.id.explore) {
                intent = new Intent(this, ExploreActivity.class);
            } else if (id == R.id.favorite) {
                intent = new Intent(this, FavoritesActivity.class);
            } else if (id == R.id.profile) {
                intent = new Intent(this, ProfilePageActivity.class);
            }
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
