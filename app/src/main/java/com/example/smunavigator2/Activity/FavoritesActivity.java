package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.FavoriteAdapter;
import com.example.smunavigator2.Domain.FavoriteItem;
import com.example.smunavigator2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<FavoriteItem> favoriteList = new ArrayList<>();
    private DatabaseReference favoriteRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        setupBottomNav(R.id.favorite);
        setupRecyclerView();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoriteRef = FirebaseDatabase.getInstance().getReference("favorites").child(uid);

        loadFavorites();
    }

    private void setupRecyclerView() {
        ProgressBar progressBar = findViewById(R.id.favoriteLoading);
        recyclerView = findViewById(R.id.favoriteRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Hide recycler initially
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        adapter = new FavoriteAdapter(this, favoriteList, item -> {
            String placeId = item.getName() + "_" + item.getLat() + "_" + item.getLng();
            placeId = placeId.replace(".", "_");

            favoriteRef.child(placeId).removeValue().addOnSuccessListener(unused -> {
                int index = favoriteList.indexOf(item);
                if (index != -1) {
                    favoriteList.remove(index);
                    adapter.notifyItemRemoved(index); // ✅ More efficient
                    Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                }
            });
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        ProgressBar progressBar = findViewById(R.id.favoriteLoading);

        favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    FavoriteItem item = snap.getValue(FavoriteItem.class);
                    favoriteList.add(item);
                }
                adapter.notifyDataSetChanged();

                // ✅ Hide loading, show list
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoritesActivity.this, getString(R.string.failed_to_load_favorites), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
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
}