package com.android.doctorcube.database;

import android.content.Context;
import android.content.SharedPreferences;
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
            Toast.makeText(context, "Missing user ID. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update User data in "app_submissions" collection, using the user ID as the document ID
        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION).document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    saveApplicationData(userData, userId, view, sharedPreferences, navController, isBottomSheet);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user data. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveApplicationData(Map<String, Object> userData, String userId, View view,
                                     SharedPreferences sharedPreferences, NavController navController, boolean isBottomSheet) {
        // Add a new document to "app_submissions" *without* using the userId as the document ID.
        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION)
                .add(userData)
                .addOnSuccessListener(documentReference -> {

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
                });
    }
}

