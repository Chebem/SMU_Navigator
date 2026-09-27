package com.example.smunavigator2.Utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.example.smunavigator2.Activity.GoogleMapActivity;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.R;

import java.util.Locale;

/** City Guide places: category images, map markers and "show on map". */
public final class PlaceUtils {

    private PlaceUtils() {
    }

    /** Image shown when a place has no photo of its own. */
    public static int placeholderImage(String category) {
        switch (normalize(category)) {
            case "restaurant":
            case "restaurants":
                return R.drawable.food_placehlolder;
            case "coffee":
                return R.drawable.coffee_placehlolder;
            case "mart":
            case "marts":
                return R.drawable.shop_placeholder;
            case "convenience":
                return R.drawable.convenience_placehlolder;
            case "accommodation":
            case "dorms":
            case "dormitory":
            case "dormitories":
                return R.drawable.accommodation_placehlolder;
            case "bars":
                return R.drawable.bar_placehlolder;
            case "facilities":
                return R.drawable.facilties;
            default:
                return R.drawable.placeholder_image;
        }
    }

    /** Marker layout key GoogleMapActivity uses for this category. */
    public static String markerLayout(String category) {
        switch (normalize(category)) {
            case "coffee":
                return "coffee_marker";
            case "restaurant":
            case "restaurants":
                return "food_marker";
            case "dorms":
            case "accommodation":
            case "dormitory":
            case "dormitories":
                return "dorm_marker";
            case "facilities":
                return "facilities_marker";
            case "convenience":
                return "convenience_marker";
            case "bars":
                return "bars_marker";
            case "mart":
            case "marts":
                return "mart_marker";
            default:
                return "store_marker";
        }
    }

    public static Intent mapIntent(Context context, StoreModel store) {
        return new Intent(context, GoogleMapActivity.class)
                .putExtra("storeLat", store.getLat())
                .putExtra("storeLng", store.getLng())
                .putExtra("storeName", store.getName())
                .putExtra("storeAddress", store.getAddress())
                .putExtra("storeHours", openingHours(store, "Opening hours not available"))
                .putExtra("storeImage", store.getImagePath())
                .putExtra("storeCategory", store.getCategory())
                .putExtra("storeDescription", store.getActivity() != null ? store.getActivity() : "")
                .putExtra("markerLayout", markerLayout(store.getCategory()));
    }

    public static String openingHours(StoreModel store, String fallback) {
        return store.getOpening_hours() != null && !store.getOpening_hours().isEmpty()
                ? TextUtils.join(", ", store.getOpening_hours()) : fallback;
    }

    private static String normalize(String category) {
        return category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
    }
}
