package com.example.school.dto;

import com.example.school.entity.Enrollment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentDTO {
    
    private Long id;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private LocalDate enrollmentDate;
    
    private Enrollment.EnrollmentStatus status;
    
    // Helper method to get status as string
    public String getStatusAsString() {
        return status != null ? status.name() : null;
    }
}
