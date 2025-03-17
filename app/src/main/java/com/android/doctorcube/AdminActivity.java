package com.android.doctorcube;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.android.doctorcube.adminpannel.adminhome.FragmentAddNewUser;
import com.android.doctorcube.adminpannel.adminhome.FragmentAdminHome;
import com.android.doctorcube.adminpannel.adminhome.FragmentExportXLData;
import com.android.doctorcube.adminpannel.adminhome.FragmentImportXLData;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AdminActivity";
    private static final String PREFS_NAME = "DoctorCubePrefs";
    private static final String KEY_USER_ROLE = "user_role";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private boolean isSuperAdmin = false; // Default to false until fetched
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI Components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Admin Panel");

        // Setup Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Setup Navigation Header
        setupNavigationHeader();

        // Load initial fragment (AdminFragment) if not restoring state
        if (savedInstanceState == null) {
            loadFragment(new FragmentAdminHome());
        }
    }

    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView adminNameTextView = headerView.findViewById(R.id.tv_admin_name);
        TextView adminEmailTextView = headerView.findViewById(R.id.tv_admin_email);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Set email and name from Firebase Authentication
            String email = currentUser.getEmail();
            String name = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Admin";

            adminEmailTextView.setText(email != null ? email : getString(R.string.admin_email));
            adminNameTextView.setText(name);

            // Fetch role from Firebase Realtime Database
            String uid = currentUser.getUid();
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        isSuperAdmin = "superadmin".equals(role);
                        // Cache role in SharedPreferences
                        prefs.edit().putString(KEY_USER_ROLE, role).apply();
                    } else {
                        // Default to "user" if no role found
                        isSuperAdmin = false;
                        prefs.edit().putString(KEY_USER_ROLE, "user").apply();
                        Toast.makeText(AdminActivity.this, "Role not found, defaulting to user", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AdminActivity.this, "Failed to fetch role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    isSuperAdmin = false; // Fallback
                }
            });
        } else {
            // User not signed in, fallback to defaults
            adminEmailTextView.setText(getString(R.string.admin_email));
            adminNameTextView.setText("Admin");
            isSuperAdmin = false;
            Toast.makeText(this, "Please sign in to access admin features", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_students) {
            loadFragment(new FragmentAdminHome());
        } else if (id == R.id.nav_add_new) {
            if (isSuperAdmin) {
                loadFragment(new FragmentAddNewUser());
            } else {
                Toast.makeText(this, "Only Super Admins can add new users.", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_import_excel) {
            loadFragment(new FragmentImportXLData());
        } else if (id == R.id.nav_export_excel) {
            loadFragment(new FragmentExportXLData());
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