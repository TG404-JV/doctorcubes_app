package com.android.doctorcube.study.adapter;

// StudyMaterialPagerAdapter.java

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.android.doctorcube.study.fragment.NotesFragment;
import com.android.doctorcube.study.fragment.VideosFragment;


public class StudyMaterialPagerAdapter extends FragmentStateAdapter {

    public StudyMaterialPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NotesFragment();
            case 1:
                return new VideosFragment();
            default:
                return new NotesFragment(); // Default case
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Notes and Videos
    }

    public Fragment getFragmentAt(int position) {
        return createFragment(position);
    }
}
