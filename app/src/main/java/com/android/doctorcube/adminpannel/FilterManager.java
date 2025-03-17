package com.android.doctorcube.adminpannel;


import android.content.Context;
import android.widget.Toast;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilterManager {
    private final List<Student> studentList;
    private final List<Student> filteredList;
    private final StudentSorter sorter;
    private final SimpleDateFormat dateFormat;
    public final SimpleDateFormat displayFormat;
    private final Context context;

    private String currentSortBy = null;
    private boolean filterInterested, filterNotInterested, filterAdmitted, filterNotAdmitted, filterCalled, filterNotCalled;
    private String dateFilter = "all";
    private String searchQuery = "";
    private boolean useCustomDateRange = false;
    private Date fromDate = null;
    private Date toDate = null;

    public FilterManager(List<Student> studentList, List<Student> filteredList, StudentSorter sorter,
                         SimpleDateFormat dateFormat, SimpleDateFormat displayFormat, Context context) {
        this.studentList = studentList;
        this.filteredList = filteredList;
        this.sorter = sorter;
        this.dateFormat = dateFormat;
        this.displayFormat = displayFormat;
        this.context = context;
    }

    public void applyFiltersAndSorting() {
        filteredList.clear();

        Calendar todayCal = Calendar.getInstance();
        String today = dateFormat.format(todayCal.getTime());
        todayCal.add(Calendar.DAY_OF_MONTH, -1);
        String yesterday = dateFormat.format(todayCal.getTime());
        Calendar weekCal = Calendar.getInstance();
        weekCal.add(Calendar.DAY_OF_MONTH, -7);
        String lastWeek = dateFormat.format(weekCal.getTime());
        Calendar monthCal = Calendar.getInstance();
        monthCal.add(Calendar.MONTH, -1);
        String lastMonth = dateFormat.format(monthCal.getTime());

        for (Student student : studentList) {
            boolean matchesFilter = true;

            // Search
            if (!searchQuery.isEmpty()) {
                String queryLower = searchQuery.toLowerCase(Locale.getDefault());
                boolean matchesSearch = (student.getName() != null && student.getName().toLowerCase(Locale.getDefault()).contains(queryLower)) ||
                        (student.getMobile() != null && student.getMobile().toLowerCase(Locale.getDefault()).contains(queryLower));
                if (!matchesSearch) matchesFilter = false;
            }

            // Filters
            if (filterInterested && !student.getIsInterested()) matchesFilter = false;
            if (filterNotInterested && student.getIsInterested()) matchesFilter = false;
            if (filterAdmitted && !student.isAdmitted()) matchesFilter = false;
            if (filterNotAdmitted && student.isAdmitted()) matchesFilter = false;
            if (filterCalled && !"called".equals(student.getCallStatus())) matchesFilter = false;
            if (filterNotCalled && "called".equals(student.getCallStatus())) matchesFilter = false;

            // Date Filters
            try {
                Date studentDate = dateFormat.parse(student.getSubmissionDate());
                Date lastUpdatedDate = student.getLastUpdatedDate() != null ? dateFormat.parse(student.getLastUpdatedDate()) : studentDate;
                Date firebasePushDate = student.getFirebasePushDate() != null ? dateFormat.parse(student.getFirebasePushDate()) : studentDate;

                switch (dateFilter) {
                    case "today":
                        if (!today.equals(student.getSubmissionDate())) matchesFilter = false;
                        break;
                    case "yesterday":
                        if (!yesterday.equals(student.getSubmissionDate())) matchesFilter = false;
                        break;
                    case "last_week":
                        if (studentDate.before(weekCal.getTime())) matchesFilter = false;
                        break;
                    case "last_month":
                        if (studentDate.before(monthCal.getTime())) matchesFilter = false;
                        break;
                    case "last_updated":
                        if (!today.equals(dateFormat.format(lastUpdatedDate))) matchesFilter = false;
                        break;
                    case "firebase_push":
                        if (!today.equals(dateFormat.format(firebasePushDate))) matchesFilter = false;
                        break;
                    case "custom":
                        if (fromDate != null && toDate != null) {
                            Calendar toCal = Calendar.getInstance();
                            toCal.setTime(toDate);
                            toCal.set(Calendar.HOUR_OF_DAY, 23);
                            toCal.set(Calendar.MINUTE, 59);
                            toCal.set(Calendar.SECOND, 59);
                            if (studentDate.before(fromDate) || studentDate.after(toCal.getTime())) matchesFilter = false;
                        }
                        break;
                }
            } catch (ParseException e) {
                Toast.makeText(context, "Error parsing date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                matchesFilter = false;
            }

            if (matchesFilter) filteredList.add(student);
        }

        // Sorting
        if (currentSortBy != null) {
            sorter.sortStudents(filteredList, currentSortBy);
        }
    }

    // Getters and Setters
    public String getCurrentSortBy() { return currentSortBy; }
    public void setCurrentSortBy(String sortBy) { this.currentSortBy = sortBy; }
    public boolean isFilterInterested() { return filterInterested; }
    public void setFilterInterested(boolean filterInterested) { this.filterInterested = filterInterested; }
    public boolean isFilterNotInterested() { return filterNotInterested; }
    public void setFilterNotInterested(boolean filterNotInterested) { this.filterNotInterested = filterNotInterested; }
    public boolean isFilterAdmitted() { return filterAdmitted; }
    public void setFilterAdmitted(boolean filterAdmitted) { this.filterAdmitted = filterAdmitted; }
    public boolean isFilterNotAdmitted() { return filterNotAdmitted; }
    public void setFilterNotAdmitted(boolean filterNotAdmitted) { this.filterNotAdmitted = filterNotAdmitted; }
    public boolean isFilterCalled() { return filterCalled; }
    public void setFilterCalled(boolean filterCalled) { this.filterCalled = filterCalled; }
    public boolean isFilterNotCalled() { return filterNotCalled; }
    public void setFilterNotCalled(boolean filterNotCalled) { this.filterNotCalled = filterNotCalled; }
    public String getDateFilter() { return dateFilter; }
    public void setDateFilter(String dateFilter) { this.dateFilter = dateFilter; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
    public boolean isUseCustomDateRange() { return useCustomDateRange; }
    public void setUseCustomDateRange(boolean useCustomDateRange) { this.useCustomDateRange = useCustomDateRange; }
    public Date getFromDate() { return fromDate; }
    public void setFromDate(Date fromDate) { this.fromDate = fromDate; }
    public Date getToDate() { return toDate; }
    public void setToDate(Date toDate) { this.toDate = toDate; }
}