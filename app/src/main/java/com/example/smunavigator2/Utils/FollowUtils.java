package com.example.smunavigator2.Utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/** Follow and unfollow, used by the profile page and the people lists. */
public final class FollowUtils {

    private FollowUtils() {
    }

    // Writes both sides at once: profiles/{them}/followers/{me} and profiles/{me}/following/{them}
    public static Task<Void> setFollowing(String myUid, String theirUid, boolean follow) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        String followerPath = "profiles/" + theirUid + "/followers/" + myUid;
        String followingPath = "profiles/" + myUid + "/following/" + theirUid;

        if (!follow) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(followerPath, null);
            updates.put(followingPath, null);
            return db.updateChildren(updates);
        }

        // The followers entry keeps my name and photo so their list can show me without another read
        return db.child("profiles").child(myUid).get().continueWithTask(me -> {
            DataSnapshot mine = me.isSuccessful() ? me.getResult() : null;
            Map<String, Object> follower = new HashMap<>();
            follower.put("name", mine != null ? mine.child("profileName").getValue(String.class) : null);
            follower.put("imageUrl", mine != null ? mine.child("profileImage").getValue(String.class) : null);

            Map<String, Object> updates = new HashMap<>();
            updates.put(followerPath, follower);
            updates.put(followingPath, true);
            return db.updateChildren(updates);
        });
    }
}
