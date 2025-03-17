package com.android.doctorcube.adminpannel.adminhome;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.doctorcube.R;
import com.android.doctorcube.adminpannel.ChipManager;
import com.android.doctorcube.adminpannel.FilterManager;
import com.android.doctorcube.adminpannel.SortFilterDialogManager;
import com.android.doctorcube.adminpannel.Student;
import com.android.doctorcube.adminpannel.StudentAdapter;
import com.android.doctorcube.adminpannel.StudentDataLoader;
import com.android.doctorcube.adminpannel.StudentSorter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentAdminHome extends Fragment {

    private static final String TAG = "FragmentAdminHome";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public StudentAdapter adapter;
    private List<Student> studentList;
    private List<Student> filteredList;
    private StudentDataLoader dataLoader;
    private StudentSorter sorter;
    private MaterialButton sortButton, filterButton;
    private ChipGroup activeFiltersChipGroup;
    private EditText searchEditText;
    private ImageButton clearSearchButton;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

    private FilterManager filterManager;
    private SortFilterDialogManager dialogManager;
    public ChipManager chipManager;

    public FragmentAdminHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // UI Setup
        recyclerView = view.findViewById(R.id.students_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        sortButton = view.findViewById(R.id.sort_button);
        filterButton = view.findViewById(R.id.filter_button);
        activeFiltersChipGroup = view.findViewById(R.id.active_filters_chip_group);
        searchEditText = view.findViewById(R.id.search_edit_text);
        clearSearchButton = view.findViewById(R.id.clear_search_button);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        studentList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new StudentAdapter(filteredList, getContext());
        recyclerView.setAdapter(adapter);

        dataLoader = new StudentDataLoader();
        sorter = new StudentSorter(dateFormat, getContext());

        // Initialize managers
        filterManager = new FilterManager(studentList, filteredList, sorter, dateFormat, displayFormat, getContext());
        dialogManager = new SortFilterDialogManager(getContext(), filterManager);
        chipManager = new ChipManager(activeFiltersChipGroup, filterManager, searchEditText);

        swipeRefreshLayout.setColorSchemeColors(0xFF4CAF50); // Green
        swipeRefreshLayout.setOnRefreshListener(this::loadStudentData);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterManager.setSearchQuery(s.toString().trim());
                filterManager.applyFiltersAndSorting();
                adapter.notifyDataSetChanged(); // Notify adapter here
                clearSearchButton.setVisibility(filterManager.getSearchQuery().isEmpty() ? View.GONE : View.VISIBLE);
                chipManager.updateChips();
            }
        });

        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            filterManager.setSearchQuery("");
            filterManager.applyFiltersAndSorting();
            adapter.notifyDataSetChanged(); // Notify adapter here
            clearSearchButton.setVisibility(View.GONE);
            chipManager.updateChips();
        });

        sortButton.setOnClickListener(v -> dialogManager.showSortFilterDialog());
        filterButton.setOnClickListener(v -> dialogManager.showSortFilterDialog());

        loadStudentData();

        return view;
    }

    private void loadStudentData() {
        swipeRefreshLayout.setRefreshing(true);
        dataLoader.loadStudents(new StudentDataLoader.DataLoadListener() {
            @Override
            public void onDataLoaded(List<Student> students) {
                Log.d(TAG, "Data loaded, size: " + students.size());
                studentList.clear();
                studentList.addAll(students);
                filterManager.applyFiltersAndSorting();
                adapter.notifyDataSetChanged(); // Ensure adapter is notified
                swipeRefreshLayout.setRefreshing(false);
                if (filteredList.isEmpty()) {
                    Log.d(TAG, "Filtered list is empty");
                    showNoStudentsPopup();
                } else {
                    Log.d(TAG, "Filtered list size: " + filteredList.size());
                }
                chipManager.updateChips();
            }

            @Override
            public void onDataLoadFailed(String error) {
                Log.e(TAG, "Data load failed: " + error);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Failed to load data: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<Student> getFilteredList() {
        return new ArrayList<>(filteredList);
    }

    private void showNoStudentsPopup() {
        new AlertDialog.Builder(getContext())
                .setTitle("No Students Found")
                .setMessage("No student registrations are available in the database or no matches for current filters.")
                .setPositiveButton("Refresh", (dialog, which) -> loadStudentData())
                .setNegativeButton("OK", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        adapter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}