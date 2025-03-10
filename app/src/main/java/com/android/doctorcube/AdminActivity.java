package com.android.doctorcube;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.doctorcube.adminpannel.Student;
import com.android.doctorcube.adminpannel.StudentAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AdminActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private List<Student> filteredList;
    private DatabaseReference databaseReference;
    private MaterialButton sortButton, filterButton;
    private ChipGroup activeFiltersChipGroup;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault()); // For 080325 format
    private String currentSortBy = null; // Track current sort state
    private boolean filterInterested, filterNotInterested, filterCallMade, filterCallNotMade, filterToday, filterYesterday, filterEarlier; // Track filter states

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize UI Components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.students_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        sortButton = findViewById(R.id.sort_button);
        filterButton = findViewById(R.id.filter_button);
        activeFiltersChipGroup = findViewById(R.id.active_filters_chip_group);

        // Setup Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Initialize student list and adapter
        studentList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new StudentAdapter(filteredList, this);
        recyclerView.setAdapter(adapter);

        // Setup Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");

        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this::loadStudentData);

        // Setup Sort and Filter Buttons
        sortButton.setOnClickListener(v -> showSortFilterDialog());
        filterButton.setOnClickListener(v -> showSortFilterDialog());

        // Initialize filter states
        filterInterested = filterNotInterested = filterCallMade = filterCallNotMade = filterToday = filterYesterday = filterEarlier = false;

        // Load data initially
        loadStudentData();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        adapter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadStudentData() {
        swipeRefreshLayout.setRefreshing(true);
        Log.d(TAG, "📌 Starting to load student data from Firebase");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                Log.d(TAG, "🔥 Total Date Nodes Found: " + snapshot.getChildrenCount());

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey(); // e.g., "080325"
                    Log.d(TAG, "📅 Processing Date Key: " + dateKey);

                    for (DataSnapshot studentSnapshot : dateSnapshot.getChildren()) {
                        try {
                            Student student = studentSnapshot.getValue(Student.class);
                            if (student != null) {
                                student.setId(studentSnapshot.getKey());
                                student.setSubmissionDate(dateKey); // Use the parent node date (e.g., "080325")
                                if (student.getCallStatus() == null) {
                                    student.setCallStatus("pending");
                                }
                                if (student.getIsInterested() == null) {
                                    student.setIsInterested(false);
                                }
                                studentList.add(student);
                                Log.d(TAG, "👤 Added student: " + student.getName());
                            } else {
                                Log.w(TAG, "⚠️ Student data conversion failed for ID: " + studentSnapshot.getKey());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "🚨 Error parsing student data: " + e.getMessage());
                        }
                    }
                }

                applyFiltersAndSorting(currentSortBy, filterInterested, filterNotInterested, filterCallMade,
                        filterCallNotMade, filterToday, filterYesterday, filterEarlier); // Apply current filters
                swipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, "📊 RecyclerView updated with " + filteredList.size() + " students");

                if (filteredList.isEmpty()) {
                    showNoStudentsPopup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "🚨 Firebase Error: " + error.getMessage());
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(AdminActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSortFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort & Filter");

        // Custom layout for dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort_filter, null);
        builder.setView(dialogView);

        // Sorting options (single selection)
        ChipGroup sortGroup = dialogView.findViewById(R.id.sort_chip_group);
        Chip sortNameChip = dialogView.findViewById(R.id.chip_sort_name);
        Chip sortDateChip = dialogView.findViewById(R.id.chip_sort_date);
        sortGroup.setSingleSelection(true); // Ensure only one sort option at a time
        sortNameChip.setCheckable(true);
        sortDateChip.setCheckable(true);
        if ("name".equals(currentSortBy)) sortNameChip.setChecked(true);
        if ("date".equals(currentSortBy)) sortDateChip.setChecked(true);

        // Filter options (multiple selection)
        Chip filterInterestedChip = dialogView.findViewById(R.id.chip_filter_interested);
        Chip filterNotInterestedChip = dialogView.findViewById(R.id.chip_filter_not_interested);
        Chip filterCallMadeChip = dialogView.findViewById(R.id.chip_filter_call_made);
        Chip filterCallNotMadeChip = dialogView.findViewById(R.id.chip_filter_call_not_made);
        Chip filterTodayChip = dialogView.findViewById(R.id.chip_filter_today);
        Chip filterYesterdayChip = dialogView.findViewById(R.id.chip_filter_yesterday);
        Chip filterEarlierChip = dialogView.findViewById(R.id.chip_filter_earlier);

        // Set current filter states
        filterInterestedChip.setChecked(filterInterested);
        filterNotInterestedChip.setChecked(filterNotInterested);
        filterCallMadeChip.setChecked(filterCallMade);
        filterCallNotMadeChip.setChecked(filterCallNotMade);
        filterTodayChip.setChecked(filterToday);
        filterYesterdayChip.setChecked(filterYesterday);
        filterEarlierChip.setChecked(filterEarlier);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Update sort and filter states
            currentSortBy = sortNameChip.isChecked() ? "name" : sortDateChip.isChecked() ? "date" : null;
            filterInterested = filterInterestedChip.isChecked();
            filterNotInterested = filterNotInterestedChip.isChecked();
            filterCallMade = filterCallMadeChip.isChecked();
            filterCallNotMade = filterCallNotMadeChip.isChecked();
            filterToday = filterTodayChip.isChecked();
            filterYesterday = filterYesterdayChip.isChecked();
            filterEarlier = filterEarlierChip.isChecked();

            applyFiltersAndSorting(currentSortBy, filterInterested, filterNotInterested, filterCallMade,
                    filterCallNotMade, filterToday, filterYesterday, filterEarlier);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void applyFiltersAndSorting(String sortBy, boolean filterInterested, boolean filterNotInterested,
                                        boolean filterCallMade, boolean filterCallNotMade,
                                        boolean filterToday, boolean filterYesterday, boolean filterEarlier) {
        filteredList.clear();
        activeFiltersChipGroup.removeAllViews();

        // Apply filters (multiple conditions can be true)
        for (Student student : studentList) {
            boolean matchesFilter = true;

            // Apply each filter if selected
            if (filterInterested && !student.getIsInterested()) matchesFilter = false;
            if (filterNotInterested && student.getIsInterested()) matchesFilter = false;
            if (filterCallMade && !"called".equals(student.getCallStatus())) matchesFilter = false;
            if (filterCallNotMade && "called".equals(student.getCallStatus())) matchesFilter = false;

            // Date filters based on "ddMMyy" format (e.g., "080325")
            String today = dateFormat.format(new Date());
            String yesterday = dateFormat.format(new Date(System.currentTimeMillis() - 86400000));
            if (filterToday && !today.equals(student.getSubmissionDate())) matchesFilter = false;
            if (filterYesterday && !yesterday.equals(student.getSubmissionDate())) matchesFilter = false;
            if (filterEarlier && (today.equals(student.getSubmissionDate()) || yesterday.equals(student.getSubmissionDate()))) matchesFilter = false;

            if (matchesFilter) filteredList.add(student);
        }

        // Apply sorting
        if (sortBy != null) {
            Collections.sort(filteredList, new Comparator<Student>() {
                @Override
                public int compare(Student s1, Student s2) {
                    if ("name".equals(sortBy)) {
                        return s1.getName().compareToIgnoreCase(s2.getName());
                    } else if ("date".equals(sortBy)) {
                        try {
                            Date date1 = dateFormat.parse(s1.getSubmissionDate());
                            Date date2 = dateFormat.parse(s2.getSubmissionDate());
                            return date2.compareTo(date1); // Descending order
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing date: " + e.getMessage());
                            return 0;
                        }
                    }
                    return 0;
                }
            });
        }

        // Update chips to reflect current filters/sort
        if (sortBy != null) addChip(sortBy.equals("name") ? "Sort: Name" : "Sort: Date");
        if (filterInterested) addChip("Interested");
        if (filterNotInterested) addChip("Not Interested");
        if (filterCallMade) addChip("Call Made");
        if (filterCallNotMade) addChip("Call Not Made");
        if (filterToday) addChip("Today");
        if (filterYesterday) addChip("Yesterday");
        if (filterEarlier) addChip("Earlier");

        adapter.notifyDataSetChanged(); // Update RecyclerView
    }

    private void addChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            activeFiltersChipGroup.removeView(chip);
            // Remove specific filter when chip is closed
            switch (text) {
                case "Sort: Name": case "Sort: Date": currentSortBy = null; break;
                case "Interested": filterInterested = false; break;
                case "Not Interested": filterNotInterested = false; break;
                case "Call Made": filterCallMade = false; break;
                case "Call Not Made": filterCallNotMade = false; break;
                case "Today": filterToday = false; break;
                case "Yesterday": filterYesterday = false; break;
                case "Earlier": filterEarlier = false; break;
            }
            applyFiltersAndSorting(currentSortBy, filterInterested, filterNotInterested, filterCallMade,
                    filterCallNotMade, filterToday, filterYesterday, filterEarlier);
        });
        activeFiltersChipGroup.addView(chip);
    }

    // Show a popup when no students are found
    private void showNoStudentsPopup() {
        new AlertDialog.Builder(this)
                .setTitle("No Students Found")
                .setMessage("No student registrations are available in the database or no matches for current filters.")
                .setPositiveButton("Refresh", (dialog, which) -> loadStudentData())
                .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_students) {
            loadStudentData();
        } else if (id == R.id.nav_call_logs) {
            Toast.makeText(this, "Call Logs feature coming soon!", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}