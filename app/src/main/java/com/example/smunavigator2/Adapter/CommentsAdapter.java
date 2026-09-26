package com.example.smunavigator2.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.Domain.Comment;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ItemCommentBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.Viewholder> {

    private final List<Comment> comments = new ArrayList<>();

    public void setComments(List<Comment> newComments) {
        comments.clear();
        comments.addAll(newComments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Comment comment = comments.get(position);
        holder.binding.commentText.setText(comment.text);
        holder.binding.commentTime.setText(comment.timestamp > 0 ? TimeUtils.getTimeAgo(comment.timestamp) : "");
        holder.binding.commentAuthorName.setText("");
        holder.binding.commentAuthorPic.setImageResource(R.drawable.smu_logo);
        if (comment.userId == null) return;

        // Tag the row so a late reply for a recycled row is ignored
        holder.itemView.setTag(comment.userId);
        FirebaseDatabase.getInstance().getReference("profiles").child(comment.userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!comment.userId.equals(holder.itemView.getTag()) || !holder.itemView.isAttachedToWindow()) return;
                    String name = snapshot.child("profileName").getValue(String.class);
                    String image = snapshot.child("profileImage").getValue(String.class);
                    holder.binding.commentAuthorName.setText(name != null ? name : "Unknown");
                    Glide.with(holder.itemView.getContext()).load(image)
                            .placeholder(R.drawable.smu_logo)
                            .into(holder.binding.commentAuthorPic);
                });

        holder.binding.commentAuthorPic.setOnClickListener(v -> v.getContext().startActivity(
                new Intent(v.getContext(), ProfilePageActivity.class)
                        .putExtra(ProfilePageActivity.EXTRA_USER_ID, comment.userId)));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        final ItemCommentBinding binding;

        Viewholder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
