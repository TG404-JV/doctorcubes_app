package com.android.doctorcube.study.fragment.models;

public class NoteItem {
    private String title;
    private String url; // Changed from notesUrl for consistency

    public NoteItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}