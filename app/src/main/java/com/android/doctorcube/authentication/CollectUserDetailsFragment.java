package com.android.doctorcube.authentication;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.doctorcube.CustomToast;
import com.android.doctorcube.R;
import com.android.doctorcube.database.FirestoreHelper;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private  String[] countries ;
    private String userId;
    private String userEmail;
    private String userPhone;

    private FirestoreHelper firestoreHelper;
    private boolean isBottomSheet = false;

    public CollectUserDetailsFragment() {
        // Required empty public constructor
    }

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
        sharedPreferences = requireContext().getSharedPreferences("DoctorCubePrefs", Context.MODE_PRIVATE);

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
        backButton = view.findViewById(R.id.backButton);

        countries = getResources().getStringArray(R.array.countries_array);

        // Initialize FirestoreHelper
        firestoreHelper = new FirestoreHelper(requireContext());

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userEmail = currentUser.getEmail();
            userPhone = currentUser.getPhoneNumber();
        } else {
            CustomToast.showToast(requireActivity(), "Please Login");
            navController.navigate(R.id.loginFragment2);
            return;
        }

        // Set up UI components and fetch user data
        setUpCountrySpinner();
        setUpListeners();
        setUserData(); // Fetch and set user data
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
                Map<String, Object> userData = getUserData();
                if (userId != null && !userId.isEmpty()) {
                    firestoreHelper.saveUserData(userData, userId, v, sharedPreferences, navController, isBottomSheet);
                } else {
                    CustomToast.showToast(requireActivity(), "Missing user information. Please sign in again.");
                }
            }
        });

        backButton.setOnClickListener(v -> navController.popBackStack());
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
            CustomToast.showToast(requireActivity(), "Select whether you have a NEET score");
            return false;
        }
        if (neetScoreYes.isChecked() && TextUtils.isEmpty(neetScore)) {
            neetScoreEditText.setError("Enter your NEET score");
            return false;
        }
        if (passportGroup.getCheckedRadioButtonId() == -1) {
            CustomToast.showToast(requireActivity(), "Select whether you have a passport");
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
        userData.put("fullName", name); // Match UserDataManager key
        userData.put("email", email);   // Match UserDataManager key
        userData.put("phone", phone);   // Match UserDataManager key
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

    private void setUserData() {
        if (userId == null || userId.isEmpty()) {
            CustomToast.showToast(requireActivity(), "User ID missing. Please sign in again.");
            return;
        }

        // Use singleton instance of UserDataManager
        UserDataManager userDataManager = UserDataManager.getInstance(requireContext().getApplicationContext());
        HashMap<String, String> userDetails = userDataManager.getUserDetails(userId);

        if (userDetails != null) {
            String fullName = userDetails.get("fullName");
            String email = userDetails.get("email");
            String phone = userDetails.get("phone");

            if (fullName != null) nameEditText.setText(fullName);
            if (email != null) emailEditText.setText(email);
            if (phone != null) phoneEditText.setText(phone);

            // Make fields read-only
            nameEditText.setEnabled(false);
            emailEditText.setEnabled(false);
            phoneEditText.setEnabled(false);
        } else {
            // Fallback to async fetch if local data is unavailable
            userDataManager.getUserDetailsWithCallback(userId, new UserDataManager.OnUserDataFetchedListener() {
                @Override
                public void onDataFetched(HashMap<String, String> data) {
                    String fullName = data.get("fullName");
                    String email = data.get("email");
                    String phone = data.get("phone");

                    if (fullName != null) nameEditText.setText(fullName);
                    if (email != null) emailEditText.setText(email);
                    if (phone != null) phoneEditText.setText(phone);

                    // Make fields read-only
                    nameEditText.setEnabled(false);
                    emailEditText.setEnabled(false);
                    phoneEditText.setEnabled(false);
                }

                @Override
                public void onDataFetchFailed() {
                    CustomToast.showToast(requireActivity(), "Failed to fetch user details.");
                }
            });
        }
    }
}