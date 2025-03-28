package com.android.doctorcube.study.fragment.models;

public class NoteItem {
    private String title, category, detail, timestamp ,description;
    private String url; // Changed from notesUrl for consistency

    public NoteItem(String title, String url, String category, String detail, String timestamp,String description) {
        this.title = title;
        this.url = url;
        this.category = category;
        this.detail = detail;
        this.timestamp = timestamp;
        this.description=description;


    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
    public String getCategory() {
        return category;
    }
    public String getDetail() {
        return detail;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}