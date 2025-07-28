package com.example.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GradeRequestDTO {
    
    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;
    
    @NotBlank(message = "Grade value is required")
    @Pattern(regexp = "^[A-D][+-]?|F|P|NP|I|W$", 
             message = "Grade must be A, A-, B+, B, B-, C+, C, C-, D+, D, F, P, NP, I, or W")
    private String gradeValue;
    
    private String comments;
    
    private boolean markAsCompleted = true;
}
