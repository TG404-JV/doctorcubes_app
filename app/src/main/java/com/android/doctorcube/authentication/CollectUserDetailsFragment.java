package com.android.doctorcube.authentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.android.doctorcube.database.FirestoreHelper;
import com.android.doctorcube.database.SharedPreferencesManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
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
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private ImageView backButton;

    private final String[] countries = {"USA", "Canada", "UK", "Germany", "France", "Australia", "India", "China", "Brazil", "Japan"};
    private String userId;
    private String userEmail;
    private String userPhone;
    private String userRole; // To store the user's role

    // Declare an instance of the new utility class
    private FirestoreHelper firestoreHelper;

    // Add a boolean to track if it's a BottomSheet
    private boolean isBottomSheet = false;

    public CollectUserDetailsFragment() {
        // Required empty public constructor
    }

    // Constructor to indicate when the fragment is used as a BottomSheet
    public CollectUserDetailsFragment(boolean isBottomSheet) {
        this.isBottomSheet = isBottomSheet;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collect_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        navController = Navigation.findNavController(view);

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
        backButton = view.findViewById(R.id.backButton);

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

        setUpCountrySpinner();

        // Initialize the FirestoreHelper
        firestoreHelper = new FirestoreHelper(requireContext());

        // Get the current user.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();  //get user id
            userEmail = currentUser.getEmail();
            userPhone = currentUser.getPhoneNumber();
            //you can get other user info.
        } else {
            //if the user is not logged in, navigate to login.
            Toast.makeText(requireContext(), "Please Login", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.loginFragment2);
            return;
        }

        setUpListeners();
        //pre-fill
        emailEditText.setText(userEmail);
        phoneEditText.setText(userPhone);
    }

    private void setUpCountrySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, countries);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setThreshold(1);
    }

    private void setUpListeners() {
        neetScoreGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.neetScoreYes) {
                neetScoreLayout.setVisibility(View.VISIBLE);
            } else {
                neetScoreLayout.setVisibility(View.GONE);
            }
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Get user data
                Map<String, Object> userData = getUserData();

                // Ensure userId is not null before using it.
                if (userId != null && !userId.isEmpty()) {
                    // Save user data to the "Users" collection
                    saveUserDataToUsersCollection(userData);
                    // Call the saveUserData method in FirestoreHelper to save to "app_submissions"
                    firestoreHelper.saveUserData(userData, userId, v, sharedPreferences, navController, isBottomSheet);
                } else {
                    Toast.makeText(getContext(), "Missing user information. Please sign in again.", Toast.LENGTH_SHORT).show();
                    //  Consider navigating the user back to the login screen
                    //  navController.navigate(R.id.loginFragment); // Replace with your login fragment ID
                }
            }
        });

        backButton.setOnClickListener(v -> {
            navController.popBackStack();
        });
    }

    private void saveUserDataToUsersCollection(Map<String, Object> userData) {
        //  Set the user's role.
        userData.put("role", userRole);

        if (userId != null && !userId.isEmpty()) {
            firestoreDB.collection("Users").document(userId)
                    .set(userData, SetOptions.merge()) // Use SetOptions.merge() to update or create
                    .addOnSuccessListener(aVoid -> {
                        // Save to shared preferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_role", userRole);
                        editor.putString("user_name", (String) userData.get("name"));
                        editor.putString("user_email", (String) userData.get("email"));
                        editor.putString("user_phone", (String) userData.get("phone"));
                        editor.apply();

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save user data. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Missing user information.  Please sign in again.", Toast.LENGTH_SHORT).show();
        }
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

    private Map<String, Object> getUserData() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String country = countrySpinner.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String neetScore = neetScoreEditText.getText().toString().trim();
        boolean hasNeetScore = neetScoreYes.isChecked();
        boolean hasPassport = passportYes.isChecked();

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
        userData.put("userId", userId);
        userData.put("timestamp", FieldValue.serverTimestamp());


        return userData;
    }
}

