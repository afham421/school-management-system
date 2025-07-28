package com.example.school.service.impl;

import com.example.school.dto.EnrollmentDTO;
import com.example.school.dto.EnrollmentRequestDTO;
import com.example.school.entity.Course;
import com.example.school.entity.Enrollment;
import com.example.school.entity.Student;
import com.example.school.exception.PrerequisiteNotMetException;
import com.example.school.exception.ResourceAlreadyExistsException;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.exception.CapacityExceededException;
import com.example.school.repository.CourseRepository;
import com.example.school.repository.EnrollmentRepository;
import com.example.school.repository.StudentRepository;
import com.example.school.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    @Override
    public Page<Enrollment> findAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll(pageable);
    }

    @Override
    public Enrollment findEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
    }

    @Override
    public List<Enrollment> findEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    public List<Enrollment> findActiveEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findActiveEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findActiveEnrollmentsByCourseId(courseId);
    }

    @Override
    @Transactional
    public Enrollment enrollStudent(EnrollmentDTO enrollmentDTO) {
        // Check if the student exists
        Student student = studentRepository.findById(enrollmentDTO.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + enrollmentDTO.getStudentId()));
        
        // Check if the course exists
        Course course = courseRepository.findById(enrollmentDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + enrollmentDTO.getCourseId()));
        
        // Check if already enrolled and active
        if (enrollmentRepository.existsActiveEnrollment(student.getId(), course.getId())) {
            throw new ResourceAlreadyExistsException("Student is already enrolled in this course");
        }
        
        // Check course capacity
        if (isCourseFull(course.getId())) {
            throw new CapacityExceededException("Course " + course.getCode() + " is full. No available seats.");
        }
        
        // Check prerequisites
        if (!hasStudentCompletedPrerequisites(student.getId(), course.getId())) {
            throw new PrerequisiteNotMetException("Student has not completed all prerequisites for this course");
        }
        
        // Create and save the enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        
        // Increment the enrolled students count
        course.incrementEnrolledStudents();
        courseRepository.save(course);
        
        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public List<Enrollment> enrollStudentInCourses(EnrollmentRequestDTO enrollmentRequest) {
        List<Enrollment> enrollments = new ArrayList<>();
        
        // Process each course ID in the request
        for (Long courseId : enrollmentRequest.getCourseIds()) {
            EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
            enrollmentDTO.setStudentId(enrollmentRequest.getStudentId());
            enrollmentDTO.setCourseId(courseId);
            
            try {
                Enrollment enrollment = enrollStudent(enrollmentDTO);
                enrollments.add(enrollment);
            } catch (Exception e) {
                // Log the error but continue with other courses
                // In a real application, you might want to implement a more sophisticated error handling strategy
                System.err.println("Failed to enroll student in course " + courseId + ": " + e.getMessage());
            }
        }
        
        if (enrollments.isEmpty()) {
            throw new RuntimeException("Failed to enroll student in any of the requested courses");
        }
        
        return enrollments;
    }

    @Override
    @Transactional
    public Enrollment updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status) {
        Enrollment enrollment = findEnrollmentById(enrollmentId);
        
        // If changing from ACTIVE to DROPPED, decrement the enrolled students count
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE && 
            status == Enrollment.EnrollmentStatus.DROPPED) {
            Course course = enrollment.getCourse();
            course.decrementEnrolledStudents();
            courseRepository.save(course);
        }
        
        enrollment.setStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public void dropEnrollment(Long enrollmentId) {
        Enrollment enrollment = findEnrollmentById(enrollmentId);
        
        // If the enrollment was active, decrement the enrolled students count
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE) {
            Course course = enrollment.getCourse();
            course.decrementEnrolledStudents();
            courseRepository.save(course);
        }
        
        enrollmentRepository.delete(enrollment);
    }

    @Override
    @Transactional
    public void dropStudentFromCourse(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for student " + studentId + " and course " + courseId));
        
        dropEnrollment(enrollment.getId());
    }

    @Override
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsActiveEnrollment(studentId, courseId);
    }

    @Override
    public boolean hasStudentCompletedPrerequisites(Long studentId, Long courseId) {
        // Get the course with its prerequisites
        Course course = courseRepository.findByIdWithPrerequisites(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        
        // If no prerequisites, return true
        if (course.getPrerequisites().isEmpty()) {
            return true;
        }
        
        // Get all completed courses for the student
        List<Enrollment> completedEnrollments = enrollmentRepository.findCompletedCoursesByStudentId(studentId);
        Set<Long> completedCourseIds = completedEnrollments.stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());
        
        // Check if all prerequisites are completed
        return course.getPrerequisites().stream()
                .allMatch(prereq -> completedCourseIds.contains(prereq.getId()));
    }

    @Override
    public boolean isCourseFull(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return !course.hasAvailableCapacity();
    }

    @Override
    public int getAvailableSeats(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
        return course.getCapacity() - course.getEnrolledStudents();
    }
}
