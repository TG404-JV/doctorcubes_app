package com.android.doctorcube.adminpannel;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
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
                            student.setFirebasePushDate(dateKey);

                            // Handle null values and incorrect types
                            student.setName(DataSnapshotHelper.getString(studentSnapshot, "name"));
                            student.setMobile(DataSnapshotHelper.getString(studentSnapshot, "mobile"));
                            student.setEmail(DataSnapshotHelper.getString(studentSnapshot, "email"));
                            student.setState(DataSnapshotHelper.getString(studentSnapshot, "state"));
                            student.setCity(DataSnapshotHelper.getString(studentSnapshot, "city"));
                            student.setInterestedCountry(DataSnapshotHelper.getString(studentSnapshot, "interestedCountry"));
                            student.setHasNeetScore(DataSnapshotHelper.getString(studentSnapshot, "hasNeetScore"));
                            student.setNeetScore(DataSnapshotHelper.getString(studentSnapshot, "neetScore"));
                            student.setHasPassport(DataSnapshotHelper.getString(studentSnapshot, "hasPassport"));
                            student.setLastCallDate(DataSnapshotHelper.getString(studentSnapshot, "lastCallDate"));
                            student.setCallStatus(DataSnapshotHelper.getString(studentSnapshot, "callStatus", "pending")); // Default value

                            student.setIsInterested(DataSnapshotHelper.getBoolean(studentSnapshot, "isInterested", false));
                            student.setAdmitted(DataSnapshotHelper.getBoolean(studentSnapshot, "isAdmitted", false));

                            // Admin Changes Node
                            if (studentSnapshot.hasChild("admin_changes")) {
                                DataSnapshot adminChanges = studentSnapshot.child("admin_changes");
                                student.setLastUpdatedDate(DataSnapshotHelper.getString(adminChanges, "updated_date"));
                            }

                            studentList.add(student);
                            Log.d(TAG, "👤 Added student: " + student.getName());
                        } catch (Exception e) {
                            Log.e(TAG, "🚨 Error parsing student data: " + e.getMessage() + " for student: " + studentSnapshot.getKey());
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
        studentRef.child("admin_changes").child("updated_date").setValue(currentDate) // Simplified admin_changes update
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Admin update recorded for " + student.getName()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update admin_changes: " + e.getMessage()));
    }

    // Helper class to safely extract data from DataSnapshot
    private static class DataSnapshotHelper {

        static String getString(DataSnapshot snapshot, String key) {
            return getString(snapshot, key, null);
        }

        static String getString(DataSnapshot snapshot, String key, String defaultValue) {
            if (snapshot.hasChild(key)) {
                try {
                    Object value = snapshot.child(key).getValue();
                    if (value != null) {
                        return value.toString().trim();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting string for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        static Boolean getBoolean(DataSnapshot snapshot, String key, boolean defaultValue) {
            if (snapshot.hasChild(key)) {
                try {
                    Boolean value = snapshot.child(key).getValue(Boolean.class);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting boolean for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        static Long getLong(DataSnapshot snapshot, String key, Long defaultValue) {
            if (snapshot.hasChild(key)) {
                try {
                    Long value = snapshot.child(key).getValue(Long.class);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting Long for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        static Double getDouble(DataSnapshot snapshot, String key, Double defaultValue) {
            if (snapshot.hasChild(key)) {
                try {
                    Double value = snapshot.child(key).getValue(Double.class);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting Double for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        static Date getDate(DataSnapshot snapshot, String key, SimpleDateFormat dateFormat, Date defaultValue) {
            if (snapshot.hasChild(key)) {
                try {
                    String dateString = snapshot.child(key).getValue(String.class);
                    if (dateString != null) {
                        return dateFormat.parse(dateString);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date for key " + key + ": " + e.getMessage());
                    return defaultValue;
                }
            }
            return defaultValue;
        }
    }
}

