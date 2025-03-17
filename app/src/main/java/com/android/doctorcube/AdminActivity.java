package com.android.doctorcube;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    private Toolbar toolbar;
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private boolean useCustomDateRange = false;
    private Date fromDate = null;
    private Date toDate = null;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());
    private String currentSortBy = null;
    private boolean filterInterested, filterNotInterested, filterCallMade, filterCallNotMade, filterToday, filterYesterday, filterEarlier;
    private String searchQuery = "";

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
        toolbar = findViewById(R.id.toolbar);
        searchEditText = findViewById(R.id.search_edit_text);
        clearSearchButton = findViewById(R.id.clear_search_button);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Admin Panel");

        // Setup Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
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
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        swipeRefreshLayout.setOnRefreshListener(this::loadStudentData);

        // Setup Search Bar
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                applyFiltersAndSorting();
                clearSearchButton.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            searchQuery = "";
            applyFiltersAndSorting();
            clearSearchButton.setVisibility(View.GONE);
        });

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
                    String dateKey = dateSnapshot.getKey();
                    Log.d(TAG, "📅 Processing Date Key: " + dateKey);

                    for (DataSnapshot studentSnapshot : dateSnapshot.getChildren()) {
                        try {
                            Student student = new Student();
                            student.setId(studentSnapshot.getKey());
                            student.setSubmissionDate(dateKey);

                            if (studentSnapshot.hasChild("name")) student.setName(studentSnapshot.child("name").getValue(String.class));
                            if (studentSnapshot.hasChild("mobile")) student.setMobile(studentSnapshot.child("mobile").getValue(String.class));
                            if (studentSnapshot.hasChild("email")) student.setEmail(studentSnapshot.child("email").getValue(String.class));
                            if (studentSnapshot.hasChild("state")) student.setState(studentSnapshot.child("state").getValue(String.class));
                            if (studentSnapshot.hasChild("city")) student.setCity(studentSnapshot.child("city").getValue(String.class));
                            if (studentSnapshot.hasChild("interestedCountry")) student.setInterestedCountry(studentSnapshot.child("interestedCountry").getValue(String.class));
                            if (studentSnapshot.hasChild("hasNeetScore")) student.setHasNeetScore(studentSnapshot.child("hasNeetScore").getValue(String.class));
                            if (studentSnapshot.hasChild("neetScore")) student.setNeetScore(studentSnapshot.child("neetScore").getValue(String.class));
                            if (studentSnapshot.hasChild("hasPassport")) student.setHasPassport(studentSnapshot.child("hasPassport").getValue(String.class));
                            if (studentSnapshot.hasChild("lastCallDate")) student.setLastCallDate(studentSnapshot.child("lastCallDate").getValue(String.class));

                            student.setCallStatus(studentSnapshot.hasChild("callStatus") ? studentSnapshot.child("callStatus").getValue(String.class) : "pending");
                            student.setIsInterested(studentSnapshot.hasChild("isInterested") ? studentSnapshot.child("isInterested").getValue(Boolean.class) : false);
                            student.setAdmitted(studentSnapshot.hasChild("isAdmitted") ? studentSnapshot.child("isAdmitted").getValue(Boolean.class) : false);

                            studentList.add(student);
                            Log.d(TAG, "👤 Added student: " + student.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "🚨 Error parsing student data: " + e.getMessage());
                        }
                    }
                }

                applyFiltersAndSorting();
                swipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, "📊 RecyclerView updated with " + filteredList.size() + " students");
                if (filteredList.isEmpty()) showNoStudentsPopup();
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

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort_filter, null);
        builder.setView(dialogView);

        // Sort options
        ChipGroup sortGroup = dialogView.findViewById(R.id.sort_chip_group);
        Chip sortNameChip = dialogView.findViewById(R.id.chip_sort_name);
        Chip sortDateChip = dialogView.findViewById(R.id.chip_sort_date);
        if ("name".equals(currentSortBy)) sortNameChip.setChecked(true);
        else if ("date".equals(currentSortBy)) sortDateChip.setChecked(true);

        // Filter options
        Chip filterInterestedChip = dialogView.findViewById(R.id.chip_filter_interested);
        Chip filterNotInterestedChip = dialogView.findViewById(R.id.chip_filter_not_interested);
        Chip filterCallMadeChip = dialogView.findViewById(R.id.chip_filter_call_made);
        Chip filterCallNotMadeChip = dialogView.findViewById(R.id.chip_filter_call_not_made);
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
            if (fromDate != null) tvFromDate.setText(dateFormat.format(fromDate));
            if (toDate != null) tvToDate.setText(dateFormat.format(toDate));
        } else {
            radioAllDates.setChecked(true);
        }

        dateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            dateRangeLayout.setVisibility(checkedId == R.id.radio_custom_range ? View.VISIBLE : View.GONE);
        });

        tvFromDate.setOnClickListener(v -> showDatePickerDialog(tvFromDate, true));
        tvToDate.setOnClickListener(v -> showDatePickerDialog(tvToDate, false));

        builder.setPositiveButton("Apply", (dialog, which) -> {
            currentSortBy = sortNameChip.isChecked() ? "name" : sortDateChip.isChecked() ? "date" : null;

            filterInterested = filterInterestedChip.isChecked();
            filterNotInterested = filterNotInterestedChip.isChecked();
            filterCallMade = filterCallMadeChip.isChecked();
            filterCallNotMade = filterCallNotMadeChip.isChecked();

            filterToday = filterYesterday = filterEarlier = useCustomDateRange = false;
            if (radioToday.isChecked()) filterToday = true;
            else if (radioYesterday.isChecked()) filterYesterday = true;
            else if (radioEarlier.isChecked()) filterEarlier = true;
            else if (radioCustomRange.isChecked()) useCustomDateRange = true;

            applyFiltersAndSorting();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void applyFiltersAndSorting() {
        filteredList.clear();
        activeFiltersChipGroup.removeAllViews();

        Calendar todayCal = Calendar.getInstance();
        String today = dateFormat.format(todayCal.getTime());
        todayCal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = dateFormat.format(todayCal.getTime());

        for (Student student : studentList) {
            boolean matchesFilter = true;

            // Search filter
            if (!searchQuery.isEmpty()) {
                String queryLower = searchQuery.toLowerCase(Locale.getDefault());
                boolean matchesSearch = (student.getName() != null && student.getName().toLowerCase(Locale.getDefault()).contains(queryLower)) ||
                        (student.getMobile() != null && student.getMobile().toLowerCase(Locale.getDefault()).contains(queryLower));
                if (!matchesSearch) matchesFilter = false;
            }

            // Apply other filters
            if (filterInterested && !student.getIsInterested()) matchesFilter = false;
            if (filterNotInterested && student.getIsInterested()) matchesFilter = false;
            if (filterCallMade && !"called".equals(student.getCallStatus())) matchesFilter = false;
            if (filterCallNotMade && "called".equals(student.getCallStatus())) matchesFilter = false;

            if (filterToday && !today.equals(student.getSubmissionDate())) matchesFilter = false;
            if (filterYesterday && !yesterday.equals(student.getSubmissionDate())) matchesFilter = false;
            if (filterEarlier) {
                try {
                    Date studentDate = dateFormat.parse(student.getSubmissionDate());
                    Date yesterdayDate = dateFormat.parse(yesterday);
                    if (studentDate.compareTo(yesterdayDate) >= 0) matchesFilter = false;
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                    matchesFilter = false;
                }
            }
            if (useCustomDateRange && fromDate != null && toDate != null) {
                try {
                    Date studentDate = dateFormat.parse(student.getSubmissionDate());
                    Calendar toCal = Calendar.getInstance();
                    toCal.setTime(toDate);
                    toCal.set(Calendar.HOUR_OF_DAY, 23);
                    toCal.set(Calendar.MINUTE, 59);
                    toCal.set(Calendar.SECOND, 59);

                    if (studentDate.before(fromDate) || studentDate.after(toCal.getTime())) {
                        matchesFilter = false;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                    matchesFilter = false;
                }
            }

            if (matchesFilter) filteredList.add(student);
        }

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

        // Update chips
        if (!searchQuery.isEmpty()) addChip("Search: " + searchQuery);
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
                    if (isFromDate) {
                        fromDate = selectedCalendar.getTime();
                    } else {
                        toDate = selectedCalendar.getTime();
                    }
                    targetTextView.setText(dateFormat.format(selectedCalendar.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void addChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setTextColor(getResources().getColor(R.color.text_primary_color));
        chip.setChipBackgroundColorResource(R.color.card_background);
        chip.setCloseIconVisible(true);
        chip.setCloseIconTintResource(R.color.icon_tint);
        chip.setOnCloseIconClickListener(v -> {
            activeFiltersChipGroup.removeView(chip);
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
                    if (text.startsWith("Search: ")) searchEditText.setText("");
                    else if (text.startsWith("Date: ")) {
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

    private void showNoStudentsPopup() {
        new AlertDialog.Builder(this)
                .setTitle("No Students Found")
                .setMessage("No student registrations are available in the database or no matches for current filters.")
                .setPositiveButton("Refresh", (dialog, which) -> loadStudentData())
                .setNegativeButton("OK", null)
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