package com.example.smunavigator2.Adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.Dialog.CommentsBottomSheet;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ViewholderPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.Viewholder> {



    private final List<Post> postList;
    private final OnPostClickListener clickListener;
    private final Map<String, String> nameCache = new HashMap<>();
    private final Map<String, String> imageCache = new HashMap<>();

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostsAdapter(List<Post> list, OnPostClickListener listener) {
        this.postList = (list != null) ? list : new ArrayList<>();
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPostBinding binding = ViewholderPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Post post = postList.get(position);

        // 🔹 Use imageUrls or fallback to mainImage
        List<String> urls = post.getImageUrls();
        if ((urls == null || urls.isEmpty()) && post.getMainImage() != null) {
            urls = new ArrayList<>();
            urls.add(post.getMainImage());
        }

        // 🔹 Set up ViewPager with images
        if (urls != null && !urls.isEmpty()) {
            PostImagePagerAdapter pagerAdapter = new PostImagePagerAdapter(urls);
            holder.binding.postImagePager.setAdapter(pagerAdapter);
            holder.binding.dotsIndicator.setViewPager2(holder.binding.postImagePager);
        }

        // 🔹 Set caption and timestamp
        holder.binding.captionText.setText(post.getCaption());
        // Old posts have no timestamp (0 = 1970), so hide the time instead of "20722 days ago"
        if (post.getTimestamp() > 0) {
            holder.binding.postTimeTxt.setVisibility(View.VISIBLE);
            holder.binding.postTimeTxt.setText(TimeUtils.getTimeAgo(post.getTimestamp()));
        } else {
            holder.binding.postTimeTxt.setVisibility(View.GONE);
        }

        // 🔹 Load user profile data
        String userId = post.getUserId();
        if (userId != null && !userId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("profiles").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("profileName").getValue(String.class);
                    String profileImage = snapshot.child("profileImage").getValue(String.class);

                    holder.binding.usernameTxt.setText(username != null ? username : "Unknown");

                    // The screen may have closed while this loaded; Glide crashes on a destroyed activity
                    if (!holder.itemView.isAttachedToWindow()) return;
                    Glide.with(holder.itemView.getContext())
                            .load(profileImage)
                            .placeholder(R.drawable.smu_logo)
                            .error(R.drawable.image_error)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("GlideError", "Profile image load failed", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(holder.binding.profilePic);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostsAdapter", "Failed to load user info", error.toException());
                }
            });
        }

        // Tap the author's photo or name -> their profile
        if (userId != null && !userId.isEmpty()) {
            View.OnClickListener openProfile = v -> {
                Intent intent = new Intent(v.getContext(), ProfilePageActivity.class);
                intent.putExtra(ProfilePageActivity.EXTRA_USER_ID, userId);
                v.getContext().startActivity(intent);
            };
            holder.binding.profilePic.setOnClickListener(openProfile);
            holder.binding.usernameTxt.setOnClickListener(openProfile);
        }

        bindLikes(holder, post);

        // Comments: count now, full list + reply box in a sheet
        holder.binding.commentsText.setText(holder.itemView.getResources()
                .getQuantityString(R.plurals.comments_count, post.getComments().size(), post.getComments().size()));
        holder.binding.commentBtn.setOnClickListener(v -> {
            if (post.getUserId() == null || post.getPostId() == null) return;
            CommentsBottomSheet.show(v.getContext(), post.getUserId(), post.getPostId(), count -> {
                if (holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;
                holder.binding.commentsText.setText(v.getResources()
                        .getQuantityString(R.plurals.comments_count, count, count));
            });
        });

        // 🔹 Post click handler
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onPostClick(post);
        });
    }

    // Like / unlike: likes/{myUid} = true under the post; count and heart update right away
    private void bindLikes(@NonNull Viewholder holder, Post post) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Boolean> likes = post.getLikes();
        showLikeState(holder, me != null && likes.containsKey(me.getUid()), likes.size());

        holder.binding.likeBtn.setOnClickListener(v -> {
            if (me == null || post.getUserId() == null || post.getPostId() == null) return;
            String uid = me.getUid();
            boolean nowLiked = !likes.containsKey(uid);
            if (nowLiked) likes.put(uid, true); else likes.remove(uid);
            showLikeState(holder, nowLiked, likes.size());

            DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("profiles")
                    .child(post.getUserId()).child("posts").child(post.getPostId()).child("likes").child(uid);
            (nowLiked ? likeRef.setValue(true) : likeRef.removeValue()).addOnFailureListener(e -> {
                // Undo the optimistic change
                if (nowLiked) likes.remove(uid); else likes.put(uid, true);
                if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                    showLikeState(holder, !nowLiked, likes.size());
                }
                Toast.makeText(v.getContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showLikeState(@NonNull Viewholder holder, boolean liked, int count) {
        holder.binding.likeIcon.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.binding.likeCountTxt.setText(String.valueOf(count));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updatePosts(List<Post> newList) {
        postList.clear();
        if (newList != null) postList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderPostBinding binding;

        public Viewholder(ViewholderPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}