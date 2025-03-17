package com.android.doctorcube.adminpannel;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentDataLoader {
    private static final String TAG = "StudentDataLoader";
    private DatabaseReference databaseReference;
    private List<Student> studentList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());

    public interface DataLoadListener {
        void onDataLoaded(List<Student> students);
        void onDataLoadFailed(String error);
    }

    public StudentDataLoader() {
        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");
        studentList = new ArrayList<>();
    }

    public void loadStudents(final DataLoadListener listener) {
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
                            student.setFirebasePushDate(dateKey); // Assuming submission date is push date

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

                            // Admin Changes Node
                            if (studentSnapshot.hasChild("admin_changes")) {
                                DataSnapshot adminChanges = studentSnapshot.child("admin_changes");
                                if (adminChanges.hasChild("updated_date")) {
                                    student.setLastUpdatedDate(adminChanges.child("updated_date").getValue(String.class));
                                }
                            }

                            studentList.add(student);
                            Log.d(TAG, "👤 Added student: " + student.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "🚨 Error parsing student data: " + e.getMessage());
                        }
                    }
                }
                listener.onDataLoaded(studentList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "🚨 Firebase Error: " + error.getMessage());
                listener.onDataLoadFailed(error.getMessage());
            }
        });
    }

    // Method to update student data from admin side
    public void updateStudent(Student student, String field, Object value) {
        DatabaseReference studentRef = databaseReference.child(student.getSubmissionDate()).child(student.getId());
        studentRef.child(field).setValue(value);

        // Update admin_changes node with current date
        String currentDate = dateFormat.format(new Date());
        studentRef.child("admin_changes").child("updated_date").child("AdminChanges").setValue(currentDate)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Admin update recorded for " + student.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update admin_changes: " + e.getMessage()));
    }
}