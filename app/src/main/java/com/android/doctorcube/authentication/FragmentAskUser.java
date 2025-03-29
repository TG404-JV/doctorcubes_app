package com.android.doctorcube.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.airbnb.lottie.LottieAnimationView;
import com.android.doctorcube.R;
import com.google.android.material.button.MaterialButton;

public class FragmentAskUser extends Fragment {

    private TextView headingText;
    private LottieAnimationView lottieAnimationView;
    private final String[] headings = {"Build Your Career", "Grow Your Skills", "Shape Your Future"};
    private final int[] lottieFiles = {R.raw.anim_ask_user1, R.raw.anim_ask_user2, R.raw.anim_ask_user3};
    private int currentIndex = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long DELAY_MS = 3000; // 3 seconds delay between transitions
    private boolean isAnimating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ask_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        headingText = view.findViewById(R.id.heading_text);
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView);
        AppCompatButton loginButton = view.findViewById(R.id.loginButton);
        AppCompatButton createAccountButton = view.findViewById(R.id.createAccountButton);

        // Set initial state
        updateHeadingAndAnimation(false); // Pass false for no animation on initial load

        // Start the looping mechanism
        startAnimationLoop();

        // Set button click listeners
        loginButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.loginFragment2)
        );

        createAccountButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.createAccountFragment2)
        );
    }

    private void updateHeadingAndAnimation(boolean animate) {
        if (isAnimating) return; // Prevent concurrent animations

        final String newHeading = headings[currentIndex];
        final int newLottieFile = lottieFiles[currentIndex];

        if (animate) {
            isAnimating = true;
            // Fade out current heading and animation
            headingText.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    headingText.setText(newHeading);
                    headingText.animate().alpha(1f).setDuration(500).setListener(null); //fade in
                }
            });

            lottieAnimationView.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Change Lottie animation source and play
                    lottieAnimationView.setAnimation(newLottieFile);
                    lottieAnimationView.playAnimation();
                    lottieAnimationView.animate().alpha(1f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            isAnimating = false; //allow next animation
                        }
                    }); // Fade in
                }
            });
        } else {
            // If not animating, set values directly
            headingText.setText(newHeading);
            lottieAnimationView.setAnimation(newLottieFile);
            lottieAnimationView.playAnimation();
            isAnimating = false;
        }
    }

    private void startAnimationLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Move to the next index, loop back to 0 if at the end
                currentIndex = (currentIndex + 1) % headings.length;
                updateHeadingAndAnimation(true); // Pass true to animate
                // Schedule the next update
                handler.postDelayed(this, DELAY_MS);
            }
        }, DELAY_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the handler to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}
