package com.android.doctorcube.adminpannel.adminhome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.doctorcube.R;
import com.android.doctorcube.adminpannel.ChipManager;
import com.android.doctorcube.adminpannel.FilterManager;
import com.android.doctorcube.adminpannel.SortFilterDialogManager;
import com.android.doctorcube.adminpannel.Student;
import com.android.doctorcube.adminpannel.StudentDataLoader;
import com.android.doctorcube.adminpannel.StudentSorter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentExportXLData extends Fragment {

    private static final int STORAGE_PERMISSION_CODE = 100;

    private List<Student> studentList;
    private List<Student> filteredStudentList;
    private Button exportButton, viewButton;
    private TextView statusTextView, filterDetailsTextView;
    private FloatingActionButton fabSortFilter;
    private File exportedFile;
    private StudentDataLoader dataLoader;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

    // Reused classes from FragmentAdminHome
    private FilterManager filterManager;
    private SortFilterDialogManager dialogManager;
    private ChipManager chipManager; // Optional, if you want to show chips here

    public static FragmentExportXLData newInstance(Bundle args) {
        FragmentExportXLData fragment = new FragmentExportXLData();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataLoader = new StudentDataLoader();
        studentList = new ArrayList<>();
        filteredStudentList = new ArrayList<>();

        // Initialize FilterManager with default state
        filterManager = new FilterManager(studentList, filteredStudentList, new StudentSorter(dateFormat, getContext()), dateFormat, displayFormat, getContext());
        dialogManager = new SortFilterDialogManager(getContext(), filterManager);
        dialogManager.setOnApplyCallback(this::updateFilterDetailsAndStatus); // Set callback to update UI after applying filters
        // chipManager = new ChipManager(null, filterManager, null); // Uncomment if you add a ChipGroup to XML
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export_xl_data, container, false);

        exportButton = view.findViewById(R.id.btn_export_excel);
        viewButton = view.findViewById(R.id.btn_view_excel);
        statusTextView = view.findViewById(R.id.tv_export_status);
        filterDetailsTextView = view.findViewById(R.id.tv_filter_details);
        fabSortFilter = view.findViewById(R.id.fab_sort_filter);

        viewButton.setEnabled(false);
        statusTextView.setText("Loading student data from Firebase...");

        loadStudentDataFromFirebase();

        exportButton.setOnClickListener(v -> checkStoragePermission());
        viewButton.setOnClickListener(v -> viewExportedFile());
        fabSortFilter.setOnClickListener(v -> dialogManager.showSortFilterDialog());

        return view;
    }

    private void loadStudentDataFromFirebase() {
        dataLoader.loadStudents(new StudentDataLoader.DataLoadListener() {
            @Override
            public void onDataLoaded(List<Student> students) {
                studentList.clear();
                studentList.addAll(students);
                filterManager.applyFiltersAndSorting();
                if (filteredStudentList.isEmpty()) {
                    statusTextView.setText("No filtered data available to export.");
                    exportButton.setEnabled(false);
                    filterDetailsTextView.setText("No filters applied or no matching data.");
                } else {
                    statusTextView.setText("Ready to export " + filteredStudentList.size() + " student records.");
                    exportButton.setEnabled(true);
                    updateFilterDetailsAndStatus();
                }
            }

            @Override
            public void onDataLoadFailed(String error) {
                Toast.makeText(getContext(), "Failed to load data: " + error, Toast.LENGTH_SHORT).show();
                statusTextView.setText("Failed to load data from Firebase.");
                exportButton.setEnabled(false);
            }
        });
    }

    public void updateFilterDetailsAndStatus() {
        StringBuilder filterDetails = new StringBuilder("Applied Filters:\n");
        if (filterManager.getCurrentSortBy() != null) {
            filterDetails.append("Sort By: ").append(filterManager.getCurrentSortBy()).append("\n");
        }
        if (!filterManager.getSearchQuery().isEmpty()) {
            filterDetails.append("Search: ").append(filterManager.getSearchQuery()).append("\n");
        }
        if (filterManager.isFilterInterested()) filterDetails.append("Interested: Yes\n");
        if (filterManager.isFilterNotInterested()) filterDetails.append("Not Interested: Yes\n");
        if (filterManager.isFilterAdmitted()) filterDetails.append("Admitted: Yes\n");
        if (filterManager.isFilterNotAdmitted()) filterDetails.append("Not Admitted: Yes\n");
        if (filterManager.isFilterCalled()) filterDetails.append("Called: Yes\n");
        if (filterManager.isFilterNotCalled()) filterDetails.append("Not Called: Yes\n");
        switch (filterManager.getDateFilter()) {
            case "today": filterDetails.append("Date: Today\n"); break;
            case "yesterday": filterDetails.append("Date: Yesterday\n"); break;
            case "last_week": filterDetails.append("Date: Last Week\n"); break;
            case "last_month": filterDetails.append("Date: Last Month\n"); break;
            case "last_updated": filterDetails.append("Date: Last Updated\n"); break;
            case "firebase_push": filterDetails.append("Date: Firebase Push\n"); break;
            case "custom":
                if (filterManager.getFromDate() != null && filterManager.getToDate() != null) {
                    filterDetails.append("Date Range: ")
                            .append(displayFormat.format(filterManager.getFromDate()))
                            .append(" - ")
                            .append(displayFormat.format(filterManager.getToDate()))
                            .append("\n");
                }
                break;
        }
        filterDetailsTextView.setText(filterDetails.toString().trim());
        statusTextView.setText("Ready to export " + filteredStudentList.size() + " student records.");
        exportButton.setEnabled(!filteredStudentList.isEmpty());
    }

    private void checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            exportToExcel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportToExcel();
        } else {
            Toast.makeText(getContext(), "Storage permission denied!", Toast.LENGTH_SHORT).show();
            statusTextView.setText("Export failed: Storage permission denied.");
        }
    }

    private void exportToExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Filtered Students");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Mobile", "Email", "State", "City", "Interested Country",
                "Has NEET Score", "NEET Score", "Has Passport", "Submission Date", "Call Status",
                "Last Call Date", "Is Interested", "Is Admitted"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (int i = 0; i < filteredStudentList.size(); i++) {
            Student student = filteredStudentList.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(student.getId() != null ? student.getId() : "");
            row.createCell(1).setCellValue(student.getName() != null ? student.getName() : "");
            row.createCell(2).setCellValue(student.getMobile() != null ? student.getMobile() : "");
            row.createCell(3).setCellValue(student.getEmail() != null ? student.getEmail() : "");
            row.createCell(4).setCellValue(student.getState() != null ? student.getState() : "");
            row.createCell(5).setCellValue(student.getCity() != null ? student.getCity() : "");
            row.createCell(6).setCellValue(student.getInterestedCountry() != null ? student.getInterestedCountry() : "");
            row.createCell(7).setCellValue(student.getHasNeetScore() != null ? student.getHasNeetScore() : "");
            row.createCell(8).setCellValue(student.getNeetScore() != null ? student.getNeetScore() : "");
            row.createCell(9).setCellValue(student.getHasPassport() != null ? student.getHasPassport() : "");
            row.createCell(10).setCellValue(student.getSubmissionDate() != null ? student.getSubmissionDate() : "");
            row.createCell(11).setCellValue(student.getCallStatus() != null ? student.getCallStatus() : "");
            row.createCell(12).setCellValue(student.getLastCallDate() != null ? student.getLastCallDate() : "");
            row.createCell(13).setCellValue(student.getIsInterested() ? "Yes" : "No");
            row.createCell(14).setCellValue(student.isAdmitted() ? "Yes" : "No");
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Students_" + timeStamp + ".xlsx";
        exportedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try (FileOutputStream fos = new FileOutputStream(exportedFile)) {
            workbook.write(fos);
            workbook.close();
            Toast.makeText(getContext(), "Excel file saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
            statusTextView.setText("Export successful! File saved as " + fileName);
            viewButton.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error saving Excel file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            statusTextView.setText("Export failed: " + e.getMessage());
            viewButton.setEnabled(false);
        }
    }

    private void viewExportedFile() {
        if (exportedFile != null && exportedFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(exportedFile);
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "No app found to open Excel file!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "File not found!", Toast.LENGTH_SHORT).show();
        }
    }
}