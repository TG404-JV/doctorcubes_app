package com.android.doctorcube.authentication;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CollectUserDetailsFragment extends Fragment {

    private EditText nameEditText, emailEditText, phoneEditText, stateEditText, cityEditText, neetScoreEditText;
    private AutoCompleteTextView countrySpinner;
    private RadioGroup neetScoreGroup, passportGroup;
    private RadioButton neetScoreYes, neetScoreNo, passportYes, passportNo;
    private TextInputLayout neetScoreLayout;
    private Button submitButton;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    // Array of countries for the dropdown
    private final String[] countries = {"USA", "Canada", "UK", "Germany", "France", "Australia", "India", "China", "Brazil", "Japan"}; //Add more
    private String userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collect_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize NavController
        navController = Navigation.findNavController(view);

        // Initialize UI elements
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        stateEditText = view.findViewById(R.id.stateEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        neetScoreEditText = view.findViewById(R.id.neetScore);
        countrySpinner = view.findViewById(R.id.countrySpinner);
        neetScoreGroup = view.findViewById(R.id.needScoreGroup);
        passportGroup = view.findViewById(R.id.Passport);
        neetScoreYes = view.findViewById(R.id.needScoreYes);
        neetScoreNo = view.findViewById(R.id.needScoreNo);
        passportYes = view.findViewById(R.id.PassportYes);
        passportNo = view.findViewById(R.id.PassportNo);
        neetScoreLayout = view.findViewById(R.id.neetScoreLayout);
        submitButton = view.findViewById(R.id.submitButton);

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

        // Get data from bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString("userId");
            userFullName = bundle.getString("fullName");
            userEmail = bundle.getString("email");
            userPhone = bundle.getString("phone");

            // Pre-fill the form and disable editing, except for phone number
            if (TextUtils.isEmpty(nameEditText.getText())) {
                nameEditText.setText(userFullName);
                nameEditText.setEnabled(false);
            }
            if (TextUtils.isEmpty(emailEditText.getText())) {
                emailEditText.setText(userEmail);
                emailEditText.setEnabled(false);
            }
            if (TextUtils.isEmpty(phoneEditText.getText())) {
                phoneEditText.setText(userPhone);
                // phoneEditText.setEnabled(true);  // Keep phone number editable
            }
            emailEditText.setEnabled(false);
            nameEditText.setEnabled(false);

        }

        // Set up listeners
        setUpListeners();
    }

    private void setUpCountrySpinner() {
        // Use ArrayAdapter to populate the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, countries);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setThreshold(1); // Minimum characters to show suggestions
    }

    private void setUpListeners() {
        neetScoreGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.needScoreYes) {
                neetScoreLayout.setVisibility(View.VISIBLE);
            } else {
                neetScoreLayout.setVisibility(View.GONE);
            }
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserDetails(v); // Pass the view to submitData
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

    private void saveUserDetails(View view) { // Added view parameter
        //String userId = mAuth.getCurrentUser().getUid();  //getting from bundle now
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String country = countrySpinner.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String neetScore = neetScoreEditText.getText().toString().trim();
        boolean hasNeetScore = neetScoreYes.isChecked();
        boolean hasPassport = passportYes.isChecked();

        // Use a Map to store the data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("country", country);
        userData.put("state", state);
        userData.put("city", city);
        userData.put("hasNeetScore", hasNeetScore);
        if (hasNeetScore) {
            userData.put("neetScore", neetScore);
        }
        userData.put("hasPassport", hasPassport);

        // Get current date in ddMMyy format (matching the JavaScript code)
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date());

        // Create a HashMap for user data
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("name", name);
        formData.put("email", email);
        formData.put("mobile", phone);
        formData.put("state", state);
        formData.put("city", city);
        formData.put("country", country);
        formData.put("needScore", hasNeetScore ? "Yes" : "No");
        formData.put("neetScore", hasNeetScore ? neetScore : "N/A");
        formData.put("passport", hasPassport ? "Yes" : "No");

        // Create reference to "registrations/{ddmmyy}" path (matching the JavaScript path)
        DatabaseReference registrationRef = FirebaseDatabase.getInstance().getReference("registrations").child(formattedDate);

        // Generate a unique key under the date node
        DatabaseReference newRegistrationRef = registrationRef.push();

        // Upload data
        newRegistrationRef.setValue(formData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Thank you! Our team will connect with you soon.", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "Data saved successfully with key: " + newRegistrationRef.getKey());

                // Set application submitted to true
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isApplicationSubmitted", true);
                editor.apply();

                // Navigate to next screen
                Navigation.findNavController(view).navigate(R.id.action_collectUserDetailsFragment_to_mainActivity2);
            } else {
                Toast.makeText(getContext(), "There was an error submitting your form. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error saving data", task.getException());
            }
        });
    }
}
