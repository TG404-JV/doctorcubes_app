package com.android.doctorcube.adminpannel;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;
import com.android.doctorcube.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private static final String TAG = "StudentAdapter";
    private List<Student> studentList;
    private Context context;
    private DatabaseReference databaseReference;

    public StudentAdapter(List<Student> studentList, Context context) {
        this.studentList = studentList;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("registrations");
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        // Verify student data before binding
        if (student == null) {
            Log.e(TAG, "Student at position " + position + " is null");
            return;
        }

        // Set basic info with null checks
        holder.name.setText(student.getName());
        holder.state.setText(student.getState());
        holder.contact.setText(student.getMobile());

        // Debug log to verify data binding
        Log.d(TAG, "Binding student at position " + position + ": " + student.getName());

        // Handle Call Button
        holder.btnCall.setOnClickListener(v -> {
            if (student.getMobile() != null && !student.getMobile().equals("N/A")) {
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + student.getMobile()));
                context.startActivity(intent);

                // Update last call date could be added here
            } else {
                Toast.makeText(context, "No valid phone number available",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Set checkbox state without triggering listener
        holder.checkBoxCallMade.setOnCheckedChangeListener(null);
        holder.checkBoxCallMade.setChecked("called".equals(student.getCallStatus()));

        // Add listener for checkbox changes
        holder.checkBoxCallMade.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Student currentStudent = studentList.get(adapterPosition);
                String newStatus = isChecked ? "called" : "pending";
                currentStudent.setCallStatus(newStatus);

                // Update status in Firebase
                updateCallStatusInFirebase(currentStudent.getId(),
                        currentStudent.getSubmissionDate(), newStatus);
            }
        });

        // Make the item view expandable for more details (optional)
        holder.itemView.setOnClickListener(v -> {
            // Could expand to show more details
            Toast.makeText(context, "Student: " + student.getName(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    // Update Firebase when call status changes
    private void updateCallStatusInFirebase(String studentId, String submissionDate, String newStatus) {
        if (studentId == null || submissionDate == null) {
            Log.e(TAG, "Cannot update Firebase: Missing ID or submission date");
            return;
        }

        databaseReference.child(submissionDate)
                .child(studentId)
                .child("callStatus")
                .setValue(newStatus)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Call status updated successfully for " + studentId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update call status: " + e.getMessage()));
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView name, state, contact;
        Button btnCall;
        CheckBox checkBoxCallMade;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_student_name);
            state = itemView.findViewById(R.id.tv_student_state);
            contact = itemView.findViewById(R.id.tv_student_contact);
            btnCall = itemView.findViewById(R.id.btn_call_student);
            checkBoxCallMade = itemView.findViewById(R.id.checkbox_call_made);
        }
    }
}