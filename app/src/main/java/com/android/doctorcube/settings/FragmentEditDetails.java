package com.android.doctorcube.settings;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.android.doctorcube.R;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentEditDetails extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "UserProfilePrefs";
    private static final String TAG = "EditDetailsFragment";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    // Views
    private ImageView profileImageView;
    private TextInputLayout emailLayout, fullNameLayout, phoneLayout;
    private EditText emailEditText, fullNameEditText, phoneEditText;
    private Button saveButton, changePasswordButton;
    private ProgressBar progressBar;
    private CardView profileCard;
    private AppCompatImageButton backButton;
    private Toolbar fragmentToolbar;
    private NavController navController;

    // User data
    private String userId;
    private Uri imageUri;
    private String localImagePath;
    private SharedPreferences sharedPreferences;
    private boolean isImageChanged = false;
    private boolean isEditing = false;
    private String firestoreImageUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_details, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Initialize SharedPreferences
        sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize NavController
        navController = NavHostFragment.findNavController(this);

        // Hide MainActivity Toolbar
        hideMainActivityToolbar();

        // Initialize views including Toolbar
        initViews(view);

        // Load user data
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        fragmentToolbar = view.findViewById(R.id.toolbar);
        if (fragmentToolbar != null) {
            fragmentToolbar.setNavigationOnClickListener(v -> navigateToHome());
        }

        profileImageView = view.findViewById(R.id.profileImageView);
        emailLayout = view.findViewById(R.id.emailLayout);
        fullNameLayout = view.findViewById(R.id.fullNameLayout);
        phoneLayout = view.findViewById(R.id.phoneLayout);
        emailEditText = view.findViewById(R.id.emailEditText);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        saveButton = view.findViewById(R.id.saveButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        progressBar = view.findViewById(R.id.progressBar);
        profileCard = view.findViewById(R.id.profileCard);
        backButton = view.findViewById(R.id.backButton);

        // Set initial state of EditTexts to uneditable
        setEditTextNonEditable();
        saveButton.setText("Edit Details");

        // Set listeners
        profileImageView.setOnClickListener(v -> {
            if (isEditing) {
                selectImage();
            }
        });
        saveButton.setOnClickListener(v -> {
            if (isEditing) {
                saveUserData();
            } else {
                enableEditing();
            }
        });
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        // Add TextWatchers to detect changes
        emailEditText.addTextChangedListener(textWatcher);
        fullNameEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);

        // Set back button listener
        backButton.setOnClickListener(v -> navigateToHome());
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isEditing = !emailEditText.getText().toString().equals(sharedPreferences.getString(userId + "_email", ""))
                    || !fullNameEditText.getText().toString().equals(sharedPreferences.getString(userId + "_fullName", ""))
                    || !phoneEditText.getText().toString().equals(sharedPreferences.getString(userId + "_phone", ""));
            saveButton.setText(isEditing ? "Save Changes" : "Edit Details");
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void enableEditing() {
        setEditTextEditable();
        saveButton.setText("Save Changes");
        isEditing = true;
    }

    private void setEditTextEditable() {
        emailEditText.setFocusableInTouchMode(true);
        fullNameEditText.setFocusableInTouchMode(true);
        phoneEditText.setFocusableInTouchMode(true);
        emailEditText.setFocusable(true);
        fullNameEditText.setFocusable(true);
        phoneEditText.setFocusable(true);
        emailEditText.requestFocus();
    }

    private void setEditTextNonEditable() {
        emailEditText.setFocusable(false);
        fullNameEditText.setFocusable(false);
        phoneEditText.setFocusable(false);
        emailEditText.setFocusableInTouchMode(false);
        fullNameEditText.setFocusableInTouchMode(false);
        phoneEditText.setFocusableInTouchMode(false);
    }

    private void loadUserData() {
        showLoading(true);
        String email = sharedPreferences.getString(userId + "_email", "");
        String fullName = sharedPreferences.getString(userId + "_fullName", "");
        String phone = sharedPreferences.getString(userId + "_phone", "");
        firestoreImageUrl = sharedPreferences.getString(userId + "_profile_image_url", "");
        boolean hasLocalData = !email.isEmpty() || !fullName.isEmpty() || !phone.isEmpty();

        File localFile = new File(getContext().getFilesDir(), userId + "_profile.jpg");
        if (localFile.exists()) {
            localImagePath = localFile.getAbsolutePath();
            Glide.with(this)
                    .load(localFile)
                    .circleCrop()
                    .placeholder(R.drawable.logo_doctor_cubes_white)
                    .into(profileImageView);
        } else if (!firestoreImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(firestoreImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.logo_doctor_cubes_white)
                    .into(profileImageView);
        }

        if (hasLocalData) {
            emailEditText.setText(email);
            fullNameEditText.setText(fullName);
            phoneEditText.setText(phone);
            showLoading(false);
        } else {
            fetchUserDataFromFirestore();
        }
    }

    private void fetchUserDataFromFirestore() {
        mFirestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        String fullName = documentSnapshot.getString("fullName");
                        String phone = documentSnapshot.getString("phone");
                        firestoreImageUrl = documentSnapshot.getString("imageUrl");

                        emailEditText.setText(email);
                        fullNameEditText.setText(fullName);
                        phoneEditText.setText(phone);

                        if (firestoreImageUrl != null && !firestoreImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(firestoreImageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.logo_doctor_cubes_white)
                                    .into(profileImageView);
                        }

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(userId + "_email", email);
                        editor.putString(userId + "_fullName", fullName);
                        editor.putString(userId + "_phone", phone);
                        editor.putString(userId + "_profile_image_url", firestoreImageUrl);
                        editor.apply();
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error loading user data", e);
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            isImageChanged = true;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                saveImageToLocalStorage(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading image", e);
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToLocalStorage(Bitmap bitmap) throws IOException {
        File outputDir = getContext().getFilesDir();
        File outputFile = new File(outputDir, userId + "_profile.jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] bitmapData = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        localImagePath = outputFile.getAbsolutePath();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(userId + "_has_local_image", true);
        editor.apply();
    }

    private void saveUserData() {
        String email = emailEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailLayout.setError("Email cannot be empty");
            return;
        }
        if (fullName.isEmpty()) {
            fullNameLayout.setError("Name cannot be empty");
            return;
        }
        if (phone.isEmpty()) {
            phoneLayout.setError("Phone cannot be empty");
            return;
        }

        emailLayout.setError(null);
        fullNameLayout.setError(null);
        phoneLayout.setError(null);
        showLoading(true);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(userId + "_email", email);
        editor.putString(userId + "_fullName", fullName);
        editor.putString(userId + "_phone", phone);
        editor.apply();

        // Update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("email", email);
        updates.put("fullName", fullName);
        updates.put("phone", phone);

        updateUserData(updates);

        isEditing = false;
        saveButton.setText("Edit Details");
        setEditTextNonEditable();
    }


    private void updateUserData(Map<String, Object> updates) {
        mFirestore.collection("Users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    if (isImageChanged && localImagePath != null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(userId + "_has_local_image", true);
                        editor.apply();
                    }
                    isImageChanged = false;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error updating user data", e);
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showChangePasswordDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);

        // Find views
        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmPasswordLayout = dialogView.findViewById(R.id.confirmPasswordLayout);

        TextInputEditText currentPasswordEditText = dialogView.findViewById(R.id.currentPasswordEditText);
        TextInputEditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        TextInputEditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmPasswordEditText);

        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton changePasswordButton = dialogView.findViewById(R.id.changePasswordButton);

        // Animation views
        FrameLayout animationContainer = dialogView.findViewById(R.id.animationContainer);
        LottieAnimationView successAnimationView = dialogView.findViewById(R.id.successAnimationView);
        LottieAnimationView failureAnimationView = dialogView.findViewById(R.id.failureAnimationView);

        // Create and configure the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Remove title as it's already in the custom layout
        builder.setTitle(null);

        // Create the dialog
        final AlertDialog dialog = builder.create();

        // Set up field validation listeners
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (!password.isEmpty()) {
                    if (password.length() < 8) {
                        newPasswordLayout.setError("Password must be at least 8 characters");
                    } else if (!containsLetterAndDigit(password)) {
                        newPasswordLayout.setError("Password must contain both letters and numbers");
                    } else {
                        newPasswordLayout.setError(null);
                    }
                } else {
                    newPasswordLayout.setError(null);
                }
            }
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String confirmPassword = s.toString();
                String newPassword = newPasswordEditText.getText().toString();
                if (!confirmPassword.isEmpty() && !confirmPassword.equals(newPassword)) {
                    confirmPasswordLayout.setError("Passwords do not match");
                } else {
                    confirmPasswordLayout.setError(null);
                }
            }
        });

        // Set up button click listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        changePasswordButton.setOnClickListener(v -> {
            // Validate all inputs
            if (validateInputs(currentPasswordLayout, newPasswordLayout, confirmPasswordLayout)) {
                // Get the entered passwords
                String currentPassword = currentPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();

                // Disable all inputs during processing
                setInputsEnabled(false, currentPasswordLayout, newPasswordLayout,
                        confirmPasswordLayout, cancelButton, changePasswordButton);

                // Call password change method
                changePassword(currentPassword, newPassword, dialog,
                        animationContainer, successAnimationView, failureAnimationView,
                        currentPasswordLayout, newPasswordLayout, confirmPasswordLayout,
                        cancelButton, changePasswordButton);
            }
        });

        // Show the dialog
        dialog.show();
    }

    // Helper method to check if a string contains at least one letter and one digit
    private boolean containsLetterAndDigit(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                return true;
            }
        }

        return false;
    }

    // Validate all input fields
    private boolean validateInputs(TextInputLayout currentPasswordLayout,
                                   TextInputLayout newPasswordLayout,
                                   TextInputLayout confirmPasswordLayout) {
        boolean isValid = true;

        // Validate current password
        if (currentPasswordLayout.getEditText().getText().toString().trim().isEmpty()) {
            currentPasswordLayout.setError("Please enter your current password");
            isValid = false;
        } else {
            currentPasswordLayout.setError(null);
        }

        // Validate new password
        String newPassword = newPasswordLayout.getEditText().getText().toString().trim();
        if (newPassword.isEmpty()) {
            newPasswordLayout.setError("Please enter a new password");
            isValid = false;
        } else if (newPassword.length() < 8) {
            newPasswordLayout.setError("Password must be at least 8 characters");
            isValid = false;
        } else if (!containsLetterAndDigit(newPassword)) {
            newPasswordLayout.setError("Password must contain both letters and numbers");
            isValid = false;
        } else {
            newPasswordLayout.setError(null);
        }

        // Validate password confirmation
        String confirmPassword = confirmPasswordLayout.getEditText().getText().toString().trim();
        if (!confirmPassword.equals(newPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    // Enable or disable all input fields
    private void setInputsEnabled(boolean enabled, TextInputLayout currentPasswordLayout,
                                  TextInputLayout newPasswordLayout,
                                  TextInputLayout confirmPasswordLayout,
                                  Button cancelButton,
                                  Button changePasswordButton) {
        currentPasswordLayout.setEnabled(enabled);
        newPasswordLayout.setEnabled(enabled);
        confirmPasswordLayout.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
        changePasswordButton.setEnabled(enabled);
    }

    // Perform the password change operation
    private void changePassword(String currentPassword, String newPassword, AlertDialog dialog,
                                FrameLayout animationContainer,
                                LottieAnimationView successAnimationView,
                                LottieAnimationView failureAnimationView,
                                TextInputLayout currentPasswordLayout,
                                TextInputLayout newPasswordLayout,
                                TextInputLayout confirmPasswordLayout,
                                Button cancelButton,
                                Button changePasswordButton) {

        showLoading(true); // Keep showing overall fragment loading state
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    showLoading(false); // Hide overall loading
                                    // Call success animation method
                                    showSuccessAnimation(dialog, animationContainer, successAnimationView);

                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update password", e);
                                    showLoading(false); // Hide overall loading
                                    // Call failure animation method
                                    showFailureAnimation(dialog, animationContainer, failureAnimationView,
                                            currentPasswordLayout, newPasswordLayout, confirmPasswordLayout,
                                            cancelButton, changePasswordButton);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Incorrect password", e);
                        showLoading(false); // Hide overall loading
                        Toast.makeText(getContext(), "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();
                        // Re-enable inputs on the dialog.
                        showFailureAnimation(dialog, animationContainer, failureAnimationView,
                                currentPasswordLayout, newPasswordLayout, confirmPasswordLayout,
                                cancelButton, changePasswordButton);
                    });
        }
    }

    // Show success animation and handle completion
    private void showSuccessAnimation(AlertDialog dialog,
                                      FrameLayout animationContainer,
                                      LottieAnimationView successAnimationView) {
        // Show animation container
        animationContainer.setVisibility(View.VISIBLE);

        // Show and play success animation
        successAnimationView.setVisibility(View.VISIBLE);
        successAnimationView.playAnimation();

        successAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // Show success dialog after animation completes
                new AlertDialog.Builder(getContext())
                        .setTitle("Success")
                        .setMessage("Your password has been successfully updated.")
                        .setPositiveButton("OK", (dialogInterface, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    // Show failure animation and handle completion
    private void showFailureAnimation(AlertDialog dialog,
                                      FrameLayout animationContainer,
                                      LottieAnimationView failureAnimationView,
                                      TextInputLayout currentPasswordLayout,
                                      TextInputLayout newPasswordLayout,
                                      TextInputLayout confirmPasswordLayout,
                                      Button cancelButton,
                                      Button changePasswordButton) {
        // Show animation container
        animationContainer.setVisibility(View.VISIBLE);

        // Show and play failure animation
        failureAnimationView.setVisibility(View.VISIBLE);
        failureAnimationView.playAnimation();

        failureAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // Hide animation container
                animationContainer.setVisibility(View.GONE);

                // Re-enable input fields
                setInputsEnabled(true, currentPasswordLayout, newPasswordLayout,
                        confirmPasswordLayout, cancelButton, changePasswordButton);

                // Show error message
                new AlertDialog.Builder(getContext())
                        .setTitle("Error")
                        .setMessage("Failed to update password. Please try again.")
                        .setPositiveButton("OK", (dialogInterface, which) -> dialogInterface.dismiss())
                        .show();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        profileCard.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        saveButton.setEnabled(!isLoading);
        changePasswordButton.setEnabled(!isLoading);
    }

    private void hideMainActivityToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            Toolbar mainToolbar = activity.findViewById(R.id.toolbar);
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.GONE);
            }
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
        }
    }

    private void showMainActivityToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            Toolbar mainToolbar = activity.findViewById(R.id.toolbar);
            if (mainToolbar != null) {
                mainToolbar.setVisibility(View.VISIBLE);
            }
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
            }
        }}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show MainActivity Toolbar when leaving this Fragment
        showMainActivityToolbar();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (navController != null) {
            navigateToHome();
        }
    }

    private void navigateToHome() {
        if (navController != null) {
            navController.navigate(R.id.action_global_settingsHome);
        }
    }
}

