package com.android.doctorcube.authentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;


import com.android.doctorcube.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SplashFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private ShapeableImageView ivLogo;
    private TextView tvAppName, tvSlogan;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        // Initialize views
        initializeViews(view);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize Encrypted SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "DoctorCubePrefs",
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            sharedPreferences = requireContext().getSharedPreferences("DoctorCubePrefs", requireContext().MODE_PRIVATE);
        }

        // Check login status and navigate after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // Fetch user role from Firestore and then navigate
                fetchUserRoleAndNavigate(currentUser, navController);
            } else {
                navController.navigate(R.id.getStartedFragment);
            }
        }, 2000);

        return view;
    }

    private void initializeViews(View view) {
        ivLogo = view.findViewById(R.id.splash_logo);
        tvAppName = view.findViewById(R.id.appname);
        tvSlogan = view.findViewById(R.id.slogun);
    }

    private void fetchUserRoleAndNavigate(FirebaseUser currentUser, NavController navController) {
        firestore.collection("Users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String userRole = document.getString("role");
                            if (userRole != null) {
                                // Navigate to the appropriate fragment
                                navigateToFragment(navController, userRole);
                            } else {
                                // Handle the case where the role is null
                                navigateToGetStarted(navController);
                            }
                        } else {
                            // Handle the case where the document doesn't exist
                            navigateToGetStarted(navController);
                        }
                    } else {
                        // Handle errors in the task
                        navigateToGetStarted(navController);
                    }
                });
    }

    private void navigateToGetStarted(NavController navController) {
        mAuth.signOut();
        navController.navigate(R.id.getStartedFragment);
    }

    private void navigateToFragment(NavController navController, String role) {
        if ("user".equals(role)) {
            navController.navigate(R.id.mainActivity2);
        } else if ("admin".equals(role) || "superadmin".equals(role)) {
            navController.navigate(R.id.adminActivity2);
        } else {
            // Handle unknown roles
            Log.w("SplashFragment", "Unknown user role: " + role);
            navigateToGetStarted(navController); // Go to GetStarted in case of unknown role
        }
    }
}
