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
import java.util.stream.Collectors;

public class UniversitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UniversityAdapter adapter;
    private List<University> universities;
    private EditText searchEditText;
    private Spinner sortSpinner;
    private TextView countryNameTitle, noUniversitiesText;
    private ImageButton filterBtn, backBtn;
    private String countryFilter;
    private boolean isSpinnerVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universities);

        countryFilter = getIntent().getStringExtra("COUNTRY_NAME") != null ?
                getIntent().getStringExtra("COUNTRY_NAME") : "All";

        recyclerView = findViewById(R.id.universities_recycler_view);
        searchEditText = findViewById(R.id.search_university);
        sortSpinner = findViewById(R.id.sort_spinner);
        countryNameTitle = findViewById(R.id.country_name_title);
        noUniversitiesText = findViewById(R.id.no_universities_text);
        filterBtn = findViewById(R.id.filterBtn);
        backBtn = findViewById(R.id.backBtn);

        countryNameTitle.setText(countryFilter.equals("All") ? "All Universities" : countryFilter + " Universities");

        loadUniversityData();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UniversityAdapter(this, universities, countryFilter); // Pass countryFilter to adapter
        recyclerView.setAdapter(adapter);

        filterByCountry();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filterByName(s.toString());
                updateNoUniversitiesView();
            }
        });

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) hideSpinner();
        });

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: adapter.sortByName(true); break;
                    case 1: adapter.sortByName(false); break;
                    case 2: adapter.sortByGrade(false); break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        filterBtn.setOnClickListener(v -> {
            if (isSpinnerVisible) {
                hideSpinner();
            } else {
                showSpinner();
            }
        });

        backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadUniversityData() {
        universities = UniversityData.getUniversities();
    }

    private void filterByCountry() {
        if (!countryFilter.equals("All")) {
            universities = universities.stream()
                    .filter(u -> u.getCountry().equalsIgnoreCase(countryFilter))
                    .collect(Collectors.toList());
        }
        adapter.updateData(universities); // Update adapter with filtered list
        updateNoUniversitiesView();
    }

    private void updateNoUniversitiesView() {
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            noUniversitiesText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noUniversitiesText.setVisibility(View.GONE);
        }
    }

    private void showSpinner() {
        if (!isSpinnerVisible) {
            sortSpinner.setVisibility(View.VISIBLE);
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(300);
            sortSpinner.startAnimation(fadeIn);
            isSpinnerVisible = true;
        }
    }

    private void hideSpinner() {
        if (isSpinnerVisible) {
            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(300);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
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