package com.example.school.dto;

import com.example.school.entity.Enrollment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GradeResponseDTO {
    private Long id;
    private String gradeValue;
    private String comments;
    private boolean courseCompleted;
    private LocalDate gradedDate;
    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private String studentName;
    private String courseTitle;
    private Enrollment.EnrollmentStatus enrollmentStatus;
}
