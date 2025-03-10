package com.android.doctorcube.adminpannel;

import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("registrations");

    // Callback Interface for Data Retrieval
    public interface DataCallback {
        void onDataLoaded(List<Student> studentList);
        void onError(String errorMessage);
    }

    // Fetch All Students from Firebase
    public static void fetchAllStudents(DataCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Student> studentList = new ArrayList<>();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot studentSnapshot : dateSnapshot.getChildren()) {
                        Student student = studentSnapshot.getValue(Student.class);
                        studentList.add(student);
                    }
                }
                callback.onDataLoaded(studentList); // Send data to UI
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError("Failed to fetch data: " + error.getMessage());
            }
        });
    }

    // Update Call Status in Firebase
    public static void updateCallStatus(String studentId, String callStatus) {
        databaseReference.child("080325") // Example date (replace dynamically)
                .child(studentId)
                .child("callStatus")
                .setValue(callStatus);
    }
}
