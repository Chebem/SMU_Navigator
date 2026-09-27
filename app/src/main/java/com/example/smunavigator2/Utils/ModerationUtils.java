package com.example.smunavigator2.Utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.smunavigator2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Report and block (required by Google Play for user posts, comments and reviews).
 * Reports go to reports/{id} (read only in the Firebase console); blocks live at blocks/{me}/{them}.
 */
public final class ModerationUtils {

    public static final String TYPE_POST = "post";
    public static final String TYPE_COMMENT = "comment";
    public static final String TYPE_REVIEW = "review";
    public static final String TYPE_USER = "user";

    private static final Set<String> blocked = new HashSet<>();
    private static final CopyOnWriteArrayList<Runnable> blockListeners = new CopyOnWriteArrayList<>();
    private static String watchedUid;

    private ModerationUtils() {
    }

    /** Start keeping the blocked list in sync for the signed-in user (safe to call repeatedly). */
    public static void watchBlocked() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid().equals(watchedUid)) return;
        watchedUid = user.getUid();
        FirebaseDatabase.getInstance().getReference("blocks").child(watchedUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        blocked.clear();
                        for (DataSnapshot child : snapshot.getChildren()) blocked.add(child.getKey());
                        for (Runnable r : blockListeners) r.run();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static boolean isBlocked(String uid) {
        return uid != null && blocked.contains(uid);
    }

    /** Called whenever the blocked list changes, so screens can hide content right away. */
    public static void addBlockListener(Runnable r) {
        blockListeners.add(r);
    }

    public static void removeBlockListener(Runnable r) {
        blockListeners.remove(r);
    }

    // ---- Report ----

    /** Ask for a reason, then file the report. {@code path} is where the content lives in the database. */
    public static void report(Context context, String type, String path, String ownerUid) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) return;
        String[] reasons = context.getResources().getStringArray(R.array.report_reasons);
        String[] codes = {"spam", "harassment", "inappropriate", "other"};
        new AlertDialog.Builder(context)
                .setTitle(R.string.report_title)
                .setItems(reasons, (d, which) -> {
                    Map<String, Object> report = new HashMap<>();
                    report.put("type", type);
                    report.put("path", path);
                    report.put("ownerUid", ownerUid);
                    report.put("reason", codes[which]);
                    report.put("reporterUid", me.getUid());
                    report.put("timestamp", System.currentTimeMillis());
                    FirebaseDatabase.getInstance().getReference("reports").push().setValue(report)
                            .addOnSuccessListener(r -> Toast.makeText(context, R.string.report_sent, Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ---- Block ----

    /** Confirm, then block: hides their content for you and removes follows between you. */
    public static void confirmBlock(Context context, String uid, String name, Runnable onBlocked) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || uid == null || uid.equals(me.getUid())) return;
        String who = name != null && !name.isEmpty() ? name : context.getString(R.string.someone);
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.block_title, who))
                .setMessage(R.string.block_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.block, (d, w) -> {
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("blocks/" + me.getUid() + "/" + uid, true);
                    // Follows you can remove yourself: you -> them, and them in your followers list
                    updates.put("profiles/" + me.getUid() + "/following/" + uid, null);
                    updates.put("profiles/" + uid + "/followers/" + me.getUid(), null);
                    updates.put("profiles/" + me.getUid() + "/followers/" + uid, null);
                    db.updateChildren(updates)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(context, context.getString(R.string.blocked_user, who), Toast.LENGTH_SHORT).show();
                                if (onBlocked != null) onBlocked.run();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    public static void unblock(Context context, String uid) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) return;
        FirebaseDatabase.getInstance().getReference("blocks").child(me.getUid()).child(uid).removeValue()
                .addOnSuccessListener(r -> Toast.makeText(context, R.string.unblocked, Toast.LENGTH_SHORT).show());
    }
}
