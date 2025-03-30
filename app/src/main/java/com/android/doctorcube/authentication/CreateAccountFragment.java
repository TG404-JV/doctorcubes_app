package com.android.doctorcube.authentication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CreateAccountFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestoreDB;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private String verificationId;
    private Handler handler = new Handler();

    // UI Elements
    private EditText fullNameEditText, emailEditText, phoneEditText, passwordEditText, confirmPasswordEditText, otpEditText;
    private Button createAccountButton, verifyOtpButton, cancelOtpButton;
    private CheckBox termsCheckbox;
    private TextView termsText, loginText, resendOtpText, passwordStrengthText;
    private CardView otpVerificationDialog;
    private ProgressBar progressBar;
    private ImageButton backButton;

    private static final String USER_COLLECTION = "Users";
    private static final String APP_SUBMISSIONS_COLLECTION = "app_submissions";
    private static final String PREFS_NAME = "DoctorCubePrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        firestoreDB = FirebaseFirestore.getInstance();
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
        otpVerificationDialog = view.findViewById(R.id.otpVerificationDialog);
        otpEditText = view.findViewById(R.id.otpNumberVerification);
        verifyOtpButton = view.findViewById(R.id.verifyOtpButton);
        cancelOtpButton = view.findViewById(R.id.cancelOtpButton);
        resendOtpText = view.findViewById(R.id.resendOtpText);
        passwordStrengthText = view.findViewById(R.id.passwordStrengthText);
        progressBar = view.findViewById(R.id.progressBar);
        backButton = view.findViewById(R.id.backButton);

        // Initialize Encrypted SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREFS_NAME, masterKeyAlias, requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(requireContext(), "Error initializing preferences", Toast.LENGTH_SHORT).show();
            sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        // Set up password visibility toggle
        passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    togglePasswordVisibility(passwordEditText);
                    return true;
                }
            }
            return false;
        });

        // Real-time validation
        setupRealTimeValidation();

        // Set Click Listeners
        createAccountButton.setOnClickListener(v -> registerUser());
        loginText.setOnClickListener(v -> navController.navigate(R.id.action_createAccountFragment2_to_loginFragment2));
        termsText.setOnClickListener(v -> showTermsAndConditions());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        cancelOtpButton.setOnClickListener(v -> otpVerificationDialog.setVisibility(View.GONE));
        resendOtpText.setOnClickListener(v -> resendOtp());
        backButton.setOnClickListener(v -> navController.navigate(R.id.action_createAccountFragment2_to_loginFragment2));

        // Handle back press
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                navController.navigate(R.id.action_createAccountFragment2_to_collectUserDetailsFragment);
                return true;
            }
            return false;
        });
    }

    private void setupRealTimeValidation() {
        fullNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    fullNameEditText.setError("Enter Full Name");
                } else if (s.toString().matches(".*\\d.*")) {
                    fullNameEditText.setError("Name cannot contain numbers");
                } else {
                    fullNameEditText.setError(null);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()) {
                    emailEditText.setError("Invalid email format");
                } else {
                    emailEditText.setError(null);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 10 || !s.toString().matches("\\d+")) {
                    phoneEditText.setError("Enter a valid 10-digit phone number");
                } else {
                    phoneEditText.setError(null);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString().trim();
                if (!password.isEmpty()) {
                    updatePasswordStrength(password);
                    if (!isValidPassword(password)) {
                        passwordEditText.setError("Password must be 8-14 chars with uppercase, lowercase, number, and special char");
                    } else {
                        passwordEditText.setError(null);
                    }
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void registerUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName) || fullName.matches(".*\\d.*")) {
            fullNameEditText.setError("Enter a valid Full Name");
            shakeField(fullNameEditText);
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid Email");
            shakeField(emailEditText);
            return;
        }
        if (phone.length() != 10 || !phone.matches("\\d+")) {
            phoneEditText.setError("Enter a valid 10-digit Phone Number");
            shakeField(phoneEditText);
            return;
        }
        if (!isValidPassword(password)) {
            passwordEditText.setError("Password must be 8-14 chars with uppercase, lowercase, number, and special char");
            shakeField(passwordEditText);
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            shakeField(confirmPasswordEditText);
            return;
        }
        if (!termsCheckbox.isChecked()) {
            Toast.makeText(requireContext(), "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        createAccountButton.setEnabled(false);
        createAccountButton.setAlpha(0.5f);
        sendOtp("+91" + phone);
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8 || password.length() > 14) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*[0-9].*")) return false;
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) return false;
        return true;
    }

    private void updatePasswordStrength(String password) {
        if (password.length() < 8) {
            passwordStrengthText.setText("Weak");
            passwordStrengthText.setTextColor(Color.RED);
        } else if (isValidPassword(password)) {
            passwordStrengthText.setText("Strong");
            passwordStrengthText.setTextColor(Color.GREEN);
        } else {
            passwordStrengthText.setText("Medium");
            passwordStrengthText.setTextColor(Color.YELLOW);
        }
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
                        progressBar.setVisibility(View.GONE);
                        createAccountButton.setEnabled(true);
                        createAccountButton.setAlpha(1.0f);
                        String message;
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid phone number format";
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            message = "Too many attempts, try again later";
                        } else {
                            message = "Verification failed: " + e.getMessage();
                        }
                        Log.e("CreateAccountFragment", "Phone verification failed", e);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressBar.setVisibility(View.GONE);
                        createAccountButton.setEnabled(true);
                        createAccountButton.setAlpha(1.0f);
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
            shakeField(otpEditText);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        verifyOtpButton.setEnabled(false);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            verifyOtpButton.setEnabled(true);
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                String fullName = fullNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();

                saveUserDetails(user.getUid(), fullName, email, phone, "user");
                saveUserLoginStatus("user");
                otpVerificationDialog.setVisibility(View.GONE);

                Bundle bundle = new Bundle();
                bundle.putString("userId", user.getUid());
                bundle.putString("fullName", fullName);
                bundle.putString("email", email);
                bundle.putString("phone", phone);
                navController.navigate(R.id.collectUserDetailsFragment, bundle);
            } else {
                Log.e("CreateAccountFragment", "Phone sign-in failed", task.getException());
                Toast.makeText(requireContext(), "OTP Verification Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
        new AlertDialog.Builder(requireContext())
                .setMessage("Resend OTP to " + phoneEditText.getText().toString().trim() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String phone = phoneEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(phone)) {
                        sendOtp("+91" + phone);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void saveUserDetails(String userId, String fullName, String email, String phone, String role) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", role);

        firestoreDB.collection(USER_COLLECTION)
                .document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateAccountFragment", "Failed to save user details to Firestore", e);
                    Toast.makeText(requireContext(), "Failed to save user details.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserLoginStatus(String role) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    private void showTermsAndConditions() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Terms and Conditions")
                .setMessage("Your terms and conditions text here...")
                .setPositiveButton("Accept", (dialog, which) -> termsCheckbox.setChecked(true))
                .setNegativeButton("Decline", null)
                .show();
    }

    private void togglePasswordVisibility(EditText editText) {
        if (editText.getTransformationMethod() == null) {
            editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        } else {
            editText.setTransformationMethod(null);
        }
        editText.setSelection(editText.getText().length());
    }

    private void shakeField(View view) {
        Animation shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
        view.startAnimation(shake);
    }
}