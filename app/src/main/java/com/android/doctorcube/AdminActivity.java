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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "AdminActivity";
    private static final String PREFS_NAME = "DoctorCubePrefs";
    private static final String KEY_USER_ROLE = "user_role";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private boolean isSuperAdmin = false;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = mAuth.getCurrentUser();

        // Initialize Encrypted SharedPreferences
        try {
            MasterKey masterKey = new MasterKey.Builder(this, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    this,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
        } catch (GeneralSecurityException | IOException e) {
            // Handle error
            Toast.makeText(this, "Error initializing secure storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
            isSuperAdmin = false;
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // fallback
        }


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


        if (currentUser != null) {
            // Set email and name from Firebase Authentication
            String email = currentUser.getEmail();
            String name = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Admin";

            adminEmailTextView.setText(email != null ? email : getString(R.string.admin_email));
            adminNameTextView.setText(name);


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
            // Fetch role from SharedPreferences
            String role = prefs.getString(KEY_USER_ROLE, "user");
            isSuperAdmin = "superadmin".equals(role);
            Log.d(TAG, "Retrieved role from Shared Preferences: " + role);

            if (isSuperAdmin) {
                loadFragment(new FragmentAddNewUser());
            } else {
                Toast.makeText(AdminActivity.this, "Only Super Admins can add new users.", Toast.LENGTH_SHORT).show();
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

