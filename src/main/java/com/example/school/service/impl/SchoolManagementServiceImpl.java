package com.example.school.service.impl;

import com.example.school.dto.EnrollmentRequestDTO;
import com.example.school.dto.GradeRequestDTO;
import com.example.school.entity.Course;
import com.example.school.entity.Enrollment;
import com.example.school.entity.Grade;
import com.example.school.entity.Student;
import com.example.school.exception.CapacityExceededException;
import com.example.school.exception.EnrollmentNotActiveException;
import com.example.school.exception.PrerequisiteNotMetException;
import com.example.school.exception.ResourceAlreadyExistsException;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.repository.CourseRepository;
import com.example.school.repository.EnrollmentRepository;
import com.example.school.repository.StudentRepository;
import com.example.school.service.EnrollmentService;
import com.example.school.service.GradeService;
import com.example.school.service.SchoolManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolManagementServiceImpl implements SchoolManagementService {

    private final EnrollmentService enrollmentService;
    private final GradeService gradeService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public List<Enrollment> enrollStudentInCourses(EnrollmentRequestDTO enrollmentRequest) {
        List<Enrollment> enrollments = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Process each course enrollment
        for (Long courseId : enrollmentRequest.getCourseIds()) {
            try {
                // Check if student exists
                Student student = studentRepository.findById(enrollmentRequest.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + enrollmentRequest.getStudentId()));

                // Check if course exists
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

                // Check if already enrolled
                if (enrollmentRepository.existsActiveEnrollment(student.getId(), courseId)) {
                    throw new ResourceAlreadyExistsException("Student is already enrolled in course: " + course.getCode());
                }

                // Check course capacity
                if (!course.hasAvailableCapacity()) {
                    throw new CapacityExceededException("Course " + course.getCode() + " is full. No available seats.");
                }

                // Check prerequisites
                if (!enrollmentService.hasStudentCompletedPrerequisites(student.getId(), courseId)) {
                    throw new PrerequisiteNotMetException("Student has not completed all prerequisites for course: " + course.getCode());
                }

                // Create and save the enrollment
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setCourse(course);
                enrollment.setEnrollmentDate(LocalDate.now());
                enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
                
                // Increment enrolled students count
                course.incrementEnrolledStudents();
                courseRepository.save(course);
                
                // Save enrollment
                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
                enrollments.add(savedEnrollment);
                
            } catch (Exception e) {
                log.error("Failed to enroll student in course {}: {}", courseId, e.getMessage());
                errors.add("Course " + courseId + ": " + e.getMessage());
                // Continue with other courses, but we'll throw an exception at the end if any failed
            }
        }

        // If there were any errors, throw an exception with all error messages
        if (!errors.isEmpty()) {
            throw new RuntimeException("Failed to complete all enrollments: " + String.join("; ", errors));
        }

        return enrollments;
    }

    @Override
    @Transactional
    public Enrollment transferStudentCourse(Long studentId, Long fromCourseId, Long toCourseId) {
        // Drop from current course
        enrollmentService.dropStudentFromCourse(studentId, fromCourseId);
        
        // Enroll in new course
        EnrollmentRequestDTO request = new EnrollmentRequestDTO();
        request.setStudentId(studentId);
        request.getCourseIds().add(toCourseId);
        
        List<Enrollment> enrollments = enrollStudentInCourses(request);
        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    @Override
    @Transactional
    public Grade recordGradeAndUpdateEnrollment(GradeRequestDTO gradeRequest) {
        // Record the grade
        Grade grade = gradeService.recordGrade(gradeRequest);
        
        // If the grade marks the course as completed, update the enrollment status
        if (gradeRequest.isMarkAsCompleted()) {
            Enrollment enrollment = grade.getEnrollment();
            if (enrollment != null && enrollment.isActive()) {
                enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
                enrollmentRepository.save(enrollment);
            }
        }
        
        return grade;
    }

    @Override
    @Transactional
    public void dropStudentFromCourse(Long studentId, Long courseId) {
        // Delegate to enrollment service but ensure it's wrapped in a transaction
        enrollmentService.dropStudentFromCourse(studentId, courseId);
    }

    @Override
    @Transactional
    public void processStudentWithdrawal(Long studentId, String reason) {
        // Find all active enrollments for the student
        List<Enrollment> activeEnrollments = enrollmentService.findActiveEnrollmentsByStudentId(studentId);
        
        // Update each enrollment to WITHDRAWN status
        for (Enrollment enrollment : activeEnrollments) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.WITHDRAWN);
            enrollment.setWithdrawalReason(reason);
            enrollment.setWithdrawalDate(LocalDate.now());
            
            // Decrement the enrolled students count for the course
            Course course = enrollment.getCourse();
            if (course != null) {
                course.decrementEnrolledStudents();
                courseRepository.save(course);
            }
            
            enrollmentRepository.save(enrollment);
        }
        
        // Additional withdrawal processing could be added here
        log.info("Processed withdrawal for student {}. Reason: {}", studentId, reason);
    }

    @Override
    @Transactional
    public Course updateCourseCapacity(Long courseId, int newCapacity) {
        if (newCapacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        // If reducing capacity, ensure it doesn't go below current enrollment
        if (newCapacity < course.getEnrolledStudents()) {
            throw new CapacityExceededException(
                    String.format("Cannot set capacity to %d when there are %d enrolled students",
                            newCapacity, course.getEnrolledStudents()));
        }
        
        course.setCapacity(newCapacity);
        return courseRepository.save(course);
    }
}
