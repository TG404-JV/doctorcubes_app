package com.android.doctorcube.adminpannel;

public class Student {
    private String id, name, mobile, email, state, city, interestedCountry, hasNeetScore, neetScore, hasPassport, submissionDate, callStatus, lastCallDate;
    private boolean isInterested, isAdmitted;

    // Default constructor required for Firebase
    public Student() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getInterestedCountry() { return interestedCountry; }
    public void setInterestedCountry(String interestedCountry) { this.interestedCountry = interestedCountry; }
    public String getHasNeetScore() { return hasNeetScore; }
    public void setHasNeetScore(String hasNeetScore) { this.hasNeetScore = hasNeetScore; }
    public String getNeetScore() { return neetScore; }
    public void setNeetScore(String neetScore) { this.neetScore = neetScore; }
    public String getHasPassport() { return hasPassport; }
    public void setHasPassport(String hasPassport) { this.hasPassport = hasPassport; }
    public String getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(String submissionDate) { this.submissionDate = submissionDate; }
    public String getCallStatus() { return callStatus; }
    public void setCallStatus(String callStatus) { this.callStatus = callStatus; }
    public boolean getIsInterested() { return isInterested; }
    public void setIsInterested(boolean isInterested) { this.isInterested = isInterested; }
    public String getLastCallDate() { return lastCallDate; }
    public void setLastCallDate(String lastCallDate) { this.lastCallDate = lastCallDate; }
    public boolean isAdmitted() { return isAdmitted; }
    public void setAdmitted(boolean isAdmitted) { this.isAdmitted = isAdmitted; }
}