package com.example.smunavigator2.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.ConvenienceFacility;
import com.example.smunavigator2.Domain.FacilityModel;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.MarkerUtils;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import com.kakao.vectormap.*;
import com.kakao.vectormap.camera.CameraPosition;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.*;

import java.util.Objects;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private KakaoMap kakaoMap;
    private final LatLng smuCenter = LatLng.from(37.180713, 128.208904 );
    private String selectedCategory = "All";
    private double currentLat = smuCenter.getLatitude();
    private double currentLng = smuCenter.getLongitude();
    private String markerLayout;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map_view);

        // 🔘 Category Buttons
        findViewById(R.id.btnFacilities).setOnClickListener(v -> {
            selectedCategory = "Facilities";
            hideOverlay(); // 👈 hide before reload
            loadMarkers();
        });
        findViewById(R.id.btnRestaurants).setOnClickListener(v -> {
            selectedCategory = "Restaurants";
            hideOverlay();
            loadMarkers();
        });
        findViewById(R.id.btnStores).setOnClickListener(v -> {
            selectedCategory = "Stores";
            hideOverlay();
            loadMarkers();
        });
        findViewById(R.id.btnCafe).setOnClickListener(v -> {
            selectedCategory = "Coffee";
            hideOverlay();
            loadMarkers();
        });
        findViewById(R.id.btnConveniences).setOnClickListener(v -> {
            selectedCategory = "Convenience";
            hideOverlay();
            loadMarkers();
        });
        findViewById(R.id.btnDorms).setOnClickListener(v -> {
            selectedCategory = "Dorms";
            hideOverlay();
            loadMarkers();
        });

        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
            }

            @Override
            public void onMapError(@NonNull Exception error) {
                Log.e("MapActivity", "Map error: " + error.getMessage());
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull KakaoMap map) {
                kakaoMap = map;
                setLabelClickListener(); // ✅ Important!

                fetchCurrentLocationAndMoveCamera();

                double lat = getIntent().getDoubleExtra("lat", 0);
                double lng = getIntent().getDoubleExtra("lng", 0);
                String locationName = getIntent().getStringExtra("locationName");
                markerLayout = getIntent().getStringExtra("markerLayout");

                if (lat != 0 && lng != 0) {
                    LatLng pos = LatLng.from(lat, lng);
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(pos));

                    try {
                        MarkerUtils markerUtils = new MarkerUtils();
                        Bitmap markerBitmap = markerUtils.createCustomMarker(MapActivity.this, getLayoutId(markerLayout));
                        LabelStyle style = LabelStyle.from(markerBitmap).setZoomLevel(10);
                        LabelStyles styles = LabelStyles.from("location_marker", style);
                        Objects.requireNonNull(kakaoMap.getLabelManager()).addLabelStyles(styles);
                        Objects.requireNonNull(kakaoMap.getLabelManager().getLayer()).addLabel(
                                LabelOptions.from(pos).setStyles(styles).setTag(locationName)
                        );
                        kakaoMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                CameraPosition.from(pos.getLatitude(), pos.getLongitude(), 16, 0, 0, 0)));
                    } catch (Exception e) {
                        Log.e("MapDebug", "Error creating custom marker: " + e.getMessage());
                    }
                    loadMarkers();
                } else {
                    kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(smuCenter));
                    loadMarkers();
                }
            }
        });

        FloatingActionButton fabSwitchToGoogle = findViewById(R.id.fabSwitchToGoogle);
        fabSwitchToGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, GoogleMapActivity.class);
            // Optional: send current location (if you have it)
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            startActivity(intent);
        });
    }

    private int getLayoutId(String layoutName) {
        if (layoutName == null) return R.layout.store_marker;
        switch (layoutName) {
            case "dorm_marker":
                return R.layout.dorm_marker;
            case "food_marker":
                return R.layout.food_marker;
            default:
                return R.layout.store_marker;
        }
    }

    private void loadMarkers() {
        if (kakaoMap == null) return;
        LabelLayer labelLayer = Objects.requireNonNull(kakaoMap.getLabelManager()).getLayer();
        if (labelLayer == null) return;
        labelLayer.removeAll();
        MarkerUtils markerUtils = new MarkerUtils();

        // Faculties
        if (selectedCategory.equals("Facilities") || selectedCategory.equals("All")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PopularEn");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ItemDomain faculty = snap.getValue(ItemDomain.class);
                        if (faculty == null || faculty.getLat() == 0) continue;
                        LatLng pos = LatLng.from(faculty.getLat(), faculty.getLng());
                        Bitmap bmp = markerUtils.createCustomMarker(MapActivity.this, R.layout.facilities_marker);
                        LabelStyle style = LabelStyle.from(bmp);
                        LabelStyles styles = LabelStyles.from("faculty_" + snap.getKey(), style);
                        kakaoMap.getLabelManager().addLabelStyles(styles);
                        labelLayer.addLabel(LabelOptions.from(pos).setStyles(styles).setTag(faculty));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        // 🍽️ Stores, Cafes, Restaurants, etc.
        if (
                selectedCategory.equals("Convenience") ||
                        selectedCategory.equals("Stores") ||
                        selectedCategory.equals("Restaurants") ||
                        selectedCategory.equals("Coffee") ||
                        selectedCategory.equals("Facilities") || // Optional group
                        selectedCategory.equals("All")
        ) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("convenienceFacilitiesEn");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ConvenienceFacility s = snap.getValue(ConvenienceFacility.class);
                        if (s == null || s.getLat() == 0) continue;

                        // 🧠 Filter: Only show matching category
                        if (!selectedCategory.equals("All") && !selectedCategory.equals("Facilities")
                                && !selectedCategory.equalsIgnoreCase(s.getCategory())) continue;

                        // 🎨 Choose marker layout by category
                        String layoutKey;
                        switch (s.getCategory()) {
                            case "Coffee":
                                layoutKey = "coffee_marker";
                                break;
                            case "Restaurants":
                                layoutKey = "food_marker";
                                break;
                            case "Stores":
                                layoutKey = "store_marker";
                                break;
                            case "Conveniences":
                                layoutKey = "convenience_marker";
                                break;
                            default:
                                layoutKey = "store_marker"; // fallback
                                break;
                        }

                        int layoutRes = getLayoutId(layoutKey);
                        LatLng pos = LatLng.from(s.getLat(), s.getLng());
                        Bitmap bmp = markerUtils.createCustomMarker(MapActivity.this, layoutRes);

                        LabelStyle style = LabelStyle.from(bmp);
                        LabelStyles styles = LabelStyles.from("facility_" + snap.getKey(), style);
                        kakaoMap.getLabelManager().addLabelStyles(styles);
                        labelLayer.addLabel(LabelOptions.from(pos).setStyles(styles).setTag(s));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            setLabelClickListener(); // ✅ overlay popup
        }


        // Dorms
        if (selectedCategory.equals("Dorms") || selectedCategory.equals("All")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("dormitoriesEn");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ItemDomain dorm = snap.getValue(ItemDomain.class);
                        if (dorm == null || dorm.getLat() == 0) continue;

                        LatLng pos = LatLng.from(dorm.getLat(), dorm.getLng());
                        Bitmap bmp = markerUtils.createCustomMarker(MapActivity.this, R.layout.dorm_marker);
                        LabelStyle style = LabelStyle.from(bmp);
                        LabelStyles styles = LabelStyles.from("dorm_" + snap.getKey(), style);
                        kakaoMap.getLabelManager().addLabelStyles(styles);
                        labelLayer.addLabel(LabelOptions.from(pos).setStyles(styles).setTag(dorm));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        setLabelClickListener(); // ✅ Needed for overlay
    }

    private void setLabelClickListener() {
        if (kakaoMap == null) return;
        kakaoMap.setOnLabelClickListener((layer, label, latLng) -> {
            Object tag = label.getTag();
            if (tag instanceof FacilityModel) {
                FacilityModel f = (FacilityModel) tag;
                showOverlay(
                        f.getImagePath(),
                        f.getName() != null ? f.getName() : "Facility",
                        f.getDescription() != null ? f.getDescription() : "No description available",
                        f.getOperatingHours()
                );
            } else if (tag instanceof ConvenienceFacility) {
                ConvenienceFacility s = (ConvenienceFacility) tag;
                showOverlay(s.getImagePath(), s.getName(), s.getLocation(), s.getOperating_hours());
            } else if (tag instanceof ItemDomain) {
                ItemDomain d = (ItemDomain) tag;
                showOverlay(d.getImagePath(), d.getName(), d.getLocationDetails(), d.getOpeningHours());
            } else {
                Log.w("MapDebug", "Unknown label tag type: " + tag);
            }
            return true;
        });
    }

    private void showOverlay(String imageUrl, String title, String address, String hours) {
        CardView card = findViewById(R.id.markerOverlayCard);
        ImageView img = findViewById(R.id.placeImage);
        TextView t = findViewById(R.id.placeTitle);
        TextView a = findViewById(R.id.placeAddress);
        TextView h = findViewById(R.id.placeHours);

        Log.d("OverlayDebug", "Showing overlay. Current visibility: " + card.getVisibility());

        t.setText((title != null && !title.trim().isEmpty()) ? title : "No name available");
        a.setText((address != null && !address.trim().isEmpty()) ? address : "No address info");
        h.setText((hours != null && !hours.trim().isEmpty()) ? "Open: " + hours : "Operating hours not available");

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.smu_logo).error(R.drawable.smu_logo).into(img);
        } else {
            img.setImageResource(R.drawable.smu_logo);
        }

        if (card.getVisibility() != View.VISIBLE) {
            card.setVisibility(View.VISIBLE);
            card.setAlpha(0f);
            card.setTranslationY(60f);
            card.animate().alpha(1f).translationY(0f).setDuration(250).start();
        }
    }

    private void hideOverlay() {
        CardView card = findViewById(R.id.markerOverlayCard);
        if (card.getVisibility() == View.VISIBLE) {
            card.setVisibility(View.GONE);
            Log.d("OverlayDebug", "Hiding overlay card.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.finish();
    }
    private void fetchCurrentLocationAndMoveCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                showLocationMarker(location);
            } else {
                // 🆕 Updated to use the new Builder pattern
                LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
                        .setMinUpdateIntervalMillis(5000L)
                        .build();

                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            Location newLocation = locationResult.getLastLocation();
                            showLocationMarker(newLocation);
                            fusedLocationClient.removeLocationUpdates(this); // ✅ Stop after one result
                        } else {
                            Log.e("MapDebug", "Real-time location fetch failed");
                        }
                    }
                }, Looper.getMainLooper());
            }
        }).addOnFailureListener(e -> Log.e("MapDebug", "Failed to get location: " + e.getMessage()));
    }

    private void showLocationMarker(Location location) {
        if (location != null && kakaoMap != null) {
            LatLng currentPos = LatLng.from(location.getLatitude(), location.getLongitude());

            kakaoMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.from(currentPos.getLatitude(), currentPos.getLongitude(), 4, 0, 0, 0)));

            try {
                MarkerUtils markerUtils = new MarkerUtils();
                Bitmap markerBitmap = markerUtils.createCustomMarker(this, R.layout.current_location); // Make sure this layout exists
                LabelStyle style = LabelStyle.from(markerBitmap);
                LabelStyles styles = LabelStyles.from("my_location_marker", style);

                LabelManager labelManager = kakaoMap.getLabelManager();
                if (labelManager != null) {
                    labelManager.addLabelStyles(styles);
                    LabelLayer layer = labelManager.getLayer();
                    if (layer != null) {
                        layer.addLabel(LabelOptions.from(currentPos)
                                .setStyles(styles)
                                .setTag("You are here"));
                        Log.d("MapDebug", "✅ Marker added at current location");
                    } else {
                        Log.e("MapDebug", "LabelLayer is null");
                    }
                } else {
                    Log.e("MapDebug", "LabelManager is null");
                }

            } catch (Exception e) {
                Log.e("MapDebug", "❌ Error creating location marker: " + e.getMessage());
            }
        }
    }
}


// Note: Make sure to add the required permissions in AndroidManifest.xml