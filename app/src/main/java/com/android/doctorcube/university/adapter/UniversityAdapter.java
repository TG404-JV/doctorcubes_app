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
import java.util.List;

public class UniversityAdapter extends RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder> {

    private List<University> universities;
    private List<University> originalUniversities;
    private Context context;

    public UniversityAdapter(Context context, List<University> universities) {
        this.context = context;
        this.universities = universities;
        this.originalUniversities = new ArrayList<>(universities);
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

        // Passing university details to UniversityDetailsActivity
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

    public void filter(String query) {
        universities.clear();

        if (query.isEmpty()) {
            universities.addAll(originalUniversities);
        } else {
            query = query.toLowerCase();
            for (University university : originalUniversities) {
                if (university.getName().toLowerCase().contains(query) ||
                        university.getLocation().toLowerCase().contains(query) ||
                        university.getCountry().toLowerCase().contains(query)) {
                    universities.add(university);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void sortByName(boolean ascending) {
        universities.sort((u1, u2) -> ascending ? u1.getName().compareToIgnoreCase(u2.getName()) :
                u2.getName().compareToIgnoreCase(u1.getName()));
        notifyDataSetChanged();
    }

    public void sortByGrade(boolean ascending) {
        universities.sort((u1, u2) -> ascending ? u1.getGrade().compareToIgnoreCase(u2.getGrade()) :
                u2.getGrade().compareToIgnoreCase(u1.getGrade()));
        notifyDataSetChanged();
    }

    public void filterByCountry(String country) {
        universities.clear();

        if (country.equals("All")) {
            universities.addAll(originalUniversities);
        } else {
            for (University university : originalUniversities) {
                if (university.getCountry().equals(country)) {
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