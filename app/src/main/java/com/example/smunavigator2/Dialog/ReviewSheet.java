package com.example.smunavigator2.Dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.smunavigator2.Domain.Review;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.BottomSheetReviewBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/** Write or edit your review of a place (stars required, text optional). */
public class ReviewSheet {

    public static void show(Context context, String placeKey, Review existing) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.RoundedBottomSheetDialog);
        BottomSheetReviewBinding binding = BottomSheetReviewBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("reviews")
                .child(placeKey).child(user.getUid());

        if (existing != null) {
            binding.reviewSheetTitle.setText(R.string.edit_review);
            binding.reviewRatingInput.setRating(existing.rating);
            binding.reviewTextInput.setText(existing.text);
            binding.deleteReviewBtn.setVisibility(View.VISIBLE);
            binding.deleteReviewBtn.setOnClickListener(v -> ref.removeValue().addOnCompleteListener(t -> dialog.dismiss()));
        }

        binding.submitReviewBtn.setOnClickListener(v -> {
            int rating = Math.round(binding.reviewRatingInput.getRating());
            if (rating < 1) {
                Toast.makeText(context, R.string.review_pick_stars, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = binding.reviewTextInput.getText().toString().trim();
            binding.submitReviewBtn.setEnabled(false);
            ref.setValue(new Review(user.getUid(), rating, text, System.currentTimeMillis()))
                    .addOnSuccessListener(r -> {
                        Toast.makeText(context, R.string.review_posted, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        binding.submitReviewBtn.setEnabled(true);
                        Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
                    });
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
    }
}
