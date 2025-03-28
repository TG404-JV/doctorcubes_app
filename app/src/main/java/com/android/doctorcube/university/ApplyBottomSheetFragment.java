package com.android.doctorcube.university;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.R;
import com.android.doctorcube.university.model.University;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ApplyBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText nameEditText, emailEditText, phoneEditText, stateEditText, cityEditText, neetScoreEditText;
    private AutoCompleteTextView countrySpinner;
    private RadioGroup neetScoreGroup, passportGroup;
    private RadioButton neetScoreYes, neetScoreNo, passportYes, passportNo;
    private TextInputLayout neetScoreLayout;
    private Button submitButton;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private University university;

    private String offer;

    // Array of countries for the dropdown
    private final String[] countries = {"USA", "Canada", "UK", "Germany", "France", "Australia", "India", "China", "Brazil", "Japan"};

    public ApplyBottomSheetFragment(University university) {
        this.university = university;
    }

    public ApplyBottomSheetFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collect_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        stateEditText = view.findViewById(R.id.stateEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        neetScoreEditText = view.findViewById(R.id.neetScore);
        countrySpinner = view.findViewById(R.id.countrySpinner);
        neetScoreGroup = view.findViewById(R.id.neetScoreGroup);
        passportGroup = view.findViewById(R.id.passportGroup);
        neetScoreYes = view.findViewById(R.id.neetScoreYes);
        neetScoreNo = view.findViewById(R.id.neetScoreNo);
        passportYes = view.findViewById(R.id.passportYes);
        passportNo = view.findViewById(R.id.passportNo);
        neetScoreLayout = view.findViewById(R.id.neetScoreLayout);
        submitButton = view.findViewById(R.id.submitButton);

        if (getArguments() != null) {
            offer = getArguments().getString("event_title", "");
        }

        if (!TextUtils.isEmpty(offer)) {
            TextView t1 = view.findViewById(R.id.PersonlizedTxt);
            t1.setText(offer);
        }

        // Initialize Encrypted SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "user_details",
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error initializing preferences", Toast.LENGTH_SHORT).show();
        }

        // Set up country spinner
        setUpCountrySpinner();

        // Pre-fill some fields if available
        prefillForm();

        // Set up listeners
        setUpListeners();
    }

    private void setUpCountrySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, countries);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setThreshold(1);
    }

    private void prefillForm() {
        if (mAuth.getCurrentUser() != null) {
            String displayName = mAuth.getCurrentUser().getDisplayName();
            String email = mAuth.getCurrentUser().getEmail();
            String phone = mAuth.getCurrentUser().getPhoneNumber();

            if (!TextUtils.isEmpty(displayName)) {
                nameEditText.setText(displayName);
                nameEditText.setEnabled(false);
            }
            if (!TextUtils.isEmpty(email)) {
                emailEditText.setText(email);
                emailEditText.setEnabled(false);
            }
            if (!TextUtils.isEmpty(phone)) {
                phoneEditText.setText(phone);
            }
        }
    }

    private void setUpListeners() {
        neetScoreGroup.setOnCheckedChangeListener((group, checkedId) -> {
            neetScoreLayout.setVisibility(checkedId == R.id.neetScoreYes ? View.VISIBLE : View.GONE);
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                checkAndSaveUserDetails(v);
            }
        });
    }

    private boolean validateInputs() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String country = countrySpinner.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String neetScore = neetScoreEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Enter your name");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Enter your email");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Enter your phone number");
            return false;
        }
        if (TextUtils.isEmpty(country)) {
            countrySpinner.setError("Select your country");
            return false;
        }
        if (TextUtils.isEmpty(state)) {
            stateEditText.setError("Enter your state/province");
            return false;
        }
        if (TextUtils.isEmpty(city)) {
            cityEditText.setError("Enter your city");
            return false;
        }
        if (neetScoreGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Select whether you have a NEET score", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (neetScoreYes.isChecked() && TextUtils.isEmpty(neetScore)) {
            neetScoreEditText.setError("Enter your NEET score");
            return false;
        }
        if (passportGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Select whether you have a passport", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkAndSaveUserDetails(View view) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String email = emailEditText.getText().toString().trim();

        firestore.collection("app_submissions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            // User data exists, show message
                            Toast.makeText(getContext(), "You have already submitted an application with this email.", Toast.LENGTH_SHORT).show();
                            dismiss(); // Dismiss the dialog
                        } else {
                            // User data does not exist, proceed to save
                            saveUserDetails();
                        }
                    } else {
                        // Handle error
                        Toast.makeText(getContext(), "Error checking for existing application.", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error checking for existing application: ", task.getException());
                    }
                });
    }

    private void saveUserDetails() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String country = countrySpinner.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String neetScore = neetScoreEditText.getText().toString().trim();
        boolean hasNeetScore = neetScoreYes.isChecked();
        boolean hasPassport = passportYes.isChecked();

        // Prepare data for Firestore
        Map<String, Object> formData = new HashMap<>();
        formData.put("userId", userId);
        formData.put("name", name);
        formData.put("email", email);
        formData.put("mobile", phone);
        formData.put("country", country);
        formData.put("state", state);
        formData.put("city", city);
        formData.put("hasNeetScore", hasNeetScore);
        formData.put("neetScore", hasNeetScore ? neetScore : "N/A");
        formData.put("hasPassport", hasPassport);
        if (university != null) {
            formData.put("universityName", university.getName());
            formData.put("universityCountry", university.getCountry()); // Added university country
            formData.put("courseName", university.getCourseName());
        }
        formData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        // Save to Firestorm
        firestore.collection("app_submissions")
                .document() // Auto-generate document ID
                .set(formData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Application submitted successfully!", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isApplicationSubmitted", true);
                    editor.apply();

                    // Dismiss the bottom sheet
                    dismiss();

                    // Navigate to main activity if needed
                    try {
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.action_collectUserDetailsFragment_to_mainActivity2);
                    } catch (Exception e) {
                        Log.e("Navigation", "Error navigating: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error submitting application", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error saving data", e);
                });
    }
}
