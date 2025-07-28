package com.example.school.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CourseDTO {
    
    private Long id;
    
    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must be less than 20 characters")
    private String code;
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 12, message = "Credits cannot exceed 12")
    private int credits = 3;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity = 30;
    
    private Set<Long> prerequisiteIds = new HashSet<>();
    
    // Helper method to add a prerequisite
    public void addPrerequisiteId(Long courseId) {
        this.prerequisiteIds.add(courseId);
    }
    
    // Helper method to remove a prerequisite
    public void removePrerequisiteId(Long courseId) {
        this.prerequisiteIds.remove(courseId);
    }
}
