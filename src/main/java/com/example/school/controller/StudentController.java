package com.example.school.controller;

import com.example.school.dto.ApiResponse;
import com.example.school.dto.StudentDTO;
import com.example.school.entity.Student;
import com.example.school.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final ModelMapper modelMapper;

    @GetMapping
    @Operation(summary = "Get all students with pagination")
    public ResponseEntity<ApiResponse<?>> getAllStudents(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Student> students = studentService.findAllStudents(pageable);
        Page<StudentDTO> studentDTOs = students.map(student -> modelMapper.map(student, StudentDTO.class));
        return ResponseEntity.ok(ApiResponse.success(studentDTOs));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all students without pagination")
    public ResponseEntity<ApiResponse<?>> getAllStudents() {
        List<Student> students = studentService.findAllStudents();
        List<StudentDTO> studentDTOs = students.stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(studentDTOs, "Students retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a student by ID")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentById(@PathVariable Long id) {
        Student student = studentService.findStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(student, StudentDTO.class)));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get a student by email")
    public ResponseEntity<ApiResponse<StudentDTO>> getStudentByEmail(@PathVariable String email) {
        Student student = studentService.findStudentByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(student, StudentDTO.class)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search students by name or email")
    public ResponseEntity<ApiResponse<Page<StudentDTO>>> searchStudents(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Student> students = studentService.searchStudents(query, pageable);
        Page<StudentDTO> studentDTOs = students.map(student -> modelMapper.map(student, StudentDTO.class));
        return ResponseEntity.ok(ApiResponse.successPage(studentDTOs));
    }

    @PostMapping
    @Operation(summary = "Create a new student")
    public ResponseEntity<ApiResponse<StudentDTO>> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        Student createdStudent = studentService.createStudent(studentDTO);
        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(
                    modelMapper.map(createdStudent, StudentDTO.class),
                    "Student created successfully"
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing student")
    public ResponseEntity<ApiResponse<StudentDTO>> updateStudent(
            @PathVariable Long id, @Valid @RequestBody StudentDTO studentDTO) {
        Student updatedStudent = studentService.updateStudent(id, studentDTO);
        return ResponseEntity.ok(ApiResponse.success(
            modelMapper.map(updatedStudent, StudentDTO.class),
            "Student updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student by ID")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }
}
