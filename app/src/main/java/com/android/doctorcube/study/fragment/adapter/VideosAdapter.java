package com.android.doctorcube.study.fragment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.doctorcube.R;
import com.android.doctorcube.study.fragment.models.VideoItem;

import java.util.List;

// VideosAdapter.java
public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    private List<VideoItem> videosList;

    public VideosAdapter(List<VideoItem> videosList) {
        this.videosList = videosList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videosList.get(position);
        holder.titleTextView.setText(video.getTitle());
        holder.durationTextView.setText(video.getDuration());
        holder.authorTextView.setText("by " + video.getAuthor());
        holder.qualityTextView.setText(video.getQuality());

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            // Handle video item click
            // You might want to play the video or show details
        });

        holder.playButton.setOnClickListener(v -> {
            // Handle play action
        });
    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    public void updateData(List<VideoItem> newData) {
        this.videosList = newData;
        notifyDataSetChanged();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView durationTextView;
        TextView authorTextView;
        TextView qualityTextView;
        ImageView playButton;
        ImageView thumbnailImageView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            qualityTextView = itemView.findViewById(R.id.qualityTextView);
            playButton = itemView.findViewById(R.id.playButton);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
        }
    }
}
