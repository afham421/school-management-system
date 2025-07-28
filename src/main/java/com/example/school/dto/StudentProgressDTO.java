package com.example.school.dto;

import java.util.List;

public class StudentProgressDTO {
    private String studentName;
    private double gpa;
    private List<CourseGradeDTO> courses;

    public StudentProgressDTO(String studentName, double gpa, List<CourseGradeDTO> courses) {
        this.studentName = studentName;
        this.gpa = gpa;
        this.courses = courses;
    }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public List<CourseGradeDTO> getCourses() { return courses; }
    public void setCourses(List<CourseGradeDTO> courses) { this.courses = courses; }
}
