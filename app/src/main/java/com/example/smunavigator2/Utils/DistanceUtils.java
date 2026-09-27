package com.example.smunavigator2.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Locale;

public final class DistanceUtils {

    // Semyung University main campus (Google Places, 2026-09-26)
    public static final double CAMPUS_LAT = 37.1732056;
    public static final double CAMPUS_LNG = 128.194364;

    // Further than this from campus (e.g. at home, on a bus) measure from campus instead
    private static final float FAR_FROM_CAMPUS_METERS = 20_000f;

    public interface OriginCallback {
        void onOrigin(double lat, double lng, boolean fromUser);
    }

    private DistanceUtils() {}

    public static float meters(double fromLat, double fromLng, double toLat, double toLng) {
        float[] result = new float[1];
        Location.distanceBetween(fromLat, fromLng, toLat, toLng, result);
        return result[0];
    }

    /** "850 m" / "1.2 km" */
    public static String format(float meters) {
        if (meters < 1000) {
            return String.format(Locale.getDefault(), "%d m", Math.round(meters / 10f) * 10);
        }
        return String.format(Locale.getDefault(), "%.1f km", meters / 1000f);
    }

    /** "850 m from you" / "1.2 km from campus" */
    public static String label(float meters, boolean fromUser, boolean isEnglish) {
        String distance = format(meters);
        if (isEnglish) return distance + (fromUser ? " from you" : " from campus");
        return (fromUser ? "내 위치에서 " : "캠퍼스에서 ") + distance;
    }

    public static boolean isEnglishLocale() {
        return !"ko".equals(Locale.getDefault().getLanguage());
    }

    /**
     * Student's current location when available and near Jecheon, otherwise the campus.
     * Uses the last known location, and asks for a fresh one when there is none.
     */
    public static void resolveOrigin(Activity activity, OriginCallback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onOrigin(CAMPUS_LAT, CAMPUS_LNG, false);
            return;
        }

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(activity);
        try {
            client.getLastLocation().addOnCompleteListener(activity, last -> {
                if (last.isSuccessful() && last.getResult() != null) {
                    deliver(last.getResult(), callback);
                    return;
                }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnCompleteListener(activity, current -> deliver(
                                current.isSuccessful() ? current.getResult() : null, callback));
            });
        } catch (SecurityException e) {
            callback.onOrigin(CAMPUS_LAT, CAMPUS_LNG, false);
        }
    }

    private static void deliver(Location location, OriginCallback callback) {
        if (location == null
                || meters(location.getLatitude(), location.getLongitude(), CAMPUS_LAT, CAMPUS_LNG) > FAR_FROM_CAMPUS_METERS) {
            callback.onOrigin(CAMPUS_LAT, CAMPUS_LNG, false);
        } else {
            callback.onOrigin(location.getLatitude(), location.getLongitude(), true);
        }
    }
}
