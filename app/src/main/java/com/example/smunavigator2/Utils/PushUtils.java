package com.example.smunavigator2.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.smunavigator2.Activity.NoticeDetailActivity;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

/** Push setup shared by the app and AppMessagingService. */
public final class PushUtils {

    public static final String TOPIC_NOTICES = "notices"; // the notice scraper sends to this topic
    public static final String TYPE_FOLLOW = "follow";

    private static final String TAG = "PushUtils";

    private PushUtils() {
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
                context.getString(R.string.notification_channel_id),
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    // After sign-in: get notice pushes, and save this device's token so the server can reach this user
    public static void registerDevice(String uid) {
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        messaging.subscribeToTopic(TOPIC_NOTICES)
                .addOnFailureListener(e -> Log.w(TAG, "Topic subscribe failed", e));
        messaging.getToken().addOnSuccessListener(token -> saveToken(uid, token));
    }

    public static void saveToken(String uid, String token) {
        if (uid == null || token == null) return;
        FirebaseDatabase.getInstance().getReference("fcmTokens").child(uid).child(token).setValue(true)
                .addOnFailureListener(e -> Log.w(TAG, "Saving token failed", e));
    }

    // Before sign-out: stop follow pushes for this account on this device
    public static Task<Void> unregisterDevice(String uid) {
        return FirebaseMessaging.getInstance().getToken().continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) return Tasks.forResult(null);
            return FirebaseDatabase.getInstance().getReference("fcmTokens")
                    .child(uid).child(task.getResult()).removeValue();
        });
    }

    /** Where a tapped notification should go, from its data payload; null if it has no target. */
    @Nullable
    public static Intent targetIntent(Context context, Map<String, String> data) {
        String noticeId = data.get("noticeId");
        if (noticeId != null) {
            return new Intent(context, NoticeDetailActivity.class)
                    .putExtra(NoticeDetailActivity.EXTRA_NOTICE_ID, noticeId);
        }
        String fromUid = data.get("fromUid");
        if (TYPE_FOLLOW.equals(data.get("type")) && fromUid != null) {
            return new Intent(context, ProfilePageActivity.class)
                    .putExtra(ProfilePageActivity.EXTRA_USER_ID, fromUid);
        }
        return null;
    }
}
