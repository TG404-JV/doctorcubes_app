package com.android.doctorcube;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
    private StudyMaterialPagerAdapter pagerAdapter;

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
                // You can perform actions when page changes
            }
        });
    }

    private void setupSearch() {
        // Implement search functionality
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                // Pass the search query to the current visible fragment
                Fragment currentFragment = pagerAdapter.getFragmentAt(viewPager.getCurrentItem());
                if (currentFragment instanceof SearchableFragment) {
                    ((SearchableFragment) currentFragment).performSearch(query);
                }
            }
            return true;
        });
    }

    // Interface to be implemented by fragments that support search
    public interface SearchableFragment {
        void performSearch(String query);
    }
}



