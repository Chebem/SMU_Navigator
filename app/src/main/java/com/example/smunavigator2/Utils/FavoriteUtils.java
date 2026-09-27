package com.example.smunavigator2.Utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

/** Favorites at favorites/{uid}/{key}, shared by the map panel and the detail page. */
public final class FavoriteUtils {

    private FavoriteUtils() {
    }

    // Same key everywhere, so a place saved on the map shows as saved on its detail page too
    public static String key(String name, double lat, double lng) {
        return (name + "_" + lat + "_" + lng).replaceAll("[.#$\\[\\]/]", "_");
    }

    public static DatabaseReference ref(String uid, String key) {
        return FirebaseDatabase.getInstance().getReference("favorites").child(uid).child(key);
    }

    /** Saves when {@code data} is given, removes when it's null. */
    public static Task<Void> set(String uid, String key, Map<String, Object> data) {
        return data != null ? ref(uid, key).setValue(data) : ref(uid, key).removeValue();
    }
}
