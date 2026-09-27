package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.Domain.AppNotification;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ItemFollowNotificationBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** "Tina started following you · 2h" rows, with a Follow back button. */
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.Viewholder> {

    public interface OnFollowToggle {
        void onToggle(String uid, boolean follow);
    }

    private final List<AppNotification> items = new ArrayList<>();
    private final Set<String> followingUids = new HashSet<>();
    private final Set<String> unreadKeys = new HashSet<>();
    private final OnFollowToggle onFollowToggle;

    public NotificationsAdapter(OnFollowToggle onFollowToggle) {
        this.onFollowToggle = onFollowToggle;
    }

    // unread is remembered from the first load, so rows stay highlighted while this screen is open
    public void setItems(List<AppNotification> newItems) {
        for (AppNotification n : newItems) if (!n.read) unreadKeys.add(n.key);
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setFollowing(Set<String> uids) {
        followingUids.clear();
        followingUids.addAll(uids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(ItemFollowNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        AppNotification n = items.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.notificationRow.setBackgroundColor(unreadKeys.contains(n.key) ? 0x143A86FF : 0);
        holder.binding.notificationTime.setText(n.timestamp > 0 ? TimeUtils.getTimeAgo(n.timestamp) : "");
        holder.binding.notificationText.setText("");
        holder.binding.notificationPic.setImageResource(R.drawable.ic_default_avatar);

        // Name and photo come from the live profile, so renames and new photos show up
        holder.itemView.setTag(n.fromUid);
        FirebaseDatabase.getInstance().getReference("profiles").child(n.fromUid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!n.fromUid.equals(holder.itemView.getTag()) || !holder.itemView.isAttachedToWindow()) return;
                    String name = snapshot.child("profileName").getValue(String.class);
                    if (name == null) name = context.getString(R.string.someone);
                    String text = context.getString(R.string.notif_follow_text, name);
                    SpannableString styled = new SpannableString(text);
                    int start = text.indexOf(name);
                    if (start >= 0) {
                        styled.setSpan(new StyleSpan(Typeface.BOLD), start, start + name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    holder.binding.notificationText.setText(styled);
                    Glide.with(context).load(snapshot.child("profileImage").getValue(String.class))
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .fallback(R.drawable.ic_default_avatar)
                            .into(holder.binding.notificationPic);
                });

        holder.itemView.setOnClickListener(v -> context.startActivity(
                new Intent(context, ProfilePageActivity.class).putExtra(ProfilePageActivity.EXTRA_USER_ID, n.fromUid)));

        boolean following = followingUids.contains(n.fromUid);
        holder.binding.followBackBtn.setVisibility(View.VISIBLE);
        holder.binding.followBackBtn.setText(following ? R.string.following_state : R.string.follow_back);
        holder.binding.followBackBtn.setBackgroundResource(following ? R.drawable.bg_button_outline : R.drawable.rounded_button2);
        holder.binding.followBackBtn.setTextColor(context.getColor(following ? R.color.blue_dark : R.color.white));
        holder.binding.followBackBtn.setOnClickListener(v -> onFollowToggle.onToggle(n.fromUid, !following));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        final ItemFollowNotificationBinding binding;

        Viewholder(ItemFollowNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
