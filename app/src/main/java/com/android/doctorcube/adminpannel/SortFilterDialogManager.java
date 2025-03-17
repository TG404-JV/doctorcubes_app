package com.android.doctorcube.adminpannel;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.doctorcube.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class SortFilterDialogManager {
    private final Context context;
    private final FilterManager filterManager;
    private Runnable onApplyCallback; // Optional callback for custom updates

    public SortFilterDialogManager(Context context, FilterManager filterManager) {
        this.context = context;
        this.filterManager = filterManager;
    }

    // Optional: Allow setting a callback for post-apply actions
    public void setOnApplyCallback(Runnable callback) {
        this.onApplyCallback = callback;
    }

    public void showSortFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sort_filter, null);
        builder.setView(dialogView);

        // Sort Chips
        ChipGroup sortGroup = dialogView.findViewById(R.id.sort_chip_group);
        Chip sortNameAscChip = dialogView.findViewById(R.id.chip_sort_name_asc);
        Chip sortNameDescChip = dialogView.findViewById(R.id.chip_sort_name_desc);
        Chip sortDateNewestChip = dialogView.findViewById(R.id.chip_sort_date_newest);
        Chip sortDateOldestChip = dialogView.findViewById(R.id.chip_sort_date_oldest);
        Chip sortLastUpdatedNewestChip = dialogView.findViewById(R.id.chip_sort_last_updated_newest);
        Chip sortLastUpdatedOldestChip = dialogView.findViewById(R.id.chip_sort_last_updated_oldest);
        Chip sortFirebasePushNewestChip = dialogView.findViewById(R.id.chip_sort_firebase_push_newest);
        Chip sortFirebasePushOldestChip = dialogView.findViewById(R.id.chip_sort_firebase_push_oldest);

        String currentSortBy = filterManager.getCurrentSortBy();
        if ("name_asc".equals(currentSortBy)) sortNameAscChip.setChecked(true);
        else if ("name_desc".equals(currentSortBy)) sortNameDescChip.setChecked(true);
        else if ("date_newest".equals(currentSortBy)) sortDateNewestChip.setChecked(true);
        else if ("date_oldest".equals(currentSortBy)) sortDateOldestChip.setChecked(true);
        else if ("last_updated_newest".equals(currentSortBy)) sortLastUpdatedNewestChip.setChecked(true);
        else if ("last_updated_oldest".equals(currentSortBy)) sortLastUpdatedOldestChip.setChecked(true);
        else if ("firebase_push_newest".equals(currentSortBy)) sortFirebasePushNewestChip.setChecked(true);
        else if ("firebase_push_oldest".equals(currentSortBy)) sortFirebasePushOldestChip.setChecked(true);

        // Filter Chips
        Chip filterInterestedChip = dialogView.findViewById(R.id.chip_filter_interested);
        Chip filterNotInterestedChip = dialogView.findViewById(R.id.chip_filter_not_interested);
        Chip filterAdmittedChip = dialogView.findViewById(R.id.chip_filter_admitted);
        Chip filterNotAdmittedChip = dialogView.findViewById(R.id.chip_filter_not_admitted);
        Chip filterCallMadeChip = dialogView.findViewById(R.id.chip_filter_call_made);
        Chip filterCallNotMadeChip = dialogView.findViewById(R.id.chip_filter_call_not_made);
        filterInterestedChip.setChecked(filterManager.isFilterInterested());
        filterNotInterestedChip.setChecked(filterManager.isFilterNotInterested());
        filterAdmittedChip.setChecked(filterManager.isFilterAdmitted());
        filterNotAdmittedChip.setChecked(filterManager.isFilterNotAdmitted());
        filterCallMadeChip.setChecked(filterManager.isFilterCalled());
        filterCallNotMadeChip.setChecked(filterManager.isFilterNotCalled());

        // Date Filters
        RadioGroup dateRadioGroup = dialogView.findViewById(R.id.date_filter_radio_group);
        RadioButton radioAllDates = dialogView.findViewById(R.id.radio_all_dates);
        RadioButton radioToday = dialogView.findViewById(R.id.radio_today);
        RadioButton radioYesterday = dialogView.findViewById(R.id.radio_yesterday);
        RadioButton radioLastWeek = dialogView.findViewById(R.id.radio_last_week);
        RadioButton radioLastMonth = dialogView.findViewById(R.id.radio_last_month);
        RadioButton radioLastUpdated = dialogView.findViewById(R.id.radio_last_updated);
        RadioButton radioFirebasePush = dialogView.findViewById(R.id.radio_firebase_push);
        RadioButton radioCustomRange = dialogView.findViewById(R.id.radio_custom_range);

        LinearLayout dateRangeLayout = dialogView.findViewById(R.id.date_range_layout);
        TextView tvFromDate = dialogView.findViewById(R.id.tv_from_date);
        TextView tvToDate = dialogView.findViewById(R.id.tv_to_date);

        switch (filterManager.getDateFilter()) {
            case "today": radioToday.setChecked(true); break;
            case "yesterday": radioYesterday.setChecked(true); break;
            case "last_week": radioLastWeek.setChecked(true); break;
            case "last_month": radioLastMonth.setChecked(true); break;
            case "last_updated": radioLastUpdated.setChecked(true); break;
            case "firebase_push": radioFirebasePush.setChecked(true); break;
            case "custom":
                radioCustomRange.setChecked(true);
                dateRangeLayout.setVisibility(View.VISIBLE);
                if (filterManager.getFromDate() != null) tvFromDate.setText(filterManager.displayFormat.format(filterManager.getFromDate()));
                if (filterManager.getToDate() != null) tvToDate.setText(filterManager.displayFormat.format(filterManager.getToDate()));
                break;
            default: radioAllDates.setChecked(true);
        }

        dateRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            dateRangeLayout.setVisibility(checkedId == R.id.radio_custom_range ? View.VISIBLE : View.GONE);
        });

        tvFromDate.setOnClickListener(v -> showDatePickerDialog(tvFromDate, true));
        tvToDate.setOnClickListener(v -> showDatePickerDialog(tvToDate, false));

        // Buttons
        Button btnApply = dialogView.findViewById(R.id.btn_apply);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.create();

        btnApply.setOnClickListener(v -> {
            // Sort
            if (sortNameAscChip.isChecked()) filterManager.setCurrentSortBy("name_asc");
            else if (sortNameDescChip.isChecked()) filterManager.setCurrentSortBy("name_desc");
            else if (sortDateNewestChip.isChecked()) filterManager.setCurrentSortBy("date_newest");
            else if (sortDateOldestChip.isChecked()) filterManager.setCurrentSortBy("date_oldest");
            else if (sortLastUpdatedNewestChip.isChecked()) filterManager.setCurrentSortBy("last_updated_newest");
            else if (sortLastUpdatedOldestChip.isChecked()) filterManager.setCurrentSortBy("last_updated_oldest");
            else if (sortFirebasePushNewestChip.isChecked()) filterManager.setCurrentSortBy("firebase_push_newest");
            else if (sortFirebasePushOldestChip.isChecked()) filterManager.setCurrentSortBy("firebase_push_oldest");
            else filterManager.setCurrentSortBy(null);

            // Filters
            filterManager.setFilterInterested(filterInterestedChip.isChecked());
            filterManager.setFilterNotInterested(filterNotInterestedChip.isChecked());
            filterManager.setFilterAdmitted(filterAdmittedChip.isChecked());
            filterManager.setFilterNotAdmitted(filterNotAdmittedChip.isChecked());
            filterManager.setFilterCalled(filterCallMadeChip.isChecked());
            filterManager.setFilterNotCalled(filterCallNotMadeChip.isChecked());

            // Date Filter
            if (radioToday.isChecked()) filterManager.setDateFilter("today");
            else if (radioYesterday.isChecked()) filterManager.setDateFilter("yesterday");
            else if (radioLastWeek.isChecked()) filterManager.setDateFilter("last_week");
            else if (radioLastMonth.isChecked()) filterManager.setDateFilter("last_month");
            else if (radioLastUpdated.isChecked()) filterManager.setDateFilter("last_updated");
            else if (radioFirebasePush.isChecked()) filterManager.setDateFilter("firebase_push");
            else if (radioCustomRange.isChecked()) filterManager.setDateFilter("custom");
            else filterManager.setDateFilter("all");

            filterManager.setUseCustomDateRange("custom".equals(filterManager.getDateFilter()));
            filterManager.applyFiltersAndSorting();
            if (onApplyCallback != null) {
                onApplyCallback.run(); // Execute custom callback if set
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDatePickerDialog(TextView targetTextView, boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();
        if ((isFromDate && filterManager.getFromDate() != null) || (!isFromDate && filterManager.getToDate() != null)) {
            calendar.setTime(isFromDate ? filterManager.getFromDate() : filterManager.getToDate());
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                    if (isFromDate) {
                        filterManager.setFromDate(selectedCalendar.getTime());
                    } else {
                        filterManager.setToDate(selectedCalendar.getTime());
                    }
                    targetTextView.setText(filterManager.displayFormat.format(selectedCalendar.getTime()));
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}