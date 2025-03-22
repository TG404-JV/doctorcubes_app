package com.android.doctorcube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;

public class MainActivity extends AppCompatActivity {

    private BubbleNavigationConstraintView bubbleNavigation;
    private Toolbar toolbar;
    private ImageView callButton;
    private TextView app_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            throw new IllegalStateException("Toolbar not found in activity_main.xml");
        }
        setSupportActionBar(toolbar);

        // Initialize call button
        callButton = toolbar.findViewById(R.id.call_button);
        if (callButton == null) {
            Toast.makeText(this, "Error: Missing call button in Toolbar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up Toolbar functionality
        setupToolbar();

        app_title=findViewById(R.id.app_title);
        app_title.setText("DoctorCubes");

        // Initialize Bubble Navigation
        bubbleNavigation = findViewById(R.id.bubbleNavigation);
        bubbleNavigation.setCurrentActiveItem(0);

        // Load the home fragment by default
        loadFragment(new HomeFragment(), false);

        // Handle navigation selection
        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                applySelectionAnimation(view);

                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = new HomeFragment();
                        toolbar.setTitle("DoctorCubes");
                        break;
                    case 1:
                        fragment = new StudyMaterialFragment();
                        toolbar.setTitle("Study Material");
                        break;
                    case 2:
                        fragment = new SettingsFragment();
                        toolbar.setTitle("Settings");
                        break;
                }

                if (fragment != null) {
                    loadFragment(fragment, true);
                }
            }
        });
    }

    private void setupToolbar() {
        // Call button click
        callButton.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:9876543210")); // Replace with your support number
            startActivity(callIntent);
        });
    }

    private void applySelectionAnimation(View view) {
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

        if (withAnimation) {
            transaction.setCustomAnimations(
                    R.anim.fragment_fade_enter,
                    R.anim.fragment_fade_exit,
                    R.anim.fragment_fade_enter,
                    R.anim.fragment_fade_exit
            );
        }

        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_container);

        if (currentFragment instanceof HomeFragment) {
            finish();
        } else {
            super.onBackPressed();
            updateNavigationSelection();
        }
    }

    private void updateNavigationSelection() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);

        if (currentFragment instanceof HomeFragment) {
            bubbleNavigation.setCurrentActiveItem(0);
            toolbar.setTitle("DoctorCubes");
        } else if (currentFragment instanceof StudyMaterialFragment) {
            bubbleNavigation.setCurrentActiveItem(1);
            toolbar.setTitle("Study Material");
        } else if (currentFragment instanceof SettingsFragment) {
            bubbleNavigation.setCurrentActiveItem(2);
            toolbar.setTitle("Settings");
        }
    }
}