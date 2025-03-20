package com.android.doctorcube.study.fragment.models;

public class NoteItem {
    private String title;
    private String description;
    private String author;
    private String size;
    private String pdfUrl;
    private String category;
    private String lastModified;
    private int totalPages;

    public NoteItem(String title, String description, String author, String size, String pdfUrl) {
        this(title, description, author, size, pdfUrl, "Unknown", "Unknown", 0);
    }

    public NoteItem(String title, String description, String author, String size, String pdfUrl,
                    String category, String lastModified, int totalPages) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.size = size;
        this.pdfUrl = pdfUrl;
        this.category = category;
        this.lastModified = lastModified;
        this.totalPages = totalPages;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getSize() { return size; }
    public String getPdfUrl() { return pdfUrl; }
    public String getCategory() { return category; }
    public String getLastModified() { return lastModified; }
    public int getTotalPages() { return totalPages; }
}