package com.example.school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "enrollments", uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "course_id"}, name = "uk_student_course")})
public class Enrollment {

    public enum EnrollmentStatus {
        ACTIVE, DROPPED, COMPLETED, FAILED,WITHDRAWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Enrollment date is required")
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "withdrawal_reason")
    private String withdrawalReason;

    @Column(name = "withdrawal_date")
    private LocalDate withdrawalDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Grade grade;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to set grade
    public void setGrade(Grade grade) {
        if (grade == null) {
            if (this.grade != null) {
                this.grade.setEnrollment(null);
            }
        } else {
            grade.setEnrollment(this);
        }
        this.grade = grade;
    }

    // Check if the enrollment is active
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE;
    }

    // Check if the enrollment is completed
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }
}
