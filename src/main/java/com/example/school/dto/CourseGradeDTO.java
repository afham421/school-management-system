package com.example.school.dto;

public class CourseGradeDTO {
    private String courseTitle;
    private String grade;
    private boolean completed;

    public CourseGradeDTO(String courseTitle, String grade, boolean completed) {
        this.courseTitle = courseTitle;
        this.grade = grade;
        this.completed = completed;
    }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
