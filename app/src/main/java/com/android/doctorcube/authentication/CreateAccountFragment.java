package com.android.doctorcube.authentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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

public class CreateAccountFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private NavController navController;
    private SharedPreferences sharedPreferences;

    private EditText fullNameEditText, emailEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private Button createAccountButton;

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize NavController
        navController = Navigation.findNavController(view);

        // Initialize Views
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        createAccountButton = view.findViewById(R.id.createAccountButton);

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

        // Handle Sign-Up Button Click
        createAccountButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError("Enter Full Name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter Email");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Enter Phone Number");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Enter Password");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Create user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserDetails(user.getUid(), fullName, email, phone);

                            // Save user login state and role in SharedPreferences
                            saveUserLoginStatus("user");

                            // Navigate to MainActivity (or UserDetailsFragment if required)
                            navController.navigate(R.id.collectUserDetailsFragment);
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
        userData.put("role", "user"); // Ensure account type is always 'user'

        databaseReference.child(userId).setValue(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "User Registered Successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save user details.", Toast.LENGTH_SHORT).show());
    }

    private void saveUserLoginStatus(String role) {
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userType", role)
                .apply();
    }
}
