package com.android.doctorcube.university.model;

public class University {
    private String id;
    private String name;
    private String location;
    private String country;
    private String courseName;
    private String degreeType;
    private String duration;
    private String grade;
    private String intake;
    private int imageResourceId;

    public University(String id, String name, String location, String country,
                      String courseName, String degreeType, String duration,
                      String grade, String intake, int imageResourceId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.country = country;
        this.courseName = courseName;
        this.degreeType = degreeType;
        this.duration = duration;
        this.grade = grade;
        this.intake = intake;
        this.imageResourceId = imageResourceId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getCountry() { return country; }
    public String getCourseName() { return courseName; }
    public String getDegreeType() { return degreeType; }
    public String getDuration() { return duration; }
    public String getGrade() { return grade; }
    public String getIntake() { return intake; }
    public int getImageResourceId() { return imageResourceId; }
}
