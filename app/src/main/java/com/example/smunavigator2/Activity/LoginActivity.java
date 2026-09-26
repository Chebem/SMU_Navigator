package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ActivityLoginBinding;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //  App Check with Play Integrity
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );

        auth = FirebaseAuth.getInstance();

        // Auto-login if already signed in
        if (auth.getCurrentUser() != null) {
            checkUserProfile(auth.getCurrentUser().getUid());
            return;
        }

        //  Login button
        binding.loginBtn.setOnClickListener(v -> doLogin());

        //  Forgot password: send a reset link to the email typed above
        binding.forgotPasswordLink.setOnClickListener(v -> sendPasswordReset());

        //  Sign-up navigation
        binding.signInLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });


    }

    private void doLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError("Invalid email");
            return;
        }
        if (password.length() < 6) {
            binding.passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        binding.loginBtn.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                checkUserProfile(uid);
            } else {
                Toast.makeText(this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                binding.loginBtn.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sendPasswordReset() {
        String email = binding.emailInput.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError(getString(R.string.reset_enter_email));
            binding.emailInput.requestFocus();
            return;
        }

        binding.forgotPasswordLink.setEnabled(false);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            binding.forgotPasswordLink.setEnabled(true);
            // Same message whether or not the account exists, so emails can't be probed
            int message = task.isSuccessful() || task.getException() instanceof FirebaseAuthInvalidUserException
                    ? R.string.reset_email_sent : R.string.reset_email_failed;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void checkUserProfile(String uid) {
        DatabaseReference profilesRef = FirebaseDatabase.getInstance().getReference("profiles");

        profilesRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar.setVisibility(View.GONE); // Hide loading
                binding.loginBtn.setEnabled(true);

                Intent intent;
                if (snapshot.exists()) {
                    intent = new Intent(LoginActivity.this, ProfilePageActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                }

                // Apply a fade transition
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.loginBtn.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Failed to check profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}