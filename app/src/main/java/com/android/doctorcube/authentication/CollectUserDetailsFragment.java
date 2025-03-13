package com.android.doctorcube.authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.doctorcube.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class CollectUserDetailsFragment extends Fragment {

    private EditText nameEditText, emailEditText, phoneEditText, stateEditText, cityEditText, neetScoreEditText;
    private Spinner countrySpinner;
    private RadioGroup needScoreGroup, passportGroup;
    private Button submitButton;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collect_user_details, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        stateEditText = view.findViewById(R.id.stateEditText);
        cityEditText = view.findViewById(R.id.cityEditText);
        neetScoreEditText = view.findViewById(R.id.neetScore);
        countrySpinner = view.findViewById(R.id.countrySpinner);
        needScoreGroup = view.findViewById(R.id.needScoreGroup);
        passportGroup = view.findViewById(R.id.Passport);
        submitButton = view.findViewById(R.id.submitButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Setup country spinner with 5 countries
        setupCountrySpinner();

        needScoreGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.needScoreYes) {
                neetScoreEditText.setVisibility(View.VISIBLE);
            } else {
                neetScoreEditText.setVisibility(View.GONE);
            }
        });

        submitButton.setOnClickListener(v -> submitData(v));

        return view;
    }

    private void setupCountrySpinner() {
        // Array of 5 countries
        String[] countries = {"India", "United States", "United Kingdom", "Canada", "Australia"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                countries);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        countrySpinner.setAdapter(adapter);
    }

    private void submitData(View view) {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String state = stateEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String country = countrySpinner.getSelectedItem().toString();
        String neetScore = neetScoreEditText.getText().toString().trim();

        int selectedNeedScoreId = needScoreGroup.getCheckedRadioButtonId();
        String needScore = (selectedNeedScoreId == R.id.needScoreYes) ? "Yes" : "No";

        int selectedPassportId = passportGroup.getCheckedRadioButtonId();
        String passport = (selectedPassportId == R.id.PassportYes) ? "Yes" : "No";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(state) || TextUtils.isEmpty(city)) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current date in ddMMyy format (matching the JavaScript code)
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date());

        // Create a HashMap for user data
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("name", name);
        formData.put("email", email);
        formData.put("phone", phone);
        formData.put("state", state);
        formData.put("city", city);
        formData.put("country", country);
        formData.put("needScore", needScore);
        formData.put("neetScore", needScore.equals("Yes") ? neetScore : "N/A");
        formData.put("passport", passport);

        // Create reference to "registrations/{ddmmyy}" path (matching the JavaScript path)
        DatabaseReference registrationRef = FirebaseDatabase.getInstance().getReference("registrations").child(formattedDate);

        // Generate a unique key under the date node
        DatabaseReference newRegistrationRef = registrationRef.push();

        // Upload data
        newRegistrationRef.setValue(formData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Thank you! Our team will connect with you soon.", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "Data saved successfully with key: " + newRegistrationRef.getKey());

                // Navigate to next screen
                Navigation.findNavController(view).navigate(R.id.action_collectUserDetailsFragment_to_mainActivity2);
            } else {
                Toast.makeText(getContext(), "There was an error submitting your form. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Error saving data", task.getException());
            }
        });
    }
}