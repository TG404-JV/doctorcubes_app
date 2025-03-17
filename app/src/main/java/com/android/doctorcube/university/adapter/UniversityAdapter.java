package com.android.doctorcube.university.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.R;
import com.android.doctorcube.UniversityDetailsActivity;
import com.android.doctorcube.university.model.University;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UniversityAdapter extends RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder> {

    private List<University> universities;
    private List<University> originalUniversities;
    private Context context;
    private String countryFilter;

    public UniversityAdapter(Context context, List<University> universities, String countryFilter) {
        this.context = context;
        this.universities = new ArrayList<>(universities);
        this.originalUniversities = new ArrayList<>(universities); // Initialize with filtered list
        this.countryFilter = countryFilter;
    }

    @NonNull
    @Override
    public UniversityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university, parent, false);
        return new UniversityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UniversityViewHolder holder, int position) {
        University university = universities.get(position);

        holder.nameTextView.setText(university.getName());
        holder.locationTextView.setText(university.getLocation() + ", " + university.getCountry());
        holder.courseTextView.setText("Course: " + university.getCourseName());
        holder.degreeTextView.setText("Degree: " + university.getDegreeType());
        holder.durationTextView.setText("Duration: " + university.getDuration());
        holder.gradeTextView.setText("Grade: " + university.getGrade());
        holder.intakeTextView.setText("Intake: " + university.getIntake());
        holder.imageView.setImageResource(university.getImageResourceId());

        holder.viewDetailsButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UniversityDetailsActivity.class);
            intent.putExtra("UNIVERSITY_ID", university.getId());
            intent.putExtra("UNIVERSITY_NAME", university.getName());
            intent.putExtra("UNIVERSITY_LOCATION", university.getLocation());
            intent.putExtra("UNIVERSITY_COUNTRY", university.getCountry());
            intent.putExtra("UNIVERSITY_COURSE", university.getCourseName());
            intent.putExtra("UNIVERSITY_DEGREE", university.getDegreeType());
            intent.putExtra("UNIVERSITY_DURATION", university.getDuration());
            intent.putExtra("UNIVERSITY_GRADE", university.getGrade());
            intent.putExtra("UNIVERSITY_INTAKE", university.getIntake());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return universities.size();
    }

    // 🔹 Update the adapter's data with a new list
    public void updateData(List<University> newUniversities) {
        this.universities.clear();
        this.universities.addAll(newUniversities);
        this.originalUniversities.clear();
        this.originalUniversities.addAll(newUniversities); // Sync originalUniversities with filtered list
        notifyDataSetChanged();
    }

    // 🔹 Sort universities by name (A-Z or Z-A)
    public void sortByName(final boolean ascending) {
        Collections.sort(universities, (u1, u2) -> ascending
                ? u1.getName().compareToIgnoreCase(u2.getName())
                : u2.getName().compareToIgnoreCase(u1.getName()));
        notifyDataSetChanged();
    }

    // 🔹 Sort universities by grade (High-Low or Low-High)
    public void sortByGrade(final boolean descending) {
        List<String> gradeOrder = Arrays.asList("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F");

        universities.sort((u1, u2) -> {
            int index1 = gradeOrder.indexOf(u1.getGrade());
            int index2 = gradeOrder.indexOf(u2.getGrade());

            if (index1 == -1) index1 = gradeOrder.size(); // If not found, push to the end
            if (index2 == -1) index2 = gradeOrder.size();

            return descending ? Integer.compare(index2, index1) : Integer.compare(index1, index2);
        });

        notifyDataSetChanged();
    }

    // 🔹 Filter universities by country (not used directly now, but kept for potential reuse)
    public void filterByCountry(String country) {
        universities.clear();
        if (country.equals("All")) {
            universities.addAll(originalUniversities);
        } else {
            for (University university : originalUniversities) {
                if (university.getCountry().equalsIgnoreCase(country)) {
                    universities.add(university);
                }
            }
        }
        notifyDataSetChanged();
    }

    // 🔹 Filter universities by name (for search functionality)
    public void filterByName(String query) {
        universities.clear();
        if (query.isEmpty()) {
            universities.addAll(originalUniversities);
        } else {
            for (University university : originalUniversities) {
                if (university.getName().toLowerCase(Locale.getDefault())
                        .contains(query.toLowerCase(Locale.getDefault()))) {
                    universities.add(university);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class UniversityViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, locationTextView, courseTextView, degreeTextView, durationTextView, gradeTextView, intakeTextView;
        Button viewDetailsButton;

        UniversityViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.university_image);
            nameTextView = itemView.findViewById(R.id.university_name);
            locationTextView = itemView.findViewById(R.id.university_location);
            courseTextView = itemView.findViewById(R.id.course_name);
            degreeTextView = itemView.findViewById(R.id.degree_type);
            durationTextView = itemView.findViewById(R.id.duration);
            gradeTextView = itemView.findViewById(R.id.grade);
            intakeTextView = itemView.findViewById(R.id.intake);
            viewDetailsButton = itemView.findViewById(R.id.btn_eligibility);
        }
    }
}