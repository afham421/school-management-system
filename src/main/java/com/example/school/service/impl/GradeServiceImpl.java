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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Grade findGradeById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
    }

    @Override
    public List<Grade> findGradesByStudentId(Long studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    @Override
    public List<Grade> findGradesByCourseId(Long courseId) {
        return gradeRepository.findByCourseId(courseId);
    }

    @Override
    public Page<Grade> findAllGrades(Pageable pageable) {
        return gradeRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Grade recordGrade(GradeRequestDTO gradeRequestDTO) {
        // Find the enrollment
        Enrollment enrollment = enrollmentRepository.findById(gradeRequestDTO.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + gradeRequestDTO.getEnrollmentId()));

        // Check if the enrollment is active
        if (!enrollment.isActive()) {
            throw new EnrollmentNotActiveException("Cannot record grade for an inactive enrollment");
        }

        // Check if a grade already exists for this enrollment
        if (enrollment.getGrade() != null) {
            throw new ResourceAlreadyExistsException("A grade already exists for this enrollment");
        }

        // Validate the grade value
        if (!isGradeValid(gradeRequestDTO.getGradeValue())) {
            throw new InvalidGradeException("Invalid grade value: " + gradeRequestDTO.getGradeValue());
        }

        // Create and save the grade
        Grade grade = new Grade();
        grade.setGradeValue(gradeRequestDTO.getGradeValue());
        grade.setComments(gradeRequestDTO.getComments());
        grade.setEnrollment(enrollment);

        // If markAsCompleted is true, mark the course as completed
        if (gradeRequestDTO.isMarkAsCompleted()) {
            grade.markAsCompleted();
        }

        // Save the grade
        Grade savedGrade = gradeRepository.save(grade);
        
        // Update the enrollment with the new grade
        enrollment.setGrade(savedGrade);
        enrollmentRepository.save(enrollment);

        return savedGrade;
    }

    @Override
    @Transactional
    public Grade updateGrade(Long gradeId, GradeRequestDTO gradeRequestDTO) {
        // Find the existing grade
        Grade existingGrade = findGradeById(gradeId);
        Enrollment enrollment = existingGrade.getEnrollment();

        // Check if the enrollment is active (if not completed yet)
        if (enrollment.isActive() && !existingGrade.isCourseCompleted()) {
            // If the enrollment is still active and the course is not marked as completed,
            // we can update the grade
            
            // Validate the new grade value
            if (!isGradeValid(gradeRequestDTO.getGradeValue())) {
                throw new InvalidGradeException("Invalid grade value: " + gradeRequestDTO.getGradeValue());
            }
            
            // Update the grade
            existingGrade.setGradeValue(gradeRequestDTO.getGradeValue());
            existingGrade.setComments(gradeRequestDTO.getComments());
            
            // If markAsCompleted is true, mark the course as completed
            if (gradeRequestDTO.isMarkAsCompleted()) {
                existingGrade.markAsCompleted();
            }
            
            return gradeRepository.save(existingGrade);
        } else {
            throw new IllegalStateException("Cannot update a grade for a completed or inactive enrollment");
        }
    }

    @Override
    @Transactional
    public void deleteGrade(Long gradeId) {
        Grade grade = findGradeById(gradeId);
        Enrollment enrollment = grade.getEnrollment();
        
        // Remove the grade from the enrollment
        if (enrollment != null && enrollment.getGrade() != null && 
            enrollment.getGrade().getId().equals(gradeId)) {
            enrollment.setGrade(null);
            enrollmentRepository.save(enrollment);
        }
        
        // Delete the grade
        gradeRepository.delete(grade);
    }

    @Override
    public Double calculateStudentGPA(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        
        if (grades.isEmpty()) {
            return null; // Or 0.0, depending on your requirements
        }
        
        double totalPoints = 0.0;
        int totalCredits = 0;
        
        for (Grade grade : grades) {
            if (grade.isCourseCompleted() && grade.getGradePoints() != null) {
                int credits = grade.getEnrollment().getCourse().getCredits();
                totalPoints += grade.getGradePoints() * credits;
                totalCredits += credits;
            }
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }

    @Override
    public boolean isCourseCompleted(Long enrollmentId) {
        return gradeRepository.findCompletedGradeByStudentAndCourse(
            enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId))
                .getStudent().getId(),
            enrollmentRepository.findById(enrollmentId).get().getCourse().getId()
        ).isPresent();
    }

    @Override
    public boolean isGradeValid(String gradeValue) {
        if (gradeValue == null || gradeValue.trim().isEmpty()) {
            return false;
        }
        
        // Valid grades: A, A-, B+, B, B-, C+, C, C-, D+, D, F, P, NP, I, W
        return gradeValue.matches("^[A-D][+-]?|F|P|NP|I|W$");
    }

    @Override
    public double convertGradeToPoints(String gradeValue) {
        if (!isGradeValid(gradeValue)) {
            throw new InvalidGradeException("Invalid grade value: " + gradeValue);
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
            default -> 0.0; // For I, W, or other non-grade values
        };
    }
}
