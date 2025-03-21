package com.android.doctorcube;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.university.adapter.UniversityAdapter;
import com.android.doctorcube.university.model.University;
import com.android.doctorcube.university.model.UniversityData;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UniversitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UniversityAdapter adapter;
    private List<University> universities;
    private EditText searchEditText;
    private TextView countryNameTitle, noUniversitiesText, universityCount, topRankedCount;
    private ImageButton backBtn;
    private MaterialButton clearFiltersBtn, filterBtn;
    private ChipGroup filterChipGroup;
    private AppBarLayout appBarLayout;
    private String countryFilter;
    private List<String> activeFilters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universities);

        // Get country filter from intent
        countryFilter = getIntent().getStringExtra("COUNTRY_NAME") != null ?
                getIntent().getStringExtra("COUNTRY_NAME") : "All";

        // Initialize views
        recyclerView = findViewById(R.id.universities_recycler_view);
        searchEditText = findViewById(R.id.search_university);
        countryNameTitle = findViewById(R.id.country_name_title);
        noUniversitiesText = findViewById(R.id.no_universities_text);
        filterBtn = findViewById(R.id.filterBtn);
        backBtn = findViewById(R.id.backBtn);
        universityCount = findViewById(R.id.university_count);
        topRankedCount = findViewById(R.id.top_ranked_count);
        clearFiltersBtn = findViewById(R.id.clear_filters_btn);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        appBarLayout = findViewById(R.id.appBarLayout);

        // Verify filterBtn initialization
        if (filterBtn == null) {
            Log.e("UniversitiesActivity", "filterBtn is null, check XML ID");
        }

        // Set title
        countryNameTitle.setText(countryFilter.equals("All") ? "All Universities" : countryFilter + " Universities");

        // Load and setup data with null safety
        loadUniversityData();
        if (universities == null) {
            universities = new ArrayList<>();
            Log.w("UniversitiesActivity", "University data was null, initialized empty list");
        }

        setupRecyclerView();
        if (adapter == null) {
            Log.e("UniversitiesActivity", "Adapter initialization failed");
            return;
        }

        setupFilterChips();
        filterByCountry();

        // Setup listeners
        setupSearch();
        setupFilterButton();
        setupClearFilters();

        backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadUniversityData() {
        try {
            universities = UniversityData.getUniversities();
            if (universities == null) {
                universities = new ArrayList<>();
                Log.w("UniversitiesActivity", "UniversityData.getUniversities() returned null");
            }
        } catch (Exception e) {
            Log.e("UniversitiesActivity", "Error loading university data", e);
            universities = new ArrayList<>();
        }
    }

    private void setupRecyclerView() {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new UniversityAdapter(this, universities, countryFilter);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("UniversitiesActivity", "Error setting up RecyclerView", e);
            adapter = null;
        }
    }

    private void setupFilterChips() {
        String[] filterOptions = {"Top Ranked", "Scholarships", "Engineering", "Sort A-Z", "Sort Z-A", "Sort Grade"};

        for (String filter : filterOptions) {
            Chip chip = new Chip(this);
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.card_highlight);
            filterChipGroup.addView(chip);

            chip.setOnCloseIconClickListener(v -> {
                chip.setChecked(false);
                activeFilters.remove(chip.getText().toString());
                applyFilters();
            });
        }

        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            activeFilters.clear();
            for (int i = 0; i < filterChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) filterChipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    activeFilters.add(chip.getText().toString());
                }
            }
            applyFilters();
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (adapter != null) {
                    adapter.filterByName(s.toString());
                    updateNoUniversitiesView();
                }
            }
        });
    }

    private void setupFilterButton() {
        if (filterBtn != null) {
            filterBtn.setOnClickListener(v -> {
                Log.d("UniversitiesActivity", "Filter button clicked");
                if (filterChipGroup != null) {
                    int visibility = filterChipGroup.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
                    filterChipGroup.setVisibility(visibility);
                    Log.d("UniversitiesActivity", "FilterChipGroup visibility set to: " + (visibility == View.VISIBLE ? "VISIBLE" : "GONE"));
                } else {
                    Log.w("UniversitiesActivity", "filterChipGroup is null");
                }
            });
        } else {
            Log.e("UniversitiesActivity", "filterBtn is null in setupFilterButton");
        }
    }

    private void setupClearFilters() {
        clearFiltersBtn.setOnClickListener(v -> {
            filterChipGroup.clearCheck();
            activeFilters.clear();
            searchEditText.setText("");
            if (adapter != null) {
                adapter.updateData(universities);
                filterByCountry();
            }
            filterChipGroup.setVisibility(View.GONE);
        });
    }

    private void filterByCountry() {
        if (adapter == null) {
            Log.w("UniversitiesActivity", "Adapter is null in filterByCountry");
            updateStats();
            updateNoUniversitiesView();
            return;
        }

        if (!countryFilter.equals("All")) {
            List<University> filtered = universities.stream()
                    .filter(u -> u.getCountry() != null && u.getCountry().equalsIgnoreCase(countryFilter))
                    .collect(Collectors.toList());
            adapter.updateData(filtered);
        } else {
            adapter.updateData(universities);
        }
        updateStats();
        updateNoUniversitiesView();
    }

    private void applyFilters() {
        if (adapter == null) {
            Log.w("UniversitiesActivity", "Adapter is null in applyFilters");
            updateStats();
            updateNoUniversitiesView();
            return;
        }

        List<University> filteredUniversities = new ArrayList<>(universities);

        if (!countryFilter.equals("All")) {
            filteredUniversities = filteredUniversities.stream()
                    .filter(u -> u.getCountry() != null && u.getCountry().equalsIgnoreCase(countryFilter))
                    .collect(Collectors.toList());
        }

        if (!activeFilters.isEmpty()) {
            for (String filter : activeFilters) {
                switch (filter.toLowerCase()) {
                    case "top ranked":
                        filteredUniversities = filteredUniversities.stream()
                                .filter(u -> u.getRanking() != null && u.getRanking().contains("Top"))
                                .collect(Collectors.toList());
                        break;
                    case "scholarships":
                        filteredUniversities = filteredUniversities.stream()
                                .filter(u -> u.getScholarshipInfo() != null && !u.getScholarshipInfo().isEmpty())
                                .collect(Collectors.toList());
                        break;
                    case "engineering":
                        filteredUniversities = filteredUniversities.stream()
                                .filter(u -> u.getField() != null && u.getField().equalsIgnoreCase("engineering"))
                                .collect(Collectors.toList());
                        break;
                    case "sort a-z":
                        adapter.sortByName(true);
                        updateNoUniversitiesView();
                        updateStats();
                        return;
                    case "sort z-a":
                        adapter.sortByName(false);
                        updateNoUniversitiesView();
                        updateStats();
                        return;
                    case "sort grade":
                        adapter.sortByGrade(true);
                        updateNoUniversitiesView();
                        updateStats();
                        return;
                }
            }
            adapter.updateData(filteredUniversities);
        }

        updateNoUniversitiesView();
        updateStats();
    }

    private void updateStats() {
        if (adapter == null) {
            universityCount.setText("0");
            topRankedCount.setText("0");
            return;
        }

        int totalCount = adapter.getItemCount();
        int topRanked = (int) universities.stream()
                .filter(u -> u.getRanking() != null && u.getRanking().contains("Top"))
                .count();

        universityCount.setText(String.valueOf(totalCount));
        topRankedCount.setText(String.valueOf(topRanked));
    }

    private void updateNoUniversitiesView() {
        if (adapter == null || adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            noUniversitiesText.setVisibility(View.VISIBLE);
            clearFiltersBtn.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noUniversitiesText.setVisibility(View.GONE);
            clearFiltersBtn.setVisibility(View.GONE);
        }
    }
}