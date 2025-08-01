package com.example.smunavigator2.Activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.smunavigator2.Domain.MarkerTagData;
import com.example.smunavigator2.Domain.StoreModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLngBounds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.ConvenienceFacility;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Polyline currentPolyline;
    private double lat;
    private double lng;
    private FusedLocationProviderClient fusedLocationClient;

    private CardView overlayCard;
    private ImageView placeImage;
    private TextView placeTitle, placeAddress, placeHours, placeDistance;


    private ImageButton favoriteBtn;


    private String selectedCategory = "All";
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private LatLng selectedOrigin, selectedDestination;


    private DatabaseReference favoriteRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // ✅ LEGAL placement
        favoriteRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId); // ✅ also safe here

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        lat = intent.getDoubleExtra("lat", lat);
        lng = intent.getDoubleExtra("lng", lng);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        fabMyLocation.setOnClickListener(v -> moveToCurrentLocation());

        findViewById(R.id.btnDorms).setOnClickListener(v -> filterByCategory("Dorms"));
        findViewById(R.id.btnRestaurants).setOnClickListener(v -> filterByCategory("Restaurants"));
        findViewById(R.id.btnConveniences).setOnClickListener(v -> filterByCategory("Convenience"));
        findViewById(R.id.btnStores).setOnClickListener(v -> filterByCategory("Stores"));
        findViewById(R.id.btnCafe).setOnClickListener(v -> filterByCategory("Coffee"));
        findViewById(R.id.btnFacilities).setOnClickListener(v -> filterByCategory("Facilities"));
        findViewById(R.id.btnMart).setOnClickListener(v -> filterByCategory("Mart"));
        findViewById(R.id.btnBar).setOnClickListener(v -> filterByCategory("Bars"));

        overlayCard = findViewById(R.id.markerOverlayCard);
        AppCompatButton favoriteBtn = findViewById(R.id.favoriteBtn);
        placeImage = findViewById(R.id.placeImage);
        placeTitle = findViewById(R.id.placeTitle);
        placeAddress = findViewById(R.id.placeAddress);
        placeHours = findViewById(R.id.placeHours);
        placeDistance = findViewById(R.id.placeDistance);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        EditText searchInput = findViewById(R.id.searchInput);
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String keyword = searchInput.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    filterMarkersByKeyword(keyword);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;

        // 🔒 Location permission
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }

        Intent intent = getIntent();
        double storeLat = intent.getDoubleExtra("storeLat", 0.0);
        double storeLng = intent.getDoubleExtra("storeLng", 0.0);
        String storeName = intent.getStringExtra("storeName");
        String storeAddress = intent.getStringExtra("storeAddress");
        String storeHours = intent.getStringExtra("storeHours");
        String storeImage = intent.getStringExtra("storeImage");
        String storeCategory = intent.getStringExtra("storeCategory");
        String storeDescription = intent.getStringExtra("storeDescription");
        String layoutKey = intent.getStringExtra("markerLayout");
        String dormFloors = intent.getStringExtra("storeFloors");
        String dormSupervisors = intent.getStringExtra("storeSupervisors");

        int layoutResId;
        if (layoutKey != null) {
            layoutResId = getLayoutId(layoutKey);
            if (layoutResId == 0) {
                layoutResId = getLayoutId(getMarkerLayoutKeyFromCategory(storeCategory));
            }
        } else {
            layoutResId = getLayoutId(getMarkerLayoutKeyFromCategory(storeCategory));
        }

        if (storeLat != 0.0 && storeLng != 0.0) {
            LatLng pos = new LatLng(storeLat, storeLng);
            Bitmap iconBitmap = getBitmapFromLayout(layoutResId);

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(storeName)
                    .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));

            if (marker != null) {
                if ("Dorms".equalsIgnoreCase(storeCategory)) {
                    marker.setTag(new MarkerTagData(
                            storeName, "", "", storeDescription != null ? storeDescription : "Description not available",
                            storeImage, storeCategory,
                            dormFloors != null ? dormFloors : "Not specified",
                            dormSupervisors != null ? dormSupervisors : "Not specified",
                            storeLat,
                            storeLng
                    ));
                } else {
                    marker.setTag(new MarkerTagData(
                            storeName,
                            (storeAddress != null && !storeAddress.trim().isEmpty()) ? storeAddress : "No address",
                            (storeHours != null && !storeHours.trim().isEmpty()) ? storeHours : "No hours",
                            (storeDescription != null && !storeDescription.trim().isEmpty()) ? storeDescription : "No description",
                            storeImage,
                            storeCategory,
                            getIntent().getStringExtra("storePhoneNumber") != null
                                    ? getIntent().getStringExtra("storePhoneNumber") : "",
                            storeLat,
                            storeLng
                    ));
                }
            }

            // ✅ Manually zoom in to selected place
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17f));

            // 🛑 Do NOT call filterByCategory again here
        } else {
            LatLng center = new LatLng(lat, lng);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 16));
            loadAllMarkers();

            filterByCategory("All");
        }

        // ✅ Unified overlay handler
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            LatLng pos = marker.getPosition();
            if (tag instanceof MarkerTagData) {
                MarkerTagData info = (MarkerTagData) tag;

                if ("Dorms".equalsIgnoreCase(info.category)) {
                    LatLng dormPos = marker.getPosition();
                    showDormOverlay(
                            info.name,
                            info.description,
                            info.floors,
                            info.supervisors,
                            info.imageUrl,
                            dormPos.latitude,
                            dormPos.longitude
                    );
                } else {
                    showPlaceOverlay(
                            info.name,
                            info.address,
                            info.hours,
                            info.description,
                            info.imageUrl,
                            info.phoneNumber, // ✅ add this line
                            pos.latitude,
                            pos.longitude
                    );
                }
            }
            return true;
        });


        // In setOnCameraIdleListener:
        googleMap.setOnCameraIdleListener(() -> {
            LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;

            // 🔁 Fallback to Google Places (if Firebase is empty)
            switch (selectedCategory) {
                case "Restaurants":
                    fetchNearbyRealPlacesWithCustomMarkers(bounds, "restaurant");
                    break;
                case "Coffee":
                    fetchNearbyRealPlacesWithCustomMarkers(bounds, "cafe");
                    break;
                case "Bars":
                    fetchNearbyRealPlacesWithCustomMarkers(bounds, "bar");
                    break;
                case "Mart":
                    fetchNearbyRealPlacesWithCustomMarkers(bounds, "supermarket");
                    break;
                case "Stores":
                    fetchNearbyRealPlacesWithCustomMarkers(bounds, "store");
                    break;
                case "Convenience":
                    fetchNearbyRealPlacesWithCustomMarkers(bounds, "convenience_store");
                    break;
            }
        });
    }


    private String getLanguageBasedNode(String baseKey) {
        // Adjust this logic to match your app's actual language settings
        String langCode = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("language", "en"); // default to "en"

        if (langCode.equalsIgnoreCase("ko")) {
            return baseKey + "Ko";
        } else {
            return baseKey + "En";
        }
    }


    //Filter markers by category
    private void filterByCategory(String category) {
        googleMap.clear(); // Clear old markers
        List<LatLng> markerPositions = new ArrayList<>();

        if (category.equalsIgnoreCase("Dorms")) {
            DatabaseReference dormRef = FirebaseDatabase.getInstance().getReference("dormitoriesEn");
            dormRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ItemDomain dorm = snap.getValue(ItemDomain.class);
                        if (dorm != null && dorm.getLat() != 0) {
                            LatLng pos = new LatLng(dorm.getLat(), dorm.getLng());
                            markerPositions.add(pos);

                            addDormMarker(
                                    dorm.getLat(),
                                    dorm.getLng(),
                                    dorm.getName(),
                                    dorm.getDescription(),
                                    dorm.getFloors(),
                                    dorm.getSupervisors(),
                                    dorm.getImagePath(),
                                    getMarkerLayoutKeyFromCategory("Dorms")
                            );
                        }
                    }
                    zoomToMarkers(markerPositions);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            // 🔁 Get lang-specific node
            String languageNode = getLanguageBasedNode("places"); // e.g., "placesEn" or "placesKo"

            // First, load from convenienceFacilitiesEn
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("convenienceFacilitiesEn");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ConvenienceFacility place = snap.getValue(ConvenienceFacility.class);
                        if (place != null && place.getLat() != 0 && category.equalsIgnoreCase(place.getCategory())) {
                            LatLng pos = new LatLng(place.getLat(), place.getLng());
                            markerPositions.add(pos);

                            addStoreMarker(
                                    place.getLat(),
                                    place.getLng(),
                                    place.getName(),
                                    place.getLocation(),
                                    place.getOperating_hours(),
                                    place.getDescription(),
                                    place.getImagePath(),
                                    place.getCategory(),
                                   null, // ✅ Pass phone number
                                    getMarkerLayoutKeyFromCategory(place.getCategory())
                            );
                        }
                    }

                    // 🔁 Then, load from placesEn / placesKo
                    DatabaseReference langRef = FirebaseDatabase.getInstance()
                            .getReference(getLanguageBasedNode("places")); // "placesEn" or "placesKo"

                    langRef.child(category.toLowerCase())  // e.g., "coffee"
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot snap : snapshot.getChildren()) {
                                        StoreModel place = snap.getValue(StoreModel.class);
                                        if (place != null && place.getLat() != 0) {
                                            LatLng pos = new LatLng(place.getLat(), place.getLng());
                                            markerPositions.add(pos);

                                            addStoreMarker(
                                                    place.getLat(),
                                                    place.getLng(),
                                                    place.getName(),
                                                    place.getAddress(),
                                                    (place.getOpening_hours() != null
                                                            ? TextUtils.join(", ", place.getOpening_hours())
                                                            : "Opening hours not available"),
                                                    place.getActivity(),  // optional description
                                                    place.getImagePath(),
                                                    place.getCategory(),
                                                    getMarkerLayoutKeyFromCategory(place.getCategory())
                                            );
                                        }
                                    }
                                    zoomToMarkers(markerPositions);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // Zoom to markers
    private void zoomToMarkers(List<LatLng> positions) {
        if (positions.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng pos : positions) {
            builder.include(pos);
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100)); // 100 = padding
    }


    private String getMarkerLayoutKeyFromCategory(String category) {
        if (category == null) return "store_marker";
        switch (category) {
            case "Coffee": return "coffee_marker";
            case "Dorms": return "dorm_marker";
            case "Restaurant":
            case "Restaurants":
                return "food_marker";
            case "Stores": return "store_marker";
            case "Convenience":
                return "convenience_marker";
            case "Facilities": return "facilities_marker";
            case "Bars": return "bars_marker";  // Use custom if available
            case "Mart": return "mart_marker";  // Use custom if available
            default: return "store_marker";
        }
    }


    //Search and filter markers by category
    private void filterMarkersByKeyword(String keyword) {
        if (googleMap == null) return;

        googleMap.clear(); // Remove all current markers

        // Convenience Facilities
        FirebaseDatabase.getInstance().getReference("convenienceFacilitiesEn")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            ConvenienceFacility s = snap.getValue(ConvenienceFacility.class);
                            if (s == null || s.getLat() == 0) continue;
                            if (!s.getName().toLowerCase().contains(keyword.toLowerCase())) continue;

                            addStoreMarker(
                                    s.getLat(),
                                    s.getLng(),
                                    s.getName(),
                                    s.getLocation(),
                                    s.getOperating_hours(),
                                    s.getDescription(),         // ✅ Correct position for description
                                    s.getImagePath(),
                                    s.getCategory(),            // ✅ category
                                    getMarkerLayoutKeyFromCategory(s.getCategory()), // ✅ layoutKey
                                    null // ✅ Added missing phone number argument
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Dormitories
        FirebaseDatabase.getInstance().getReference("dormitoriesEn")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            ItemDomain dorm = snap.getValue(ItemDomain.class);
                            if (dorm == null || dorm.getLat() == 0) continue;
                            if (!dorm.getName().toLowerCase().contains(keyword.toLowerCase())) continue;

                            String combinedDescription = dorm.getDescription() + "\nFloors: " + dorm.getFloors() + "\nSupervisors: " + dorm.getSupervisors();

                            addDormMarker(
                                    dorm.getLat(),
                                    dorm.getLng(),
                                    dorm.getName(),
                                    dorm.getLocationDetails(),
                                    dorm.getOpeningHours(),
                                    combinedDescription,
                                    dorm.getImagePath(),
                                    "Dorms"
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private String getLayoutKeyForPlaceType(String type) {
        switch (type.toLowerCase()) {
            case "restaurant": return "food_marker";
            case "cafe":
            case "coffee": return "coffee_marker";
            case "bar": return "bars_marker";
            case "convenience_store": return "convenience_marker";
            case "supermarket":
            case "grocery_or_supermarket": return "mart_marker";
            case "store":
            case "shopping_mall": return "store_marker";
            default: return "store_marker";
        }
    }

    private void fetchNearbyRealPlacesWithCustomMarkers(LatLngBounds bounds, String categoryType) {
        String apiKey = "AIzaSyDh2Tshp2CYwKMuyrFEgv02b9WWpa5cX38"; // 🔐 Use your real Google Maps API key
        LatLng center = bounds.getCenter();

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + center.latitude + "," + center.longitude +
                "&radius=700" +
                "&type=" + categoryType.toLowerCase() +
                "&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            String name = place.getString("name");

                            JSONObject geometry = place.getJSONObject("geometry").getJSONObject("location");
                            double lat = geometry.getDouble("lat");
                            double lng = geometry.getDouble("lng");

                            String layoutKey = getLayoutKeyForPlaceType(categoryType);
                            int layoutId = getLayoutId(layoutKey);
                            Bitmap icon = getBitmapFromLayout(layoutId);

                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lng))
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.fromBitmap(icon)));

                            if (marker != null) {
                                marker.setTag(new MarkerTagData(
                                        name,
                                        "Not available", "", "",  // address, hours, desc
                                        "",                        // image
                                        categoryType,
                                        "",                        // phone
                                        lat, lng
                                ));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("PlacesAPI", "Parsing error", e);
                    }
                },
                error -> Log.e("PlacesAPI", "Request failed", error)
        );

        queue.add(request);
    }

    private void showModeSelector() {
        LinearLayout modeLayout = findViewById(R.id.modeSelectorLayout);
        modeLayout.setVisibility(View.VISIBLE);

        findViewById(R.id.btnWalk).setOnClickListener(v -> {
            modeLayout.setVisibility(View.GONE);
            drawRouteWithMode(selectedOrigin, selectedDestination, "walking");
        });

        findViewById(R.id.btnDrive).setOnClickListener(v -> {
            modeLayout.setVisibility(View.GONE);
            drawRouteWithMode(selectedOrigin, selectedDestination, "driving");
        });

        findViewById(R.id.btnBus).setOnClickListener(v -> {
            modeLayout.setVisibility(View.GONE);
            drawRouteWithMode(selectedOrigin, selectedDestination, "transit");
        });
    }




    private void moveToCurrentLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && googleMap != null) {
                    LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16));
                }
            });
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    private void showDirectionOptions(LatLng destination, String placeName, String address) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.RoundedBottomSheetDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.direction_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView placeTitle = sheetView.findViewById(R.id.placeTitle);
        placeTitle.setText(placeName);

        sheetView.findViewById(R.id.openNaverMap).setOnClickListener(v -> {
            String uri = "nmap://route/walk?dlat=" + destination.latitude + "&dlng=" + destination.longitude + "&dname=" + Uri.encode(placeName);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.nhn.android.nmap");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Naver Map not installed", Toast.LENGTH_SHORT).show();
            }
        });

        sheetView.findViewById(R.id.openKakaoMap).setOnClickListener(v -> {
            String kakaoUrl = "kakaomap://route?ep=" + destination.latitude + "," + destination.longitude + "&by=FOOT";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kakaoUrl));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "KakaoMap not installed", Toast.LENGTH_SHORT).show();
            }
        });

        sheetView.findViewById(R.id.copyAddress).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Address", address);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Address copied", Toast.LENGTH_SHORT).show();
        });

        Button doneBtn = sheetView.findViewById(R.id.doneButton);
        doneBtn.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }


    private void drawRoute(LatLng origin, LatLng destination) {
        if (currentPolyline != null) currentPolyline.remove();
        drawRouteWithMode(origin, destination, "walking");
    }

    private void drawRouteWithMode(LatLng origin, LatLng destination, String mode) {
        Log.d("RouteDebug", "From: " + origin.latitude + "," + origin.longitude);
        Log.d("RouteDebug", "To: " + destination.latitude + "," + destination.longitude);
        Log.d("RouteDebug", "Mode: " + mode);

        if (currentPolyline != null) currentPolyline.remove();

        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=" + mode +
                "&key=AIzaSyDh2Tshp2CYwKMuyrFEgv02b9WWpa5cX38";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            String encodedPoints = routes.getJSONObject(0)
                                    .getJSONObject("overview_polyline")
                                    .getString("points");
                            List<LatLng> path = decodePolyline(encodedPoints);
                            if (currentPolyline != null) currentPolyline.remove();
                            currentPolyline = googleMap.addPolyline(new PolylineOptions()
                                    .addAll(path)
                                    .color(Color.BLUE)
                                    .width(10f));
                        } else {
                            // 🔁 fallback to another mode
                            if ("walking".equals(mode)) {
                                Log.w("RouteFallback", "No walking route found, retrying with transit...");
                                drawRouteWithMode(origin, destination, "transit");
                            } else if ("transit".equals(mode)) {
                                Log.e("RouteError", "No route found even for transit.");
                                Toast.makeText(this, "No route found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("RouteError", "Parsing error", e);
                    }
                },
                error -> {
                    Log.e("RouteError", "Request error", error);
                });

        queue.add(request);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // ✅ REQUIRED!

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveToCurrentLocation();

                // ✅ Safely enable location layer with try-catch
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e("GoogleMapActivity", "Location permission error: ", e);
                    }
                }
            } else {
                Log.w("GoogleMapActivity", "Location permission denied by user");
            }
        }
    }

    private void fetchDistanceAndDuration(LatLng origin, LatLng destination, TextView targetTextView) {
        String apiKey = "AIzaSyDh2Tshp2CYwKMuyrFEgv02b9WWpa5cX38"; // real key
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=" + origin.latitude + "," + origin.longitude +
                "&destinations=" + destination.latitude + "," + destination.longitude +
                "&mode=walking&language=en&key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray rows = response.getJSONArray("rows");
                        if (rows.length() > 0) {
                            JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                            if (elements.length() > 0) {
                                JSONObject element = elements.getJSONObject(0);

                                String status = element.optString("status", "UNKNOWN");

                                if ("OK".equals(status)) {
                                    String distance = element.getJSONObject("distance").getString("text");
                                    String duration = element.getJSONObject("duration").getString("text");
                                    targetTextView.setText(distance + " • " + duration);
                                } else {
                                    targetTextView.setText("Not available");
                                    Log.w("DistanceMatrix", "Route status: " + status);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DistanceMatrix", "Parse error", e);
                        targetTextView.setText(""); // fallback
                    }
                },
                error -> Log.e("DistanceMatrix", "Request failed", error));

        queue.add(request);
    }
    private void loadAllMarkers() {
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();

            if (tag instanceof String[]) {
                String[] info = (String[]) tag;
                double lat = Double.parseDouble(info[5]);
                double lng = Double.parseDouble(info[6]);
                String category = info.length >= 5 ? info[4] : "";
                if (category.equalsIgnoreCase("Dorms")) {
                    showDormOverlay(info[0], info[1], info[2], info[3], info[4], lat, lng);
                } else {
                    showStoreOverlay(info[0], info[1], info[2], info[3],info[4],lat, lng);
                }
            }
            return true;
        });
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("convenienceFacilitiesEn");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ConvenienceFacility s = snap.getValue(ConvenienceFacility.class);
                    if (s == null || s.getLat() == 0) continue;
                    if (!selectedCategory.equals("All") && !selectedCategory.equalsIgnoreCase(s.getCategory())) continue;
                    addStoreMarker(
                            s.getLat(),
                            s.getLng(),
                            s.getName(),
                            s.getLocation(),
                            s.getOperating_hours(),
                            s.getDescription(),         // ✅ Correct position for description
                            s.getImagePath(),
                            s.getCategory(),
                            null, // ✅ Add this// ✅ category
                            getMarkerLayoutKeyFromCategory(s.getCategory()) // ✅ layoutKey
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        DatabaseReference dormRef = FirebaseDatabase.getInstance().getReference("dormitoriesEn");
        dormRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ItemDomain dorm = snap.getValue(ItemDomain.class);
                    if (dorm == null || dorm.getLat() == 0) continue;
                    addDormMarker(
                            dorm.getLat(),
                            dorm.getLng(),
                            dorm.getName(),
                            dorm.getDescription(),
                            dorm.getFloors(),
                            dorm.getSupervisors(),
                            dorm.getImagePath(),
                            getMarkerLayoutKeyFromCategory("Dorms")
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // For Stores (or default non-dorm categories)
    private void addStoreMarker(double lat, double lng, String name, String address, String hours, String description, String imageUrl, String category, String layoutKey) {
        addStoreMarker(lat, lng, name, address, hours, description, imageUrl, category, null, layoutKey);
    }


    private void addStoreMarker(double lat, double lng, String name, String address, String hours, String description, String imageUrl, String category, String phoneNumber, String layoutKey) {
        int layoutResId = getLayoutId(layoutKey);
        Bitmap iconBitmap = getBitmapFromLayout(layoutResId);

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(name)
                .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));

        if (marker != null) {
            marker.setTag(new MarkerTagData(
                    name,
                    address != null ? address : "Address not available",
                    hours != null ? hours : "Hours not available",
                    description != null ? description : "Description not available",
                    imageUrl,
                    category,  // storeCategory is used as layoutKey here
                    phoneNumber != null ? phoneNumber : "",   // ✅ Add phone number here
                    lat,
                    lng
            ));
        }
    }

    //for Dorm
    private void addDormMarker(double lat, double lng, String name, String description, String floors, String supervisors, String imageUrl, String layoutKey) {
        int layoutResId = getLayoutId(layoutKey);
        Bitmap iconBitmap = getBitmapFromLayout(layoutResId);

        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(name)
                .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));

        if (marker != null) {
            marker.setTag(new MarkerTagData(
                    name,
                    "", // address not used for dorms
                    "", // hours not used for dorms
                    description,
                    imageUrl,
                    "Dorms",
                    floors,
                    supervisors,
                    lat,
                    lng
            ));
        }
    }
    // With this new method:
    private int getLayoutId(String layoutKey) {
        switch (layoutKey) {
            case "coffee_marker":
            case "Coffee":
                return R.layout.coffee_marker;

            case "food_marker":
            case "Restaurant":
                return R.layout.food_marker;

            case "dorm_marker":
            case "Dorms":
                return R.layout.dorm_marker;

            case "facilities_marker":
            case "Facilities":
                return R.layout.facilities_marker;

            case "convenience_marker":
                return R.layout.convenience_marker;

            case "bars_marker": return R.layout.bars_marker;
            case "mart_marker": return R.layout.store_marker;

            case "store_marker":
            case "Stores":
            default:
                return R.layout.store_marker;
        }
    }

    private Bitmap getBitmapFromLayout(int layoutId) {
        View markerView = getLayoutInflater().inflate(layoutId, null);
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);
        return bitmap;
    }


    //overlay for store

    private void showStoreOverlay(String title, String address, String hours, String description, String imageUrl, double storeLat, double storeLng) {
        placeTitle.setText(title != null ? title : "No name");

        // 📝 Prefer showing description if available
        if (description != null && !description.trim().isEmpty()) {
            placeAddress.setText(description.trim());
        } else {
            // 🧹 Fallback: Clean and shorten address
            String fullAddress = address != null ? address.toLowerCase() : "";
            fullAddress = fullAddress.replace("jecheon-si", "")
                    .replace("chungbuk", "")
                    .replace("republic of korea", "")
                    .replace(",", "")
                    .trim();

            String shortAddress = trimToMaxWords(fullAddress, 5);
            placeAddress.setText(shortAddress);
        }

        // ⏰ Format hours if structured
        if (hours != null && !hours.trim().isEmpty()) {
            if (hours.contains("Monday")) {
                placeHours.setText(getString(R.string.open_with_hours, formatOpeningHours(hours)));
            } else {
                placeHours.setText(getString(R.string.open_fallback, hours));
            }
        } else {
            placeHours.setText(getString(R.string.hours_unknown));
        }

        // ✅ Use lat/lng instead of geocoding
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng dest = new LatLng(storeLat, storeLng); //
                    fetchDistanceAndDuration(origin, dest, placeDistance);
                }
            });
        } else {
            placeDistance.setText(""); // Or "Location unavailable"
        }


        // 🖼️ Load image or fallback
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(placeImage);
        } else {
            placeImage.setImageResource(R.drawable.smu_logo);
        }

        Button directionBtn = findViewById(R.id.btnOpenMaps);
        directionBtn.setOnClickListener(v -> {
            showDirectionOptions(new LatLng(storeLat, storeLng), title, address);
        });

        overlayCard.setVisibility(View.VISIBLE);
    }


    //Overlay for Dormitory
    private void showDormOverlay(String name, String description, String floors, String supervisors, String imageUrl, double dormLat, double dormLng) {
        placeTitle.setText(name != null ? name : "Dormitory");

        StringBuilder details = new StringBuilder();
       if (description != null && !description.trim().isEmpty())
           details.append(description).append("\n");

       if (floors != null && !floors.trim().isEmpty())
           details.append("Floors: ").append(floors).append("\n");

       if (supervisors != null && !supervisors.trim().isEmpty())
           details.append("Supervisors: ").append(supervisors);
        placeAddress.setText(details.toString().trim());

       placeHours.setText(getString(R.string.contact_dorm_office));

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(placeImage);
        } else {
            placeImage.setImageResource(R.drawable.smu_logo);
        }

        // 🔁 Distance logic from user's location to dorm coordinates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng dest = new LatLng(dormLat, dormLng);
                    fetchDistanceAndDuration(origin, dest, placeDistance); // ➕ Ensure `placeDistance` TextView exists
                }
            });
        } else {
            placeDistance.setText(""); // Or use "Location unavailable"
        }
        // 🧭 Show direction options
        Button directionBtn = findViewById(R.id.btnOpenMaps);
        directionBtn.setOnClickListener(v -> {
            showDirectionOptions(new LatLng(dormLat, dormLng), name, description);
        });

        overlayCard.setVisibility(View.VISIBLE);
    }
    private String trimToMaxWords(String text, int maxWords) {
        if (text == null) return "";
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) return text;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            result.append(words[i]).append(" ");
        }
        result.append("...");
        return result.toString().trim();
    }

    private void showPlaceOverlay(String name, String address, String hours, String description, String imageUrl, String phoneNumber, double placeLat, double placeLng) {
        placeTitle.setText(name != null ? name : "Store");



        // 📝 Use activity/description first, fallback to address
        if (description != null && !description.trim().isEmpty()) {
            placeAddress.setText(description);
        } else {
            String fullAddress = address != null ? address.toLowerCase() : "";
            fullAddress = fullAddress.replace("jecheon-si", "")
                    .replace("chungbuk", "")
                    .replace("republic of korea", "")
                    .replace(",", "")
                    .trim();
            placeAddress.setText(trimToMaxWords(fullAddress, 5));
        }

        if (hours != null && !hours.trim().isEmpty()) {
            placeHours.setText(getString(R.string.open_with_hours, formatOpeningHours(hours)));
        } else {
            placeHours.setText(getString(R.string.hours_unknown));
        }

        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(placeImage);
        } else {
            placeImage.setImageResource(R.drawable.smu_logo);
        }

        // 📞 Handle call button
        Button callBtn = findViewById(R.id.btnCallStore);
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            callBtn.setVisibility(View.VISIBLE);
            callBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            });
        } else {
            callBtn.setVisibility(View.GONE);
        }

        // 📍 Distance display
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    LatLng dest = new LatLng(placeLat, placeLng);
                    fetchDistanceAndDuration(origin, dest, placeDistance);
                }
            });
        } else {
            placeDistance.setText("");
        }

        Button directionBtn = findViewById(R.id.btnOpenMaps);
        directionBtn.setOnClickListener(v -> {
            showDirectionOptions(new LatLng(placeLat, placeLng), name, address);
        });

        // ❤️ Favorite functionality
        AppCompatButton favoriteBtn = findViewById(R.id.favoriteBtn);
        String safeName = name != null ? name : "Unknown";
        String placeId = (name + "_" + placeLat + "_" + placeLng).replace(".", "_");

        if (favoriteRef == null) {
            Toast.makeText(this, "Favorites not available", Toast.LENGTH_SHORT).show();
            return;
        }

        favoriteRef.child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.filled_heart, 0, 0, 0);
                } else {
                    favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart__fav, 0, 0, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        favoriteBtn.setOnClickListener(v -> {
            favoriteRef.child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        favoriteRef.child(placeId).removeValue();
                        favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart__fav, 0, 0, 0);
                        Toast.makeText(GoogleMapActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> favoriteData = new HashMap<>();
                        favoriteData.put("name", safeName);
                        favoriteData.put("address", address);
                        favoriteData.put("description", description);
                        favoriteData.put("imageUrl", imageUrl);
                        favoriteData.put("category", "Store");
                        favoriteData.put("lat", placeLat);
                        favoriteData.put("lng", placeLng);
                        favoriteData.put("phone", phoneNumber);

                        favoriteRef.child(placeId).setValue(favoriteData);
                        favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.filled_heart, 0, 0, 0);
                        Toast.makeText(GoogleMapActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        overlayCard.setVisibility(View.VISIBLE);
    }


    //Shorten Opening hours with helper function
    private String formatOpeningHours(String raw) {
        // Split by day
        String[] lines = raw.split(",");
        List<String> openDays = new ArrayList<>();
        List<String> closedDays = new ArrayList<>();
        String hours = "";

        for (String line : lines) {
            line = line.trim();
            if (line.contains("Closed")) {
                String day = line.split(":")[0];
                closedDays.add(day);
            } else {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String day = parts[0];
                    String time = line.substring(day.length() + 1).trim();
                    openDays.add(day);
                    hours = time; // Assume all times are the same for simplification
                }
            }
        }

        String openDaysText = !openDays.isEmpty() ? TextUtils.join(", ", openDays) + ": " + hours : "";
        String closedText = !closedDays.isEmpty() ? "Closed: " + TextUtils.join(", ", closedDays) : "";

        return openDaysText + (closedText.isEmpty() ? "" : "\n" + closedText);
    }
    private void hideOverlay() {
        overlayCard.setVisibility(View.GONE);
    }
}