package com.example.school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "courses")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Course code is required")
    @Column(unique = true, nullable = false, length = 20)
    private String code;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 100)
    private String title;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 12, message = "Credits cannot exceed 12")
    private int credits = 3; // Default value
    
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private int capacity = 30; // Default capacity
    
    @Column(name = "enrolled_students")
    private int enrolledStudents = 0; // Track number of enrolled students
    
    @ManyToMany
    @JoinTable(
        name = "course_prerequisites",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private Set<Course> prerequisites = new HashSet<>();
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Enrollment> enrollments = new HashSet<>();
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Helper method to add prerequisite
    public void addPrerequisite(Course prerequisite) {
        prerequisites.add(prerequisite);
    }
    
    // Helper method to remove prerequisite
    public void removePrerequisite(Course prerequisite) {
        prerequisites.remove(prerequisite);
    }
    
    // Check if course has available capacity
    public boolean hasAvailableCapacity() {
        return enrolledStudents < capacity;
    }
    
    // Increment enrolled students count
    public void incrementEnrolledStudents() {
        this.enrolledStudents++;
    }
    
    // Decrement enrolled students count
    public void decrementEnrolledStudents() {
        if (this.enrolledStudents > 0) {
            this.enrolledStudents--;
        }
    }
}
