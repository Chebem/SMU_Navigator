package com.example.smunavigator2.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.Domain.Comment;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.ModerationUtils;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ItemCommentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.Viewholder> {

    private final List<Comment> comments = new ArrayList<>();
    private String commentsPath; // profiles/{owner}/posts/{postId}/comments, for delete and report

    public void setCommentsPath(String path) {
        this.commentsPath = path;
    }

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
        holder.binding.commentAuthorPic.setImageResource(R.drawable.ic_default_avatar);
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
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .fallback(R.drawable.ic_default_avatar)
                            .into(holder.binding.commentAuthorPic);
                });

        // Long-press: delete your own comment, or report / block someone else's
        holder.itemView.setOnLongClickListener(v -> {
            FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
            if (me == null || commentsPath == null || comment.key == null) return false;
            boolean mine = comment.userId.equals(me.getUid());
            String path = commentsPath + "/" + comment.key;
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            if (mine) {
                menu.getMenu().add(0, 1, 0, R.string.delete);
            } else {
                menu.getMenu().add(0, 2, 0, R.string.report);
                menu.getMenu().add(0, 3, 1, R.string.block);
            }
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    FirebaseDatabase.getInstance().getReference(path).removeValue();
                } else if (item.getItemId() == 2) {
                    ModerationUtils.report(v.getContext(), ModerationUtils.TYPE_COMMENT, path, comment.userId);
                } else {
                    ModerationUtils.confirmBlock(v.getContext(), comment.userId,
                            holder.binding.commentAuthorName.getText().toString(), null);
                }
                return true;
            });
            menu.show();
            return true;
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
