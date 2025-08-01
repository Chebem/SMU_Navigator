package com.example.smunavigator2.Adapter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ViewholderPostBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.Viewholder> {

    private final List<Post> postList;
    private final OnPostClickListener clickListener;

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
        holder.binding.postTimeTxt.setText(TimeUtils.getTimeAgo(post.getTimestamp()));

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

        // 🔹 Post click handler
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onPostClick(post);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderPostBinding binding;

        public Viewholder(ViewholderPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}