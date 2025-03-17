package com.android.doctorcube.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.doctorcube.R;

public class SettingsHome extends Fragment {
    private static final String TAG = "SettingsHomeFragment";
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController inside onViewCreated()
        navController = Navigation.findNavController(view);

        CardView editDetailsCard = view.findViewById(R.id.profile_settings_card);
        CardView aboutCard = view.findViewById(R.id.about_card);
        LinearLayout privacyLayout = view.findViewById(R.id.privacy_policy_layout);
        LinearLayout faqLayout = view.findViewById(R.id.faq_layout);
        Button logoutButton = view.findViewById(R.id.logout_button);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Set click listeners with safe navigation
        if (editDetailsCard != null) {
            editDetailsCard.setOnClickListener(v -> safeNavigate(R.id.action_settingsHome_to_fragmentEditDetails));
        }
        if (aboutCard != null) {
            aboutCard.setOnClickListener(v -> safeNavigate(R.id.action_settingsHome_to_fragmentAbout));
        }
        if (privacyLayout != null) {
            privacyLayout.setOnClickListener(v -> safeNavigate(R.id.action_settingsHome_to_fragmentPrivacy));
        }
        if (faqLayout != null) {
            faqLayout.setOnClickListener(v -> safeNavigate(R.id.action_settingsHome_to_fragmentFAQ));
        }
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> Log.d(TAG, "Logout clicked"));
        }

        // Handle toolbar back button
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() != R.id.settingsHome) {
                    navController.popBackStack(R.id.settingsHome, false);
                } else {
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    // Safe navigation method to avoid crashes
    private void safeNavigate(int actionId) {
        try {
            if (navController != null && navController.getCurrentDestination() != null) {
                navController.navigate(actionId);
            } else {
                Log.e(TAG, "NavController is null or navigation failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed: " + e.getMessage());
        }
    }
}
