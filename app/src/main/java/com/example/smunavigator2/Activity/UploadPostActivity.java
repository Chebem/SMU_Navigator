package com.example.smunavigator2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.ImagePreviewAdapter;
import com.example.smunavigator2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UploadPostActivity extends AppCompatActivity {

    private RecyclerView imageRecycler;
    private ImagePreviewAdapter adapter;
    private List<Uri> imageUris = new ArrayList<>();
    private Button uploadBtn;


    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        imageUris.add(uri);
                        adapter.notifyItemInserted(imageUris.size() - 1);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);

        imageRecycler = findViewById(R.id.imageRecycler);
        uploadBtn = findViewById(R.id.uploadBtn);
        adapter = new ImagePreviewAdapter(imageUris, new ImagePreviewAdapter.OnImageRemoveListener() {
            @Override
            public void onRemove(int position) {
                imageUris.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onAddImage() {
                openImagePicker(); // or whatever method triggers your image picker
            }
        });

        imageRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        imageRecycler.setAdapter(adapter);

        // Add image on click
        imageRecycler.setOnClickListener(v -> openImagePicker());

        uploadBtn.setOnClickListener(v -> {
            if (imageUris.isEmpty()) {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadImagesToFirebase();
        });


            setupBottomNav(R.id.post);

    }

    private void openImagePicker() {
        Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(pick);
    }

    private void uploadImagesToFirebase() {
        List<String> uploadedUrls = new ArrayList<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int total = imageUris.size();

        for (Uri uri : imageUris) {
            String filename = UUID.randomUUID().toString();
            FirebaseStorage.getInstance().getReference("posts/" + uid + "/" + filename)
                    .putFile(uri)
                    .addOnSuccessListener(task -> task.getStorage().getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                uploadedUrls.add(downloadUri.toString());
                                if (uploadedUrls.size() == total) {
                                    savePostToDatabase(uid, uploadedUrls);
                                }
                            }));
        }
    }


    private void savePostToDatabase(String uid, List<String> urls) {
        EditText captionInput = findViewById(R.id.postText);
        String caption = captionInput.getText().toString().trim();

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("imageUrls", urls);
        postMap.put("mainImage", urls.get(0)); // 👈 Add this
        postMap.put("caption", caption);
        postMap.put("timestamp", System.currentTimeMillis());
        postMap.put("userId", uid);
        postMap.put("visibility", "public"); // or "private"

        FirebaseDatabase.getInstance().getReference("profiles")
                .child(uid)
                .child("posts")
                .push()
                .setValue(postMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                    imageUris.clear();
                    startActivity(new Intent(UploadPostActivity.this, ProfilePageActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                });
    }




    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = findViewById(R.id.navigationBar);
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