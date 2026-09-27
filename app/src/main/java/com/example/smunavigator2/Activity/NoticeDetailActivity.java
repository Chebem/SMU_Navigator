package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.DistanceUtils;
import com.google.firebase.database.FirebaseDatabase;

public class NoticeDetailActivity extends AppCompatActivity {

    /** Intent extra: notice key under Notices/, used when opened from a push */
    public static final String EXTRA_NOTICE_ID = "noticeId";

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        webView = findViewById(R.id.noticeWebView);
        ImageButton backBtn = findViewById(R.id.backBtn);
        progressBar = findViewById(R.id.progressBar); // ProgressBar from XML

        // 🔙 Back Button
        backBtn.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        });

        // WebView Setup
        // Notice HTML is scraped from an external site: never run its scripts
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setAllowFileAccess(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE); // Show ProgressBar
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE); // Hide ProgressBar
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 🌐 Open links in external browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        // Opened from a push: only the notice ID is known, so load it first
        String noticeId = getIntent().getStringExtra(EXTRA_NOTICE_ID);
        if (getIntent().getStringExtra("html") == null && noticeId != null) {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference("Notices").child(noticeId).get()
                    .addOnSuccessListener(snap -> {
                        if (isFinishing() || isDestroyed()) return;
                        boolean english = DistanceUtils.isEnglishLocale();
                        String title = snap.child(english ? "title_en" : "title_ko").getValue(String.class);
                        String html = snap.child(english ? "html_en" : "html_ko").getValue(String.class);
                        if (html == null) html = snap.child("html_ko").getValue(String.class);
                        showNotice(title, html != null ? html : "");
                    })
                    .addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
            return;
        }

        showNotice(getIntent().getStringExtra("title"), getIntent().getStringExtra("html"));
    }

    private void showNotice(String title, String htmlContent) {
        setTitle(title);

        String wrappedHtml = "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: sans-serif; padding: 16px; color: #333; }" +
                "img { max-width: 100%; height: auto; display: block; margin: 12px 0; }" +
                "table { width: 100%; border-collapse: collapse; }" +
                "td, th { border: 1px solid #ccc; padding: 8px; }" +
                "a { color: #1a73e8; text-decoration: none; }" +
                "</style></head><body>" +
                htmlContent +
                "</body></html>";

        webView.loadDataWithBaseURL("https://www.semyung.ac.kr/", wrappedHtml, "text/html", "UTF-8", null);
    }
}
