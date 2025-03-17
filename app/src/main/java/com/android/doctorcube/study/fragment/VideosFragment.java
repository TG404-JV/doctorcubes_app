package com.android.doctorcube.study.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment implements StudyMaterialFragment.SearchableFragment {

    private static final String API_KEY = "AIzaSyB7xC-rgEft4YaAxxoULLDyQo_tmv_gliI"; // Replace with your API key
    private static final String PLAYLIST_ID = "PLtF3QWh0kRCvEwDRt-vEafqdw4CYINdnG"; // Replace with your playlist ID
    private static final String YOUTUBE_API_URL =
            "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=" + PLAYLIST_ID + "&key=" + API_KEY;

    private RecyclerView recyclerView;
    private VideosAdapter adapter;
    private List<VideoItem> videoList;
    private List<VideoItem> originalVideoList;
    private TextView emptyView;
    private boolean isDataLoaded = false;
    private RequestQueue requestQueue;
    private String pendingSearchQuery = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Use getContext() safely

        videoList = new ArrayList<>();
        originalVideoList = new ArrayList<>();
        adapter = new VideosAdapter(videoList, getContext()); // Use getContext() safely
        recyclerView.setAdapter(adapter);

        // Initialize RequestQueue with Application Context
        requestQueue = Volley.newRequestQueue(requireActivity().getApplicationContext());
        fetchYouTubeVideos();

        return view;
    }

    private void fetchYouTubeVideos() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, YOUTUBE_API_URL, null,
                response -> {
                    if (!isAdded() || getContext() == null) return; // Exit if Fragment is detached
                    try {
                        JSONArray items = response.getJSONArray("items");
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject snippet = item.getJSONObject("snippet");
                            String title = snippet.getString("title");
                            String description = snippet.has("description") ?
                                    snippet.getString("description") : "";
                            String videoId = snippet.getJSONObject("resourceId").getString("videoId");
                            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");

                            VideoItem videoItem = new VideoItem(title, videoId, thumbnailUrl);
                            videoItem.setDescription(description);

                            videoList.add(videoItem);
                            originalVideoList.add(videoItem);
                        }
                        adapter.notifyDataSetChanged();
                        isDataLoaded = true;

                        // Apply pending search if exists
                        if (pendingSearchQuery != null && !pendingSearchQuery.isEmpty()) {
                            performSearch(pendingSearchQuery);
                            pendingSearchQuery = null;
                        }

                    } catch (Exception e) {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    if (!isAdded() || getContext() == null) return; // Exit if Fragment is detached
                    Toast.makeText(getContext(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();

                    if (emptyView != null) {
                        emptyView.setText("Failed to load videos. Please check your internet connection.");
                        emptyView.setVisibility(View.VISIBLE);
                    }
                });

        jsonObjectRequest.setTag("VideosRequest"); // Tag for cancellation
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void performSearch(String query) {
        if (!isAdded() || getContext() == null) return; // Exit if detached

        // Store query if data isn’t loaded
        if (!isDataLoaded) {
            pendingSearchQuery = query;
            return;
        }

        List<VideoItem> filteredList = new ArrayList<>();
        for (VideoItem video : originalVideoList) {
            if (video.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    (video.getDescription() != null &&
                            video.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(video);
            }
        }
        updateSearchResults(filteredList, query);
    }

    @Override
    public void resetSearch() {
        if (!isAdded() || getContext() == null) return; // Exit if detached

        pendingSearchQuery = null;
        if (isDataLoaded) {
            videoList.clear();
            videoList.addAll(originalVideoList);
            adapter.notifyDataSetChanged();
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void updateSearchResults(List<VideoItem> filteredList, String query) {
        if (!isAdded() || getContext() == null) return; // Exit if detached

        videoList.clear();
        videoList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            if (emptyView != null) {
                emptyView.setText("No videos found for: " + query);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "No videos found for: " + query, Toast.LENGTH_SHORT).show();
            }
        } else if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll("VideosRequest"); // Cancel tagged requests
        }
    }
}