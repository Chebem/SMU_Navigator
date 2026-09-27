package com.example.smunavigator2.Service;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.smunavigator2.Activity.SplashActivity;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.PushUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Pushes that arrive while the app is open (Android shows the others itself, and taps
 * then open SplashActivity with the data as extras, which routes them the same way).
 */
public class AppMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) PushUtils.saveToken(user.getUid(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        RemoteMessage.Notification n = message.getNotification();
        if (n == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        PushUtils.createChannel(this);
        Map<String, String> data = message.getData();
        Intent tap = new Intent(this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        for (Map.Entry<String, String> e : data.entrySet()) tap.putExtra(e.getKey(), e.getValue());
        PendingIntent pending = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), tap,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this, R.color.blue_dark))
                .setContentTitle(localized(n.getTitle(), n.getTitleLocalizationKey(), n.getTitleLocalizationArgs()))
                .setContentText(localized(n.getBody(), n.getBodyLocalizationKey(), n.getBodyLocalizationArgs()))
                .setAutoCancel(true)
                .setContentIntent(pending);
        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    // The follow push sends a string resource name (e.g. notif_follow_body) so it shows in the phone's language
    private String localized(String text, String key, String[] args) {
        if (key != null) {
            int id = getResources().getIdentifier(key, "string", getPackageName());
            if (id != 0) return args != null ? getString(id, (Object[]) args) : getString(id);
        }
        return text;
    }
}
