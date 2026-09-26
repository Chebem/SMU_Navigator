package com.example.smunavigator2.Dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smunavigator2.Adapter.CommentsAdapter;
import com.example.smunavigator2.Domain.Comment;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.BottomSheetCommentsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/** Comments for one post: live list, oldest first, plus a box to add one. */
public class CommentsBottomSheet {

    public interface OnCountChanged {
        void onCountChanged(int count);
    }

    public static void show(Context context, String ownerId, String postId, OnCountChanged onCountChanged) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.RoundedBottomSheetDialog);
        BottomSheetCommentsBinding binding = BottomSheetCommentsBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        CommentsAdapter adapter = new CommentsAdapter();
        binding.commentsRecycler.setLayoutManager(new LinearLayoutManager(context));
        binding.commentsRecycler.setAdapter(adapter);

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("profiles")
                .child(ownerId).child("posts").child(postId).child("comments");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Comment comment = child.getValue(Comment.class);
                    if (comment != null && comment.text != null) comments.add(comment);
                }
                comments.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));
                adapter.setComments(comments);
                binding.emptyCommentsTxt.setVisibility(comments.isEmpty() ? View.VISIBLE : View.GONE);
                if (!comments.isEmpty()) binding.commentsRecycler.scrollToPosition(comments.size() - 1);
                onCountChanged.onCountChanged(comments.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
            }
        };
        commentsRef.addValueEventListener(listener);
        dialog.setOnDismissListener(d -> commentsRef.removeEventListener(listener));

        binding.sendCommentBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String text = binding.commentInput.getText().toString().trim();
            if (user == null || text.isEmpty()) return;

            binding.sendCommentBtn.setEnabled(false);
            commentsRef.push().setValue(new Comment(user.getUid(), text, System.currentTimeMillis()))
                    .addOnCompleteListener(task -> {
                        binding.sendCommentBtn.setEnabled(true);
                        if (task.isSuccessful()) {
                            binding.commentInput.setText("");
                        } else {
                            Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Keep the comment box above the keyboard
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
    }
}
