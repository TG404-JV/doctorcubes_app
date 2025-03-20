package com.android.doctorcube.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.android.doctorcube.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView; // Import MaterialCardView
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private TextView forgotPasswordText;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private MaterialCardView googleSignInButton; // Change to MaterialCardView

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
        forgotPasswordText = view.findViewById(R.id.forgotPasswordText);
        googleSignInButton = view.findViewById(R.id.googleSignInButton);

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
            // Handle error appropriately, e.g., show a message to the user
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //Use your Client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

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

        // Handle Forgot Password
        forgotPasswordText.setOnClickListener(v -> handleForgotPassword());

        // Handle Google Sign In
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

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
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    saveUserLoginStatus(role);
                    navigateToFragment(navController, role);
                } else {
                    //If the user exists in Firebase Auth but not in the Realtime Database, navigate to CollectUserDetails.
                    Navigation.findNavController(getView()).navigate(R.id.collectUserDetailsFragment);
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
        } else if ("admin".equals(role) || "superadmin".equals(role)) {
            navController.navigate(R.id.adminActivity2);
        } else {
            // Handle the case where the role is not defined or is invalid
            Toast.makeText(getContext(), "Invalid user role.", Toast.LENGTH_SHORT).show();
            mAuth.signOut(); //sign out the user.
        }
    }

    private void handleForgotPassword() {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email to reset password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.e("Google SignIn Failed", "API Exception: " + e.getStatusCode() + ", Message: " + e.getMessage());
                Toast.makeText(getContext(), "Google Sign In Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            //Check if the user is in the database
                            fetchGoogleUserData(user.getUid());
                        }
                    } else {
                        Toast.makeText(getContext(), "Google sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchGoogleUserData(String uid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                NavController navController = Navigation.findNavController(getView());
                if (snapshot.exists()) {
                    // User exists in database, proceed with login
                    String role = snapshot.child("role").getValue(String.class);
                    saveUserLoginStatus(role);
                    navigateToFragment(navController, role);
                } else {
                    // User doesn't exist, navigate to CollectUserDetails
                    Navigation.findNavController(getView()).navigate(R.id.collectUserDetailsFragment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

