package com.android.doctorcube.study.fragment.models;

public class VideoItem {
    private String title;
    private String videoId;
    private String thumbnailUrl;
    public VideoItem(){

    }

    public VideoItem(String title, String videoId, String thumbnailUrl) {
        this.title = title;
        this.videoId = videoId;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
