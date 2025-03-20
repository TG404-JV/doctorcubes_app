package com.android.doctorcube;

import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;

public class MainActivity extends AppCompatActivity {

    private BubbleNavigationConstraintView bubbleNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Bubble Navigation
        bubbleNavigation = findViewById(R.id.bubbleNavigation);

        // Set initial item & prevent reselection animation
        bubbleNavigation.setCurrentActiveItem(0);

        // Load the home fragment by default
        loadFragment(new HomeFragment(), false);

        // Handle navigation selection
        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                // Apply animation to the selected view
                applySelectionAnimation(view);

                Fragment fragment = null;

                switch (position) {
                    case 0:
                        fragment = new HomeFragment();
                        break;
                    case 1:
                        fragment = new StudyMaterialFragment();
                        break;
                    case 2:
                        fragment = new SettingsFragment();
                        break;
                }

                if (fragment != null) {
                    loadFragment(fragment, true);
                }
            }
        });
    }

    private void applySelectionAnimation(View view) {
        // Apply a bounce/overshoot animation to the selected tab
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }

    private void loadFragment(Fragment fragment, boolean withAnimation) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Add animations to fragment transitions
        if (withAnimation) {
            transaction.setCustomAnimations(
                    R.anim.fragment_fade_enter,
                    R.anim.fragment_fade_exit,
                    R.anim.fragment_fade_enter,
                    R.anim.fragment_fade_exit
            );
        }

        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null); // Allow navigating back
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_container);

        // Check if the current fragment is HomeFragment
        if (currentFragment instanceof HomeFragment) {
            finish(); // Close the app
        } else {
            super.onBackPressed(); // Go to previous fragment
            updateNavigationSelection(); // Update bottom navigation state
        }
    }

    private void updateNavigationSelection() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);

        if (currentFragment instanceof HomeFragment) {
            bubbleNavigation.setCurrentActiveItem(0);
        } else if (currentFragment instanceof StudyMaterialFragment) {
            bubbleNavigation.setCurrentActiveItem(1);
        } else if (currentFragment instanceof SettingsFragment) {
            bubbleNavigation.setCurrentActiveItem(2);
        }
    }
}
