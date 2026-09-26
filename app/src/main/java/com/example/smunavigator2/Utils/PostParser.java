package com.example.smunavigator2.Utils;

import android.util.Log;

import com.example.smunavigator2.Domain.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;

public final class PostParser {

    private PostParser() {
    }

    /** One malformed post (e.g. a number saved as text) is skipped instead of crashing the screen. */
    public static Post parse(DataSnapshot postSnap) {
        try {
            return postSnap.getValue(Post.class);
        } catch (DatabaseException e) {
            Log.w("PostParser", "Skipping malformed post " + postSnap.getKey(), e);
            return null;
        }
    }
}
