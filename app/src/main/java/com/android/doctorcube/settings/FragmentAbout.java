package com.android.doctorcube.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar; // Use AppCompat Toolbar
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;  // Import NavHostFragment
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.doctorcube.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentAbout#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentAbout extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private NavController navController;  // Add NavController
    private Toolbar toolbar; // Declare Toolbar

    public FragmentAbout() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAbout.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAbout newInstance(String param1, String param2) {
        FragmentAbout fragment = new FragmentAbout();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Initialize NavController
        navController = NavHostFragment.findNavController(this);

        // Initialize Toolbar and set back button listener
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome(); // Call the navigation method
            }
        });

        // Initialize UI elements and set listeners (Inside onCreateView)
        MaterialButton emailButton = view.findViewById(R.id.btnEmail);
        MaterialButton whatsappButton = view.findViewById(R.id.btnWhatsApp);
        MaterialButton callButton = view.findViewById(R.id.btnCall);

        emailButton.setOnClickListener(v -> openEmailApp());
        whatsappButton.setOnClickListener(v -> openWhatsApp());
        callButton.setOnClickListener(v -> openDialer());

        return view;
    }

    private void openEmailApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@doctorcube.com")); // Replace with your support email
        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    private void openWhatsApp() {
        String phoneNumber = "+1234567890";  // Replace with your WhatsApp number
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void openDialer() {
        String phoneNumber = "+1234567890";  // Replace with your phone number
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Handle back navigation when the fragment is detached
        if (navController != null) {
            navigateToHome();
        }
    }

    private void navigateToHome() {
        if (navController != null) {
            navController.navigate(R.id.action_global_settingsHome); // Use the correct action ID
        }
    }
}

