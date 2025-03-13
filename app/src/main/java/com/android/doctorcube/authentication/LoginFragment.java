package com.android.doctorcube.authentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.android.doctorcube.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private MaterialButton loginButton;
    private TextInputEditText emailField, passwordField;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Elements
        emailField = view.findViewById(R.id.emailEditText);
        passwordField = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);

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

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            String userType = sharedPreferences.getString("userType", "");
            navigateToFragment(navController, userType);
        }

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password, navController);
            }
        });

        // Navigate to CreateAccountFragment on button click
        view.findViewById(R.id.createAccountText).setOnClickListener(v ->
                navController.navigate(R.id.createAccountFragment2));

        return view;
    }

    private void loginUser(String email, String password, NavController navController) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserData(user.getUid(), navController);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserData(String userId, NavController navController) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("role")) {
                    String role = snapshot.child("role").getValue(String.class);
                    saveUserLoginStatus(role);
                    navigateToFragment(navController, role);
                } else {
                    saveUserLoginStatus("admin");
                    navigateToFragment(navController, "admin");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserLoginStatus(String role) {
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userType", role)
                .apply();
    }

    private void navigateToFragment(NavController navController, String role) {
        if ("user".equals(role)) {
            navController.navigate(R.id.mainActivity2);
        } else {
            navController.navigate(R.id.adminActivity2);
        }
    }
}
