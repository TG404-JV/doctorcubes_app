package com.android.doctorcube.authentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.doctorcube.R;

public class GetStartedFragment extends Fragment {

    private Button btnGetStarted, btnCreateAccount, btnLogin;
    private boolean isButtonsVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_get_started, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnGetStarted = view.findViewById(R.id.btn_get_started);
        btnCreateAccount = view.findViewById(R.id.btn_create_account);
        btnLogin = view.findViewById(R.id.btn_login);

        btnGetStarted.setOnClickListener(v -> {
            if (!isButtonsVisible) {
                showButtonsWithAnimation();
                isButtonsVisible = true;
            }
        });

        btnCreateAccount.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.createAccountFragment2)
        );

        btnLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.loginFragment2)
        );
    }

    private void showButtonsWithAnimation() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);

        btnCreateAccount.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

        btnCreateAccount.startAnimation(fadeIn);
        btnLogin.startAnimation(fadeIn);
    }
}
