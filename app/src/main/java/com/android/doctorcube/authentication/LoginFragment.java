package com.android.doctorcube.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDB;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private GoogleSignInClient mGoogleSignInClient;
    private String verificationCode;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String resetEmail;
    private static final int RC_SIGN_IN = 9001;
    private static final String PREFS_NAME = "DoctorCubePrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String USER_COLLECTION = "Users";
    private boolean isSuperAdmin = false;

    private TextInputEditText emailField, passwordField;
    private MaterialButton loginButton;
    private TextView forgotPasswordText;
    private MaterialCardView googleSignInButton;
    private ImageButton backButton; // Declare backButton

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        // Initialize UI Elements
        emailField = view.findViewById(R.id.emailEditText);
        passwordField = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        forgotPasswordText = view.findViewById(R.id.forgotPasswordText);
        googleSignInButton = view.findViewById(R.id.googleSignInButton);
        backButton = view.findViewById(R.id.backButton); // Initialize backButton

        // Initialize Encrypted SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error initializing secure storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
            sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Check login status
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn && currentUser != null) {
            String userType = sharedPreferences.getString(KEY_USER_ROLE, "");
            navigateToFragment(navController, userType);
        } else {
            // User is not logged in, explicitly sign out
            if (currentUser != null) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
            }
            // Proceed to setup login UI (user stays on LoginFragment)
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

        // Navigate to CreateAccountFragment
        view.findViewById(R.id.createAccountText).setOnClickListener(v ->
                navController.navigate(R.id.createAccountFragment2));

        // Handle back button click
        backButton.setOnClickListener(v -> {
            // Navigate to the GetStartedFragment
            navController.navigate(R.id.fragmentAskUser);
        });

        // Check intent destination
        String destination = requireActivity().getIntent().getStringExtra("destination");
        if (destination != null) {
            Log.d("LoginFragment", "Destination from intent: " + destination);
        }

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
        firestoreDB.collection(USER_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String role = document.getString("role");
                            if (role != null) {
                                saveUserLoginStatus(role);
                                navigateToFragment(navController, role);
                            } else {
                                Toast.makeText(getContext(), "User role is null.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();
                            }
                        } else {
                            Toast.makeText(getContext(), "User document does not exist.", Toast.LENGTH_SHORT).show();
                            navController.navigate(R.id.collectUserDetailsFragment);
                        }
                    } else {
                        Log.e("LoginFragment", "Error fetching user data", task.getException());
                        Toast.makeText(getActivity(), "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserLoginStatus(String role) {
        sharedPreferences.edit()
                .putBoolean("isLoggedIn", true)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    private void navigateToFragment(NavController navController, String role) {
        if ("user".equals(role)) {
            navController.navigate(R.id.mainActivity2);
        } else if ("admin".equals(role) || "superadmin".equals(role)) {
            navController.navigate(R.id.adminActivity2);
        } else {
            Toast.makeText(getContext(), "Invalid user role.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();
        }
    }

    private void handleForgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Forgot Password");
        builder.setMessage("Choose how to reset your password:");

        builder.setPositiveButton("Email", (dialog, which) -> resetPasswordByEmail());
        builder.setNegativeButton("Phone", (dialog, which) -> resetPasswordByPhone());
        builder.show();
    }

    private void resetPasswordByEmail() {
        resetEmail = emailField.getText().toString().trim();
        if (resetEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email to reset password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(resetEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPasswordByPhone() {
        final EditText phoneInput = new EditText(getContext());
        phoneInput.setHint("+91-");
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Phone Number");
        builder.setView(phoneInput);

        builder.setPositiveButton("Send Code", (dialog, which) -> {
            String phoneNumber = phoneInput.getText().toString().trim();
            if (phoneNumber.isEmpty() || !phoneNumber.matches("\\d{10}")) {
                Toast.makeText(requireContext(), "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show();
                return;
            }
            String fullPhoneNumber = "+91" + phoneNumber;
            sendVerificationCode(fullPhoneNumber);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendVerificationCode(String phoneNumber) {
        verificationCode = String.format("%06d", new java.util.Random().nextInt(1000000));
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                requireActivity(),
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("PhoneAuth", "Verification failed", e);
                        Toast.makeText(getContext(), "Phone verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        mResendToken = token;
                        showOtpDialogForReset(mVerificationId);
                    }
                });
    }

    private void showOtpDialogForReset(final String verificationId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Verification Code");
        final EditText input = new EditText(getContext());
        builder.setView(input);
        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredCode = input.getText().toString().trim();
            if (enteredCode.equals(verificationCode)) {
                verifyPhoneNumberWithCodeForReset(verificationId, enteredCode);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Incorrect code. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void verifyPhoneNumberWithCodeForReset(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        showNewPasswordDialog();
                    } else {
                        Log.e("PhoneAuth", "Phone sign-in failed", task.getException());
                        Toast.makeText(getContext(), "Phone sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNewPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter New Password");
        final EditText newPasswordInput = new EditText(getContext());
        newPasswordInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(newPasswordInput);
        builder.setPositiveButton("Reset Password", (dialog, which) -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            if (newPassword.length() < 6) {
                Toast.makeText(getContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                showNewPasswordDialog();
                return;
            }
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Password reset successfully. Please login.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                emailField.setText("");
                                passwordField.setText("");
                            } else {
                                Toast.makeText(getContext(), "Error resetting password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        assert user != null;
                        String phoneNumber = user.getPhoneNumber();
                        NavController navController = Navigation.findNavController(getView());
                        fetchUserData(user.getUid(), navController);
                    } else {
                        Log.e("PhoneAuth", "Phone sign-in failed", task.getException());
                        Toast.makeText(getContext(), "Phone sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                            fetchGoogleUserData(user.getUid());
                        }
                    } else {
                        Toast.makeText(getContext(), "Google sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void fetchGoogleUserData(String uid) {
        firestoreDB.collection(USER_COLLECTION)
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        NavController navController = Navigation.findNavController(getView());
                        if (document.exists()) {
                            String role = document.getString("role");
                            if (role != null) {
                                saveUserLoginStatus(role);
                                navigateToFragment(navController, role);
                            } else {
                                Toast.makeText(getContext(), "User role is null.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();
                            }
                        } else {
                            navController.navigate(R.id.collectUserDetailsFragment);
                        }
                    } else {
                        Log.e("LoginFragment", "Error fetching Google user data", task.getException());
                        Toast.makeText(getContext(), "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
