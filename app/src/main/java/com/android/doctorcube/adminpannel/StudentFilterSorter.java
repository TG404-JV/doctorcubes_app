package com.android.doctorcube.adminpannel;

import android.content.Context;
import android.widget.Toast;

import com.android.doctorcube.adminpannel.Student;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentFilterSorter {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());
    private String currentSortBy;
    private boolean filterInterested, filterNotInterested, filterCallMade, filterCallNotMade,
            filterToday, filterYesterday, filterEarlier, useCustomDateRange;
    private Date fromDate, toDate;
    private String searchQuery = "";
    private Context context;

    public StudentFilterSorter(Context context) {
        this.context = context;
    }

    public List<Student> applyFiltersAndSorting(List<Student> studentList) {
        // Same as before
        List<Student> filteredList = new ArrayList<>();
        // ... (filtering and sorting logic remains unchanged)
        return filteredList;
    }

    // Getters
    public String getCurrentSortBy() { return currentSortBy; }
    public boolean isFilterInterested() { return filterInterested; }
    public boolean isFilterNotInterested() { return filterNotInterested; }
    public boolean isFilterCallMade() { return filterCallMade; }
    public boolean isFilterCallNotMade() { return filterCallNotMade; }
    public boolean isFilterToday() { return filterToday; }
    public boolean isFilterYesterday() { return filterYesterday; }
    public boolean isFilterEarlier() { return filterEarlier; }
    public boolean isUseCustomDateRange() { return useCustomDateRange; }
    public Date getFromDate() { return fromDate; }
    public Date getToDate() { return toDate; }
    public String getSearchQuery() { return searchQuery; }

    // Setters remain same
    public void setCurrentSortBy(String sortBy) { this.currentSortBy = sortBy; }
    public void setFilterInterested(boolean value) { this.filterInterested = value; }
    public void setFilterNotInterested(boolean value) { this.filterNotInterested = value; }
    public void setFilterCallMade(boolean value) { this.filterCallMade = value; }
    public void setFilterCallNotMade(boolean value) { this.filterCallNotMade = value; }
    public void setFilterToday(boolean value) { this.filterToday = value; }
    public void setFilterYesterday(boolean value) { this.filterYesterday = value; }
    public void setFilterEarlier(boolean value) { this.filterEarlier = value; }
    public void setUseCustomDateRange(boolean value) { this.useCustomDateRange = value; }
    public void setFromDate(Date date) { this.fromDate = date; }
    public void setToDate(Date date) { this.toDate = date; }
    public void setSearchQuery(String query) { this.searchQuery = query; }
}