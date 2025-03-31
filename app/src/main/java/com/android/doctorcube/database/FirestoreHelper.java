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
    private static final String TAG = "FirestoreHelper"; // Added TAG for consistent logging
    private static final String APP_SUBMISSIONS_COLLECTION = "app_submissions";

    public FirestoreHelper(Context context) {
        this.context = context;
        this.firestoreDB = FirebaseFirestore.getInstance();
    }

    public void saveUserData(Map<String, Object> userData, String userId, View view,
                             SharedPreferences sharedPreferences, NavController navController, boolean isBottomSheet) {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Error: userId is null or empty. Cannot update user data.");
            Toast.makeText(context, "Missing user ID. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update User data in "app_submissions" collection, using the user ID as the document ID
        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION).document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data updated successfully in " + APP_SUBMISSIONS_COLLECTION + " collection for user: " + userId);
                    saveApplicationData(userData, userId, view, sharedPreferences, navController, isBottomSheet);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user data. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating user data for user: " + userId, e);
                });
    }

    private void saveApplicationData(Map<String, Object> userData, String userId, View view,
                                     SharedPreferences sharedPreferences, NavController navController, boolean isBottomSheet) {
        // Add a new document to "app_submissions" *without* using the userId as the document ID.
        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION)
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Thank you! Our team will connect with you soon.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Application data saved successfully in " + APP_SUBMISSIONS_COLLECTION + " with ID: " + documentReference.getId() + " for user: " + userId);

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
                    Log.e(TAG, "Error saving application data for user: " + userId, e);
                });
    }
}

