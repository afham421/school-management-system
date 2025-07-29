package com.example.school.service.impl;

import com.example.school.dto.GradeRequestDTO;
import com.example.school.entity.Enrollment;
import com.example.school.entity.Grade;
import com.example.school.exception.InvalidGradeException;
import com.example.school.exception.ResourceAlreadyExistsException;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.exception.EnrollmentNotActiveException;
import com.example.school.repository.EnrollmentRepository;
import com.example.school.repository.GradeRepository;
import com.example.school.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Grade findGradeById(Long id) {
        log.debug("Finding grade by id: {}", id);
        return gradeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Grade not found with id: {}", id);
                    return new ResourceNotFoundException("Grade not found with id: " + id);
                });
    }

    @Override
    public List<Grade> findGradesByStudentId(Long studentId) {
        log.debug("Finding grades for student id: {}", studentId);
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        log.debug("Found {} grades for student id: {}", grades.size(), studentId);
        return grades;
    }

    @Override
    public List<Grade> findGradesByCourseId(Long courseId) {
        log.debug("Finding grades for course id: {}", courseId);
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        log.debug("Found {} grades for course id: {}", grades.size(), courseId);
        return grades;
    }

    @Override
    public Page<Grade> findAllGrades(Pageable pageable) {
        log.debug("Finding all grades with pagination: {}", pageable);
        Page<Grade> page = gradeRepository.findAll(pageable);
        log.debug("Found {} grades (page {} of {})", 
                 page.getNumberOfElements(), 
                 page.getNumber() + 1, 
                 page.getTotalPages());
        return page;
    }

    @Override
    @Transactional
    public Grade recordGrade(GradeRequestDTO gradeRequestDTO) {
        log.info("Recording new grade for enrollment id: {}", gradeRequestDTO.getEnrollmentId());
        
        // Find the enrollment
        Enrollment enrollment = enrollmentRepository.findById(gradeRequestDTO.getEnrollmentId())
                .orElseThrow(() -> {
                    log.warn("Enrollment not found with id: {}", gradeRequestDTO.getEnrollmentId());
                    return new ResourceNotFoundException("Enrollment not found with id: " + gradeRequestDTO.getEnrollmentId());
                });

        // Check if the enrollment is active
        if (!enrollment.isActive()) {
            log.warn("Cannot record grade for inactive enrollment id: {}", enrollment.getId());
            throw new EnrollmentNotActiveException("Cannot record grade for an inactive enrollment");
        }

        // Check if a grade already exists for this enrollment
        if (enrollment.getGrade() != null) {
            log.warn("Grade already exists for enrollment id: {}", enrollment.getId());
            throw new ResourceAlreadyExistsException("A grade already exists for this enrollment");
        }

        // Validate the grade value
        if (!isGradeValid(gradeRequestDTO.getGradeValue())) {
            log.warn("Invalid grade value: {}", gradeRequestDTO.getGradeValue());
            throw new InvalidGradeException("Invalid grade value: " + gradeRequestDTO.getGradeValue());
        }

        log.debug("Creating new grade for enrollment id: {}", enrollment.getId());
        
        // Create and save the grade
        Grade grade = new Grade();
        grade.setGradeValue(gradeRequestDTO.getGradeValue());
        grade.setComments(gradeRequestDTO.getComments());
        grade.setEnrollment(enrollment);

        // If markAsCompleted is true, mark the course as completed
        if (gradeRequestDTO.isMarkAsCompleted()) {
            log.debug("Marking course as completed for enrollment id: {}", enrollment.getId());
            grade.markAsCompleted();
        }

        try {
            // Save the grade
            Grade savedGrade = gradeRepository.save(grade);
            
            // Update the enrollment with the new grade
            enrollment.setGrade(savedGrade);
            enrollmentRepository.save(enrollment);
            
            log.info("Successfully recorded grade with id: {} for enrollment id: {}", 
                    savedGrade.getId(), enrollment.getId());
                    
            return savedGrade;
        } catch (Exception e) {
            log.error("Error recording grade for enrollment id: {}", enrollment.getId(), e);
            throw new RuntimeException("Failed to record grade: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Grade updateGrade(Long gradeId, GradeRequestDTO gradeRequestDTO) {
        log.info("Updating grade with id: {}", gradeId);
        
        // Find the existing grade
        Grade existingGrade = findGradeById(gradeId);
        Enrollment enrollment = existingGrade.getEnrollment();

        // Check if the enrollment is active
        if (!enrollment.isActive()) {
            log.warn("Cannot update grade for inactive enrollment id: {}", enrollment.getId());
            throw new EnrollmentNotActiveException("Cannot update grade for an inactive enrollment");
        }

        // Validate the new grade value if it's being changed
        if (gradeRequestDTO.getGradeValue() != null && 
            !gradeRequestDTO.getGradeValue().equals(existingGrade.getGradeValue())) {
            
            if (!isGradeValid(gradeRequestDTO.getGradeValue())) {
                log.warn("Invalid grade value: {}", gradeRequestDTO.getGradeValue());
                throw new InvalidGradeException("Invalid grade value: " + gradeRequestDTO.getGradeValue());
            }
            log.debug("Updating grade value from '{}' to '{}' for grade id: {}", 
                    existingGrade.getGradeValue(), gradeRequestDTO.getGradeValue(), gradeId);
            existingGrade.setGradeValue(gradeRequestDTO.getGradeValue());
        }

        // Update comments if provided
        if (gradeRequestDTO.getComments() != null) {
            log.debug("Updating comments for grade id: {}", gradeId);
            existingGrade.setComments(gradeRequestDTO.getComments());
        }

        // Update completion status if needed
        if (gradeRequestDTO.isMarkAsCompleted() && !existingGrade.isCourseCompleted()) {
            log.debug("Marking course as completed for grade id: {}", gradeId);
            existingGrade.markAsCompleted();
        } else if (!gradeRequestDTO.isMarkAsCompleted() && existingGrade.isCourseCompleted()) {
            log.debug("Unmarking course as completed for grade id: {}", gradeId);
            existingGrade.unmarkAsCompleted();
        }

        try {
            // Save the updated grade
            Grade updatedGrade = gradeRepository.save(existingGrade);
            log.info("Successfully updated grade with id: {}", gradeId);
            return updatedGrade;
        } catch (Exception e) {
            log.error("Error updating grade with id: {}", gradeId, e);
            throw new RuntimeException("Failed to update grade: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteGrade(Long gradeId) {
        log.info("Deleting grade with id: {}", gradeId);
        
        // Check if the grade exists
        Grade grade = findGradeById(gradeId);
        
        try {
            // Remove the grade reference from the enrollment
            Enrollment enrollment = grade.getEnrollment();
            if (enrollment != null) {
                log.debug("Removing grade reference from enrollment id: {}", enrollment.getId());
                enrollment.setGrade(null);
                enrollmentRepository.save(enrollment);
            }
            
            // Delete the grade
            gradeRepository.delete(grade);
            log.info("Successfully deleted grade with id: {}", gradeId);
            
        } catch (Exception e) {
            log.error("Error deleting grade with id: {}", gradeId, e);
            throw new RuntimeException("Failed to delete grade: " + e.getMessage(), e);
        }
    }

    @Override
    public Double calculateStudentGPA(Long studentId) {
        log.debug("Calculating GPA for student id: {}", studentId);
        
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        
        if (grades.isEmpty()) {
            log.debug("No grades found for student id: {}", studentId);
            return 0.0;
        }
        
        double totalPoints = 0.0;
        int totalGradedCourses = 0;
        
        for (Grade grade : grades) {
            if (grade.isCourseCompleted() && grade.getGradeValue() != null) {
                double points = convertGradeToPoints(grade.getGradeValue());
                log.trace("Grade: {}, Points: {}", grade.getGradeValue(), points);
                totalPoints += points;
                totalGradedCourses++;
            }
        }
        
        double gpa = totalGradedCourses > 0 ? totalPoints / totalGradedCourses : 0.0;
        log.debug("Calculated GPA for student id {}: {}", studentId, gpa);
        
        return gpa;
    }

    @Override
    public boolean isCourseCompleted(Long enrollmentId) {
        log.debug("Checking if course is completed for enrollment id: {}", enrollmentId);
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> {
                    log.warn("Enrollment not found with id: {}", enrollmentId);
                    return new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId);
                });
                
        boolean isCompleted = enrollment.getGrade() != null && enrollment.getGrade().isCourseCompleted();
        log.debug("Course completion status for enrollment id {}: {}", enrollmentId, isCompleted);
        
        return isCompleted;
    }

    @Override
    public boolean isGradeValid(String gradeValue) {
        if (!StringUtils.hasText(gradeValue)) {
            log.debug("Grade value is null or empty");
            return false;
        }
        
        // Regular expression to match valid grade formats: A, A-, B+, B, B-, C+, C, C-, D+, D, F, P, NP, I, W
        boolean isValid = gradeValue.matches("^[A-D][+-]?|F|P|NP|I|W$");
        
        if (!isValid) {
            log.debug("Invalid grade value: {}", gradeValue);
        }
        
        return isValid;
    }

    @Override
    public double convertGradeToPoints(String gradeValue) {
        if (!isGradeValid(gradeValue)) {
            log.warn("Attempted to convert invalid grade value: {}", gradeValue);
            throw new IllegalArgumentException("Invalid grade value: " + gradeValue);
        }
        
        double points = switch (gradeValue) {
            case "A" -> 4.0;
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
            // P, I, W are not included in GPA calculation
            default -> 0.0;
        };
        
        log.trace("Converted grade '{}' to {} points", gradeValue, points);
        return points;
    }
}
