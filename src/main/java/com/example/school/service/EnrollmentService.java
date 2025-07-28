package com.example.school.service;

import com.example.school.dto.EnrollmentDTO;
import com.example.school.dto.EnrollmentRequestDTO;
import com.example.school.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface EnrollmentService {
    
    List<Enrollment> findAllEnrollments();
    
    Page<Enrollment> findAllEnrollments(Pageable pageable);
    
    Enrollment findEnrollmentById(Long id);
    
    List<Enrollment> findEnrollmentsByStudentId(Long studentId);
    
    List<Enrollment> findEnrollmentsByCourseId(Long courseId);
    
    List<Enrollment> findActiveEnrollmentsByStudentId(Long studentId);
    
    List<Enrollment> findActiveEnrollmentsByCourseId(Long courseId);
    
    Enrollment enrollStudent(EnrollmentDTO enrollmentDTO);
    
    List<Enrollment> enrollStudentInCourses(EnrollmentRequestDTO enrollmentRequest);
    
    Enrollment updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status);
    
    void dropEnrollment(Long enrollmentId);
    
    void dropStudentFromCourse(Long studentId, Long courseId);
    
    boolean isStudentEnrolled(Long studentId, Long courseId);
    
    boolean hasStudentCompletedPrerequisites(Long studentId, Long courseId);
    
    boolean isCourseFull(Long courseId);
    
    int getAvailableSeats(Long courseId);
}
