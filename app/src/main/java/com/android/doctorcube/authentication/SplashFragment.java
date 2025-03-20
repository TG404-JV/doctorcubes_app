package com.android.doctorcube.authentication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.android.doctorcube.R;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SplashFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private ImageView ivGlobe, ivLogo, ivAirplane;
    private TextView tvAppName, tvTagline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        // Initialize views
        initializeViews(view);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Initialize Encrypted SharedPreferences
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "user_prefs",
                    masterKeyAlias,
                    requireContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        // Start animations with a slight delay
        new Handler(Looper.getMainLooper()).postDelayed(this::startAnimations, 300);

        // Delay for 2 seconds before navigation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sharedPreferences.getBoolean("isLoggedIn", false)) {
                String userType = sharedPreferences.getString("userType", "");
                navigateToFragment(navController, userType);
            } else {
                // Redirect to GetStartedFragment if user is NOT logged in
                navController.navigate(R.id.getStartedFragment);
            }
        }, 2000); // 2-second splash delay

        return view;
    }

    private void initializeViews(View view) {
        ivGlobe = view.findViewById(R.id.ivGlobe);
        ivLogo = view.findViewById(R.id.ivLogo);
        ivAirplane = view.findViewById(R.id.ivAirplane);
        tvAppName = view.findViewById(R.id.tvAppName);
        tvTagline = view.findViewById(R.id.tvTagline);
    }

    private void startAnimations() {
        // 1. Globe rotation animation
        ObjectAnimator globeRotation = ObjectAnimator.ofFloat(ivGlobe, View.ROTATION, 0f, 360f);
        globeRotation.setDuration(3000);
        globeRotation.setRepeatCount(ValueAnimator.INFINITE);
        globeRotation.setInterpolator(new LinearInterpolator());

        // 2. Logo scale animation
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(ivLogo, View.SCALE_X, 0.5f, 1f);
        logoScaleX.setDuration(1000);
        logoScaleX.setInterpolator(new AnticipateOvershootInterpolator());

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(ivLogo, View.SCALE_Y, 0.5f, 1f);
        logoScaleY.setDuration(1000);
        logoScaleY.setInterpolator(new AnticipateOvershootInterpolator());

        // 3. App name animation
        ObjectAnimator appNameFadeIn = ObjectAnimator.ofFloat(tvAppName, View.ALPHA, 0f, 1f);
        appNameFadeIn.setDuration(800);
        appNameFadeIn.setStartDelay(500);

        // 4. Tagline fade in
        ObjectAnimator taglineFadeIn = ObjectAnimator.ofFloat(tvTagline, View.ALPHA, 0f, 1f);
        taglineFadeIn.setDuration(800);
        taglineFadeIn.setStartDelay(800);

        // 5. Airplane flying animation
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        ObjectAnimator airplaneMove = ObjectAnimator.ofFloat(
                ivAirplane,
                View.TRANSLATION_X,
                -400f,
                screenWidth
        );
        airplaneMove.setDuration(1500);
        airplaneMove.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator airplanePathY = ObjectAnimator.ofFloat(
                ivAirplane,
                View.TRANSLATION_Y,
                0f, -50f, 0f, 50f, 0f
        );
        airplanePathY.setDuration(1500);
        airplanePathY.setInterpolator(new AccelerateDecelerateInterpolator());

        // Play animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(globeRotation);
        animatorSet.play(logoScaleX).with(logoScaleY);
        animatorSet.play(appNameFadeIn).after(logoScaleX);
        animatorSet.play(taglineFadeIn).after(appNameFadeIn);
        animatorSet.play(airplaneMove).with(airplanePathY).after(500);
        animatorSet.start();
    }

    private void navigateToFragment(NavController navController, String role) {
        if ("user".equals(role)) {
            navController.navigate(R.id.mainActivity2);
        } else {
            navController.navigate(R.id.adminActivity2);
        }
    }
}