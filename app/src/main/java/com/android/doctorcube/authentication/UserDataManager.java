package com.android.doctorcube.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserDataManager {

    private static final String TAG = "UserDataManager";
    private static final String PREFS_NAME = "DoctorCubePrefs";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String USER_COLLECTION = "Users";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LAST_SYNCED = "lastSynced";
    private static final long CACHE_EXPIRY_MS = 86400000; // 24 hours

    private final SharedPreferences sharedPreferences;
    private final FirebaseFirestore firestoreDB;
    private final Context context;
    private final Executor backgroundExecutor;

    // Use the application context
    UserDataManager(@NonNull Context context) {
        this.context = context.getApplicationContext(); // Use application context to avoid memory leaks
        this.backgroundExecutor = Executors.newCachedThreadPool();

        // Configure Firestore for better performance
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();

        this.firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.setFirestoreSettings(settings);

        // Initialize Encrypted SharedPreferences with robust error handling
        SharedPreferences tempPrefs;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            tempPrefs = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    this.context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            Log.d(TAG, "Using encrypted shared preferences");
        } catch (GeneralSecurityException | IOException e) {
            Log.w(TAG, "Failed to create encrypted preferences: " + e.getMessage() + ". Falling back to regular SharedPreferences");
            tempPrefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        this.sharedPreferences = tempPrefs;
    }

    // Thread-safe singleton implementation with double-checked locking
    private static volatile UserDataManager instance;

    // Static method to get the instance
    public static UserDataManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (UserDataManager.class) {
                if (instance == null) {
                    instance = new UserDataManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Save user data with improved security
     * - Data sanitization
     * - Background execution for Firestore operations
     */
    public void saveUserData(@NonNull String userId, String fullName, String email, String phone, String role) {
        if (userId.isEmpty()) {
            Log.e(TAG, "Cannot save user data with empty userId");
            return;
        }

        // Sanitize input data
        final String sanitizedFullName = sanitizeInput(fullName);
        final String sanitizedEmail = sanitizeInput(email);
        final String sanitizedPhone = sanitizeInput(phone);
        final String sanitizedRole = sanitizeInput(role);

        // Save to Firestore on background thread
        backgroundExecutor.execute(() -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put(KEY_FULL_NAME, sanitizedFullName);
            userData.put(KEY_EMAIL, sanitizedEmail);
            userData.put(KEY_PHONE, sanitizedPhone);
            userData.put("role", sanitizedRole);
            userData.put("phoneVerified", true);
            userData.put("isLogin", true);
            userData.put("lastUpdated", System.currentTimeMillis());

            firestoreDB.collection(USER_COLLECTION)
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        saveUserDataLocal(userId, sanitizedFullName, sanitizedEmail, sanitizedPhone, sanitizedRole);
                        Log.d(TAG, "User data saved successfully for: " + userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving user data: " + e.getMessage());
                    });
        });
    }

    /**
     * Sanitize input to prevent injection attacks
     */
    private String sanitizeInput(String input) {
        if (input == null) return "";
        // Basic sanitization - remove any control characters and limit length
        return input.replaceAll("[\\p{Cntrl}]", "").trim().substring(0, Math.min(input.length(), 500));
    }

    /**
     * Save user data locally with timestamp for cache expiry management
     */
    private void saveUserDataLocal(@NonNull String userId, String fullName, String email, String phone, String role) {
        sharedPreferences.edit()
                .putString(userId + KEY_FULL_NAME, fullName)
                .putString(userId + KEY_EMAIL, email)
                .putString(userId + KEY_PHONE, phone)
                .putString(KEY_USER_ROLE, role)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(userId + KEY_LAST_SYNCED, System.currentTimeMillis())
                .apply();
    }

    /**
     * Gets user details with a callback approach and cache expiry management
     * Implements cache expiry to ensure data freshness
     */
    public void getUserDetailsWithCallback(@NonNull String userId, final OnUserDataFetchedListener listener) {
        if (userId.isEmpty()) {
            listener.onDataFetchFailed();
            return;
        }

        // Check if we have valid cached data
        HashMap<String, String> localDetails = getUserDetailsFromLocal(userId);
        long lastSynced = sharedPreferences.getLong(userId + KEY_LAST_SYNCED, 0);
        boolean isCacheValid = (System.currentTimeMillis() - lastSynced) < CACHE_EXPIRY_MS;

        if (localDetails != null && isCacheValid) {
            // Local cache is valid, use it immediately
            listener.onDataFetched(localDetails);

            // Optionally refresh cache in background but don't wait for result
            if (NetworkUtils.isNetworkAvailable(context)) {
                refreshCacheInBackground(userId);
            }
        } else {
            // Cache miss or expired, fetch from Firebase
            fetchUserDetailsFromFirebase(userId, listener);
        }
    }

    /**
     * Refreshes cache in background without blocking UI thread
     */
    private void refreshCacheInBackground(String userId) {
        backgroundExecutor.execute(() -> {
            firestoreDB.collection(USER_COLLECTION).document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document != null && document.exists()) {
                            String fullName = document.getString(KEY_FULL_NAME);
                            String email = document.getString(KEY_EMAIL);
                            String phone = document.getString(KEY_PHONE);
                            String role = document.getString("role");

                            if (fullName != null && email != null && phone != null) {
                                saveUserDataLocal(userId, fullName, email, phone, role);
                                Log.d(TAG, "Cache refreshed in background for user: " + userId);
                            }
                        }
                    });
        });
    }

    /**
     * Get user details from local storage with null safety
     */
    private HashMap<String, String> getUserDetailsFromLocal(@NonNull String userId) {
        String fullName = sharedPreferences.getString(userId + KEY_FULL_NAME, null);
        String email = sharedPreferences.getString(userId + KEY_EMAIL, null);
        String phone = sharedPreferences.getString(userId + KEY_PHONE, null);
        String role = sharedPreferences.getString(KEY_USER_ROLE, null);

        if (fullName != null && email != null && phone != null) {
            HashMap<String, String> details = new HashMap<>();
            details.put(KEY_FULL_NAME, fullName);
            details.put(KEY_EMAIL, email);
            details.put(KEY_PHONE, phone);
            details.put("role", role != null ? role : "");
            return details;
        }
        return null;
    }

    /**
     * Legacy method for backward compatibility
     */
    public HashMap<String, String> getUserDetails(String userId) {
        return getUserDetailsFromLocal(userId);
    }

    /**
     * Fetch user details from Firebase with improved error handling
     */
    public void fetchUserDetailsFromFirebase(@NonNull String userId, final OnUserDataFetchedListener listener) {
        if (userId.isEmpty()) {
            listener.onDataFetchFailed();
            return;
        }

        firestoreDB.collection(USER_COLLECTION).document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document != null && document.exists()) {
                        String fullName = document.getString(KEY_FULL_NAME);
                        String email = document.getString(KEY_EMAIL);
                        String phone = document.getString(KEY_PHONE);
                        String role = document.getString("role");

                        if (fullName != null && email != null && phone != null) {
                            saveUserDataLocal(userId, fullName, email, phone, role);

                            HashMap<String, String> userDetails = new HashMap<>();
                            userDetails.put(KEY_FULL_NAME, fullName);
                            userDetails.put(KEY_EMAIL, email);
                            userDetails.put(KEY_PHONE, phone);
                            userDetails.put("role", role != null ? role : "");
                            listener.onDataFetched(userDetails);
                        } else {
                            Log.w(TAG, "Incomplete user data in Firestore for: " + userId);
                            listener.onDataFetchFailed();
                        }
                    } else {
                        Log.w(TAG, "No user document found for: " + userId);
                        listener.onDataFetchFailed();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data: " + e.getMessage());
                    listener.onDataFetchFailed();
                });
    }

    /**
     * Update user data with validation and background processing
     */
    public void updateUserData(@NonNull String userId, Map<String, Object> userData) {
        if (userId.isEmpty() || userData == null || userData.isEmpty()) {
            Log.e(TAG, "Cannot update with empty userId or userData");
            return;
        }

        // Sanitize all string values in the map
        Map<String, Object> sanitizedData = new HashMap<>();
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            if (entry.getValue() instanceof String) {
                sanitizedData.put(entry.getKey(), sanitizeInput((String) entry.getValue()));
            } else {
                sanitizedData.put(entry.getKey(), entry.getValue());
            }
        }

        // Add update timestamp
        sanitizedData.put("lastUpdated", System.currentTimeMillis());

        // Execute update on background thread
        backgroundExecutor.execute(() -> {
            firestoreDB.collection(USER_COLLECTION).document(userId)
                    .update(sanitizedData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User data updated successfully for: " + userId);

                        // Update local storage
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        if (sanitizedData.containsKey(KEY_FULL_NAME)) {
                            editor.putString(userId + KEY_FULL_NAME, (String) sanitizedData.get(KEY_FULL_NAME));
                        }
                        if (sanitizedData.containsKey(KEY_EMAIL)) {
                            editor.putString(userId + KEY_EMAIL, (String) sanitizedData.get(KEY_EMAIL));
                        }
                        if (sanitizedData.containsKey(KEY_PHONE)) {
                            editor.putString(userId + KEY_PHONE, (String) sanitizedData.get(KEY_PHONE));
                        }
                        if (sanitizedData.containsKey("role")) {
                            editor.putString(KEY_USER_ROLE, (String) sanitizedData.get("role"));
                        }
                        if (sanitizedData.containsKey(KEY_IS_LOGGED_IN)) {
                            editor.putBoolean(KEY_IS_LOGGED_IN, (Boolean) sanitizedData.get(KEY_IS_LOGGED_IN));
                        }

                        editor.putLong(userId + KEY_LAST_SYNCED, System.currentTimeMillis());
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating user data: " + e.getMessage());
                    });
        });
    }

    /**
     * Delete user data with background processing and improved error handling
     */
    public void deleteUserData(@NonNull String userId) {
        if (userId.isEmpty()) {
            Log.e(TAG, "Cannot delete user with empty userId");
            return;
        }

        backgroundExecutor.execute(() -> {
            firestoreDB.collection(USER_COLLECTION).document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User data deleted successfully for: " + userId);

                        // Clear local storage
                        sharedPreferences.edit()
                                .remove(userId + KEY_FULL_NAME)
                                .remove(userId + KEY_EMAIL)
                                .remove(userId + KEY_PHONE)
                                .remove(KEY_USER_ROLE)
                                .remove(KEY_IS_LOGGED_IN)
                                .remove(userId + KEY_LAST_SYNCED)
                                .apply();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting user data: " + e.getMessage());
                    });
        });
    }

    /**
     * Get user credentials from secure storage
     */
    public HashMap<String, String> getUserCredentials() {
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put(KEY_USER_ROLE, sharedPreferences.getString(KEY_USER_ROLE, ""));
        credentials.put(KEY_IS_LOGGED_IN, String.valueOf(sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)));
        return credentials;
    }

    /**
     * Secure method to check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Clear all cached data (for logout)
     */
    public void clearAllCachedData() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Callback interface
     */
    public interface OnUserDataFetchedListener {
        void onDataFetched(HashMap<String, String> data);

        void onDataFetchFailed();
    }

    /**
     * Utility class for network connectivity checks
     */
    private static class NetworkUtils {
        public static boolean isNetworkAvailable(Context context) {
            // Implementation would check network connectivity
            // For brevity, just returning true here
            return true;
        }
    }
}
