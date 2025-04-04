package com.android.doctorcube.database;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;
import androidx.navigation.NavController;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.CustomToast;
import com.android.doctorcube.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private FirebaseFirestore firestoreDB;
    private Context context;
    private static final String APP_SUBMISSIONS_COLLECTION = "app_submissions";
    private static final String PREFS_NAME = "DoctorCubePrefs";

    public FirestoreHelper(Context context) {
        this.context = context;
        this.firestoreDB = FirebaseFirestore.getInstance();
    }

    // Method to get EncryptedSharedPreferences
    private SharedPreferences getEncryptedSharedPreferences() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encryption fails
            Toast.makeText(context, "Failed to initialize encrypted storage", Toast.LENGTH_SHORT).show();
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    // Save user data to Firestore and EncryptedSharedPreferences
    public void saveUserData(Map<String, Object> userData, String userId, View view,
                             SharedPreferences sharedPreferences, NavController navController, boolean isBottomSheet) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Missing user ID. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get EncryptedSharedPreferences
        SharedPreferences encryptedPrefs = getEncryptedSharedPreferences();

        // Save to EncryptedSharedPreferences
        SharedPreferences.Editor editor = encryptedPrefs.edit();
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            String key = userId + "_" + entry.getKey(); // Prefix with userId to avoid conflicts
            Object value = entry.getValue();
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } // Add more type checks as needed
        }
        editor.apply();

        // Update Firestore "app_submissions" collection, using userId as document ID
        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION).document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    saveApplicationData(userData, userId, view, encryptedPrefs, navController, isBottomSheet);
                    Toast.makeText(context, "User data saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Save application data (additional submission without userId as document ID)
    private void saveApplicationData(Map<String, Object> userData, String userId, View view,
                                     SharedPreferences encryptedPrefs, NavController navController, boolean isBottomSheet) {
        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION)
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    SharedPreferences.Editor editor = encryptedPrefs.edit();
                    editor.putBoolean(userId + "_isApplicationSubmitted", true);
                    editor.apply();

                    if (!isBottomSheet) {
                        navController.navigate(R.id.action_collectUserDetailsFragment_to_mainActivity2);
                    }
                    Toast.makeText(context, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error submitting form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Update a specific field in SharedPreferences and Firestore
    public void updateUserDataField(String userId, String fieldName, Object fieldValue) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Missing user ID. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update EncryptedSharedPreferences
        SharedPreferences encryptedPrefs = getEncryptedSharedPreferences();
        SharedPreferences.Editor editor = encryptedPrefs.edit();
        String key = userId + "_" + fieldName;

        if (fieldValue instanceof String) {
            editor.putString(key, (String) fieldValue);
        } else if (fieldValue instanceof Boolean) {
            editor.putBoolean(key, (Boolean) fieldValue);
        } // Add more type checks as needed
        editor.apply();

        // Update Firestore
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(fieldName, fieldValue);

        firestoreDB.collection(APP_SUBMISSIONS_COLLECTION).document(userId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    CustomToast.showToast((Activity) context, "Field updated successfully");
                })
                .addOnFailureListener(e -> {
                    CustomToast.showToast((Activity) context, "Failed to update field");
                });
    }

    // Fetch a specific field from EncryptedSharedPreferences
    public String getUserDataField(String userId, String fieldName) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        SharedPreferences encryptedPrefs = getEncryptedSharedPreferences();
        return encryptedPrefs.getString(userId + "_" + fieldName, null);
    }

    // Fetch user data from Firestore and load it into SharedPreferences if it doesn't exist
    public void loadUserDataFromFirestore(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        SharedPreferences encryptedPrefs = getEncryptedSharedPreferences();
        Map<String, ?> allPrefs = encryptedPrefs.getAll();
        boolean dataExists = false;

        for (String key : allPrefs.keySet()) {
            if (key.startsWith(userId + "_")) {
                dataExists = true;
                break;
            }
        }

        if (!dataExists) {
            firestoreDB.collection(APP_SUBMISSIONS_COLLECTION).document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            if (userData != null) {
                                SharedPreferences.Editor editor = encryptedPrefs.edit();
                                for (Map.Entry<String, Object> entry : userData.entrySet()) {
                                    String key = userId + "_" + entry.getKey();
                                    Object value = entry.getValue();
                                    if (value instanceof String) {
                                        editor.putString(key, (String) value);
                                    } else if (value instanceof Boolean) {
                                        editor.putBoolean(key, (Boolean) value);
                                    } // Add more type checks as needed
                                }
                                editor.apply();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        CustomToast.showToast((Activity) context, "Failed to load user data");
                    });
        }
    }
}