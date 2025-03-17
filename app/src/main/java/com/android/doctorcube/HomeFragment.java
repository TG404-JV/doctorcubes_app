package com.android.doctorcube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.doctorcube.home.adapter.FeaturesAdapter;
import com.android.doctorcube.home.adapter.OffersSliderAdapter;
import com.android.doctorcube.home.data.FeatureData;
import com.android.doctorcube.home.model.Feature;
import com.android.doctorcube.home.model.OfferSlide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements FeaturesAdapter.OnFeatureClickListener {

    private ViewPager2 offersViewPager;
    private OffersSliderAdapter offersAdapter;
    private RecyclerView featuresRecyclerView;
    private FeaturesAdapter featuresAdapter;
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;
    private final int AUTO_SLIDE_INTERVAL = 3000; // 3 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize ViewPager for offers
        setupOffersSlider(view);

        // Set up features RecyclerView
        setupFeaturesRecyclerView(view);

        // Set up country selection click listeners
        setupCountrySelectionListeners(view);

        // Set up call and WhatsApp buttons
        setupCommunicationButtons(view);

        return view;
    }

    private void setupFeaturesRecyclerView(View view) {
        featuresRecyclerView = view.findViewById(R.id.features_recycler_view);
        featuresRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get features data from FeatureData class
        List<Feature> features = FeatureData.getInstance().getFeatures(requireContext());

        // Set up adapter
        featuresAdapter = new FeaturesAdapter(features, this);
        featuresRecyclerView.setAdapter(featuresAdapter);
    }

    // Implement the OnFeatureClickListener method
    @Override
    public void onFeatureClick(Feature feature) {
        // Handle feature clicks based on feature ID
        if (feature.getId() == Feature.TYPE_UNIVERSITY_LISTINGS) {
            // Open university listings screen or perform specific action
            Toast.makeText(requireContext(), "University Listings Selected", Toast.LENGTH_SHORT).show();
        } else if (feature.getId() == Feature.TYPE_SCHOLARSHIP) {
            // Open scholarship screen
            Toast.makeText(requireContext(), "Scholarship Selected", Toast.LENGTH_SHORT).show();
        } else if (feature.getId() == Feature.TYPE_VISA_ADMISSION) {
            // Open visa & admission screen
            Toast.makeText(requireContext(), "Visa & Admission Selected", Toast.LENGTH_SHORT).show();
        } else if (feature.getId() == Feature.TYPE_TRACKING) {
            // Open tracking screen
            Toast.makeText(requireContext(), "Application Tracking Selected", Toast.LENGTH_SHORT).show();
        } else if (feature.getId() == Feature.TYPE_SUPPORT) {
            // Open support screen or contact options
            Toast.makeText(requireContext(), "Support Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupOffersSlider(View view) {
        // Your existing code for offers slider setup
        offersViewPager = view.findViewById(R.id.offers_viewpager);

        // Create sample offers data
        List<OfferSlide> offerSlides = new ArrayList<>();
        offerSlides.add(new OfferSlide(R.drawable.flag_china, "Special Discount on Application Fee", "Limited time offer for early applicants"));
        offerSlides.add(new OfferSlide(R.drawable.create_account_gradient_background, "Scholarship Opportunities", "Up to 50% scholarship for merit students"));
        offerSlides.add(new OfferSlide(R.drawable.flag_china, "Free Visa Assistance", "Complete guidance for visa processing"));

        // Set up adapter
        offersAdapter = new OffersSliderAdapter(offerSlides);
        offersViewPager.setAdapter(offersAdapter);



        // Auto-slide functionality
        sliderRunnable = () -> {
            int currentPosition = offersViewPager.getCurrentItem();
            if (currentPosition == offersAdapter.getItemCount() - 1) {
                offersViewPager.setCurrentItem(0);
            } else {
                offersViewPager.setCurrentItem(currentPosition + 1);
            }
            sliderHandler.postDelayed(sliderRunnable, AUTO_SLIDE_INTERVAL);
        };

        // Start auto-sliding
        startAutoSlide();

        // Stop auto-sliding when user interacts with the ViewPager
        offersViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, AUTO_SLIDE_INTERVAL);
            }
        });
    }

    private void setupCountrySelectionListeners(View view) {
        // Your existing code for country selection
        LinearLayout russiaLayout = view.findViewById(R.id.country_russia);
        LinearLayout georgiaLayout = view.findViewById(R.id.country_georgia);
        LinearLayout kazakhstanLayout = view.findViewById(R.id.country_kazakhstan);
        LinearLayout nepalLayout = view.findViewById(R.id.country_nepal);
        LinearLayout chinaLayout = view.findViewById(R.id.country_china);
        LinearLayout uzbekistanLayout = view.findViewById(R.id.country_uzbekistan);

        // Set click listeners for each country
        russiaLayout.setOnClickListener(v -> openUniversitiesActivity("Russia"));
        georgiaLayout.setOnClickListener(v -> openUniversitiesActivity("Georgia"));
        kazakhstanLayout.setOnClickListener(v -> openUniversitiesActivity("Kazakhstan"));
        nepalLayout.setOnClickListener(v -> openUniversitiesActivity("Nepal"));
        chinaLayout.setOnClickListener(v -> openUniversitiesActivity("China"));
        uzbekistanLayout.setOnClickListener(v -> openUniversitiesActivity("Uzbekistan"));
    }

    private void openUniversitiesActivity(String country) {
        Intent intent = new Intent(getActivity(), UniversitiesActivity.class);
        intent.putExtra("COUNTRY_NAME", country);
        startActivity(intent);
    }

    private void setupCommunicationButtons(View view) {
        // Your existing code for communication buttons
        MaterialCardView callButton = view.findViewById(R.id.call_now_button);
        MaterialCardView whatsappButton = view.findViewById(R.id.whatsapp_button);

        // Set up call button
        callButton.setOnClickListener(v -> {
            String phoneNumber = "+1234567890"; // Replace with your actual phone number
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        });

        // Set up WhatsApp button
        whatsappButton.setOnClickListener(v -> {
            String phoneNumber = "919730037126"; // Use without '+' sign
            String message = "Hello, I would like to inquire about studying medicine abroad.";

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message)));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Please install WhatsApp first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startAutoSlide() {
        sliderHandler.postDelayed(sliderRunnable, AUTO_SLIDE_INTERVAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restart auto-slide when the fragment resumes
        startAutoSlide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        if (sliderHandler != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }
}