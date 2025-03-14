package com.android.doctorcube.study.fragment;

import android.content.Intent;
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

public class VideosFragment extends Fragment {

    private static final String API_KEY = "AIzaSyB7xC-rgEft4YaAxxoULLDyQo_tmv_gliI"; // Replace with your API key
    private static final String PLAYLIST_ID = "PLtF3QWh0kRCvEwDRt-vEafqdw4CYINdnG"; // Replace with your playlist ID
    private static final String YOUTUBE_API_URL =
            "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=" + PLAYLIST_ID + "&key=" + API_KEY;

    private RecyclerView recyclerView;
    private VideosAdapter adapter;
    private List<VideoItem> videoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        videoList = new ArrayList<>();
        adapter = new VideosAdapter(videoList, requireContext());
        recyclerView.setAdapter(adapter);

        fetchYouTubeVideos();

        return view;
    }

    private void fetchYouTubeVideos() {
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, YOUTUBE_API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray items = response.getJSONArray("items");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                JSONObject snippet = item.getJSONObject("snippet");
                                String title = snippet.getString("title");
                                String videoId = snippet.getJSONObject("resourceId").getString("videoId");
                                String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");

                                videoList.add(new VideoItem(title, videoId, thumbnailUrl));
                            }
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }
}
