package com.example.smunavigator2.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.Post;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ItemSocialPostBinding;

import java.util.List;

public class SocialPostAdapter extends RecyclerView.Adapter<SocialPostAdapter.SocialPostViewHolder> {

    private final List<Post> postList;

    public SocialPostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public SocialPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSocialPostBinding binding = ItemSocialPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SocialPostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SocialPostViewHolder holder, int position) {
        Post post = postList.get(position);

        // 🖼 Load post image
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder)
                    .into(holder.binding.postImage);
        }

        // 👤 Profile image and username (static placeholder or fetched from Firebase separately)
        Glide.with(holder.itemView.getContext())
                .load(R.drawable.smu_logo)
                .into(holder.binding.profilePic);

        holder.binding.usernameTxt.setText("User"); // Set real username if fetched

        // 📝 Set caption and time
        holder.binding.captionTxt.setText(post.getCaption());
        holder.binding.postTimeTxt.setText(TimeUtils.getTimeAgo(post.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class SocialPostViewHolder extends RecyclerView.ViewHolder {
        ItemSocialPostBinding binding;

        public SocialPostViewHolder(ItemSocialPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}