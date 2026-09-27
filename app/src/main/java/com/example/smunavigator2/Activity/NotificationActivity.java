package com.example.smunavigator2.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smunavigator2.Adapter.NotificationsAdapter;
import com.example.smunavigator2.Domain.AppNotification;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.FollowUtils;
import com.example.smunavigator2.databinding.ActivityNotificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** notifications opening this screen marks them read. */
public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private NotificationsAdapter adapter;
    private String myUid;

    private DatabaseReference notificationsRef;
    private ValueEventListener notificationsListener;
    private DatabaseReference followingRef;
    private ValueEventListener followingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.backBtn.setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        myUid = user.getUid();

        adapter = new NotificationsAdapter((uid, follow) ->
                FollowUtils.setFollowing(myUid, uid, follow).addOnFailureListener(e ->
                        Toast.makeText(this, R.string.follow_failed, Toast.LENGTH_SHORT).show()));
        binding.notificationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.notificationsRecycler.setAdapter(adapter);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        notificationsRef = db.child("notifications").child(myUid);
        notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.notificationsProgress.setVisibility(View.GONE);
                List<AppNotification> items = new ArrayList<>();
                Map<String, Object> markRead = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    AppNotification n;
                    try {
                        n = child.getValue(AppNotification.class);
                    } catch (Exception e) {
                        continue; // skip anything malformed
                    }
                    if (n == null || n.fromUid == null) continue;
                    n.key = child.getKey();
                    items.add(n);
                    if (!n.read) markRead.put(n.key + "/read", true);
                }
                items.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
                adapter.setItems(items);
                binding.emptyMessage.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

                // Seen now: clears the red dot on the home bell
                if (!markRead.isEmpty()) notificationsRef.updateChildren(markRead);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.notificationsProgress.setVisibility(View.GONE);
                Toast.makeText(NotificationActivity.this, R.string.action_failed, Toast.LENGTH_SHORT).show();
            }
        };
        notificationsRef.addValueEventListener(notificationsListener);

        // Keep Follow back / Following in sync
        followingRef = db.child("profiles").child(myUid).child("following");
        followingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uids = new HashSet<>();
                for (DataSnapshot child : snapshot.getChildren()) uids.add(child.getKey());
                adapter.setFollowing(uids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        followingRef.addValueEventListener(followingListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsRef != null) notificationsRef.removeEventListener(notificationsListener);
        if (followingRef != null) followingRef.removeEventListener(followingListener);
    }
}
