package com.android.doctorcube.university;

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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.CustomToast;
import com.android.doctorcube.R;
import com.android.doctorcube.database.FirestoreHelper;
import com.android.doctorcube.university.model.University;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ApplyBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText nameEditText, emailEditText, phoneEditText, stateEditText, cityEditText, neetScoreEditText;
    private AutoCompleteTextView countrySpinner;
    private RadioGroup neetScoreGroup, passportGroup;
    private RadioButton neetScoreYes, neetScoreNo, passportYes, passportNo;
    private TextInputLayout neetScoreLayout;
    private Button submitButton;
    private LinearLayout headerUniversity;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private University university;
    private String offer;
    private boolean isExistingData = false;
    private String existingDocumentId;
    private  String[] countries ;
    private FirestoreHelper firestoreHelper;

    public ApplyBottomSheetFragment(University university) {
        this.university = university;
    }

    public ApplyBottomSheetFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collect_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper(requireContext());

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
        headerUniversity = view.findViewById(R.id.headerUniversity);
        headerUniversity.setVisibility(View.GONE);


        countries = getResources().getStringArray(R.array.countries_array);


        if (getArguments() != null) {
            offer = getArguments().getString("event_title", "");
        }

        if (!TextUtils.isEmpty(offer)) {
            TextView t1 = view.findViewById(R.id.PersonlizedTxt);
            t1.setText(offer);
        }

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
            CustomToast.showToast(requireActivity(), "Error In Storing The data");
        }

        setUpCountrySpinner();
        prefillForm();
        setUpListeners();
        loadData();
    }

    private void setUpCountrySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, countries);
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
                phoneEditText.setEnabled(false);
            }
        }
    }

    private void setUpListeners() {
        neetScoreGroup.setOnCheckedChangeListener((group, checkedId) -> {
            neetScoreLayout.setVisibility(checkedId == R.id.neetScoreYes ? View.VISIBLE : View.GONE);
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
               firestoreHelper.updateUserDataField(Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), "fullName", nameEditText.getText().toString());
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "email", emailEditText.getText().toString());
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "phone", phoneEditText.getText().toString());
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "country", countrySpinner.getText().toString());
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "state", stateEditText.getText().toString());
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "city", cityEditText.getText().toString());
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "neetScore", neetScoreYes.isChecked() ? neetScoreEditText.getText().toString() : "N/A");
               firestoreHelper.updateUserDataField(mAuth.getCurrentUser().getUid(), "hasPassport", passportYes.isChecked());
               dismiss();
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

    private void loadData() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String phone = phoneEditText.getText().toString().trim();

        firestoreHelper.loadUserDataFromFirestore(userId);
        loadFromSharedPrefferences(userId,phone);


    }

    private void loadFromSharedPrefferences(String userId, String phone) {
        nameEditText.setText(firestoreHelper.getUserDataField(userId, "fullName"));
        nameEditText.setEnabled(false);
        emailEditText.setText(firestoreHelper.getUserDataField(userId, "email"));
        emailEditText.setEnabled(false);
        phoneEditText.setText(firestoreHelper.getUserDataField(userId, "phone"));
        phoneEditText.setEnabled(false);
        countrySpinner.setText(firestoreHelper.getUserDataField(userId, "country"));
        stateEditText.setText(firestoreHelper.getUserDataField(userId, "state"));
        cityEditText.setText(firestoreHelper.getUserDataField(userId, "city"));
        String neetScore = firestoreHelper.getUserDataField(userId, "neetScore");
        Boolean hasNeetScore = neetScore != null && !neetScore.equals("N/A");
        if (hasNeetScore) {
            neetScoreYes.setChecked(true);
            neetScoreNo.setChecked(false);
            neetScoreEditText.setText(neetScore);
            neetScoreLayout.setVisibility(View.VISIBLE);
        } else {
            neetScoreYes.setChecked(false);
            neetScoreNo.setChecked(true);
            neetScoreLayout.setVisibility(View.GONE);
        }

        Boolean hasPassport = Boolean.parseBoolean(firestoreHelper.getUserDataField(userId, "hasPassport"));
        if (hasPassport) {
            passportYes.setChecked(true);
            passportNo.setChecked(false);
        } else {
            passportYes.setChecked(false);
            passportNo.setChecked(true);
        }

        submitButton.setText("Update Application");
        isExistingData = true;
    }




}

