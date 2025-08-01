// ProfileSetupActivity.java
package com.example.smunavigator2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smunavigator2.databinding.ActivityProfileSetupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ProfileSetupActivity extends AppCompatActivity {

    private ActivityProfileSetupBinding binding;
    private Uri selectedImageUri;
    private FirebaseAuth auth;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profileImages");
        databaseRef = FirebaseDatabase.getInstance().getReference("profiles");

        binding.profileImage.setOnClickListener(v -> openImagePicker());
        binding.saveProfileBtn.setOnClickListener(v -> saveUserProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                binding.profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveUserProfile() {
        String name = binding.nameInput.getText().toString().trim();
        String bio = binding.bioInput.getText().toString().trim();
        String department = binding.departmentInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(bio) || TextUtils.isEmpty(department)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            String filename = UUID.randomUUID().toString() + ".jpg";
            StorageReference fileRef = storageRef.child(auth.getCurrentUser().getUid()).child("profile.jpg");

            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveToDatabase(name, bio, uri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            saveToDatabase(name, bio, "");
        }
    }

    private void saveToDatabase(String name, String bio, String imageUrl) {
        String uid = auth.getCurrentUser().getUid();
        String department = binding.departmentInput.getText().toString().trim();
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("profileName", name);
        profileMap.put("about", bio);
        profileMap.put("department", department);
        profileMap.put("profileImage", imageUrl);
        profileMap.put("followersNum", 0);
        profileMap.put("followingNum", 0);
        profileMap.put("likes", 0);

        databaseRef.child(uid).setValue(profileMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ProfilePageActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}