package com.android.doctorcube;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.university.adapter.UniversityAdapter;
import com.android.doctorcube.university.model.University;
import com.android.doctorcube.university.model.UniversityData;

import java.util.List;

public class UniversitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UniversityAdapter adapter;
    private List<University> universities;
    private EditText searchEditText;
    private Spinner sortSpinner;
    private TextView countryNameTitle, noUniversitiesText;
    private ImageButton filterBtn, backBtn;
    private String countryFilter;
    private boolean isSpinnerVisible = false;  // To track spinner visibility

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universities);

        // Get country from intent
        countryFilter = getIntent().getStringExtra("COUNTRY_NAME") != null ?
                getIntent().getStringExtra("COUNTRY_NAME") : "All";

        // Initialize views
        recyclerView = findViewById(R.id.universities_recycler_view);
        searchEditText = findViewById(R.id.search_university);
        sortSpinner = findViewById(R.id.sort_spinner);
        countryNameTitle = findViewById(R.id.country_name_title);
        noUniversitiesText = findViewById(R.id.no_universities_text);
        filterBtn = findViewById(R.id.filterBtn);
        backBtn = findViewById(R.id.backBtn);

        // Set title
        countryNameTitle.setText(countryFilter.equals("All") ? "All Universities" : countryFilter + " Universities");

        // Load university data
        loadUniversityData();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UniversityAdapter(this, universities);
        recyclerView.setAdapter(adapter);

        // Filter by country
        if (!countryFilter.equals("All")) {
            adapter.filterByCountry(countryFilter);
        }

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
                updateNoUniversitiesView();
            }
        });

        // Hide spinner when search bar is clicked
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hideSpinner();
            }
        });

        // Sorting functionality
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: adapter.sortByName(true); break;  // A-Z
                    case 1: adapter.sortByName(false); break; // Z-A
                    case 2: adapter.sortByGrade(false); break; // High-Low
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Filter button click - show/hide spinner with animation
        filterBtn.setOnClickListener(v -> {
            if (isSpinnerVisible) {
                hideSpinner();
            } else {
                showSpinner();
            }
        });

        // Back button click - go to previous fragment/activity
        backBtn.setOnClickListener(v -> onBackPressed());
    }

    // Load university data
    private void loadUniversityData() {
        universities = UniversityData.getUniversities();
    }

    // Update UI when no universities are found
    private void updateNoUniversitiesView() {
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            noUniversitiesText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noUniversitiesText.setVisibility(View.GONE);
        }
    }

    // Show spinner with fade-in animation
    private void showSpinner() {
        if (!isSpinnerVisible) {
            sortSpinner.setVisibility(View.VISIBLE);
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(300);
            sortSpinner.startAnimation(fadeIn);
            isSpinnerVisible = true;
        }
    }

    // Hide spinner with fade-out animation
    private void hideSpinner() {
        if (isSpinnerVisible) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.setAnimationListener(new AlphaAnimation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    sortSpinner.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationStart(Animation animation) {}
            });
            sortSpinner.startAnimation(fadeOut);
            isSpinnerVisible = false;
        }
    }
}
