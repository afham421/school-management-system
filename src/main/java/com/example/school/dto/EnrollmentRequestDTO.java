package com.example.school.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class EnrollmentRequestDTO {
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "At least one course ID is required")
    private Set<Long> courseIds = new HashSet<>();
    
    // Helper method to add a course ID
    public void addCourseId(Long courseId) {
        this.courseIds.add(courseId);
    }
    
    // Helper method to remove a course ID
    public void removeCourseId(Long courseId) {
        this.courseIds.remove(courseId);
    }
}
