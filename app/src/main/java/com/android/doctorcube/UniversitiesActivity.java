package com.android.doctorcube;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.university.adapter.UniversityAdapter;
import com.android.doctorcube.university.model.University;
import com.android.doctorcube.university.model.UniversityData;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;

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
    private Spinner filterSpinner;
    private AppBarLayout appBarLayout;
    private String countryFilter;
    private String selectedFilter = "None"; // Default filter

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
        filterSpinner = findViewById(R.id.filter_spinner);
        appBarLayout = findViewById(R.id.appBarLayout);

        // Verify critical components
        if (filterBtn == null || filterSpinner == null) {
            Log.e("UniversitiesActivity", "Critical UI component is null: filterBtn=" + filterBtn + ", filterSpinner=" + filterSpinner);
            return;
        }

        // Set initial title
        countryNameTitle.setText(countryFilter.equals("All") ? "All Universities" : "Universities in " + countryFilter);

        // Load and setup data
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

        setupFilterSpinner();
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

    private void setupFilterSpinner() {
        String[] filterOptions = {"None", "Top Ranked", "Scholarships", "Engineering", "Sort A-Z", "Sort Z-A", "Sort Grade"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = filterOptions[position];
                Log.d("UniversitiesActivity", "Spinner selected: " + selectedFilter);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFilter = "None";
                applyFilters();
            }
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
        filterBtn.setOnClickListener(v -> {
            Log.d("UniversitiesActivity", "Filter button clicked");
            Toast.makeText(this, "Filter button clicked", Toast.LENGTH_SHORT).show();
            int visibility = filterSpinner.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            filterSpinner.setVisibility(visibility);
            Log.d("UniversitiesActivity", "FilterSpinner visibility set to: " + (visibility == View.VISIBLE ? "VISIBLE" : "GONE"));
        });
    }

    private void setupClearFilters() {
        clearFiltersBtn.setOnClickListener(v -> {
            Log.d("UniversitiesActivity", "Clear filters button clicked");
            Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
            filterSpinner.setSelection(0); // Reset to "None"
            selectedFilter = "None";
            searchEditText.setText("");
            if (adapter != null) {
                adapter.updateData(universities);
                filterByCountry();
            }
            filterSpinner.setVisibility(View.GONE);
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

        if (!selectedFilter.equals("None")) {
            switch (selectedFilter.toLowerCase()) {
                case "top ranked":
                    filteredUniversities = filteredUniversities.stream()
                            .filter(u -> u.getRanking() != null && u.getRanking().contains("Top"))
                            .collect(Collectors.toList());
                    adapter.updateData(filteredUniversities);
                    break;
                case "scholarships":
                    filteredUniversities = filteredUniversities.stream()
                            .filter(u -> u.getScholarshipInfo() != null && !u.getScholarshipInfo().isEmpty())
                            .collect(Collectors.toList());
                    adapter.updateData(filteredUniversities);
                    break;
                case "engineering":
                    filteredUniversities = filteredUniversities.stream()
                            .filter(u -> u.getField() != null && u.getField().equalsIgnoreCase("engineering"))
                            .collect(Collectors.toList());
                    adapter.updateData(filteredUniversities);
                    break;
                case "sort a-z":
                    adapter.sortByName(true);
                    break;
                case "sort z-a":
                    adapter.sortByName(false);
                    break;
                case "sort grade":
                    adapter.sortByGrade(true);
                    break;
            }
        } else {
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
        View emptyStateContainer = findViewById(R.id.empty_state_container);
        if (adapter == null || adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }
}