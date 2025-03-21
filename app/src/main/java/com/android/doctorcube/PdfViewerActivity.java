package com.android.doctorcube;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfViewerActivity extends AppCompatActivity {

    private PDFView pdfView;
    private ProgressBar progressBar;
    private LinearLayout loadingLayout;
    private TextView loadingTextView;
    private TextView titleTextView;
    private ImageButton backButton;

    private String pdfTitle;
    private String pdfFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // Initialize views
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);
        loadingLayout = findViewById(R.id.loadingLayout);
        loadingTextView = findViewById(R.id.loadingTextView);
        titleTextView = findViewById(R.id.titleTextView);
        backButton = findViewById(R.id.backButton);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            pdfTitle = intent.getStringExtra("pdfTitle");
            pdfFilePath = intent.getStringExtra("pdfFilePath");
            // You can also get other extras if needed
            String pdfDescription = intent.getStringExtra("pdfDescription");
            String pdfAuthor = intent.getStringExtra("pdfAuthor");
            String pdfSize = intent.getStringExtra("pdfSize");
            String pdfCategory = intent.getStringExtra("pdfCategory");
            String pdfLastModified = intent.getStringExtra("pdfLastModified");
            int pdfTotalPages = intent.getIntExtra("pdfTotalPages", 0);
        }

        // Set title
        if (pdfTitle != null) {
            titleTextView.setText(pdfTitle);
        }

        // Set back button click listener
        backButton.setOnClickListener(v -> finish());

        // Load PDF
        if (pdfFilePath != null && !pdfFilePath.isEmpty()) {
            loadPdfFromUrl(pdfFilePath);
        } else {
            Toast.makeText(this, "PDF URL is invalid", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPdfFromUrl(String url) {
        showLoading(true);

        // Create a file in cache directory to store the downloaded PDF
        File outputFile = new File(getCacheDir(), "temp_pdf_" + System.currentTimeMillis() + ".pdf");

        // Use AsyncTask to download the file in background
        new DownloadPdfTask(outputFile).execute(url);
    }

    private void showLoading(boolean isLoading) {
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void displayPdf(File pdfFile) {
        pdfView.fromFile(pdfFile)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAnnotationRendering(false)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10)
                .onLoad(nbPages -> {
                    showLoading(false);
                })
                .onError(t -> {
                    showLoading(false);
                    Toast.makeText(PdfViewerActivity.this, "Error loading PDF: " + t.getMessage(), Toast.LENGTH_LONG).show();
                })
                .load();
    }

    // AsyncTask to download PDF file
    private class DownloadPdfTask extends AsyncTask<String, Integer, Boolean> {
        private File outputFile;
        private Exception exception;

        public DownloadPdfTask(File outputFile) {
            this.outputFile = outputFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingTextView.setText("Downloading PDF...");
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            if (urls.length == 0) return false;

            String url = urls[0];
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                URL fileUrl = new URL(url);
                connection = (HttpURLConnection) fileUrl.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    exception = new IOException("Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                    return false;
                }

                int fileLength = connection.getContentLength();

                // Download the file
                input = connection.getInputStream();
                output = new FileOutputStream(outputFile);

                byte[] data = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    total += count;

                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }

                    output.write(data, 0, count);
                }

                return true;
            } catch (Exception e) {
                exception = e;
                return false;
            } finally {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                } catch (IOException e) {
                    // Ignore
                }

                if (connection != null) connection.disconnect();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values.length > 0) {
                int progress = values[0];
                loadingTextView.setText("Downloading PDF... " + progress + "%");
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                loadingTextView.setText("Opening PDF...");
                displayPdf(outputFile);
            } else {
                showLoading(false);
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                Toast.makeText(PdfViewerActivity.this, "Failed to download PDF: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up temporary files - optional
        // Consider keeping this file if you want to avoid re-downloading
        // when the user returns to this activity
    }
}