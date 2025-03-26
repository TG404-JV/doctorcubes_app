package com.android.doctorcube.authentication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SplashFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private ImageView ivLogo;
    private TextView tvAppName, tvTagline, tvLoading;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        // Initialize views
        initializeViews(view);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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

        // Start animations with a slight delay
        new Handler(Looper.getMainLooper()).postDelayed(this::startAnimations, 300);

        // Check login status and navigate after 2 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

            if (isLoggedIn && currentUser != null) {
                String userRole = sharedPreferences.getString("user_role", "");
                navigateToFragment(navController, userRole);
            } else {
                if (currentUser != null) {
                    mAuth.signOut();
                }
                navController.navigate(R.id.getStartedFragment);
            }
        }, 2000);

        return view;
    }

    private void initializeViews(View view) {
        ivLogo = view.findViewById(R.id.ivLogo);
        tvAppName = view.findViewById(R.id.tvAppName);
        tvTagline = view.findViewById(R.id.tvTagline);
        progressBar = view.findViewById(R.id.progressBar);
        tvLoading = view.findViewById(R.id.tvLoading);
    }

    private void startAnimations() {
        // Logo scale animation
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, View.SCALE_X, 0.5f, 1f);
        logoScaleX.setDuration(1000);
        logoScaleX.setInterpolator(new AnticipateOvershootInterpolator());

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, View.SCALE_Y, 0.5f, 1f);
        logoScaleY.setDuration(1000);
        logoScaleY.setInterpolator(new AnticipateOvershootInterpolator());

        // App name fade in
        ObjectAnimator appNameFadeIn = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 0f, 1f);
        appNameFadeIn.setDuration(800);
        appNameFadeIn.setStartDelay(500);

        // Tagline fade in
        ObjectAnimator taglineFadeIn = ObjectAnimator.ofFloat(tvTagline, View.ALPHA, 0f, 1f);
        taglineFadeIn.setDuration(800);
        taglineFadeIn.setStartDelay(800);

        // Progress bar fade in
        ObjectAnimator progressFadeIn = ObjectAnimator.ofFloat(progressBar, View.ALPHA, 0f, 1f);
        progressFadeIn.setDuration(800);
        progressFadeIn.setStartDelay(1000);

        // Loading text fade in
        ObjectAnimator loadingFadeIn = ObjectAnimator.ofFloat(tvLoading, View.ALPHA, 0f, 1f);
        loadingFadeIn.setDuration(800);
        loadingFadeIn.setStartDelay(1200);

        // Play animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(logoScaleX).with(logoScaleY);
        animatorSet.play(appNameFadeIn).after(logoScaleX);
        animatorSet.play(taglineFadeIn).after(appNameFadeIn);
        animatorSet.play(progressFadeIn).after(taglineFadeIn);
        animatorSet.play(loadingFadeIn).after(progressFadeIn);
        animatorSet.start();
    }

    private void navigateToFragment(NavController navController, String role) {
        if ("user".equals(role)) {
            navController.navigate(R.id.mainActivity2);
        } else if ("admin".equals(role) || "superadmin".equals(role)) {
            navController.navigate(R.id.adminActivity2);
        } else {
            mAuth.signOut();
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();
            navController.navigate(R.id.getStartedFragment);
        }
    }
}