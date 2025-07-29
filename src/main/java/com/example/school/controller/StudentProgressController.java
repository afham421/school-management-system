package com.example.school.controller;

import com.example.school.dto.ApiResponse;
import com.example.school.dto.StudentProgressDTO;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Progress", description = "APIs for student progress tracking")
public class StudentProgressController {

    private final StudentService studentService;

    @GetMapping("/{studentId}/progress")
    @Operation(summary = "Get student progress and grades")
    public ResponseEntity<ApiResponse<StudentProgressDTO>> getStudentProgress(@PathVariable Long studentId) {
        try {
            StudentProgressDTO progress = studentService.getStudentProgress(studentId);
            return ResponseEntity.ok(ApiResponse.success(progress, "Student progress retrieved successfully"));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.error("An error occurred while retrieving student progress"));
        }
    }
}
