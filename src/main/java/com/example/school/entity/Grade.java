package com.example.school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "grades")
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Grade value is required")
    @Pattern(regexp = "^[A-D][+-]?|F|P|NP|I|W$", 
             message = "Grade must be A, A-, B+, B, B-, C+, C, C-, D+, D, F, P, NP, I, or W")
    @Column(name = "grade_value", nullable = false, length = 2)
    private String gradeValue;
    
    @Size(max = 500, message = "Comments must be less than 500 characters")
    private String comments;
    
    @Column(name = "is_course_completed")
    private boolean isCourseCompleted = false;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;
    
    @Column(name = "graded_date")
    private LocalDate gradedDate;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Calculate grade points based on grade value
    public Double getGradePoints() {
        if (gradeValue == null || gradeValue.isEmpty()) {
            return null;
        }
        
        return switch (gradeValue) {
            case "A", "A+" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "F", "NP" -> 0.0;
            case "P" -> 2.0; // Default passing grade points
            default -> null; // For I, W, or other non-grade values
        };
    }
    
    // Check if the grade is passing
    public boolean isPassingGrade() {
        if (gradeValue == null) return false;
        return !(gradeValue.equals("F") || gradeValue.equals("NP") || 
                gradeValue.equals("I") || gradeValue.equals("W"));
    }
    
    // Mark the course as completed with this grade
    public void markAsCompleted() {
        this.isCourseCompleted = true;
        this.gradedDate = LocalDate.now();
        if (this.enrollment != null) {
            this.enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        }
    }
}
