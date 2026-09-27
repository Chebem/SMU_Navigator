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
                return R.drawable.ph_restaurant;
            case "coffee":
                return R.drawable.ph_coffee;
            case "mart":
            case "marts":
                return R.drawable.ph_mart;
            case "convenience":
                return R.drawable.ph_convenience;
            case "accommodation":
            case "dorms":
            case "dormitory":
            case "dormitories":
                return R.drawable.ph_dorm;
            case "bars":
                return R.drawable.ph_bar;
            case "facilities":
                return R.drawable.ph_facilities;
            default:
                return R.drawable.ph_place;
        }
    }

    /** Small round-thumbnail version of {@link #placeholderImage}. */
    public static int placeholderThumb(String category) {
        int cover = placeholderImage(category);
        if (cover == R.drawable.ph_restaurant) return R.drawable.ph_restaurant_thumb;
        if (cover == R.drawable.ph_coffee) return R.drawable.ph_coffee_thumb;
        if (cover == R.drawable.ph_mart) return R.drawable.ph_mart_thumb;
        if (cover == R.drawable.ph_convenience) return R.drawable.ph_convenience_thumb;
        if (cover == R.drawable.ph_dorm) return R.drawable.ph_dorm_thumb;
        if (cover == R.drawable.ph_bar) return R.drawable.ph_bar_thumb;
        if (cover == R.drawable.ph_facilities) return R.drawable.ph_facilities_thumb;
        return R.drawable.ph_place_thumb;
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
