package com.android.doctorcube.study.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.doctorcube.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.Arrays;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final List<String> PLAYLIST_VIDEOS = Arrays.asList(
            "VIDEO_ID_1", "VIDEO_ID_2", "VIDEO_ID_3", "VIDEO_ID_4"  // Add actual video IDs from your playlist
    );

    private YouTubePlayerView youtubePlayerView;
    private boolean isFullScreen = false;
    private ImageView fullscreenToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        fullscreenToggle = findViewById(R.id.fullscreenToggle);

        getLifecycle().addObserver(youtubePlayerView); // Lifecycle management

        String videoId = getIntent().getStringExtra("videoId");



        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);
            }
        });

        fullscreenToggle.setOnClickListener(v -> toggleFullScreen());
    }

    private void toggleFullScreen() {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isFullScreen = false;
            fullscreenToggle.setImageResource(R.drawable.ic_home); // Replace with actual fullscreen icon
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isFullScreen = true;
            fullscreenToggle.setImageResource(R.drawable.ic_hostel); // Replace with actual exit fullscreen icon
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            toggleFullScreen(); // Exit fullscreen first
        } else {
            super.onBackPressed();
        }
    }
}
