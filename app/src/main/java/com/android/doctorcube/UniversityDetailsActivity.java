package com.android.doctorcube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UniversityDetailsActivity extends AppCompatActivity {

    // Views
    private ImageView universityImageView;
    private TextView nameTextView, locationTextView, courseTextView, degreeTextView,
            durationTextView, gradeTextView, intakeTextView, descriptionTextView,
            establishedTextView, rankingTextView, addressTextView, phoneTextView, emailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_university_details);

        // Initialize views
        initializeViews();

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get data from the intent
        Intent intent = getIntent();
        if (intent != null) {
            // Retrieve university details from the intent
            String location = intent.getStringExtra("UNIVERSITY_LOCATION");
            String country = intent.getStringExtra("UNIVERSITY_COUNTRY");
            String intake = intent.getStringExtra("UNIVERSITY_INTAKE");
            int imageResourceId = intent.getIntExtra("UNIVERSITY_IMAGE", R.drawable.flag_china);

            // Set data to views
            locationTextView.setText(location + ", " + country);
            universityImageView.setImageResource(imageResourceId);

            // Example static data for additional fields
            descriptionTextView.setText("This university was established in 1965 and has been ranked among the top educational institutions consistently for the past decade.");
            establishedTextView.setText("1965");
            rankingTextView.setText("#15 National");
            addressTextView.setText("123 University Avenue, Boston, MA 02215");
            phoneTextView.setText("+1 (555) 123-4567");
            emailTextView.setText("admissions@university.edu");
        }
    }

    private void initializeViews() {
        universityImageView = findViewById(R.id.university_image);
        locationTextView = findViewById(R.id.university_location);
        descriptionTextView = findViewById(R.id.university_description);
        establishedTextView = findViewById(R.id.university_established);
        rankingTextView = findViewById(R.id.university_ranking);
        addressTextView = findViewById(R.id.university_address);
        phoneTextView = findViewById(R.id.university_phone);
        emailTextView = findViewById(R.id.university_email);
    }
}
