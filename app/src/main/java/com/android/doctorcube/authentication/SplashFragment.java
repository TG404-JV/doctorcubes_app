package com.android.doctorcube.authentication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
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

        // Delay for 2 seconds before navigation
        new Handler().postDelayed(() -> {
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

    private void navigateToFragment(NavController navController, String role) {
        if ("user".equals(role)) {
            navController.navigate(R.id.mainActivity2);
        } else {
            navController.navigate(R.id.adminActivity2);
        }
    }
}
