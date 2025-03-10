package com.android.doctorcube;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import java.util.Calendar;
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
    // Add these instance variables to AdminActivity
    private boolean useCustomDateRange = false;
    private Date fromDate = null;
    private Date toDate = null;

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
                            Student student = new Student();
                            student.setId(studentSnapshot.getKey());
                            student.setSubmissionDate(dateKey);

                            // Get all other properties from Firebase
                            if (studentSnapshot.hasChild("name")) {
                                student.setName(studentSnapshot.child("name").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("mobile")) {
                                student.setMobile(studentSnapshot.child("mobile").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("email")) {
                                student.setEmail(studentSnapshot.child("email").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("state")) {
                                student.setState(studentSnapshot.child("state").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("city")) {
                                student.setCity(studentSnapshot.child("city").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("interestedCountry")) {
                                student.setInterestedCountry(studentSnapshot.child("interestedCountry").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("hasNeetScore")) {
                                student.setHasNeetScore(studentSnapshot.child("hasNeetScore").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("neetScore")) {
                                student.setNeetScore(studentSnapshot.child("neetScore").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("hasPassport")) {
                                student.setHasPassport(studentSnapshot.child("hasPassport").getValue(String.class));
                            }
                            if (studentSnapshot.hasChild("lastCallDate")) {
                                student.setLastCallDate(studentSnapshot.child("lastCallDate").getValue(String.class));
                            }

                            // Set default values for callStatus and isInterested if not present
                            if (studentSnapshot.hasChild("callStatus")) {
                                student.setCallStatus(studentSnapshot.child("callStatus").getValue(String.class));
                            } else {
                                student.setCallStatus("pending");
                            }

                            if (studentSnapshot.hasChild("isInterested")) {
                                student.setIsInterested(studentSnapshot.child("isInterested").getValue(Boolean.class));
                            } else {
                                student.setIsInterested(false);
                            }

                            studentList.add(student);
                            Log.d(TAG, "👤 Added student: " + student.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "🚨 Error parsing student data: " + e.getMessage());
                        }
                    }
                }

                applyFiltersAndSorting(); // Apply current filters
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

    // Update showSortFilterDialog method
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
        sortGroup.setSingleSelection(true);
        sortNameChip.setCheckable(true);
        sortDateChip.setCheckable(true);
        if ("name".equals(currentSortBy)) sortNameChip.setChecked(true);
        if ("date".equals(currentSortBy)) sortDateChip.setChecked(true);

        // Filter options
        Chip filterInterestedChip = dialogView.findViewById(R.id.chip_filter_interested);
        Chip filterNotInterestedChip = dialogView.findViewById(R.id.chip_filter_not_interested);
        Chip filterCallMadeChip = dialogView.findViewById(R.id.chip_filter_call_made);
        Chip filterCallNotMadeChip = dialogView.findViewById(R.id.chip_filter_call_not_made);

        // Set current filter states
        filterInterestedChip.setChecked(filterInterested);
        filterNotInterestedChip.setChecked(filterNotInterested);
        filterCallMadeChip.setChecked(filterCallMade);
        filterCallNotMadeChip.setChecked(filterCallNotMade);

        // Date filter options
        RadioGroup dateRadioGroup = dialogView.findViewById(R.id.date_filter_radio_group);
        RadioButton radioAllDates = dialogView.findViewById(R.id.radio_all_dates);
        RadioButton radioToday = dialogView.findViewById(R.id.radio_today);
        RadioButton radioYesterday = dialogView.findViewById(R.id.radio_yesterday);
        RadioButton radioEarlier = dialogView.findViewById(R.id.radio_earlier);
        RadioButton radioCustomRange = dialogView.findViewById(R.id.radio_custom_range);

        // Date range selection layout
        LinearLayout dateRangeLayout = dialogView.findViewById(R.id.date_range_layout);
        TextView tvFromDate = dialogView.findViewById(R.id.tv_from_date);
        TextView tvToDate = dialogView.findViewById(R.id.tv_to_date);

        // Set current date filter selections
        if (filterToday) radioToday.setChecked(true);
        else if (filterYesterday) radioYesterday.setChecked(true);
        else if (filterEarlier) radioEarlier.setChecked(true);
        else if (useCustomDateRange) {
            radioCustomRange.setChecked(true);
            dateRangeLayout.setVisibility(View.VISIBLE);
            // Display selected dates if available
            if (fromDate != null) {
                tvFromDate.setText(dateFormat.format(fromDate));
            }
            if (toDate != null) {
                tvToDate.setText(dateFormat.format(toDate));
            }
        } else {
            radioAllDates.setChecked(true);
        }

        // Setup date range visibility toggle on radio button selection
        dateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_custom_range) {
                dateRangeLayout.setVisibility(View.VISIBLE);
            } else {
                dateRangeLayout.setVisibility(View.GONE);
            }
        });

        // Setup date pickers
        tvFromDate.setOnClickListener(v -> {
            showDatePickerDialog(tvFromDate, true);
        });

        tvToDate.setOnClickListener(v -> {
            showDatePickerDialog(tvToDate, false);
        });

        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Update sort state
            currentSortBy = sortNameChip.isChecked() ? "name" : sortDateChip.isChecked() ? "date" : null;

            // Update filter states
            filterInterested = filterInterestedChip.isChecked();
            filterNotInterested = filterNotInterestedChip.isChecked();
            filterCallMade = filterCallMadeChip.isChecked();
            filterCallNotMade = filterCallNotMadeChip.isChecked();

            // Update date filter states
            filterToday = radioToday.isChecked();
            filterYesterday = radioYesterday.isChecked();
            filterEarlier = radioEarlier.isChecked();
            useCustomDateRange = radioCustomRange.isChecked();

            // If none of the specific date filters are selected, treat as "all dates"
            if (!filterToday && !filterYesterday && !filterEarlier && !useCustomDateRange) {
                // All dates
            }

            applyFiltersAndSorting();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    // Update the applyFiltersAndSorting method to remove parameters and use class variables
    private void applyFiltersAndSorting() {
        filteredList.clear();
        activeFiltersChipGroup.removeAllViews();

        // Apply filters
        for (Student student : studentList) {
            boolean matchesFilter = true;

            // Apply each filter if selected
            if (filterInterested && !student.getIsInterested()) matchesFilter = false;
            if (filterNotInterested && student.getIsInterested()) matchesFilter = false;
            if (filterCallMade && !"called".equals(student.getCallStatus())) matchesFilter = false;
            if (filterCallNotMade && "called".equals(student.getCallStatus())) matchesFilter = false;

            // Apply date filters
            String today = dateFormat.format(new Date());
            String yesterday = dateFormat.format(new Date(System.currentTimeMillis() - 86400000));

            if (filterToday && !today.equals(student.getSubmissionDate())) {
                matchesFilter = false;
            } else if (filterYesterday && !yesterday.equals(student.getSubmissionDate())) {
                matchesFilter = false;
            } else if (filterEarlier && (today.equals(student.getSubmissionDate()) || yesterday.equals(student.getSubmissionDate()))) {
                matchesFilter = false;
            } else if (useCustomDateRange && fromDate != null && toDate != null) {
                try {
                    Date studentDate = dateFormat.parse(student.getSubmissionDate());
                    // Add one day to toDate to make the range inclusive
                    Calendar toCalendar = Calendar.getInstance();
                    toCalendar.setTime(toDate);
                    toCalendar.add(Calendar.DAY_OF_MONTH, 1);

                    if (studentDate.before(fromDate) || studentDate.after(toCalendar.getTime())) {
                        matchesFilter = false;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                    matchesFilter = false;
                }
            }

            if (matchesFilter) filteredList.add(student);
        }

        // Apply sorting
        if (currentSortBy != null) {
            Collections.sort(filteredList, (s1, s2) -> {
                if ("name".equals(currentSortBy)) {
                    return s1.getName().compareToIgnoreCase(s2.getName());
                } else if ("date".equals(currentSortBy)) {
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
            });
        }

        // Update chips to reflect current filters/sort
        if (currentSortBy != null) addChip(currentSortBy.equals("name") ? "Sort: Name" : "Sort: Date");
        if (filterInterested) addChip("Interested");
        if (filterNotInterested) addChip("Not Interested");
        if (filterCallMade) addChip("Call Made");
        if (filterCallNotMade) addChip("Call Not Made");
        if (filterToday) addChip("Today");
        if (filterYesterday) addChip("Yesterday");
        if (filterEarlier) addChip("Earlier");
        if (useCustomDateRange && fromDate != null && toDate != null) {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            addChip("Date: " + displayFormat.format(fromDate) + " - " + displayFormat.format(toDate));
        }

        adapter.notifyDataSetChanged();
    }


    private void showDatePickerDialog(TextView targetTextView, boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        if ((isFromDate && fromDate != null) || (!isFromDate && toDate != null)) {
            calendar.setTime(isFromDate ? fromDate : toDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

                    // Update appropriate date variable
                    if (isFromDate) {
                        fromDate = selectedCalendar.getTime();
                    } else {
                        toDate = selectedCalendar.getTime();
                    }

                    // Format for display
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    targetTextView.setText(displayFormat.format(selectedCalendar.getTime()));
                },
                year, month, day
        );

        datePickerDialog.show();
    }



    // Update the addChip method to handle the new date range filter
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
                default:
                    if (text.startsWith("Date: ")) {
                        useCustomDateRange = false;
                        fromDate = null;
                        toDate = null;
                    }
                    break;
            }
            applyFiltersAndSorting();
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