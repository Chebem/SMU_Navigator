package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.Committee;
import com.example.smunavigator2.Domain.ConvenienceFacility;
import com.example.smunavigator2.Domain.FacilityModel;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private Object object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();

        if (object == null) {
            Toast.makeText(this, "Failed to load details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setVariable();
        setupExploreButton();
    }

    private void getIntentExtra() {
        object = getIntent().getSerializableExtra("object");
        Log.d("DetailType", "Object class: " + object.getClass().getSimpleName());
    }

    private void setVariable() {
        if (object instanceof ItemDomain) {
            ItemDomain item = (ItemDomain) object;
            binding.titleTxt.setText(item.getName());
            binding.addressTxt2.setText(item.getLocationDetails());
            binding.contactTxt.setText(item.getContactNumber());
            binding.ratingTxt.setText(formatRating(item.getScore()));
            loadImage(item.getImagePath());

            if ("Dorms".equalsIgnoreCase(item.getCategory())) {
                StringBuilder dormDetails = new StringBuilder();
                if (item.getGender() != null) dormDetails.append("Gender: ").append(item.getGender()).append("\n");
                if (item.getFloors() != null) dormDetails.append("Floors: ").append(item.getFloors()).append("\n");
                if (item.getSupervisors() != null) dormDetails.append("Supervisors: ").append(item.getSupervisors()).append("\n");
                if (item.getTotal_rooms() != null) dormDetails.append("Rooms: ").append(item.getTotal_rooms()).append("\n");
                dormDetails.append("Contact Dorm Office for Hours");

                binding.descriptionTxt.setText(dormDetails.toString().trim());
                binding.openinghoursTxt.setText("");
            } else {
                binding.descriptionTxt.setText(item.getDescription());
                binding.openinghoursTxt.setText(item.getOpeningHours());
            }

        } else if (object instanceof ConvenienceFacility) {
            ConvenienceFacility facility = (ConvenienceFacility) object;
            binding.titleTxt.setText(facility.getName());
            binding.descriptionTxt.setText(facility.getDescription());
            binding.addressTxt2.setText(facility.getLocation());
            binding.openinghoursTxt.setText(facility.getOperating_hours());
            binding.contactTxt.setText(facility.getType());
            binding.ratingTxt.setText(formatRating(facility.getScore()));
            loadImage(facility.getImagePath());

        } else if (object instanceof Committee) {
            Committee committee = (Committee) object;
            binding.titleTxt.setText(committee.getTitle());
            binding.descriptionTxt.setText(committee.getDescription());
            binding.addressTxt2.setText(committee.getLocation());
            binding.openinghoursTxt.setText(committee.getOpeningHours());
            binding.contactTxt.setText("Committee Booth");
            binding.ratingTxt.setText(formatRating(String.valueOf(committee.getScore())));
            loadImage(committee.getImageUrl());

        } else if (object instanceof FacilityModel) {
            FacilityModel facility = (FacilityModel) object;
            binding.titleTxt.setText(facility.getName());
            binding.descriptionTxt.setText(facility.getDescription());
            binding.addressTxt2.setText(facility.getLocation());
            binding.openinghoursTxt.setText(facility.getOperatingHours());
            binding.contactTxt.setText(facility.getType());
            binding.ratingTxt.setText("Info");
            loadImage(facility.getImagePath());
        }

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupExploreButton() {
        binding.btnShowOnMap.setOnClickListener(v -> {
            double lat = 0, lng = 0;
            String name = "", category = "", layoutKey = "store_marker";

            if (object instanceof ItemDomain) {
                ItemDomain item = (ItemDomain) object;
                lat = item.getLat(); lng = item.getLng();
                name = item.getName(); category = item.getCategory();

            } else if (object instanceof ConvenienceFacility) {
                ConvenienceFacility f = (ConvenienceFacility) object;
                lat = f.getLat(); lng = f.getLng();
                name = f.getName(); category = f.getCategory();

            } else if (object instanceof FacilityModel) {
                FacilityModel f = (FacilityModel) object;
                lat = f.getLat(); lng = f.getLng();
                name = f.getName(); category = f.getCategory();

            } else if (object instanceof Committee) {
                Committee c = (Committee) object;
                lat = c.getLat(); lng = c.getLng();
                name = c.getTitle(); category = "Committee";
            }

            // 🔁 Assign layoutKey by category
            switch (category) {
                case "Coffee": layoutKey = "coffee_marker"; break;
                case "Dorms": layoutKey = "dorm_marker"; break;
                case "Restaurant": layoutKey = "food_marker"; break;
                case "Stores": layoutKey = "store_marker"; break;
                case "Convenience": layoutKey = "convenience_marker"; break;
                case "Facilities":
                case "Facilties": layoutKey = "facilities_marker"; break;
                case "Park": layoutKey = "park_marker"; break;
                case "Sports": layoutKey = "sports_marker"; break;
                default: layoutKey = "store_marker"; break;
            }

            if (lat != 0 && lng != 0) {
                Intent intent = new Intent(DetailActivity.this, GoogleMapActivity.class);
                intent.putExtra("storeLat", lat);
                intent.putExtra("storeLng", lng);
                intent.putExtra("storeName", name);
                intent.putExtra("storeAddress", binding.addressTxt2.getText().toString());
                intent.putExtra("storeHours", binding.openinghoursTxt.getText().toString());
                intent.putExtra("storeCategory", category);
                intent.putExtra("markerLayout", layoutKey);

                // ✅ storeImage for all types
                String image = "";
                if (object instanceof ItemDomain) image = ((ItemDomain) object).getImagePath();
                else if (object instanceof ConvenienceFacility) image = ((ConvenienceFacility) object).getImagePath();
                else if (object instanceof FacilityModel) image = ((FacilityModel) object).getImagePath();
                intent.putExtra("storeImage", image);

                // 🏠 Dormitory extras
                if (object instanceof ItemDomain && "Dorms".equalsIgnoreCase(((ItemDomain) object).getCategory())) {
                    ItemDomain dorm = (ItemDomain) object;
                    intent.putExtra("storeDescription", dorm.getDescription() != null ? dorm.getDescription() : "");
                    intent.putExtra("storeFloors", dorm.getFloors());
                    intent.putExtra("storeSupervisors", dorm.getSupervisors());
                    intent.putExtra("storeGender", dorm.getGender());
                    intent.putExtra("storeRooms", dorm.getTotal_rooms());
                } else if (object instanceof FacilityModel) {
                    FacilityModel f = (FacilityModel) object;
                    intent.putExtra("storeDescription", f.getDescription() != null ? f.getDescription() : "");
                } else {
                    intent.putExtra("storeDescription", binding.descriptionTxt.getText().toString());
                }

                startActivity(intent);
            } else {
                Toast.makeText(this, "Location not available for this place.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImage(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.pic_1)
                .error(R.drawable.pic_1)
                .into(binding.pic);
    }

    private String formatRating(String score) {
        return (score != null && !score.isEmpty()) ? score + " Rating" : "No Rating";
    }
}