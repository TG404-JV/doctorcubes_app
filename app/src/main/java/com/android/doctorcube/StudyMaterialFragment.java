package com.android.doctorcube;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.doctorcube.study.adapter.StudyMaterialPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StudyMaterialFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private EditText searchEditText;
    private ImageView clearSearchButton;
    private StudyMaterialPagerAdapter pagerAdapter;
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study_material, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);

        // Initially hide the clear button
        if (clearSearchButton != null) {
            clearSearchButton.setVisibility(View.GONE);
        }

        // Set up the ViewPager with the adapter
        setupViewPager();

        // Set up search functionality
        setupSearch();
    }

    private void setupViewPager() {
        // Create adapter
        pagerAdapter = new StudyMaterialPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Notes");
                    break;
                case 1:
                    tab.setText("Videos");
                    break;
            }
        }).attach();

        // Add page change listener for custom actions when tab changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // When switching tabs, re-apply search if query exists
                if (!currentSearchQuery.isEmpty()) {
                    performSearch(currentSearchQuery);
                }
            }
        });
    }

    private void setupSearch() {
        // Clear button functionality
        if (clearSearchButton != null) {
            clearSearchButton.setOnClickListener(v -> {
                searchEditText.setText("");
                clearSearchButton.setVisibility(View.GONE);
                currentSearchQuery = "";
                resetSearch();
                hideKeyboard();
            });
        }

        // Handle search action
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchQuery = searchEditText.getText().toString().trim();
                if (!currentSearchQuery.isEmpty()) {
                    performSearch(currentSearchQuery);
                } else {
                    resetSearch();
                }
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Real-time search as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            private Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on search text
                if (clearSearchButton != null) {
                    clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }

                // Cancel any pending search
                if (searchRunnable != null) {
                    searchEditText.removeCallbacks(searchRunnable);
                }

                // Delay search to avoid searching on every character typed
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    currentSearchQuery = query;

                    if (query.length() >= 2) {
                        performSearch(query);
                    } else if (query.isEmpty()) {
                        resetSearch();
                    }
                };

                searchEditText.postDelayed(searchRunnable, 300); // 300ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }

    // Method to hide keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && searchEditText != null) {
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    // Perform search on the current visible fragment
    private void performSearch(String query) {
        Fragment currentFragment = pagerAdapter.getFragmentAt(viewPager.getCurrentItem());
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).performSearch(query);
        }
    }

    // Reset search on the current visible fragment
    private void resetSearch() {
        Fragment currentFragment = pagerAdapter.getFragmentAt(viewPager.getCurrentItem());
        if (currentFragment instanceof SearchableFragment) {
            ((SearchableFragment) currentFragment).resetSearch();
        }
    }

    // Getter for current search query
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    // Interface to be implemented by fragments that support search
    public interface SearchableFragment {
        void performSearch(String query);
        void resetSearch(); // Method to reset search
    }
}