package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Adapter.ReviewsAdapter;
import com.example.smunavigator2.Dialog.ReviewSheet;
import com.example.smunavigator2.Domain.Committee;
import com.example.smunavigator2.Domain.ConvenienceFacility;
import com.example.smunavigator2.Domain.FacilityModel;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.Domain.Review;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.FavoriteUtils;
import com.example.smunavigator2.Utils.ModerationUtils;
import com.example.smunavigator2.Utils.PlaceUtils;
import com.example.smunavigator2.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private Object object;
    private String placeKey;

    private ReviewsAdapter reviewsAdapter;
    private final List<Review> allReviews = new ArrayList<>();
    private Review myReview;
    private DatabaseReference reviewsRef;
    private ValueEventListener reviewsListener;
    private final Runnable onBlockedChanged = this::showReviews;

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
        setupFavoriteButton();
        setupReviews();
    }

    // ❤️ Same favorites as the map, so saved places show on the Favorites screen
    private void setupFavoriteButton() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = binding.titleTxt.getText().toString();
        double lat = 0, lng = 0;
        String category = "", image = "";
        if (object instanceof ItemDomain) {
            ItemDomain i = (ItemDomain) object;
            lat = i.getLat(); lng = i.getLng(); category = i.getCategory(); image = i.getImagePath();
        } else if (object instanceof ConvenienceFacility) {
            ConvenienceFacility f = (ConvenienceFacility) object;
            lat = f.getLat(); lng = f.getLng(); category = f.getCategory(); image = f.getImagePath();
        } else if (object instanceof FacilityModel) {
            FacilityModel f = (FacilityModel) object;
            lat = f.getLat(); lng = f.getLng(); category = f.getCategory(); image = f.getImagePath();
        } else if (object instanceof Committee) {
            Committee c = (Committee) object;
            lat = c.getLat(); lng = c.getLng(); category = "Committee"; image = c.getImageUrl();
        } else if (object instanceof StoreModel) {
            StoreModel st = (StoreModel) object;
            lat = st.getLat(); lng = st.getLng(); category = st.getCategory(); image = st.getImagePath();
        }
        placeKey = name.isEmpty() ? null : FavoriteUtils.key(name, lat, lng); // also keys this place's reviews
        if (user == null || name.isEmpty() || (lat == 0 && lng == 0)) {
            binding.imageView5.setVisibility(View.GONE); // nothing to save it by
            return;
        }

        String uid = user.getUid();
        String key = FavoriteUtils.key(name, lat, lng);
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("address", binding.addressTxt2.getText().toString());
        data.put("description", binding.descriptionTxt.getText().toString());
        data.put("imageUrl", image);
        data.put("category", category);
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("phone", binding.contactTxt.getText().toString());

        boolean[] saved = {false};
        FavoriteUtils.ref(uid, key).get().addOnSuccessListener(snap -> {
            saved[0] = snap.exists();
            showFavorite(saved[0]);
        });
        binding.imageView5.setOnClickListener(v -> {
            boolean save = !saved[0];
            saved[0] = save;
            showFavorite(save);
            FavoriteUtils.set(uid, key, save ? data : null)
                    .addOnSuccessListener(r -> Toast.makeText(this,
                            save ? R.string.added_to_favorites : R.string.removed_from_favorites, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        saved[0] = !save;
                        showFavorite(!save);
                        Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                    });
        });
    }

    // ⭐ Reviews: one per person, live average and list; blocked people's reviews are hidden
    private void setupReviews() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || placeKey == null) {
            binding.writeReviewBtn.setVisibility(View.GONE);
            return;
        }
        ModerationUtils.watchBlocked();
        ModerationUtils.addBlockListener(onBlockedChanged);

        reviewsAdapter = new ReviewsAdapter(user.getUid(), placeKey, review -> ReviewSheet.show(this, placeKey, review));
        binding.reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.reviewsRecycler.setAdapter(reviewsAdapter);
        binding.writeReviewBtn.setOnClickListener(v -> ReviewSheet.show(this, placeKey, myReview));

        reviewsRef = FirebaseDatabase.getInstance().getReference("reviews").child(placeKey);
        reviewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReviews.clear();
                myReview = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Review r;
                    try {
                        r = child.getValue(Review.class);
                    } catch (Exception e) {
                        continue;
                    }
                    if (r == null || r.userId == null || r.rating < 1) continue;
                    allReviews.add(r);
                    if (r.userId.equals(user.getUid())) myReview = r;
                }
                allReviews.sort((a, b) -> Long.compare(b.timestamp, a.timestamp)); // newest first
                binding.writeReviewBtn.setText(myReview != null ? R.string.edit_review : R.string.write_review);
                showReviews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        reviewsRef.addValueEventListener(reviewsListener);
    }

    private void showReviews() {
        if (reviewsAdapter == null) return;
        List<Review> visible = new ArrayList<>();
        int total = 0;
        for (Review r : allReviews) {
            total += r.rating; // the average counts everyone, even people you blocked
            if (!ModerationUtils.isBlocked(r.userId)) visible.add(r);
        }
        reviewsAdapter.setReviews(visible);

        int count = allReviews.size();
        if (count == 0) {
            binding.ratingBar.setVisibility(View.GONE);
            binding.ratingTxt.setText(R.string.no_reviews_yet);
            binding.reviewSummaryTxt.setText(R.string.be_first_review);
        } else {
            float average = (float) total / count;
            binding.ratingBar.setVisibility(View.VISIBLE);
            binding.ratingBar.setRating(average);
            String summary = getResources().getQuantityString(R.plurals.review_summary, count,
                    String.format(Locale.getDefault(), "%.1f", average), count);
            binding.ratingTxt.setText(summary);
            binding.reviewSummaryTxt.setText(summary);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reviewsRef != null) reviewsRef.removeEventListener(reviewsListener);
        ModerationUtils.removeBlockListener(onBlockedChanged);
    }

    private void showFavorite(boolean saved) {
        binding.imageView5.setImageResource(saved ? R.drawable.fav_icon_filled : R.drawable.fav_icon);
        binding.imageView5.setContentDescription(getString(saved ? R.string.removed_from_favorites : R.string.favorite));
    }

    private void getIntentExtra() {
        object = getIntent().getSerializableExtra("object");
        assert object != null;
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
        } else if (object instanceof StoreModel) {
            // City Guide place (restaurant, café, store…): same page as campus places
            StoreModel store = (StoreModel) object;
            binding.titleTxt.setText(store.getName());
            binding.addressTxt2.setText(store.getAddress());
            binding.descriptionTxt.setText(store.getActivity());
            binding.openinghoursTxt.setText(PlaceUtils.openingHours(store, getString(R.string.hours_unknown)));
            String phone = store.getPhone_number();
            binding.contactTxt.setText(phone != null && !phone.isEmpty() ? phone : "—");
            // No ratings yet: reviews will fill these in
            binding.ratingBar.setVisibility(View.GONE);
            binding.ratingTxt.setText(R.string.no_reviews_yet);
            int placeholder = PlaceUtils.placeholderImage(store.getCategory());
            Glide.with(this).load(store.getImagePath())
                    .placeholder(placeholder).error(placeholder).fallback(placeholder)
                    .into(binding.pic);
        }

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupExploreButton() {
        binding.btnShowOnMap.setOnClickListener(v -> {
            if (object instanceof StoreModel) {
                startActivity(PlaceUtils.mapIntent(this, (StoreModel) object));
                return;
            }
            double lat = 0, lng = 0;
            String name = "", category = "", layoutKey = "store_marker";

            if (object instanceof ItemDomain) {
                ItemDomain item = (ItemDomain) object;
                lat = item.getLat();
                lng = item.getLng();
                name = item.getName();
                category = item.getCategory();

            } else if (object instanceof ConvenienceFacility) {
                ConvenienceFacility f = (ConvenienceFacility) object;
                lat = f.getLat();
                lng = f.getLng();
                name = f.getName();
                category = f.getCategory();

            } else if (object instanceof FacilityModel) {
                FacilityModel f = (FacilityModel) object;
                lat = f.getLat();
                lng = f.getLng();
                name = f.getName();
                category = f.getCategory();

            } else if (object instanceof Committee) {
                Committee c = (Committee) object;
                lat = c.getLat();
                lng = c.getLng();
                name = c.getTitle();
                category = "Committee";
            }

            // 🔁 Assign layoutKey by category
            switch (category) {
                case "Coffee": layoutKey = "coffee_marker"; break;
                case "Dorms": layoutKey = "dorm_marker"; break;
                case "Restaurant": layoutKey = "food_marker"; break;
                case "Convenience": layoutKey = "convenience_marker"; break;
                case "Facilities": layoutKey = "facilities_marker"; break;
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

                // storeImage for all types
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