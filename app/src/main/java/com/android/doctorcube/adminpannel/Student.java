package com.android.doctorcube.adminpannel;

public class Student {
    private String id;
    private String name;
    private String state;
    private String city;
    private String mobile;
    private String email;
    private String interestedCountry;
    private String hasNeetScore;
    private String hasPassport;
    private String neetScore;
    private String submissionDate;
    private String callStatus; // "pending", "called", "not_interested"
    private String lastCallDate; // Timestamp of the last call
    private Boolean isInterested; // Use Boolean to handle null values
    private String notes; // Admin notes about the student

    // Default constructor (required for Firebase)
    public Student() {
        this.callStatus = "pending"; // Default value if not set in Firebase
        this.isInterested = false; // Default value if null in Firebase
        this.notes = ""; // Avoid null pointer issues
    }

    // Constructor with all fields
    public Student(String id, String name, String state, String city, String mobile, String email,
                   String interestedCountry, String hasNeetScore, String hasPassport, String neetScore,
                   String submissionDate, String callStatus, String lastCallDate, Boolean isInterested, String notes) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.city = city;
        this.mobile = mobile;
        this.email = email;
        this.interestedCountry = interestedCountry;
        this.hasNeetScore = hasNeetScore;
        this.hasPassport = hasPassport;
        this.neetScore = neetScore;
        this.submissionDate = submissionDate;
        this.callStatus = (callStatus != null) ? callStatus : "pending"; // Default to "pending"
        this.lastCallDate = lastCallDate;
        this.isInterested = (isInterested != null) ? isInterested : false; // Default to false
        this.notes = (notes != null) ? notes : ""; // Default to empty string
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return (name != null) ? name : "N/A";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return (state != null) ? state : "N/A";
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return (city != null) ? city : "N/A";
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMobile() {
        return (mobile != null) ? mobile : "N/A";
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return (email != null) ? email : "N/A";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInterestedCountry() {
        return (interestedCountry != null) ? interestedCountry : "N/A";
    }

    public void setInterestedCountry(String interestedCountry) {
        this.interestedCountry = interestedCountry;
    }

    public String getHasNeetScore() {
        return (hasNeetScore != null) ? hasNeetScore : "N/A";
    }

    public void setHasNeetScore(String hasNeetScore) {
        this.hasNeetScore = hasNeetScore;
    }

    public String getHasPassport() {
        return (hasPassport != null) ? hasPassport : "N/A";
    }

    public void setHasPassport(String hasPassport) {
        this.hasPassport = hasPassport;
    }

    public String getNeetScore() {
        return (neetScore != null) ? neetScore : "N/A";
    }

    public void setNeetScore(String neetScore) {
        this.neetScore = neetScore;
    }

    public String getSubmissionDate() {
        return (submissionDate != null) ? submissionDate : "N/A";
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getCallStatus() {
        return (callStatus != null) ? callStatus : "pending";
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getLastCallDate() {
        return (lastCallDate != null) ? lastCallDate : "N/A";
    }

    public void setLastCallDate(String lastCallDate) {
        this.lastCallDate = lastCallDate;
    }

    public Boolean getIsInterested() {
        return (isInterested != null) ? isInterested : false;
    }

    public void setIsInterested(Boolean isInterested) {
        this.isInterested = isInterested;
    }

    public String getNotes() {
        return (notes != null) ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
