package com.example.school.dto;

import com.example.school.entity.Enrollment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private LocalDate enrollmentDate;
    private Enrollment.EnrollmentStatus status;
}
