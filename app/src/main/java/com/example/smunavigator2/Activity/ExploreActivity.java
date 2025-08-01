package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.NoticeAdapter;
import com.example.smunavigator2.Domain.NoticeModel;
import com.example.smunavigator2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    private RecyclerView noticeRecyclerView;
    private NoticeAdapter adapter;
    private List<NoticeModel> noticeList = new ArrayList<>();
    private boolean isEnglish = true; // Toggle flag

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
        });

        fetchNoticesFromFirebase(); // 🔥 live data from Firebase
    }

    private void fetchNoticesFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notices");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noticeList.clear();

                for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                    NoticeModel notice = noticeSnap.getValue(NoticeModel.class);
                    if (notice != null) {
                        noticeList.add(notice);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExploreActivity.this, "Failed to load notices", Toast.LENGTH_SHORT).show();
            }
        });
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
