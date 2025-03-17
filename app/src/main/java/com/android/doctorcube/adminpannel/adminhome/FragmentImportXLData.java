package com.android.doctorcube.adminpannel.adminhome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.doctorcube.R;
import com.android.doctorcube.adminpannel.Student;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FragmentImportXLData extends Fragment {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private TextView statusTextView;
    private Button selectExcelButton;
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());

    // Activity Result Launcher for file picker
    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleFileSelection);

    public FragmentImportXLData() {
        // Required empty public constructor
    }

    public static FragmentImportXLData newInstance() {
        return new FragmentImportXLData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import_x_l_data, container, false);

        statusTextView = view.findViewById(R.id.tv_import_status);
        selectExcelButton = view.findViewById(R.id.btn_select_excel);

        selectExcelButton.setOnClickListener(v -> checkStoragePermission());

        return view;
    }

    private void checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            openFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            Toast.makeText(getContext(), "Storage permission denied!", Toast.LENGTH_SHORT).show();
            statusTextView.setText("Import failed: Storage permission denied.");
        }
    }

    private void openFilePicker() {
        filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private void handleFileSelection(Uri uri) {
        if (uri == null) {
            Toast.makeText(getContext(), "No file selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        statusTextView.setText("Processing Excel file...");
        List<Student> students = parseExcelFile(uri);
        if (students.isEmpty()) {
            statusTextView.setText("No valid data found in the Excel file.");
            return;
        }

        uploadToFirebase(students);
    }

    private List<Student> parseExcelFile(Uri uri) {
        List<Student> students = new ArrayList<>();
        try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            String[] headers = {"ID", "Name", "Mobile", "Email", "State", "City", "Interested Country",
                    "Has NEET Score", "NEET Score", "Has Passport", "Submission Date", "Call Status",
                    "Last Call Date", "Is Interested", "Is Admitted"};

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skip header row (i=0)
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Student student = new Student();
                student.setId(getCellValue(row, 0));
                student.setName(getCellValue(row, 1));
                student.setMobile(getCellValue(row, 2));
                student.setEmail(getCellValue(row, 3));
                student.setState(getCellValue(row, 4));
                student.setCity(getCellValue(row, 5));
                student.setInterestedCountry(getCellValue(row, 6));
                student.setHasNeetScore(getCellValue(row, 7));
                student.setNeetScore(getCellValue(row, 8));
                student.setHasPassport(getCellValue(row, 9));
                student.setSubmissionDate(getCellValue(row, 10));
                student.setCallStatus(getCellValue(row, 11));
                student.setLastCallDate(getCellValue(row, 12));
                student.setIsInterested("Yes".equalsIgnoreCase(getCellValue(row, 13)));
                student.setAdmitted("Yes".equalsIgnoreCase(getCellValue(row, 14)));

                // Ensure submission date is in "ddMMyy" format if provided, otherwise use current date
                if (student.getSubmissionDate() == null || student.getSubmissionDate().isEmpty()) {
                    student.setSubmissionDate(dateFormat.format(new Date()));
                }

                students.add(student);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error reading Excel file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return students;
    }

    private String getCellValue(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null) return "";
        return row.getCell(cellIndex).toString().trim();
    }

    private void uploadToFirebase(List<Student> students) {
        statusTextView.setText("Uploading " + students.size() + " students to Firebase...");

        for (Student student : students) {
            String dateKey = student.getSubmissionDate(); // Use submission date as the parent node
            String studentId = student.getId() != null && !student.getId().isEmpty() ?
                    student.getId() : databaseReference.child(dateKey).push().getKey(); // Generate ID if not provided

            Map<String, Object> studentData = new HashMap<>();
            studentData.put("name", student.getName());
            studentData.put("mobile", student.getMobile());
            studentData.put("email", student.getEmail());
            studentData.put("state", student.getState());
            studentData.put("city", student.getCity());
            studentData.put("interestedCountry", student.getInterestedCountry());
            studentData.put("hasNeetScore", student.getHasNeetScore());
            studentData.put("neetScore", student.getNeetScore());
            studentData.put("hasPassport", student.getHasPassport());
            studentData.put("submissionDate", student.getSubmissionDate());
            studentData.put("callStatus", student.getCallStatus() != null ? student.getCallStatus() : "pending");
            studentData.put("lastCallDate", student.getLastCallDate());
            studentData.put("isInterested", student.getIsInterested());
            studentData.put("isAdmitted", student.isAdmitted());
            studentData.put("firebasePushDate", dateFormat.format(new Date())); // Add push date

            // Upload to Firebase under registrations/dateKey/studentId
            databaseReference.child(dateKey).child(studentId).setValue(studentData)
                    .addOnSuccessListener(aVoid -> {
                        // Update status incrementally if needed
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Upload failed for " + student.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        statusTextView.setText("Upload completed: " + students.size() + " students imported.");
        Toast.makeText(getContext(), "Data upload completed!", Toast.LENGTH_SHORT).show();
    }
}