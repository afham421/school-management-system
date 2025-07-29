package com.example.school.controller;

import com.example.school.dto.GradeRequestDTO;
import com.example.school.dto.GradeResponseDTO;
import com.example.school.entity.Grade;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/grades")
@Tag(name = "Grade Management", description = "APIs for managing student grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get a grade by ID")
    public ResponseEntity<GradeResponseDTO> getGradeById(@PathVariable Long id) {
        try {
            Grade grade = gradeService.findGradeById(id);
            GradeResponseDTO response = convertToDto(grade);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            log.warn("Grade not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error retrieving grade with id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all grades for a student")
    public ResponseEntity<List<GradeResponseDTO>> getGradesByStudentId(@PathVariable Long studentId) {
        try {
            List<Grade> grades = gradeService.findGradesByStudentId(studentId);
            List<GradeResponseDTO> response = grades.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error retrieving grades for student id: {}", studentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all grades for a course")
    public ResponseEntity<List<GradeResponseDTO>> getGradesByCourseId(@PathVariable Long courseId) {
        try {
            List<Grade> grades = gradeService.findGradesByCourseId(courseId);
            List<GradeResponseDTO> response = grades.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error retrieving grades for course id: {}", courseId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all grades with pagination")
    public ResponseEntity<Page<GradeResponseDTO>> getAllGrades(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<Grade> grades = gradeService.findAllGrades(pageable);
            List<GradeResponseDTO> content = grades.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            Page<GradeResponseDTO> response = new PageImpl<>(
                content, 
                pageable, 
                grades.getTotalElements()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error retrieving paginated grades", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Record a new grade")
    public ResponseEntity<GradeResponseDTO> recordGrade(@Valid @RequestBody GradeRequestDTO gradeRequestDTO) {
        try {
            Grade grade = gradeService.recordGrade(gradeRequestDTO);
            GradeResponseDTO response = convertToDto(grade);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceNotFoundException ex) {
            log.warn("Resource not found: {}", ex.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error recording grade", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing grade")
    public ResponseEntity<GradeResponseDTO> updateGrade(
            @PathVariable Long id, @Valid @RequestBody GradeRequestDTO gradeRequestDTO) {
        try {
            Grade updatedGrade = gradeService.updateGrade(id, gradeRequestDTO);
            GradeResponseDTO response = convertToDto(updatedGrade);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            log.warn("Grade not found with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            log.warn("Cannot update grade: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            log.error("Error updating grade with id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a grade by ID")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        try {
            gradeService.deleteGrade(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException ex) {
            log.warn("Grade not found for deletion with id: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error deleting grade with id: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/student/{studentId}/gpa")
    @Operation(summary = "Calculate GPA for a student")
    public ResponseEntity<Double> calculateStudentGPA(@PathVariable Long studentId) {
        try {
            Double gpa = gradeService.calculateStudentGPA(studentId);
            return ResponseEntity.ok(gpa);
        } catch (Exception ex) {
            log.error("Error calculating GPA for student id: {}", studentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/enrollment/{enrollmentId}/completed")
    @Operation(summary = "Check if a course is completed for an enrollment")
    public ResponseEntity<Boolean> isCourseCompleted(@PathVariable Long enrollmentId) {
        try {
            boolean isCompleted = gradeService.isCourseCompleted(enrollmentId);
            return ResponseEntity.ok(isCompleted);
        } catch (ResourceNotFoundException ex) {
            log.warn("Enrollment not found with id: {}", enrollmentId);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Error checking course completion for enrollment id: {}", enrollmentId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to convert Grade entity to GradeResponseDTO
    private GradeResponseDTO convertToDto(Grade grade) {
        if (grade == null) {
            return null;
        }
        
        GradeResponseDTO dto = modelMapper.map(grade, GradeResponseDTO.class);
        
        // Map additional fields from related entities
        if (grade.getEnrollment() != null) {
            dto.setEnrollmentId(grade.getEnrollment().getId());
            dto.setEnrollmentStatus(grade.getEnrollment().getStatus());
            
            if (grade.getEnrollment().getStudent() != null) {
                dto.setStudentId(grade.getEnrollment().getStudent().getId());
                dto.setStudentName(grade.getEnrollment().getStudent().getFullName());
            }
            
            if (grade.getEnrollment().getCourse() != null) {
                dto.setCourseId(grade.getEnrollment().getCourse().getId());
                dto.setCourseTitle(grade.getEnrollment().getCourse().getTitle());
            }
        }
        
        return dto;
    }
}
