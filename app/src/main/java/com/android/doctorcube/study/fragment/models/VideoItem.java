package com.android.doctorcube.study.fragment.models;

public class VideoItem {
    private String title;
    private String duration;
    private String author;
    private String quality;

    public VideoItem(String title, String duration, String author, String quality) {
        this.title = title;
        this.duration = duration;
        this.author = author;
        this.quality = quality;
    }

    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public String getAuthor() { return author; }
    public String getQuality() { return quality; }
}
