package com.android.doctorcube;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity {

    private PDFView pdfView;
    private ProgressBar progressBar;
    private TextView titleTextView, descriptionTextView, sizeTextView;
    private String pdfUrl;
    private String pdfFileName = "downloaded_pdf.pdf"; // Name for local storage

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        sizeTextView = findViewById(R.id.sizeTextView);
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);

        // Get data from Intent
        Intent intent = getIntent();
        String pdfTitle = intent.getStringExtra("pdfTitle");
        String pdfDescription = intent.getStringExtra("pdfDescription");
        String pdfSize = intent.getStringExtra("pdfSize");
        pdfUrl = intent.getStringExtra("pdfFilePath");

        // Set UI values
        titleTextView.setText(pdfTitle);
        descriptionTextView.setText(pdfDescription);
        sizeTextView.setText(pdfSize);

        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            downloadAndOpenPdf(pdfUrl);
        } else {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadAndOpenPdf(String url) {
        progressBar.setVisibility(View.VISIBLE);

        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), pdfFileName);

        if (pdfFile.exists()) {
            // If file already exists, load it
            loadPdf(pdfFile);
        } else {
            // Download file using DownloadManager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Downloading PDF");
            request.setDescription("Please wait...");
            request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, pdfFileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // FIXED

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show();

            // Wait for the file to download
            new android.os.Handler().postDelayed(() -> {
                if (pdfFile.exists()) {
                    loadPdf(pdfFile);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PdfViewerActivity.this, "Failed to download PDF", Toast.LENGTH_SHORT).show();
                }
            }, 5000); // Wait 5 seconds for the download to complete
        }
    }

    private void loadPdf(File file) {
        if (file.exists()) {
            pdfView.fromFile(file)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .onLoad(nbPages -> progressBar.setVisibility(View.GONE))
                    .load();
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "PDF file not found!", Toast.LENGTH_SHORT).show();
        }
    }
}