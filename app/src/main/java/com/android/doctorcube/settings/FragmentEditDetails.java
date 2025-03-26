package com.android.doctorcube.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import com.android.doctorcube.R;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentEditDetails extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "UserProfilePrefs";

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Views
    private ImageView profileImageView;
    private TextInputLayout emailLayout, fullNameLayout, phoneLayout;
    private EditText emailEditText, fullNameEditText, phoneEditText;
    private Button saveButton, changePasswordButton;
    private ProgressBar progressBar;
    private CardView profileCard;
    private AppCompatImageButton backButton;
    private Toolbar fragmentToolbar; // Added for fragment-specific toolbar

    // User data
    private String userId;
    private Uri imageUri;
    private String localImagePath;
    private SharedPreferences sharedPreferences;
    private boolean isImageChanged = false;
    private boolean isEditing = false;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_details, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

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
        // Initialize Fragment's Toolbar
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
        emailEditText.setFocusable(false);
        fullNameEditText.setFocusable(false);
        phoneEditText.setFocusable(false);
        emailEditText.setFocusableInTouchMode(false);
        fullNameEditText.setFocusableInTouchMode(false);
        phoneEditText.setFocusableInTouchMode(false);

        // Set initial button text
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
        emailEditText.setFocusableInTouchMode(true);
        fullNameEditText.setFocusableInTouchMode(true);
        phoneEditText.setFocusableInTouchMode(true);
        emailEditText.setFocusable(true);
        fullNameEditText.setFocusable(true);
        phoneEditText.setFocusable(true);
        emailEditText.requestFocus();
        saveButton.setText("Save Changes");
        isEditing = true;
    }

    private void loadUserData() {
        showLoading(true);
        String email = sharedPreferences.getString(userId + "_email", "");
        String fullName = sharedPreferences.getString(userId + "_fullName", "");
        String phone = sharedPreferences.getString(userId + "_phone", "");
        boolean hasLocalData = !email.isEmpty() || !fullName.isEmpty() || !phone.isEmpty();

        File localFile = new File(getContext().getFilesDir(), userId + "_profile.jpg");
        if (localFile.exists()) {
            localImagePath = localFile.getAbsolutePath();
            Glide.with(this)
                    .load(localFile)
                    .circleCrop()
                    .placeholder(R.drawable.ic_doctorcubes_white)
                    .into(profileImageView);
        }

        if (hasLocalData) {
            emailEditText.setText(email);
            fullNameEditText.setText(fullName);
            phoneEditText.setText(phone);
            showLoading(false);
        } else {
            fetchUserDataFromFirebase();
        }
    }

    private void fetchUserDataFromFirebase() {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String fullName = dataSnapshot.child("fullName").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    emailEditText.setText(email);
                    fullNameEditText.setText(fullName);
                    phoneEditText.setText(phone);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(userId + "_email", email);
                    editor.putString(userId + "_fullName", fullName);
                    editor.putString(userId + "_phone", phone);
                    editor.apply();
                }
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showLoading(false);
                Toast.makeText(getContext(), "Failed to load profile: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
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

        Map<String, Object> updates = new HashMap<>();
        updates.put("email", email);
        updates.put("fullName", fullName);
        updates.put("phone", phone);

        updateUserData(updates);
        isEditing = false;
        saveButton.setText("Edit Details");
        setEditTextNonEditable();
    }

    private void setEditTextNonEditable() {
        emailEditText.setFocusable(false);
        fullNameEditText.setFocusable(false);
        phoneEditText.setFocusable(false);
        emailEditText.setFocusableInTouchMode(false);
        fullNameEditText.setFocusableInTouchMode(false);
        phoneEditText.setFocusableInTouchMode(false);
    }

    private void updateUserData(Map<String, Object> updates) {
        mDatabase.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to update online profile, but saved locally: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        EditText currentPasswordEditText = dialogView.findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmPasswordEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String currentPassword = currentPasswordEditText.getText().toString().trim();
                    String newPassword = newPasswordEditText.getText().toString().trim();
                    String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                    if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "New passwords don't match", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    changePassword(currentPassword, newPassword);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        showLoading(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(), "Password updated successfully",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(), "Failed to update password: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();
                    });
        }
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
        }
    }

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