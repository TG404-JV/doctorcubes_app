package com.android.doctorcube.adminpannel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private static final String TAG = "StudentAdapter";
    private static final int REQUEST_CALL_PHONE = 1;
    private List<Student> studentList;
    private Context context;
    private DatabaseReference databaseReference;
    private Student studentToCall;

    public StudentAdapter(List<Student> studentList, Context context) {
        this.studentList = studentList;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("registrations");
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        if (student == null) {
            Log.e(TAG, "Student at position " + position + " is null");
            return;
        }

        holder.name.setText(student.getName());
        holder.state.setText(student.getState());
        holder.contact.setText(student.getMobile());
        holder.lastUpdated.setText("Last Updated: " + (student.getLastCallDate() != null ? student.getLastCallDate() : "N/A"));

        holder.btnCall.setOnClickListener(v -> {
            if (student.getMobile() != null && !student.getMobile().equals("N/A")) {
                showCallConfirmationDialog(student);
            } else {
                Toast.makeText(context, "No valid phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        holder.checkBoxCallMade.setOnCheckedChangeListener(null);
        holder.checkBoxCallMade.setChecked("called".equals(student.getCallStatus()));
        holder.checkBoxCallMade.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Student currentStudent = studentList.get(adapterPosition);
                currentStudent.setCallStatus(isChecked ? "called" : "pending");
                updateCallStatusInFirebase(currentStudent);
            }
        });

        holder.checkBoxInterested.setOnCheckedChangeListener(null);
        holder.checkBoxInterested.setChecked(student.getIsInterested());
        holder.checkBoxInterested.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Student currentStudent = studentList.get(adapterPosition);
                currentStudent.setIsInterested(isChecked);
                updateInterestedStatusInFirebase(currentStudent);
            }
        });

        // Handle "Admitted" checkbox (assuming this is a new field in Student class)
        holder.checkBoxAdmitted.setOnCheckedChangeListener(null);
        holder.checkBoxAdmitted.setChecked(student.isAdmitted()); // Add isAdmitted() to Student class
        holder.checkBoxAdmitted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Student currentStudent = studentList.get(adapterPosition);
                currentStudent.setAdmitted(isChecked); // Add setAdmitted() to Student class
                updateAdmittedStatusInFirebase(currentStudent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    private void showCallConfirmationDialog(Student student) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm Call")
                .setMessage("Are you sure you want to call " + student.getName() + " at " + student.getMobile() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        studentToCall = student;
                        ActivityCompat.requestPermissions((android.app.Activity) context, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + student.getMobile()));
                        context.startActivity(intent);
                        updateLastCallDate(student);
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true)
                .show();
    }

    private void updateCallStatusInFirebase(Student student) {
        if (student.getId() == null || student.getSubmissionDate() == null) return;
        databaseReference.child(student.getSubmissionDate()).child(student.getId())
                .child("callStatus").setValue(student.getCallStatus())
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update call status: " + e.getMessage()));
    }

    private void updateInterestedStatusInFirebase(Student student) {
        if (student.getId() == null || student.getSubmissionDate() == null) return;
        databaseReference.child(student.getSubmissionDate()).child(student.getId())
                .child("isInterested").setValue(student.getIsInterested())
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update interested status: " + e.getMessage()));
    }

    private void updateAdmittedStatusInFirebase(Student student) {
        if (student.getId() == null || student.getSubmissionDate() == null) return;
        databaseReference.child(student.getSubmissionDate()).child(student.getId())
                .child("isAdmitted").setValue(student.isAdmitted())
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update admitted status: " + e.getMessage()));
    }

    private void updateLastCallDate(Student student) {
        if (student.getId() == null || student.getSubmissionDate() == null) return;
        String currentDate = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date());
        student.setLastCallDate(currentDate);
        databaseReference.child(student.getSubmissionDate()).child(student.getId())
                .child("lastCallDate").setValue(currentDate)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update last call date: " + e.getMessage()));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PHONE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (studentToCall != null) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + studentToCall.getMobile()));
                context.startActivity(intent);
                updateLastCallDate(studentToCall);
                studentToCall = null;
            }
        } else {
            Toast.makeText(context, "Call permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView name, state, contact, lastUpdated;
        Button btnCall;
        CheckBox checkBoxCallMade, checkBoxInterested, checkBoxAdmitted;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_student_name);
            state = itemView.findViewById(R.id.tv_student_state);
            contact = itemView.findViewById(R.id.tv_student_contact);
            lastUpdated = itemView.findViewById(R.id.tv_last_updated);
            btnCall = itemView.findViewById(R.id.btn_call_student);
            checkBoxCallMade = itemView.findViewById(R.id.checkbox_call_made);
            checkBoxInterested = itemView.findViewById(R.id.checkbox_interested);
            checkBoxAdmitted = itemView.findViewById(R.id.checkbox_admitted);
        }
    }
}