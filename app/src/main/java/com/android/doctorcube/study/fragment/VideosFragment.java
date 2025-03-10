package com.android.doctorcube.study.fragment;

// VideosFragment.java

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.R;
import com.android.doctorcube.StudyMaterialFragment;
import com.android.doctorcube.study.fragment.adapter.VideosAdapter;
import com.android.doctorcube.study.fragment.models.VideoItem;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment implements StudyMaterialFragment.SearchableFragment {

    private RecyclerView recyclerView;
    private VideosAdapter adapter;
    private List<VideoItem> videosList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize data
        videosList = getVideosList();

        // Set up adapter
        adapter = new VideosAdapter(videosList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<VideoItem> getVideosList() {
        // This should be replaced with your actual data source
        List<VideoItem> videos = new ArrayList<>();
        videos.add(new VideoItem("Introduction to Algorithms", "45:30", "Prof. Davis", "HD"));
        videos.add(new VideoItem("Database Management Systems", "32:15", "Dr. Thompson", "HD"));
        videos.add(new VideoItem("Computer Networks", "55:20", "Prof. Anderson", "HD"));
        // Add more items as needed
        return videos;
    }

    @Override
    public void performSearch(String query) {
        List<VideoItem> filteredList = new ArrayList<>();
        for (VideoItem video : videosList) {
            if (video.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    video.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(video);
            }
        }
        adapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No videos found for: " + query, Toast.LENGTH_SHORT).show();
        }
    }
}