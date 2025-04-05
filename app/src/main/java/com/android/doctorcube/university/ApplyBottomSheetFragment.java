package com.android.doctorcube.university;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.android.doctorcube.CustomToast;
import com.android.doctorcube.R;
import com.android.doctorcube.authentication.datamanager.EncryptedSharedPreferencesManager;
import com.android.doctorcube.database.FirestoreHelper;
import com.android.doctorcube.university.model.University;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private EncryptedSharedPreferencesManager encryptedSharedPreferencesManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private University university;
    private String offer;
    private boolean isExistingData = false;
    private String existingDocumentId;
    private String[] countries;
    private FirestoreHelper firestoreHelper;
    private Context context;

    public ApplyBottomSheetFragment(University university) {
        this.university = university;
    }

    public ApplyBottomSheetFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collect_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if context is null
        if (context == null) {
            // Handle the error appropriately, such as logging or showing a message
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper(context);
        encryptedSharedPreferencesManager = new EncryptedSharedPreferencesManager(context);

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

        nameEditText.setText(encryptedSharedPreferencesManager.getString("name", ""));
        emailEditText.setText(encryptedSharedPreferencesManager.getString("email", ""));
        phoneEditText.setText(encryptedSharedPreferencesManager.getString("phone", ""));
        stateEditText.setText(encryptedSharedPreferencesManager.getString("state", ""));
        cityEditText.setText(encryptedSharedPreferencesManager.getString("city", ""));
        countrySpinner.setText(encryptedSharedPreferencesManager.getString("country", ""));
        neetScoreEditText.setText(encryptedSharedPreferencesManager.getString("neetScore", ""));
        if (encryptedSharedPreferencesManager.getBoolean("hasNeetScore", false)) {
            neetScoreYes.setChecked(true);
            neetScoreLayout.setVisibility(View.VISIBLE);
        } else {
            neetScoreNo.setChecked(true);
            neetScoreLayout.setVisibility(View.GONE);
        }
        if (encryptedSharedPreferencesManager.getBoolean("hasPassport", false)) {
            passportYes.setChecked(true);
        } else {
            passportNo.setChecked(true);
        }

        submitButton.setText("Update Application");
        isExistingData = true;

        countries = getResources().getStringArray(R.array.countries_array);

        if (getArguments() != null) {
            offer = getArguments().getString("event_title", "");
        }

        if (!TextUtils.isEmpty(offer)) {
            TextView t1 = view.findViewById(R.id.PersonlizedTxt);
            t1.setText(offer);
        }

        setUpCountrySpinner();
        setUpListeners();
        setupTextWatchers();

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveOrUpdateApplication();
                dismiss();
            }
        });
    }

    private void setUpCountrySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, countries);
        countrySpinner.setAdapter(adapter);
        countrySpinner.setThreshold(1);
    }

    private void setupTextWatchers() {
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("name", s.toString());
                updateFirestore("name", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("email", s.toString());
                updateFirestore("email", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("phone", s.toString());
                updateFirestore("phone", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        countrySpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("country", s.toString());
                updateFirestore("country", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        stateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("state", s.toString());
                updateFirestore("state", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        cityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("city", s.toString());
                updateFirestore("city", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        neetScoreEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                encryptedSharedPreferencesManager.putString("neetScore", s.toString());
                updateFirestore("neetScore", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setUpListeners() {
        neetScoreGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean hasNeet = checkedId == R.id.neetScoreYes;
            neetScoreLayout.setVisibility(hasNeet ? View.VISIBLE : View.GONE);
            encryptedSharedPreferencesManager.putBoolean("hasNeetScore", hasNeet);
            updateFirestore("hasNeetScore", hasNeet);
            if (!hasNeet) {
                encryptedSharedPreferencesManager.putString("neetScore", "");
                neetScoreEditText.setText("");
                updateFirestore("neetScore", "");
            }
        });

        passportGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean hasPassport = checkedId == R.id.passportYes;
            encryptedSharedPreferencesManager.putBoolean("hasPassport", hasPassport);
            updateFirestore("hasPassport", hasPassport);
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

    private void saveOrUpdateApplication() {
        if (context == null) return;
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String country = countrySpinner.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String neetScore = neetScoreEditText.getText().toString().trim();
        boolean hasNeetScore = neetScoreYes.isChecked();
        boolean hasPassport = passportYes.isChecked();
        String applicationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> applicationData = new HashMap<>();
        applicationData.put("userId", userId);
        applicationData.put("name", name);
        applicationData.put("email", email);
        applicationData.put("phone", phone);
        applicationData.put("country", country);
        applicationData.put("state", state);
        applicationData.put("city", city);
        applicationData.put("neetScore", hasNeetScore ? neetScore : "");
        applicationData.put("hasNeetScore", hasNeetScore);
        applicationData.put("hasPassport", hasPassport);
        applicationData.put("applicationDate", applicationDate);
        if (university != null) {
            applicationData.put("universityId", university.getId());
            applicationData.put("universityName", university.getName());
        }
        if (!TextUtils.isEmpty(offer)) {
            applicationData.put("appliedForOffer", offer);
        }

        if (isExistingData && existingDocumentId != null) {
            firestore.collection("app_submissions").document(existingDocumentId)
                    .update(applicationData)
                    .addOnSuccessListener(aVoid -> {
                        if (context != null) {
                            CustomToast.showToast(requireActivity(), "Application updated successfully");
                        }
                    })
                    .addOnFailureListener(e ->  {
                        if (context != null) {
                            CustomToast.showToast(requireActivity(), "Failed to update application: " + e.getMessage());
                        }
                    });
        } else {
            firestore.collection("app_submissions")
                    .add(applicationData)
                    .addOnSuccessListener(documentReference -> {
                        if (context != null) {
                            CustomToast.showToast(requireActivity(), "Application submitted successfully");
                        }
                        existingDocumentId = documentReference.getId();
                        isExistingData = true;
                        submitButton.setText("Update Application");
                    })
                    .addOnFailureListener(e -> {
                        if (context != null) {
                            CustomToast.showToast(requireActivity(), "Failed to submit application: " + e.getMessage());
                        }
                    });
        }
    }

    private void updateFirestore(String field, Object value) {
        if (context == null) return;
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        if (isExistingData && existingDocumentId != null) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put(field, value);
            firestore.collection("app_submissions").document(existingDocumentId)
                    .update(updateData)
                    .addOnFailureListener(e -> {
                        if (context != null) {
                            // Log the error or show a generic message if needed
                            CustomToast.showToast(requireActivity(), "Failed to update " + field + ": " + e.getMessage());
                        }
                    });
        } else if (mAuth.getCurrentUser() != null) {
            // If no existing document, try to find one based on user ID
            firestore.collection("app_submissions")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            existingDocumentId = documentSnapshot.getId();
                            isExistingData = true;
                            submitButton.setText("Update Application");
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put(field, value);
                            firestore.collection("app_submissions").document(existingDocumentId) // Corrected collection name
                                    .update(updateData)
                                    .addOnFailureListener(e -> {
                                        if (context != null) {
                                            // Log the error
                                            CustomToast.showToast(requireActivity(), "Failed to update " + field + ": " + e.getMessage());
                                        }
                                    });
                        } else {
                            //No document found.  You might want to create a new one.
                            Map<String, Object> newData = new HashMap<>();
                            newData.put(field, value);
                            newData.put("userId", userId); //  Include userId
                            firestore.collection("app_submissions")
                                    .add(newData)
                                    .addOnSuccessListener(documentReference -> {
                                        if (context != null) {
                                            existingDocumentId = documentReference.getId();
                                            isExistingData = true;
                                            submitButton.setText("Update Application");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (context != null){
                                            CustomToast.showToast(requireActivity(), "Failed to create new document: " + e.getMessage());
                                        }
                                    });
                        }
                    });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null; // Prevent further operations on a detached context.
    }
}

