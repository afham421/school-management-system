package com.example.school.controller;

import com.example.school.dto.GradeRequestDTO;
import com.example.school.entity.Grade;
import com.example.school.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@Tag(name = "Grade Management", description = "APIs for managing student grades")
public class GradeController {

    private final GradeService gradeService;
    private final ModelMapper modelMapper;

    public GradeController(GradeService gradeService, ModelMapper modelMapper) {
        this.gradeService = gradeService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a grade by ID")
    public ResponseEntity<Grade> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.findGradeById(id);
        return new ResponseEntity<>(grade, HttpStatus.OK);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all grades for a student")
    public ResponseEntity<List<Grade>> getGradesByStudentId(@PathVariable Long studentId) {
        List<Grade> grades = gradeService.findGradesByStudentId(studentId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all grades for a course")
    public ResponseEntity<List<Grade>> getGradesByCourseId(@PathVariable Long courseId) {
        List<Grade> grades = gradeService.findGradesByCourseId(courseId);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get all grades with pagination")
    public ResponseEntity<Page<Grade>> getAllGrades(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Grade> grades = gradeService.findAllGrades(pageable);
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Record a new grade")
    public ResponseEntity<Grade> recordGrade(@Valid @RequestBody GradeRequestDTO gradeRequestDTO) {
        Grade grade = gradeService.recordGrade(gradeRequestDTO);
        return new ResponseEntity<>(grade, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing grade")
    public ResponseEntity<Grade> updateGrade(
            @PathVariable Long id, @Valid @RequestBody GradeRequestDTO gradeRequestDTO) {
        Grade updatedGrade = gradeService.updateGrade(id, gradeRequestDTO);
        return new ResponseEntity<>(updatedGrade, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a grade by ID")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/student/{studentId}/gpa")
    @Operation(summary = "Calculate GPA for a student")
    public ResponseEntity<Double> calculateStudentGPA(@PathVariable Long studentId) {
        Double gpa = gradeService.calculateStudentGPA(studentId);
        return new ResponseEntity<>(gpa, HttpStatus.OK);
    }

    @GetMapping("/enrollment/{enrollmentId}/completed")
    @Operation(summary = "Check if a course is completed for an enrollment")
    public ResponseEntity<Boolean> isCourseCompleted(@PathVariable Long enrollmentId) {
        boolean isCompleted = gradeService.isCourseCompleted(enrollmentId);
        return new ResponseEntity<>(isCompleted, HttpStatus.OK);
    }
}
