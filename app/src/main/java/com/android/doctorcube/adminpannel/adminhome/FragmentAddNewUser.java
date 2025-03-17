package com.android.doctorcube.adminpannel.adminhome;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class FragmentAddNewUser extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    private EditText etUserName, etEmail, etPhone, etPassword;
    private Button btnAddUser;

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

        // Initialize Views
        etUserName = view.findViewById(R.id.etUserName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
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

        // Handle Add User Button Click
        btnAddUser.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = etUserName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etUserName.setError("Enter Full Name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter Email");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Enter Phone Number");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter Password");
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserDetails(user.getUid(), fullName, email, phone);
                                saveUserLoginStatus("admin");

                                // Reset fields after successful registration
                                etUserName.setText("");
                                etEmail.setText("");
                                etPhone.setText("");
                                etPassword.setText("");

                                Toast.makeText(requireContext(), "Admin Registered Successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Sign-up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDetails(String userId, String fullName, String email, String phone) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", "admin"); // Role set as Admin

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
