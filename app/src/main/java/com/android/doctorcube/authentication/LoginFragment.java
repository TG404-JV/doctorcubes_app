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
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private MaterialButton loginButton;
    private TextInputEditText emailField, passwordField;
    private SharedPreferences sharedPreferences;
    private TextView forgotPasswordText;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private MaterialCardView googleSignInButton;
    private String verificationCode;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String resetEmail; // Store email for reset
    private static final String PREFS_NAME = "DoctorCubePrefs";  // Added constant
    private static final String KEY_USER_ROLE = "user_role";    // Added constant
    private boolean isSuperAdmin = false;

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
                    PREFS_NAME, // Changed to use constant
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle error appropriately
            Toast.makeText(getContext(), "Error initializing secure storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
            sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            String userType = sharedPreferences.getString(KEY_USER_ROLE, ""); //changed key
            navigateToFragment(navController, userType);
        }

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password, Navigation.findNavController(getView()));
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
                .putString(KEY_USER_ROLE, role) //changed key
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Forgot Password");
        builder.setMessage("Choose how to reset your password:");

        builder.setPositiveButton("Email", (dialog, which) -> {
            resetPasswordByEmail();
        });

        builder.setNegativeButton("Phone", (dialog, which) -> {
            resetPasswordByPhone();
        });

        builder.show();
    }

    private void resetPasswordByEmail() {
        resetEmail = emailField.getText().toString().trim(); // Store for later
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
        phoneInput.setHint("+91-"); // Set hint with country code
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Phone Number");
        builder.setView(phoneInput);

        builder.setPositiveButton("Send Code", (dialog, which) -> {
            String phoneNumber = phoneInput.getText().toString().trim();
            if (phoneNumber.isEmpty() || !phoneNumber.matches("\\d{10}")) { //basic validation
                Toast.makeText(requireContext(), "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Append the country code before sending for verification
            String fullPhoneNumber = "+91" + phoneNumber;
            sendVerificationCode(fullPhoneNumber); // Use the method with phone number
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendVerificationCode(String phoneNumber) {
        // Generate a random 6-digit code
        verificationCode = String.format("%06d", new java.util.Random().nextInt(1000000));
        String message = "Your verification code is: " + verificationCode;

        // Use Firebase to send SMS
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout in seconds
                TimeUnit.SECONDS,   // Unit of timeout
                requireActivity(),      // Activity
                new OnVerificationStateChangedCallbacks() { // OnVerificationStateChangedCallbacks
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // This callback will be invoked instantly if you have previously verified the phone number.
                        // Sign in directly.
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // This callback will be invoked if the verification fails.
                        Log.e("PhoneAuth", "Verification failed", e);
                        Toast.makeText(getContext(), "Phone verification failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the user.
                        // Save the verification ID and resending token for later use.
                        mVerificationId = verificationId;
                        mResendToken = token;
                        // Show a dialog or fragment to input the code.
                        showOtpDialogForReset(mVerificationId); // Pass verification ID
                    }
                });

    }

    private void showOtpDialogForReset(final String verificationId) { //changed parameter name
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Verification Code");
        final EditText input = new EditText(getContext());
        builder.setView(input);
        builder.setPositiveButton("Verify", (dialog, which) -> {
            String enteredCode = input.getText().toString().trim();
            if (enteredCode.equals(verificationCode)) {
                // Correct OTP, proceed with reset
                verifyPhoneNumberWithCodeForReset(verificationId, enteredCode); //use new method
                dialog.dismiss(); // Dismiss after successful verification
            } else {
                Toast.makeText(getContext(), "Incorrect code. Please try again.", Toast.LENGTH_SHORT).show();
                // Do NOT dismiss the dialog here.  Allow the user to try again.
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }



    private void verifyPhoneNumberWithCodeForReset(String verificationId, String code) {
        // [START verify_phone_number_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_phone_number_with_code]
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        // Now that phone number is verified, show dialog to enter new password
                        showNewPasswordDialog();

                    } else {
                        // Sign in failed
                        Log.e("PhoneAuth", "Phone sign-in failed", task.getException());
                        Toast.makeText(getContext(), "Phone sign-in failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
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
                showNewPasswordDialog(); // Show the dialog again
                return;
            }
            // Get the current user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Update the password
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Password has been reset successfully. Please login with new password", Toast.LENGTH_LONG).show();
                                FirebaseAuth.getInstance().signOut();
                                // Clear the fields
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
                        // Sign in success
                        FirebaseUser user = task.getResult().getUser();
                        // Get the user's phone number.
                        String phoneNumber = user.getPhoneNumber();
                        NavController navController = Navigation.findNavController(getView());
                        // Now that we have signed in, fetch the user data.
                        fetchUserData(user.getUid(), navController);
                    } else {
                        // Sign in failed
                        Log.e("PhoneAuth", "Phone sign-in failed", task.getException());
                        Toast.makeText(getContext(), "Phone sign-in failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
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

