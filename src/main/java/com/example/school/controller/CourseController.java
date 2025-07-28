package com.example.school.controller;

import com.example.school.dto.ApiResponse;
import com.example.school.dto.CourseDTO;
import com.example.school.entity.Course;
import com.example.school.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course Management", description = "APIs for managing courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final ModelMapper modelMapper;

    @GetMapping
    @Operation(summary = "Get all courses with pagination")
    public ResponseEntity<ApiResponse<?>> getAllCourses(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Course> courses = courseService.findAllCourses(pageable);
        Page<CourseDTO> courseDTOs = courses.map(course -> modelMapper.map(course, CourseDTO.class));
        return ResponseEntity.ok(ApiResponse.success(courseDTOs));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all courses without pagination")
    public ResponseEntity<ApiResponse<?>> getAllCourses() {
        List<Course> courses = courseService.findAllCourses();
        List<CourseDTO> courseDTOs = courses.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(courseDTOs, "Courses retrieved successfully"));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all courses with available capacity")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getCoursesWithAvailableCapacity() {
        List<Course> courses = courseService.findCoursesWithAvailableCapacity();
        List<CourseDTO> courseDTOs = courses.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(courseDTOs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by ID")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseById(@PathVariable Long id) {
        Course course = courseService.findCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(course, CourseDTO.class)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get a course by code")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseByCode(@PathVariable String code) {
        Course course = courseService.findCourseByCode(code);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(course, CourseDTO.class)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses by title or code")
    public ResponseEntity<ApiResponse<Page<CourseDTO>>> searchCourses(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Course> courses = courseService.searchCourses(query, pageable);
        Page<CourseDTO> courseDTOs = courses.map(course -> modelMapper.map(course, CourseDTO.class));
        return ResponseEntity.ok(ApiResponse.successPage(courseDTOs));
    }

    @PostMapping
    @Operation(summary = "Create a new course")
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        Course createdCourse = courseService.createCourse(courseDTO);
        return ResponseEntity
                .status(201)
                .body(ApiResponse.success(
                    modelMapper.map(createdCourse, CourseDTO.class),
                    "Course created successfully"
                ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course by ID")
    public ResponseEntity<ApiResponse<CourseDTO>> updateCourse(
            @PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        Course updatedCourse = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(ApiResponse.success(
            modelMapper.map(updatedCourse, CourseDTO.class),
            "Course updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }

    @GetMapping("/{id}/prerequisites")
    @Operation(summary = "Get all prerequisites for a course")
    public ResponseEntity<ApiResponse<Set<CourseDTO>>> getPrerequisites(@PathVariable Long id) {
        Set<Course> prerequisites = courseService.getPrerequisites(id);
        Set<CourseDTO> prerequisiteDTOs = prerequisites.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .collect(Collectors.toSet());
        return ResponseEntity.ok(ApiResponse.success(prerequisiteDTOs));
    }

    @PostMapping("/{id}/prerequisites")
    @Operation(summary = "Add a prerequisite to a course")
    public ResponseEntity<ApiResponse<Void>> addPrerequisite(
            @PathVariable Long id, @RequestParam Long prerequisiteId) {
        courseService.addPrerequisite(id, prerequisiteId);
        return ResponseEntity.ok(ApiResponse.success(null, "Prerequisite added successfully"));
    }

    @DeleteMapping("/{id}/prerequisites/{prerequisiteId}")
    @Operation(summary = "Remove a prerequisite from a course")
    public ResponseEntity<ApiResponse<Void>> removePrerequisite(
            @PathVariable Long id, @PathVariable Long prerequisiteId) {
        courseService.removePrerequisite(id, prerequisiteId);
        return ResponseEntity.ok(ApiResponse.success(null, "Prerequisite removed successfully"));
    }

    @GetMapping("/{courseId}/has-available-capacity")
    @Operation(summary = "Check if a course has available capacity")
    public ResponseEntity<ApiResponse<Boolean>> hasAvailableCapacity(@PathVariable Long courseId) {
        boolean hasCapacity = courseService.hasAvailableCapacity(courseId);
        return ResponseEntity.ok(ApiResponse.success(hasCapacity));
    }
}
