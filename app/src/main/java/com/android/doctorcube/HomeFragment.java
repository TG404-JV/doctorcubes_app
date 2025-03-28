package com.android.doctorcube;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.android.doctorcube.university.ApplyBottomSheetFragment;
import com.android.doctorcube.university.model.University;
import com.android.doctorcube.university.model.UniversityData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements FeaturesAdapter.OnFeatureClickListener, EventAdapter.OnItemClickListener {

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
    private List<University> fullUniversityList;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsAdapter searchResultsAdapter;
    private List<SearchItem> fullSearchList;

    // Category buttons
    private MaterialCardView studyButton, examButton, universityButton;

    // Upcoming Events "SEE ALL" button
    private TextView seeAllEventsButton;

    // Invite Friends button
    private MaterialButton inviteButton;

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI components
        searchEditText = view.findViewById(R.id.searchEditText);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view);
        studyButton = view.findViewById(R.id.studyButton);
        examButton = view.findViewById(R.id.examButton);
        universityButton = view.findViewById(R.id.universityButton);
        seeAllEventsButton = view.findViewById(R.id.see_all_events);
        inviteButton = view.findViewById(R.id.invite_button);
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

    private void setUpComingEvents() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        eventList = new ArrayList<>();
        eventList.add(new Event(R.drawable.ic_offer_1, "GET", "International Band", "London, UK", "+20 Going"));
        eventList.add(new Event(R.drawable.ic_offer_2, "GET", "Jo Malone", "Radius Gal", "+21 Going"));
        eventList.add(new Event(R.drawable.ic_offer_3, "GET", "Jo Malone", "Radius Gal", "+21 Going"));

        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, Event event) {
        FragmentManager fragmentManager = ((AppCompatActivity) requireContext()).getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putString("event_title", event.getTitle());
        ApplyBottomSheetFragment bottomSheet = new ApplyBottomSheetFragment();
        bottomSheet.setArguments(args);
        bottomSheet.show(fragmentManager, "ApplyBottomSheet");
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
        fullUniversityList = UniversityData.getUniversities();
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

    private void setupCommunicationButtons(View view) {
        CommunicationUtils commUtils = new CommunicationUtils(getActivity());
        commUtils.setupCommunicationButtons(view);
    }

    private void setupSearchBar() {
        fullSearchList = new ArrayList<>();

        // Add Universities
        for (int i = 0; i < fullUniversityList.size(); i++) {
            University uni = fullUniversityList.get(i);
            fullSearchList.add(new SearchItem(
                    uni.getName() + ", " + uni.getCountry(),
                    "University",
                    uni,
                    i
            ));
        }

        // Add Events
        for (int i = 0; i < eventList.size(); i++) {
            Event event = eventList.get(i);
            fullSearchList.add(new SearchItem(
                    event.getTitle() + ", " + event.getLocation(),
                    "Event",
                    event,
                    i
            ));
        }

        // Add Features
        List<Feature> features = FeatureData.getInstance().getFeatures(requireContext());
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            fullSearchList.add(new SearchItem(
                    feature.getTitle(),
                    "Feature",
                    feature,
                    i
            ));
        }

        // Add Testimonials
        List<Testimonial> testimonials = testimonialsAdapter.getTestimonials();
        for (int i = 0; i < testimonials.size(); i++) {
            Testimonial testimonial = testimonials.get(i);
            fullSearchList.add(new SearchItem(
                    testimonial.getName() + " - " + testimonial.getUniversity(),
                    "Testimonial",
                    testimonial,
                    i
            ));
        }

        SearchUtils<SearchItem> searchUtils = new SearchUtils<>(
                getActivity(),
                searchEditText,
                fullSearchList,
                new SearchUtils.SearchCallback<SearchItem>() {
                    @Override
                    public void onSearchResults(List<SearchItem> filteredList) {
                        if (searchResultsAdapter == null) {
                            searchResultsAdapter = new SearchResultsAdapter(filteredList, item -> navigateToSection(item));
                            searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            searchResultsRecyclerView.setAdapter(searchResultsAdapter);
                        } else {
                            searchResultsAdapter.updateData(filteredList);
                        }
                        searchResultsRecyclerView.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public String getSearchText(SearchItem item) {
                        return item.getTitle();
                    }
                }
        );
        searchUtils.setupSearchBar();

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && searchEditText.getText().toString().isEmpty()) {
                searchResultsRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void navigateToSection(SearchItem item) {
        switch (item.getType()) {
            case "University":
                University university = (University) item.getData();
                openUniversitiesActivity(university.getCountry());
                break;
            case "Event":
                recyclerView.smoothScrollToPosition(item.getSectionPosition());
                break;
            case "Feature":
                featuresRecyclerView.smoothScrollToPosition(item.getSectionPosition());
                break;
            case "Testimonial":
                testimonialsViewPager.setCurrentItem(item.getSectionPosition(), true);
                break;
            default:
                Toast.makeText(getContext(), "Unknown item type", Toast.LENGTH_SHORT).show();
                break;
        }

        searchEditText.clearFocus();
        searchResultsRecyclerView.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    private void setupCategoryButtons() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        if (studyButton != null) {
            studyButton.setOnClickListener(v -> {
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
                ((MainActivity) requireActivity()).getToolbar().setTitle("Study");
                ((MainActivity) requireActivity()).getBubbleNavigation().setCurrentActiveItem(1);
            });
        }

        if (examButton != null) {
            examButton.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (universityButton != null) {
            universityButton.setOnClickListener(v -> openUniversitiesActivity("All"));
        }
    }

    private void setupEventListeners() {
        if (seeAllEventsButton != null) {
            seeAllEventsButton.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "See All Events Clicked", Toast.LENGTH_SHORT).show();
            });
        }

        if (inviteButton != null) {
            inviteButton.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "Invite Friends Clicked", Toast.LENGTH_SHORT).show();
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

    // SearchItem class
    public static class SearchItem {
        private String title;
        private String type;
        private Object data;
        private int sectionPosition;

        public SearchItem(String title, String type, Object data, int sectionPosition) {
            this.title = title;
            this.type = type;
            this.data = data;
            this.sectionPosition = sectionPosition;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public Object getData() {
            return data;
        }

        public int getSectionPosition() {
            return sectionPosition;
        }
    }

    class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        private List<SearchItem> searchItems;
        private final OnItemClickListener listener;

        public SearchResultsAdapter(List<SearchItem> searchItems, OnItemClickListener listener) {
            this.searchItems = searchItems;
            this.listener = listener;
        }

        public void updateData(List<SearchItem> newItems) {
            this.searchItems = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SearchItem item = searchItems.get(position);
            holder.textView.setText(item.getTitle());
            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return searchItems.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }

        interface OnItemClickListener {
            void onItemClick(SearchItem item);
        }
    }
}