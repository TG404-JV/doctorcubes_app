package com.android.doctorcube.study.fragment.models;

// Model classes

// NoteItem.java
public class NoteItem {
    private String title;
    private String description;
    private String author;
    private String size;
    private String pdfUrl;

    public NoteItem(String title, String description, String author, String size, String pdfUrl) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.size = size;
        this.pdfUrl = pdfUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getSize() { return size; }
    public String getPdfUrl() { return pdfUrl; }
}
