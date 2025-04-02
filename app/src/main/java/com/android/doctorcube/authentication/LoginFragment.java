package com.android.doctorcube.authentication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.CustomToast;
import com.android.doctorcube.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreDB;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String resetEmailOrPhone;
    private static final String PREFS_NAME = "DoctorCubePrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String USER_COLLECTION = "Users";
    private TextInputEditText accountField;
    private TextInputEditText passwordField;
    private MaterialButton loginButton;
    private MaterialButton getOtpButton;
    private TextView forgotPasswordText;
    private ImageButton backButton;
    private String phoneNumber;
    private boolean isPhoneAuthentication = false;
    private TextInputLayout accountInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextView resendOtpText;
    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        // Initialize UI Elements
        accountInputLayout = view.findViewById(R.id.accountInputLayout);
        accountField = view.findViewById(R.id.accountEditText);
        passwordField = view.findViewById(R.id.passwordEditText);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        loginButton = view.findViewById(R.id.loginButton);
        getOtpButton = view.findViewById(R.id.getOtpButton);
        forgotPasswordText = view.findViewById(R.id.forgotPasswordText);
        backButton = view.findViewById(R.id.backButton);
        resendOtpText = view.findViewById(R.id.resendOtpText);

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
            sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

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
            }
        }

        // Handle text input changes to detect if user enters email or phone
        accountField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                updateAuthenticationMode(input);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Handle Get OTP button click
        getOtpButton.setOnClickListener(v -> {
            phoneNumber = accountField.getText().toString().trim();
            if (phoneNumber.isEmpty() || !isValidPhoneNumber(phoneNumber)) {
                CustomToast.showToast(requireActivity(),"Please Enter A Valid Number");
                return;
            }

            // Format phone number with country code
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
            sendVerificationCode(formattedPhoneNumber);
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String account = accountField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (account.isEmpty()) {
                CustomToast.showToast(requireActivity(),"Please Enter The Valid Number Or Email");
                return;
            }

            if (password.isEmpty()) {
                CustomToast.showToast(requireActivity(),"Please Enter The OTP");

                return;
            }

            if (isPhoneAuthentication) {
                if (mVerificationId != null) {
                    verifyPhoneNumberWithCode(mVerificationId, password);
                } else {
                    CustomToast.showToast(requireActivity(),"Please Request The OTP First");

                }
            } else {
                // Email authentication
                loginUserWithEmailAndPassword(account, password, navController);
            }
        });

        // Handle Forgot Password
        forgotPasswordText.setOnClickListener(v -> handleForgotPassword());

        // Navigate to CreateAccountFragment
        view.findViewById(R.id.createAccountText).setOnClickListener(v ->
                navController.navigate(R.id.createAccountFragment2));

        // Handle back button click
        backButton.setOnClickListener(v -> {
            navController.navigate(R.id.fragmentAskUser);
        });

        // Handle resend OTP
        resendOtpText.setOnClickListener(v -> {
            if (isResendEnabled) {
                String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
                sendVerificationCode(formattedPhoneNumber, mResendToken); // Pass the resend token
            }
        });

        return view;
    }

    private void updateAuthenticationMode(String input) {
        if (isEmailAddress(input)) {
            // Switch to email authentication
            isPhoneAuthentication = false;
            accountInputLayout.setStartIconDrawable(R.drawable.ic_email);
            passwordInputLayout.setHint("Password");
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            getOtpButton.setVisibility(View.GONE);
            resendOtpText.setVisibility(View.GONE);
            loginButton.setText(R.string.sign_in);

            // Cancel any ongoing timer
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        } else if (isPhoneNumber(input)) {
            // Switch to phone authentication
            isPhoneAuthentication = true;
            accountInputLayout.setStartIconDrawable(R.drawable.ic_call);
            passwordInputLayout.setHint("OTP");
            passwordField.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            getOtpButton.setVisibility(View.VISIBLE);
            resendOtpText.setVisibility(View.GONE);
            loginButton.setText("Verify & Sign In");
        }
    }

    private boolean isEmailAddress(String input) {
        return input.contains("@") && Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }

    private boolean isPhoneNumber(String input) {
        return input.matches("\\d+") && input.length() >= 10;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Validate phone number format - adjust as needed for your requirements
        return phoneNumber.matches("\\d{10}");
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Add country code if not present - adjust for your country code
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        return "+91" + phoneNumber;
    }

    private void loginUserWithEmailAndPassword(String email, String password, NavController navController) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserData(user.getUid(), navController);
                        }
                    } else {
                        CustomToast.showToast(requireActivity(),"Failed To Login ");

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
                                // User has a document, but no role.  Assign default and update.
                                assignDefaultRole(userId, navController);
                            }
                        } else {
                            // User data does not exist in Firestore, create it
                            createUserDocument(userId, navController);
                        }
                    } else {
                        CustomToast.showToast(requireActivity(),"Error Fetching User Data");
                        mAuth.signOut();
                        navController.navigate(R.id.getStartedFragment);
                    }
                });
    }

    private void assignDefaultRole(String userId, NavController navController) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("role", "user"); // Default role
        firestoreDB.collection(USER_COLLECTION)
                .document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    saveUserLoginStatus("user");
                    navigateToFragment(navController, "user");
                })
                .addOnFailureListener(e -> {
                    CustomToast.showToast(requireActivity(),"Failed to set default role");
                    mAuth.signOut();
                    navController.navigate(R.id.getStartedFragment);
                });
    }


    private void createUserDocument(String userId, NavController navController) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            CustomToast.showToast(requireActivity(),"User Not Authenticated");
            return; // Exit if no user
        }
        String phoneNumber = user.getPhoneNumber();
        String email = user.getEmail();
        String displayName = user.getDisplayName();

        // Create a map with user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("role", "user"); // Default role
        userData.put("phone", phoneNumber);
        userData.put("email", email);
        userData.put("fullName", displayName);

        // Add the data to the Firestore Users collection
        firestoreDB.collection(USER_COLLECTION)
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    saveUserLoginStatus("user");
                    navController.navigate(R.id.collectUserDetailsFragment);
                })
                .addOnFailureListener(e -> {
                    CustomToast.showToast(requireActivity(),"failed to validate user");
                    mAuth.signOut(); // Sign out the user if data creation fails
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
            CustomToast.showToast(requireActivity(),"User Not Verified");

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
        resetEmailOrPhone = accountField.getText().toString().trim();
        if (resetEmailOrPhone.isEmpty()) {
            CustomToast.showToast(requireActivity(),"Please Enter Your Email & Phone");

            return;
        }
        if (resetEmailOrPhone.contains("@")) {
            mAuth.sendPasswordResetEmail(resetEmailOrPhone)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            CustomToast.showToast(requireActivity(),"Email Sent");
                        } else {
                            CustomToast.showToast(requireActivity(),"Unable To Send Mail");
                        }
                    });
        } else if (isPhoneNumber(resetEmailOrPhone)) {
            resetPasswordByPhone();
        } else {
            CustomToast.showToast(requireActivity(),"Invalid Details");
        }
    }

    private void resetPasswordByPhone() {
        final EditText phoneInput = new EditText(getContext());
        phoneInput.setHint("+91-");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Phone Number");
        builder.setView(phoneInput);

        builder.setPositiveButton("Send Code", (dialog, which) -> {
            String phoneNumber = phoneInput.getText().toString().trim();
            if (phoneNumber.isEmpty() || !isValidPhoneNumber(phoneNumber)) {
                CustomToast.showToast(requireActivity(),"Please Enter The Valid Phone No");
                return;
            }
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
            sendVerificationCode(formattedPhoneNumber);
            dialog.dismiss();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendVerificationCode(String phoneNumber) {
        sendVerificationCode(phoneNumber, null);
    }

    private void sendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // This callback will be invoked instantly when the phone number is verified
                        // automatically with a previously used credential.
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        // This callback will be invoked when the verification process fails.
                        CustomToast.showToast(requireActivity(),"Verification Failed");
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the user's phone.
                        mVerificationId = verificationId;
                        mResendToken = token;  // Save the token for resending
                        startTimer();
                        showOtpSentMessage();
                    }
                };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout in seconds
                TimeUnit.SECONDS,   // Unit of timeout
                requireActivity(),      // Activity (for callback binding)
                callbacks,           // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken, or null to start verification
    }


    private void showOtpSentMessage() {
        CustomToast.showToast(requireActivity(),"OTP Sent");
        // Make OTP field and button visible
        passwordInputLayout.setVisibility(View.VISIBLE);
        resendOtpText.setVisibility(View.VISIBLE);
    }

    private void startTimer() {
        isResendEnabled = false;
        resendOtpText.setTextColor(getResources().getColor(R.color.hint_color, requireContext().getTheme()));
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendOtpText.setText("Resend OTP in " + millisUntilFinished / 1000 + " seconds");
            }

            @Override
            public void onFinish() {
                isResendEnabled = true;
                resendOtpText.setText("Resend OTP");
                resendOtpText.setTextColor(getResources().getColor(R.color.text_link, requireContext().getTheme()));
            }
        }.start();
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            fetchUserData(user.getUid(), Navigation.findNavController(getView()));
                        }
                    } else {
                        CustomToast.showToast(requireActivity(),"Invalid OTP");
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        assert user != null;
                        NavController navController = Navigation.findNavController(getView());
                        fetchUserData(user.getUid(), navController);
                    } else {
                        CustomToast.showToast(requireActivity(),"Sign In Failed");
                    }
                });
    }
}

