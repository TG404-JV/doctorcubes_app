package com.android.doctorcube.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.navigation.NavController;

import com.android.doctorcube.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.Map;

public class FirestoreHelper {

    private FirebaseFirestore firestoreDB;
    private Context context;

    public FirestoreHelper(Context context) {
        this.context = context;
        this.firestoreDB = FirebaseFirestore.getInstance();
    }

    public void saveUserData(Map<String, Object> userData, String userId, View view,
                             SharedPreferences sharedPreferences, NavController navController, boolean isBottomSheet) {
        // Update User data in "app_submissions" collection
        firestoreDB.collection("app_submissions").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User data updated successfully in app_submissions collection");
                    saveApplicationData(userData, view, sharedPreferences, navController, isBottomSheet);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user data. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating user data: " + e.getMessage());
                });
    }

    private void saveApplicationData(Map<String, Object> userData, View view,
                                     SharedPreferences sharedPreferences, NavController navController, boolean isBottomSheet) {
        firestoreDB.collection("app_submissions")
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Thank you! Our team will co nnect with you soon.", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Document saved successfully in app_submissions with ID: " + documentReference.getId());

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isApplicationSubmitted", true);
                    editor.apply();

                    if (!isBottomSheet) {
                        navController.navigate(R.id.action_collectUserDetailsFragment_to_mainActivity2);
                    }
                    //  If it is a bottom sheet, do not navigate.  The bottom sheet should just dismiss.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "There was an error submitting your form. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error saving application data", e);
                });
    }
}

