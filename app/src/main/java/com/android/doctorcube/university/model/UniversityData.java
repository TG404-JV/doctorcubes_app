package com.android.doctorcube.university.model;

import com.android.doctorcube.R;
import java.util.ArrayList;
import java.util.List;

public class UniversityData {
    public static List<University> getUniversities() {
        List<University> universities = new ArrayList<>();

        universities.add(new University(
                "ru001", "Sechenov University", "Moscow", "Russia",
                "General Medicine", "Bachelor", "6 years", "A+", "Feb-25, Sep-25",
                "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 500", "Scholarships up to 30% available"
        ));

        universities.add(new University(
                "ru002", "Moscow State University of Medicine and Dentistry", "Moscow", "Russia",
                "Dentistry", "Bachelor", "6 years", "A", "Feb-25, Sep-25",
                "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_russia,
                "medical", "Top 500", "Scholarships up to 25% available"
        ));

        universities.add(new University(
                "ch001", "Peking University", "Beijing", "China",
                "MBBS", "Bachelor", "5 years", "A+", "Mar-25, Oct-25",
                "English", "Public",
                R.drawable.icon_university, R.drawable.icon_university, R.drawable.flag_china,
                "medical", "Top 100", "Scholarships up to 40% available"
        ));

        return universities;
    }
}