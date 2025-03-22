package com.android.doctorcube.authentication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.android.doctorcube.R;
import com.google.android.material.button.MaterialButton;

public class FragmentAskUser extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ask_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton loginButton = view.findViewById(R.id.loginButton);
        MaterialButton createAccountButton = view.findViewById(R.id.createAccountButton);

        loginButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.loginFragment2)
        );

        createAccountButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.createAccountFragment2)
        );
    }
}