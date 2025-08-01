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

public class NoticeDetailActivity extends AppCompatActivity {

    private WebView webView;
    private ImageButton backBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        webView = findViewById(R.id.noticeWebView);
        backBtn = findViewById(R.id.backBtn);
        progressBar = findViewById(R.id.progressBar); // 👈 Link ProgressBar from XML

        // 🔙 Back Button
        backBtn.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        });

        // 🌍 WebView Setup
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE); // 👈 Show ProgressBar
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE); // 👈 Hide ProgressBar
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 🌐 Open links in external browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        // 📥 Load HTML content
        String title = getIntent().getStringExtra("title");
        String htmlContent = getIntent().getStringExtra("html");
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
