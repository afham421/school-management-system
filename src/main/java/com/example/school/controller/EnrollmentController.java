package com.example.school.controller;

import com.example.school.dto.EnrollmentDTO;
import com.example.school.dto.EnrollmentRequestDTO;
import com.example.school.dto.EnrollmentResponseDTO;
import com.example.school.entity.Enrollment;
import com.example.school.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment Controller", description = "APIs for managing student course enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    private final ModelMapper modelMapper;

    private EnrollmentResponseDTO convertToDto(Enrollment enrollment) {
        EnrollmentResponseDTO dto = modelMapper.map(enrollment, EnrollmentResponseDTO.class);
        dto.setStudentId(enrollment.getStudent().getId());
        dto.setStudentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setCourseTitle(enrollment.getCourse().getTitle());
        dto.setCourseCode(enrollment.getCourse().getCode());
        return dto;
    }

    @GetMapping
    @Operation(summary = "Get all enrollments with pagination")
    public ResponseEntity<Page<EnrollmentResponseDTO>> getAllEnrollments(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentService.findAllEnrollments(pageable);
        List<EnrollmentResponseDTO> enrollmentDTOs = enrollments.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(enrollmentDTOs, pageable, enrollments.getTotalElements()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get enrollment by ID")
    public ResponseEntity<EnrollmentResponseDTO> getEnrollmentById(@PathVariable Long id) {
        Enrollment enrollment = enrollmentService.findEnrollmentById(id);
        return ResponseEntity.ok(convertToDto(enrollment));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all enrollments for a student")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollmentsByStudentId(
            @PathVariable Long studentId) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.findEnrollmentsByStudentId(studentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all enrollments for a course")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollmentsByCourseId(
            @PathVariable Long courseId) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.findEnrollmentsByCourseId(courseId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/student/{studentId}/active")
    @Operation(summary = "Get active enrollments for a student")
    public ResponseEntity<List<EnrollmentResponseDTO>> getActiveEnrollmentsByStudentId(
            @PathVariable Long studentId) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.findActiveEnrollmentsByStudentId(studentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @PostMapping
    @Operation(summary = "Enroll a student in a course")
    public ResponseEntity<EnrollmentResponseDTO> createEnrollment(
            @RequestBody EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = enrollmentService.enrollStudent(enrollmentDTO);
        return ResponseEntity.ok(convertToDto(enrollment));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Enroll a student in multiple courses")
    public ResponseEntity<List<EnrollmentResponseDTO>> createEnrollments(
            @RequestBody EnrollmentRequestDTO enrollmentRequest) {
        List<EnrollmentResponseDTO> enrollments = enrollmentService.enrollStudentInCourses(enrollmentRequest).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(enrollments);
    }

    @PutMapping("/{enrollmentId}/status")
    @Operation(summary = "Update enrollment status")
    public ResponseEntity<EnrollmentResponseDTO> updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestParam("status") Enrollment.EnrollmentStatus status) {
        Enrollment enrollment = enrollmentService.updateEnrollmentStatus(enrollmentId, status);
        return ResponseEntity.ok(convertToDto(enrollment));
    }

    @DeleteMapping("/{enrollmentId}")
    @Operation(summary = "Delete an enrollment by ID")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long enrollmentId) {
        enrollmentService.dropEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/student/{studentId}/course/{courseId}")
    @Operation(summary = "Drop a student from a course")
    public ResponseEntity<Void> dropStudentFromCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        enrollmentService.dropStudentFromCourse(studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/student/{studentId}/course/{courseId}/enrolled")
    @Operation(summary = "Check if a student is enrolled in a course")
    public ResponseEntity<Boolean> isStudentEnrolled(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.isStudentEnrolled(studentId, courseId));
    }

    @GetMapping("/course/{courseId}/available-seats")
    @Operation(summary = "Get available seats in a course")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getAvailableSeats(courseId));
    }
}
