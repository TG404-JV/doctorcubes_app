package com.android.doctorcube;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.android.doctorcube.home.adapter.TestimonialsSliderAdapter;
import com.android.doctorcube.home.adapter.UniversityListAdapter;
import com.android.doctorcube.home.data.FeatureData;
import com.android.doctorcube.home.data.Testimonial;
import com.android.doctorcube.home.model.Feature;
import com.android.doctorcube.home.model.OfferSlide;
import com.android.doctorcube.university.model.University;
import com.android.doctorcube.university.model.UniversityData;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements FeaturesAdapter.OnFeatureClickListener {

    private ViewPager2 offersViewPager;
    private OffersSliderAdapter offersAdapter;
    private RecyclerView featuresRecyclerView;
    private FeaturesAdapter featuresAdapter;
    private RecyclerView universitiesRecyclerView;
    private UniversityListAdapter universityListAdapter;
    private ViewPager2 testimonialsViewPager;
    private TestimonialsSliderAdapter testimonialsAdapter;
    private Handler sliderHandler = new Handler();
    private Runnable offersSliderRunnable;
    private Runnable testimonialsSliderRunnable;
    private final int AUTO_SLIDE_INTERVAL = 3000;

    // Search bar components
    private EditText searchEditText;
    private ImageView filterIcon;
    private List<University> fullUniversityList; // Store the full list for filtering

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize search bar components
        searchEditText = view.findViewById(R.id.search_edit_text); // Add an ID to EditText in XML
        filterIcon = view.findViewById(R.id.filter_icon); // Add an ID to filter ImageView in XML

        setupOffersSlider(view);
        setupFeaturesRecyclerView(view);
        setupUniversitiesRecyclerView(view);
        setupTestimonialsSlider(view);
        setupCountrySelectionListeners(view);
        setupCommunicationButtons(view);
        setupSearchBar();

        return view;
    }

    private void setupOffersSlider(View view) {
        offersViewPager = view.findViewById(R.id.offers_viewpager);
        if (offersViewPager == null) {
            Toast.makeText(getActivity(), "Offers ViewPager not found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OfferSlide> offerSlides = new ArrayList<>();
        offerSlides.add(new OfferSlide(R.drawable.flag_china, "Special Discount on Application Fee", "Limited time offer for early applicants"));
        offerSlides.add(new OfferSlide(R.drawable.create_account_gradient_background, "Scholarship Opportunities", "Up to 50% scholarship for merit students"));
        offerSlides.add(new OfferSlide(R.drawable.flag_russia, "Free Visa Assistance", "Complete guidance for visa processing"));

        offersAdapter = new OffersSliderAdapter(offerSlides);
        offersViewPager.setAdapter(offersAdapter);

        offersSliderRunnable = () -> {
            int currentPosition = offersViewPager.getCurrentItem();
            if (currentPosition == offersAdapter.getItemCount() - 1) {
                offersViewPager.setCurrentItem(0);
            } else {
                offersViewPager.setCurrentItem(currentPosition + 1);
            }
            sliderHandler.postDelayed(offersSliderRunnable, AUTO_SLIDE_INTERVAL);
        };
        startOffersAutoSlide();

        offersViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(offersSliderRunnable);
                sliderHandler.postDelayed(offersSliderRunnable, AUTO_SLIDE_INTERVAL);
            }
        });
    }

    private void setupFeaturesRecyclerView(View view) {
        featuresRecyclerView = view.findViewById(R.id.features_recycler_view);
        if (featuresRecyclerView == null) {
            Toast.makeText(getActivity(), "Features RecyclerView not found", Toast.LENGTH_SHORT).show();
            return;
        }

        featuresRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<Feature> features = FeatureData.getInstance().getFeatures(requireContext());
        featuresAdapter = new FeaturesAdapter(features, this);
        featuresRecyclerView.setAdapter(featuresAdapter);
    }

    private void setupUniversitiesRecyclerView(View view) {
        universitiesRecyclerView = view.findViewById(R.id.universities_recycler_view);
        if (universitiesRecyclerView == null) {
            Toast.makeText(getActivity(), "Universities RecyclerView not found", Toast.LENGTH_SHORT).show();
            return;
        }

        universitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        fullUniversityList = UniversityData.getUniversities(); // Store full list
        if (getContext() != null) {
            universityListAdapter = new UniversityListAdapter(getContext(), new ArrayList<>(fullUniversityList)); // Pass a copy
            universitiesRecyclerView.setAdapter(universityListAdapter);
        } else {
            Toast.makeText(getActivity(), "Context unavailable for universities", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTestimonialsSlider(View view) {
        testimonialsViewPager = view.findViewById(R.id.testimonials_viewpager);
        if (testimonialsViewPager == null) {
            Toast.makeText(getActivity(), "Testimonials ViewPager not found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Testimonial> testimonials = new ArrayList<>();
        testimonials.add(new Testimonial(R.drawable.ic_profile, "John Doe", "Studying at Sechenov University was a life-changing experience!", String.valueOf(R.drawable.flag_russia), "Sechenov University", "Batch 2023", 5.0f));
        testimonials.add(new Testimonial(R.drawable.ic_profile, "Priya Sharma", "Thanks to DoctorCubes, I got a scholarship in China.", String.valueOf(R.drawable.flag_china), "Peking University", "Batch 2024", 4.5f));
        testimonials.add(new Testimonial(R.drawable.ic_profile, "Alex Ivanov", "The visa process was smooth and hassle-free.", String.valueOf(R.drawable.flag_georgia), "Tbilisi State Medical University", "Batch 2022", 4.8f));

        testimonialsAdapter = new TestimonialsSliderAdapter(testimonials);
        testimonialsViewPager.setAdapter(testimonialsAdapter);

        testimonialsSliderRunnable = () -> {
            int currentPosition = testimonialsViewPager.getCurrentItem();
            if (currentPosition == testimonialsAdapter.getItemCount() - 1) {
                testimonialsViewPager.setCurrentItem(0);
            } else {
                testimonialsViewPager.setCurrentItem(currentPosition + 1);
            }
            sliderHandler.postDelayed(testimonialsSliderRunnable, AUTO_SLIDE_INTERVAL);
        };
        startTestimonialsAutoSlide();

        testimonialsViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(testimonialsSliderRunnable);
                sliderHandler.postDelayed(testimonialsSliderRunnable, AUTO_SLIDE_INTERVAL);
            }
        });
    }

    private void setupCountrySelectionListeners(View view) {
        LinearLayout russiaLayout = view.findViewById(R.id.country_russia);
        LinearLayout georgiaLayout = view.findViewById(R.id.country_georgia);
        LinearLayout kazakhstanLayout = view.findViewById(R.id.country_kazakhstan);
        LinearLayout nepalLayout = view.findViewById(R.id.country_nepal);
        LinearLayout chinaLayout = view.findViewById(R.id.country_china);
        LinearLayout uzbekistanLayout = view.findViewById(R.id.country_uzbekistan);

        if (russiaLayout != null) russiaLayout.setOnClickListener(v -> openUniversitiesActivity("Russia"));
        if (georgiaLayout != null) georgiaLayout.setOnClickListener(v -> openUniversitiesActivity("Georgia"));
        if (kazakhstanLayout != null) kazakhstanLayout.setOnClickListener(v -> openUniversitiesActivity("Kazakhstan"));
        if (nepalLayout != null) nepalLayout.setOnClickListener(v -> openUniversitiesActivity("Nepal"));
        if (chinaLayout != null) chinaLayout.setOnClickListener(v -> openUniversitiesActivity("China"));
        if (uzbekistanLayout != null) uzbekistanLayout.setOnClickListener(v -> openUniversitiesActivity("Uzbekistan"));
    }

    private void setupCommunicationButtons(View view) {
        MaterialCardView callButton = view.findViewById(R.id.call_now_button);
        MaterialCardView whatsappButton = view.findViewById(R.id.whatsapp_button);

        if (callButton != null) {
            callButton.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:+1234567890"));
                startActivity(callIntent);
            });
        }

        if (whatsappButton != null) {
            whatsappButton.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=919730037126&text=" + Uri.encode("Hello, I would like to inquire about studying medicine abroad.")));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Please install WhatsApp first", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupSearchBar() {
        if (searchEditText == null || filterIcon == null) {
            Toast.makeText(getActivity(), "Search bar components not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TextWatcher for dynamic search
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUniversities(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Filter icon click (placeholder for future filter options)
        filterIcon.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Filter options coming soon!", Toast.LENGTH_SHORT).show();
            // Future: Open a dialog or fragment for advanced filtering
        });
    }

    private void filterUniversities(String query) {
        if (fullUniversityList == null || universityListAdapter == null) return;

        List<University> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (University university : fullUniversityList) {
            // Assuming University has getName() and getCountry() methods
            if (university.getName().toLowerCase().contains(lowerCaseQuery) ||
                    university.getCountry().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(university);
            }
        }

        universityListAdapter.updateData(filteredList); // Update adapter with filtered list
    }

    private void openUniversitiesActivity(String country) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), UniversitiesActivity.class);
            intent.putExtra("COUNTRY_NAME", country);
            startActivity(intent);
        }
    }

    @Override
    public void onFeatureClick(Feature feature) {
        if (getActivity() == null) return;

        switch (feature.getId()) {
            case Feature.TYPE_UNIVERSITY_LISTINGS:
                Toast.makeText(getActivity(), "University Listings Selected", Toast.LENGTH_SHORT).show();
                break;
            case Feature.TYPE_SCHOLARSHIP:
                Toast.makeText(getActivity(), "Scholarship Selected", Toast.LENGTH_SHORT).show();
                break;
            case Feature.TYPE_VISA_ADMISSION:
                Toast.makeText(getActivity(), "Visa & Admission Selected", Toast.LENGTH_SHORT).show();
                break;
            case Feature.TYPE_TRACKING:
                Toast.makeText(getActivity(), "Application Tracking Selected", Toast.LENGTH_SHORT).show();
                break;
            case Feature.TYPE_SUPPORT:
                Toast.makeText(getActivity(), "Support Selected", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startOffersAutoSlide() {
        if (offersSliderRunnable != null && sliderHandler != null) {
            sliderHandler.postDelayed(offersSliderRunnable, AUTO_SLIDE_INTERVAL);
        }
    }

    private void startTestimonialsAutoSlide() {
        if (testimonialsSliderRunnable != null && sliderHandler != null) {
            sliderHandler.postDelayed(testimonialsSliderRunnable, AUTO_SLIDE_INTERVAL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startOffersAutoSlide();
        startTestimonialsAutoSlide();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sliderHandler != null) {
            sliderHandler.removeCallbacks(offersSliderRunnable);
            sliderHandler.removeCallbacks(testimonialsSliderRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null) {
            sliderHandler.removeCallbacks(offersSliderRunnable);
            sliderHandler.removeCallbacks(testimonialsSliderRunnable);
            sliderHandler = null;
        }
    }
}