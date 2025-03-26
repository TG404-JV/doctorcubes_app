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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.doctorcube.communication.CommunicationUtils;
import com.android.doctorcube.communication.SearchUtils;
import com.android.doctorcube.home.adapter.EventAdapter;
import com.android.doctorcube.home.adapter.FeaturesAdapter;
import com.android.doctorcube.home.adapter.TestimonialsSliderAdapter;
import com.android.doctorcube.home.adapter.UniversityListAdapter;
import com.android.doctorcube.home.data.FeatureData;
import com.android.doctorcube.home.data.Testimonial;
import com.android.doctorcube.home.model.Event;
import com.android.doctorcube.home.model.Feature;
import com.android.doctorcube.university.model.University;
import com.android.doctorcube.university.model.UniversityData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements FeaturesAdapter.OnFeatureClickListener , EventAdapter.OnItemClickListener {

    private RecyclerView featuresRecyclerView;
    private FeaturesAdapter featuresAdapter;
    private RecyclerView universitiesRecyclerView;
    private UniversityListAdapter universityListAdapter;
    private ViewPager2 testimonialsViewPager;
    private TestimonialsSliderAdapter testimonialsAdapter;
    private Handler sliderHandler = new Handler();
    private Runnable testimonialsSliderRunnable;
    private final int AUTO_SLIDE_INTERVAL = 3000;

    // Search bar components
    private EditText searchEditText;
    private List<University> fullUniversityList; // Store the full list for filtering

    // Category buttons
    private MaterialCardView studyButton, examButton, universityButton;

    // Upcoming Events "SEE ALL" button
    private TextView seeAllEventsButton;

    // Invite Friends button
    private MaterialButton inviteButton;


    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList; // Expects List<Event>, not List<EventAdapter>
    // Bottom Navigation

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        searchEditText = view.findViewById(R.id.searchEditText);
        studyButton = view.findViewById(R.id.studyButton); // Add IDs to MaterialCardView in XML
        examButton = view.findViewById(R.id.examButton);
        universityButton = view.findViewById(R.id.universityButton);
        seeAllEventsButton = view.findViewById(R.id.see_all_events); // Add ID to "SEE ALL" TextView in XML
        inviteButton = view.findViewById(R.id.invite_button); // Add ID to "INVITE" Button in XML
        recyclerView = view.findViewById(R.id.recyclerView);


        setupToolbar();
        setUpComingEvents();
        setupFeaturesRecyclerView(view);
        setupUniversitiesRecyclerView(view);
        setupTestimonialsSlider(view);
        setupCountrySelectionListeners(view);
        setupCommunicationButtons(view);
        setupSearchBar();
        setupCategoryButtons();
        setupEventListeners();

        return view;
    }


    private void setUpComingEvents()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Prepare data
        eventList = new ArrayList<>();
        eventList.add(new Event(R.drawable.ic_offer_1, "10\nJUNE", "International Band", "London, UK", "+20 Going"));
        eventList.add(new Event(R.drawable.ic_offer_2, "10\nJUNE", "Jo Malone", "Radius Gal", "+21 Going"));
        eventList.add(new Event(R.drawable.ic_offer_3, "10\nJUNE", "Jo Malone", "Radius Gal", "+21 Going"));

        // Set adapter
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onItemClick(int position, Event event) {
        // Handle the click event here
        String message = "Clicked on: " + event.getTitle() + " at position " + position;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        // You can add more actions here, like starting a new activity:
        // Intent intent = new Intent(this, DetailActivity.class);
        // intent.putExtra("event_title", event.getTitle());
        // startActivity(intent);
    }

    private void setupFeaturesRecyclerView(View view) {
        featuresRecyclerView = view.findViewById(R.id.features_recycler_view);
        if (featuresRecyclerView == null) {
            Toast.makeText(getActivity(), "Features RecyclerView not found", Toast.LENGTH_SHORT).show();
            return;
        }

        featuresRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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
            universityListAdapter = new UniversityListAdapter(getContext(), new ArrayList<>(fullUniversityList));
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
        CardView russiaLayout = view.findViewById(R.id.country_russia_card);
        CardView georgiaLayout = view.findViewById(R.id.country_georgia_card);
        CardView kazakhstanLayout = view.findViewById(R.id.country_kazakhstan_card);
        CardView nepalLayout = view.findViewById(R.id.country_nepal_card);
        CardView chinaLayout = view.findViewById(R.id.country_china_card);
        CardView uzbekistanLayout = view.findViewById(R.id.country_uzbekistan_card);

        if (russiaLayout != null) russiaLayout.setOnClickListener(v -> openUniversitiesActivity("Russia"));
        if (georgiaLayout != null) georgiaLayout.setOnClickListener(v -> openUniversitiesActivity("Georgia"));
        if (kazakhstanLayout != null) kazakhstanLayout.setOnClickListener(v -> openUniversitiesActivity("Kazakhstan"));
        if (nepalLayout != null) nepalLayout.setOnClickListener(v -> openUniversitiesActivity("Nepal"));
        if (chinaLayout != null) chinaLayout.setOnClickListener(v -> openUniversitiesActivity("China"));
        if (uzbekistanLayout != null) uzbekistanLayout.setOnClickListener(v -> openUniversitiesActivity("Uzbekistan"));
    }

    // In HomeFragment.java
    private void setupCommunicationButtons(View view) {
        CommunicationUtils commUtils = new CommunicationUtils(getActivity());
        commUtils.setupCommunicationButtons(view);
    }
    // In HomeFragment.java
    private void setupSearchBar() {
        SearchUtils<University> searchUtils = new SearchUtils<>(
                getActivity(),
                searchEditText,
                fullUniversityList,
                new SearchUtils.SearchCallback<University>() {
                    @Override
                    public void onSearchResults(List<University> filteredList) {
                        if (universityListAdapter != null) {
                            universityListAdapter.updateData(filteredList);
                        }
                    }

                    @Override
                    public String getSearchText(University university) {
                        // Define what to search in University objects
                        return university.getName() + " " + university.getCountry();
                    }
                }
        );
        searchUtils.setupSearchBar();
    }

    private void setupCategoryButtons() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        if (studyButton != null) {
            studyButton.setOnClickListener(v -> {

                // Create and load StudyFragment
                StudyMaterialFragment studyFragment = new StudyMaterialFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, studyFragment)
                        .setCustomAnimations(
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit,
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit
                        )
                        .addToBackStack(null)
                        .commit();

                // Update toolbar title
                ((MainActivity) requireActivity()).getToolbar().setTitle("Study");
                // Update bubble navigation to deselect all (optional, since this is a sub-section)
                ((MainActivity) requireActivity()).getBubbleNavigation().setCurrentActiveItem(1);
            });
        }

        if (examButton != null) {
            examButton.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "Comming Soon", Toast.LENGTH_SHORT).show();

                /*// Create and load ExamFragment
                ExamFragment examFragment = new ExamFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, examFragment)
                        .setCustomAnimations(
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit,
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit
                        )
                        .addToBackStack(null)
                        .commit();

                // Update toolbar title
                ((MainActivity) requireActivity()).getToolbar().setTitle("Exams");
                ((MainActivity) requireActivity()).getBubbleNavigation().setCurrentActiveItem(-1);*/
            });
        }

        if (universityButton != null) {
            universityButton.setOnClickListener(v -> {

              /*  // Create and load UniversityFragment
                UniversityFragment universityFragment = new UniversityFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, universityFragment)
                        .setCustomAnimations(
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit,
                                R.anim.fragment_fade_enter,
                                R.anim.fragment_fade_exit
                        )
                        .addToBackStack(null)
                        .commit();

                // Update toolbar title
                ((MainActivity) requireActivity()).getToolbar().setTitle("Universities");
                ((MainActivity) requireActivity()).getBubbleNavigation().setCurrentActiveItem(-1);*/

                openUniversitiesActivity("All");

            });
        }
    }    private void setupEventListeners() {
        if (seeAllEventsButton != null) {
            seeAllEventsButton.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "See All Events Clicked", Toast.LENGTH_SHORT).show();
                // Navigate to an EventsActivity or Fragment
              /*  Intent intent = new Intent(getActivity(), EventsActivity.class);
                startActivity(intent);*/
            });
        }

        if (inviteButton != null) {
            inviteButton.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "Invite Friends Clicked", Toast.LENGTH_SHORT).show();
                // Implement invite functionality (e.g., share intent)
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Join me on DoctorCube to explore study abroad opportunities! Get $20 for a ticket: [Your Referral Link]");
                startActivity(Intent.createChooser(shareIntent, "Invite Friends"));
            });
        }
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

    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            if (toolbar != null) {
                TextView appTitle = toolbar.findViewById(R.id.app_title);
                if (appTitle != null) {
                    appTitle.setText("Home");
                } else {
                    Toast.makeText(getContext(), "app_title TextView not found in Toolbar", Toast.LENGTH_SHORT).show();
                }
                if (activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().setSubtitle("Premium Study Materials for Abroad Education");
                }
            } else {
                Toast.makeText(getContext(), "MainActivity Toolbar not set", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Activity is not AppCompatActivity", Toast.LENGTH_SHORT).show();
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
        startTestimonialsAutoSlide();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (sliderHandler != null) {
            sliderHandler.removeCallbacks(testimonialsSliderRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sliderHandler != null) {
            sliderHandler.removeCallbacks(testimonialsSliderRunnable);
            sliderHandler = null;
        }
    }
}