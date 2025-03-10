package com.android.doctorcube;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.doctorcube.adminpannel.Student;
import com.android.doctorcube.adminpannel.StudentAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AdminActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize UI Components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.students_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Setup Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Setup RecyclerView with proper layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Initialize student list and adapter
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList, this);
        recyclerView.setAdapter(adapter);

        // Display a loading message
        Toast.makeText(this, "Loading student data...", Toast.LENGTH_SHORT).show();

        // Setup Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("registrations");

        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(this::loadStudentData);

        // Load data initially
        loadStudentData();
    }

    // Fetch data from Firebase
    private void loadStudentData() {
        swipeRefreshLayout.setRefreshing(true);
        Log.d(TAG, "📌 Starting to load student data from Firebase");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Student> newStudentList = new ArrayList<>();
                Log.d(TAG, "🔥 Total Date Nodes Found: " + snapshot.getChildrenCount());

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();
                    Log.d(TAG, "📅 Processing Date Key: " + dateKey);

                    for (DataSnapshot studentSnapshot : dateSnapshot.getChildren()) {
                        try {
                            Student student = studentSnapshot.getValue(Student.class);
                            if (student != null) {
                                student.setId(studentSnapshot.getKey());
                                if (student.getCallStatus() == null) {
                                    student.setCallStatus("pending");
                                }

                                // Add submission date if missing
                                if (student.getSubmissionDate() == null) {
                                    student.setSubmissionDate(dateKey);
                                }

                                newStudentList.add(student);
                                Log.d(TAG, "👤 Added student: " + student.getName());
                            } else {
                                Log.w(TAG, "⚠️ Student data conversion failed for ID: " + studentSnapshot.getKey());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "🚨 Error parsing student data: " + e.getMessage());
                        }
                    }
                }

                // Update UI on the main thread
                runOnUiThread(() -> {
                    studentList.clear();
                    studentList.addAll(newStudentList);
                    adapter.notifyDataSetChanged(); // Use simple notify instead for full refresh

                    swipeRefreshLayout.setRefreshing(false);

                    Log.d(TAG, "📊 RecyclerView updated with " + studentList.size() + " students");

                    if (studentList.isEmpty()) {
                        showNoStudentsPopup();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "🚨 Firebase Error: " + error.getMessage());
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(AdminActivity.this,
                            "Failed to load data: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // Show a popup when no students are found
    private void showNoStudentsPopup() {
        new AlertDialog.Builder(this)
                .setTitle("No Students Found")
                .setMessage("No student registrations are available in the database.")
                .setPositiveButton("Refresh", (dialog, which) -> loadStudentData())
                .setNegativeButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_students) {
            loadStudentData();
        } else if (id == R.id.nav_call_logs) {
            Toast.makeText(this, "Call Logs feature coming soon!", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}