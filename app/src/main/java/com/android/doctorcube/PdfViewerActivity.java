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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PdfViewerActivity extends AppCompatActivity implements OnLoadCompleteListener, OnPageChangeListener {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String TAG = "PdfViewerActivity";
    private PDFView pdfView;
    private ProgressBar progressBar;
    private TextView titleTextView, descriptionTextView, pageCountTextView;
    private TextView sizeTextView, categoryTextView, authorTextView, lastModifiedTextView, totalPagesTextView;
    private TextView searchResultsTextView, loadingTextView;
    private Toolbar toolbar;
    private Button viewDetailsButton;
    private CardView infoCardView;
    private CardView searchCardView; // Unused, but keep for now in case you want to add search back
    private EditText searchEditText; // Unused, but keep for now in case you want to add search back
    private ImageButton clearSearchButton; // Unused, but keep for now in case you want to add search back
    private ImageButton prevSearchResultButton;  // Unused, but keep for now in case you want to add search back
    private ImageButton nextSearchResultButton;  // Unused, but keep for now in case you want to add search back
    private ImageButton prevPageButton, nextPageButton;
    private LinearLayout loadingLayout;
    private LinearLayout searchResultsLayout; // Unused, but keep for now in case you want to add search back
    private String pdfUrl;
    private String pdfFileName;
    private File pdfFile;
    private int currentPage = 0;
    private int totalPages = 0;
    private int[] searchResults; // Unused, but keep for now in case you want to add search back
    private int currentSearchIndex = -1; // Unused, but keep for now in case you want to add search back
    private Map<String, DownloadPdfTask> activeDownloads = new HashMap<>();
    private boolean isInfoCardVisible = false; // Keep track of info card visibility
    private static android.os.Handler handler = new android.os.Handler(); // Unused, removed search functionality
    private String searchQuery = "";  // Unused, removed search functionality
    private Runnable searchRunnable = new Runnable() { // Unused, removed search functionality
        @Override
        public void run() {
            searchInPdf(searchQuery);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        initializeViews();
        setupToolbar();
        setupPageNavigation(); // Keep page navigation
        // Removed setupSearch();

        Intent intent = getIntent();
        String pdfTitle = intent.getStringExtra("pdfTitle");
        String pdfDescription = intent.getStringExtra("pdfDescription");
        String pdfAuthor = intent.getStringExtra("pdfAuthor");
        String pdfSize = intent.getStringExtra("pdfSize");
        pdfUrl = intent.getStringExtra("pdfFilePath");
        String pdfCategory = intent.getStringExtra("pdfCategory");
        String pdfLastModified = intent.getStringExtra("pdfLastModified");

        pdfFileName = pdfTitle != null ? pdfTitle.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf" : "downloaded_pdf.pdf";
        pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), pdfFileName);

        populateDocumentInfo(pdfTitle, pdfDescription, pdfAuthor, pdfSize, pdfCategory, pdfLastModified);

        viewDetailsButton.setOnClickListener(v -> toggleInfoCard());

        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            if (checkPermission()) {
                downloadAndOpenPdf(pdfUrl);
            } else {
                requestPermission();
            }
        } else {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        pageCountTextView = findViewById(R.id.pageCountTextView);
        sizeTextView = findViewById(R.id.sizeTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        authorTextView = findViewById(R.id.authorTextView);
        lastModifiedTextView = findViewById(R.id.lastModifiedTextView);
        totalPagesTextView = findViewById(R.id.totalPagesTextView);
        toolbar = findViewById(R.id.toolbar);
        viewDetailsButton = findViewById(R.id.viewDetailsButton);
        infoCardView = findViewById(R.id.infoCardView);
        searchCardView = findViewById(R.id.searchCardView); // Keep
        searchEditText = findViewById(R.id.searchEditText); // Keep
        clearSearchButton = findViewById(R.id.clearSearchButton); // Keep
        prevSearchResultButton = findViewById(R.id.prevSearchResultButton); // Keep
        nextSearchResultButton = findViewById(R.id.nextSearchResultButton); // Keep
        prevPageButton = findViewById(R.id.prevPageButton);
        nextPageButton = findViewById(R.id.nextPageButton);
        searchResultsTextView = findViewById(R.id.searchResultsTextView);
        loadingLayout = findViewById(R.id.loadingLayout);
        searchResultsLayout = findViewById(R.id.searchResultsLayout); // Keep
        loadingTextView = findViewById(R.id.loadingTextView);
        infoCardView.setVisibility(View.GONE); // Initial state of infoCard
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void setupPageNavigation() {
        prevPageButton.setOnClickListener(v -> {
            if (currentPage > 0) {
                pdfView.jumpTo(currentPage - 1, true);
            }
        });

        nextPageButton.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                pdfView.jumpTo(currentPage + 1, true);
            }
        });
    }

    private void populateDocumentInfo(String title, String desc, String author, String size, String category, String lastModified) {
        titleTextView.setText(title != null ? title : "Untitled Document");
        descriptionTextView.setText(desc != null ? desc : "No description available");
        sizeTextView.setText(size != null ? size : "Unknown size");
        categoryTextView.setText(category != null ? category : "Unknown category");
        authorTextView.setText(author != null ? author : "Unknown author");
        lastModifiedTextView.setText(lastModified != null ? lastModified : "Unknown date");
    }

    private void toggleInfoCard() {
        isInfoCardVisible = !isInfoCardVisible; // Toggle the state
        infoCardView.setVisibility(isInfoCardVisible ? View.VISIBLE : View.GONE);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadAndOpenPdf(pdfUrl);
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void downloadAndOpenPdf(String url) {
        loadingLayout.setVisibility(View.VISIBLE);

        if (pdfFile.exists()) {
            loadPdf(pdfFile);
            return;
        }

        String directDownloadUrl = convertGoogleDriveUrl(url);
        DownloadPdfTask downloadTask = new DownloadPdfTask();
        activeDownloads.put(directDownloadUrl, downloadTask);
        downloadTask.execute(directDownloadUrl);
    }

    private String convertGoogleDriveUrl(String url) {
        if (url.contains("drive.google.com")) {
            try {
                String fileId;
                if (url.contains("/file/d/")) {
                    fileId = url.split("/file/d/")[1].split("/")[0];
                } else if (url.contains("id=")) {
                    fileId = url.split("id=")[1].split("&")[0];
                } else {
                    Log.w(TAG, "Unrecognized Google Drive URL format. Attempting direct download.");
                    return url; // Return original URL and try direct download
                }
                return "https://drive.google.com/uc?export=download&id=" + "1XYZabcDEF456";
            } catch (Exception e) {
                Log.e(TAG, "URL conversion failed: " + e.getMessage());
                return url; // Return original URL
            }
        }
        return url;
    }

    private class DownloadPdfTask extends AsyncTask<String, Void, Boolean> {
        private String downloadUrl; // Store for cancellation

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadUrl = null; // Reset
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            downloadUrl = urls[0]; // Store the download URL
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + responseCode + " for URL: " + downloadUrl);
                    return false;
                }

                File directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (!directory.exists()) {
                    if (!directory.mkdirs()) {
                        Log.e(TAG, "Failed to create directory");
                        return false;
                    }
                }

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream outputStream = new FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1 && !isCancelled()) { // Check for cancellation
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    if (isCancelled()) {
                        // Clean up if cancelled
                        connection.disconnect();
                        return false;
                    }
                }
                connection.disconnect();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Download error for URL: " + downloadUrl + " : " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (downloadUrl != null) {
                activeDownloads.remove(downloadUrl);
            }
            if (success) {
                loadPdf(pdfFile);
            } else {
                loadingLayout.setVisibility(View.GONE);
                if (!isCancelled()) { //  Don't show error if task was cancelled.
                    Toast.makeText(PdfViewerActivity.this, "Failed to download PDF", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    //if the task is cancelled.
                    if (pdfFile.exists()) {
                        pdfFile.delete();
                    }
                }
            }
        }

        @Override
        protected void onCancelled(Boolean result) {
            super.onCancelled(result);
            if (downloadUrl != null) {
                activeDownloads.remove(downloadUrl);
            }
            Log.d(TAG, "Download task cancelled for URL: " + downloadUrl);
        }
    }

    private void loadPdf(File file) {
        if (file.exists()) {
            pdfView.fromFile(file)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onLoad(this)
                    .onPageChange(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    //.pageFitPolicy(FitPolicy.WIDTH) // Removed
                    .load();
        } else {
            loadingLayout.setVisibility(View.GONE);
            Toast.makeText(this, "PDF file not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        loadingLayout.setVisibility(View.GONE);
        totalPages = nbPages;
        totalPagesTextView.setText(String.valueOf(nbPages));
        pageCountTextView.setText("Page 1 of " + nbPages);
        Toast.makeText(this, "PDF loaded (" + nbPages + " pages)", Toast.LENGTH_SHORT).show();
        // Enable page navigation buttons
        prevPageButton.setEnabled(true);
        nextPageButton.setEnabled(true);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPage = page;
        pageCountTextView.setText("Page " + (page + 1) + " of " + pageCount);
    }



    private void searchInPdf(String query) {
        if (query.isEmpty()) {
            searchResultsLayout.setVisibility(View.GONE);
            searchResults = null;
            currentSearchIndex = -1;
            return;
        }

    }
    private void navigateSearchResults(int direction) {
        if (searchResults == null || searchResults.length == 0) return;

        currentSearchIndex += direction;
        if (currentSearchIndex < 0) {
            currentSearchIndex = searchResults.length - 1;
        } else if (currentSearchIndex >= searchResults.length) {
            currentSearchIndex = 0;
        }

        updateSearchResultsUI();
        pdfView.jumpTo(searchResults[currentSearchIndex], true);
    }

    private void updateSearchResultsUI() {
        if (searchResults != null && searchResults.length > 0) {
            searchResultsLayout.setVisibility(View.VISIBLE);
            searchResultsTextView.setText((currentSearchIndex + 1) + " of " + searchResults.length);
        } else {
            searchResultsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel all downloads
        for (DownloadPdfTask task : activeDownloads.values()) {
            task.cancel(true);
        }
        handler.removeCallbacks(searchRunnable); // Remove the callback. // Removed searchRunnable
    }
}

