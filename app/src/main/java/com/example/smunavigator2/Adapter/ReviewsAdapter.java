package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.Domain.Review;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.ModerationUtils;
import com.example.smunavigator2.Utils.TimeUtils;
import com.example.smunavigator2.databinding.ItemReviewBinding;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/** Reviews on a place's detail page. Your own: edit / delete. Others': report / block. */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.Viewholder> {

    public interface OnEditOwn {
        void onEdit(Review review);
    }

    private final List<Review> reviews = new ArrayList<>();
    private final String myUid;
    private final String placeKey;
    private final OnEditOwn onEditOwn;

    public ReviewsAdapter(String myUid, String placeKey, OnEditOwn onEditOwn) {
        this.myUid = myUid;
        this.placeKey = placeKey;
        this.onEditOwn = onEditOwn;
    }

    public void setReviews(List<Review> newReviews) {
        reviews.clear();
        reviews.addAll(newReviews);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(ItemReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Review review = reviews.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.reviewStars.setRating(review.rating);
        holder.binding.reviewText.setText(review.text);
        holder.binding.reviewText.setVisibility(TextUtils.isEmpty(review.text) ? View.GONE : View.VISIBLE);
        holder.binding.reviewTime.setText(review.timestamp > 0 ? TimeUtils.getTimeAgo(review.timestamp) : "");
        holder.binding.reviewAuthorName.setText("");
        holder.binding.reviewAuthorPic.setImageResource(R.drawable.ic_default_avatar);

        String[] authorName = {null};
        holder.itemView.setTag(review.userId);
        FirebaseDatabase.getInstance().getReference("profiles").child(review.userId).get()
                .addOnSuccessListener(snapshot -> {
                    if (!review.userId.equals(holder.itemView.getTag()) || !holder.itemView.isAttachedToWindow()) return;
                    String name = snapshot.child("profileName").getValue(String.class);
                    authorName[0] = name;
                    holder.binding.reviewAuthorName.setText(name != null ? name : context.getString(R.string.someone));
                    Glide.with(context).load(snapshot.child("profileImage").getValue(String.class))
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .fallback(R.drawable.ic_default_avatar)
                            .into(holder.binding.reviewAuthorPic);
                });

        holder.binding.reviewAuthorPic.setOnClickListener(v -> context.startActivity(
                new Intent(context, ProfilePageActivity.class).putExtra(ProfilePageActivity.EXTRA_USER_ID, review.userId)));

        boolean mine = review.userId.equals(myUid);
        holder.binding.reviewMoreBtn.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, v);
            if (mine) {
                menu.getMenu().add(0, 1, 0, R.string.edit);
                menu.getMenu().add(0, 2, 1, R.string.delete);
            } else {
                menu.getMenu().add(0, 3, 0, R.string.report);
                menu.getMenu().add(0, 4, 1, R.string.block);
            }
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: onEditOwn.onEdit(review); return true;
                    case 2:
                        FirebaseDatabase.getInstance().getReference("reviews").child(placeKey).child(myUid).removeValue();
                        return true;
                    case 3:
                        ModerationUtils.report(context, ModerationUtils.TYPE_REVIEW,
                                "reviews/" + placeKey + "/" + review.userId, review.userId);
                        return true;
                    case 4:
                        ModerationUtils.confirmBlock(context, review.userId, authorName[0], null);
                        return true;
                }
                return false;
            });
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        final ItemReviewBinding binding;

        Viewholder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
