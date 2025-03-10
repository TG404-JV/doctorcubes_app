package com.android.doctorcube;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PdfViewerActivity extends AppCompatActivity {

    private WebView pdfWebView;
    private ProgressBar progressBar;
    private TextView titleTextView, descriptionTextView, sizeTextView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        sizeTextView = findViewById(R.id.sizeTextView);
        pdfWebView = findViewById(R.id.pdfWebView);
        progressBar = findViewById(R.id.progressBar);

        // Get data from Intent
        String pdfTitle = getIntent().getStringExtra("pdfTitle");
        String pdfDescription = getIntent().getStringExtra("pdfDescription");
        String pdfSize = getIntent().getStringExtra("pdfSize");
        String pdfFilePath = getIntent().getStringExtra("pdfFilePath");

        // Set UI values
        titleTextView.setText(pdfTitle);
        descriptionTextView.setText(pdfDescription);
        sizeTextView.setText(pdfSize);

        // Load PDF if path exists
        if (pdfFilePath != null) {
            loadPdf(pdfFilePath);
        }
    }

    private void loadPdf(String pdfUrl) {
        progressBar.setVisibility(View.VISIBLE);

        pdfWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        pdfWebView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = pdfWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // Load PDF using Google Docs Viewer
        String pdfViewerUrl = "https://drive.google.com/viewerng/viewer?embedded=true&url=" + Uri.encode(pdfUrl);
        pdfWebView.loadUrl(pdfViewerUrl);
    }
}
