package com.android.doctorcube.adminpannel.adminhome;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.textfield.TextInputLayout; // Import TextInputLayout

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class FragmentAddNewUser extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    // Use TextInputLayout instead of EditText for proper error display
    private TextInputLayout tilUserName, tilEmail, tilPhone, tilPassword, tilRole;
    private EditText etUserName, etEmail, etPhone, etPassword;
    private AutoCompleteTextView etRole;
    private Button btnAddUser;
    private String selectedRole;

    public FragmentAddNewUser() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize Views using TextInputLayout
        tilUserName = view.findViewById(R.id.tilUserName);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPhone = view.findViewById(R.id.tilPhone);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilRole = view.findViewById(R.id.tilRole); // Find TextInputLayout for Role
        etUserName = view.findViewById(R.id.etUserName); // Find EditText inside TextInputLayout
        etEmail = view.findViewById(R.id.etEmail);     // Find EditText inside TextInputLayout
        etPhone = view.findViewById(R.id.etPhone);     // Find EditText inside TextInputLayout
        etPassword = view.findViewById(R.id.etPassword); // Find EditText inside TextInputLayout
        etRole = view.findViewById(R.id.etRole);       // Find AutoCompleteTextView
        btnAddUser = view.findViewById(R.id.btnAddUser);

        // Initialize Encrypted SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "user_prefs",
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        // Set up the role dropdown
        setupRoleDropdown();

        // Handle Add User Button Click
        btnAddUser.setOnClickListener(v -> registerUser());
    }

    private void setupRoleDropdown() {
        // Define the roles
        String[] roles = new String[]{"admin", "superadmin"};
        // Create an adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, roles);
        // Set the adapter to the AutoCompleteTextView
        etRole.setAdapter(adapter);

        // Set a listener to get the selected role
        etRole.setOnItemClickListener((parent, view, position, id) -> {
            selectedRole = (String) parent.getItemAtPosition(position);
        });
    }

    private void registerUser() {
        String fullName = etUserName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs using TextInputLayout's setError()
        if (TextUtils.isEmpty(fullName)) {
            tilUserName.setError("Enter Full Name");
            return;
        } else {
            tilUserName.setError(null); // Clear error
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Enter Email");
            return;
        } else {
            tilEmail.setError(null);
        }
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Enter Phone Number");
            return;
        } else {
            tilPhone.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Enter Password");
            return;
        } else {
            tilPassword.setError(null);
        }
        if (TextUtils.isEmpty(selectedRole)) {
            tilRole.setError("Select a Role");
            return;
        } else {
            tilRole.setError(null);
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserDetails(user.getUid(), fullName, email, phone, selectedRole);
                                saveUserLoginStatus(selectedRole);

                                // Reset fields after successful registration
                                etUserName.setText("");
                                etEmail.setText("");
                                etPhone.setText("");
                                etPassword.setText("");
                                etRole.setText("");
                                selectedRole = null;

                                Toast.makeText(requireContext(), "Admin Registered Successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Sign-up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDetails(String userId, String fullName, String email, String phone, String role) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", role);

        databaseReference.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "User Data Saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save user data.", Toast.LENGTH_SHORT).show());
    }

    private void saveUserLoginStatus(String role) {
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userType", role)
                .apply();
    }
}
