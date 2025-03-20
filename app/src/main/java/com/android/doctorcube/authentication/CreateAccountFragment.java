package com.android.doctorcube.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CreateAccountFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private GoogleSignInClient googleSignInClient;
    private String verificationId; // For OTP verification

    private EditText fullNameEditText, emailEditText, phoneEditText, passwordEditText, confirmPasswordEditText, otpEditText;
    private Button createAccountButton, verifyOtpButton, cancelOtpButton;
    private CheckBox termsCheckbox;
    private ImageView googleSignInButton;
    private TextView termsText, loginText, resendOtpText;
    private CardView otpVerificationDialog;

    private static final int RC_SIGN_IN = 9001;

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

        // Initialize Firebase Auth and Database
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
        termsCheckbox = view.findViewById(R.id.termsCheckbox);
        termsText = view.findViewById(R.id.termsText);
        loginText = view.findViewById(R.id.loginText);
        googleSignInButton = view.findViewById(R.id.googleSignInButton);
        otpVerificationDialog = view.findViewById(R.id.otpVerificationDialog);
        otpEditText = view.findViewById(R.id.otpNumberVerification);
        verifyOtpButton = view.findViewById(R.id.verifyOtpButton);
        cancelOtpButton = view.findViewById(R.id.cancelOtpButton);
        resendOtpText = view.findViewById(R.id.resendOtpText);

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
            Toast.makeText(requireContext(), "Error initializing preferences", Toast.LENGTH_SHORT).show();
        }

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Set Click Listeners
        createAccountButton.setOnClickListener(v -> registerUser());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        loginText.setOnClickListener(v -> navController.navigate(R.id.action_createAccountFragment2_to_collectUserDetailsFragment));
        termsText.setOnClickListener(v -> showTermsAndConditions());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        cancelOtpButton.setOnClickListener(v -> otpVerificationDialog.setVisibility(View.GONE));
        resendOtpText.setOnClickListener(v -> resendOtp());
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
        if (!termsCheckbox.isChecked()) {
            Toast.makeText(requireContext(), "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start phone verification
        sendOtp(phone);
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(requireContext(), "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = verId;
                        otpVerificationDialog.setVisibility(View.VISIBLE);
                        startOtpCountdown();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String otp = otpEditText.getText().toString().trim();
        if (TextUtils.isEmpty(otp)) {
            otpEditText.setError("Enter OTP");
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                String fullName = fullNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();

                saveUserDetails(user.getUid(), fullName, email, phone);
                saveUserLoginStatus("user");
                otpVerificationDialog.setVisibility(View.GONE);
                // Pass data to CollectUserDetailsFragment
                Bundle bundle = new Bundle();
                bundle.putString("userId", user.getUid());
                bundle.putString("fullName", fullName);
                bundle.putString("email", email);
                bundle.putString("phone", phone);
                navController.navigate(R.id.collectUserDetailsFragment, bundle);

            } else {
                Toast.makeText(requireContext(), "OTP Verification Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startOtpCountdown() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                resendOtpText.setText("Resend in " + millisUntilFinished / 1000 + "s");
                resendOtpText.setEnabled(false);
            }

            public void onFinish() {
                resendOtpText.setText("Resend OTP");
                resendOtpText.setEnabled(true);
            }
        }.start();
    }

    private void resendOtp() {
        String phone = phoneEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(phone)) {
            sendOtp(phone);
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); // Pass account
            } catch (ApiException e) {
                Toast.makeText(requireContext(), "Google Sign-In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) { // Added account parameter
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                String fullName = account.getDisplayName();  // Get from Google Account
                String email = account.getEmail();        // Get from Google Account
                String phone = "000000000";
                if (phone == null)
                {
                    phone = "N/A";
                }

                saveUserDetails(user.getUid(), fullName, email, phone);
                saveUserLoginStatus("user");

                // Pass data to CollectUserDetailsFragment
                Bundle bundle = new Bundle();
                bundle.putString("userId", user.getUid());
                bundle.putString("fullName", fullName);
                bundle.putString("email", email);
                bundle.putString("phone", phone);
                navController.navigate(R.id.collectUserDetailsFragment, bundle);

            } else {
                Toast.makeText(requireContext(), "Google Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDetails(String userId, String fullName, String email, String phone) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", "user");

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

    private void showTermsAndConditions() {
        // Implement your Terms and Conditions display logic here (e.g., dialog or navigation)
        Toast.makeText(requireContext(), "Terms and Conditions clicked", Toast.LENGTH_SHORT).show();
    }
}

