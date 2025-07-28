package com.example.school.controller;

import com.example.school.entity.DataImport;
import com.example.school.service.FileImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Tag(name = "Data Import", description = "APIs for importing data via files")
public class FileImportController {

    private final FileImportService fileImportService;

    @PostMapping("/students/upload")
    @Operation(summary = "Upload a file to import student data")
    public ResponseEntity<ImportResponse> uploadStudentFile(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, DataImport.ImportType.STUDENTS);
    }

    @PostMapping("/courses/upload")
    @Operation(summary = "Upload a file to import course data")
    public ResponseEntity<ImportResponse> uploadCourseFile(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, DataImport.ImportType.COURSES);
    }

    @PostMapping("/enrollments/upload")
    @Operation(summary = "Upload a file to import enrollment data")
    public ResponseEntity<ImportResponse> uploadEnrollmentFile(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, DataImport.ImportType.ENROLLMENTS);
    }

    @PostMapping("/grades/upload")
    @Operation(summary = "Upload a file to import grade data")
    public ResponseEntity<ImportResponse> uploadGradeFile(@RequestParam("file") MultipartFile file) {
        return handleFileUpload(file, DataImport.ImportType.GRADES);
    }

    @GetMapping("/{importId}")
    @Operation(summary = "Get the status of an import job")
    public ResponseEntity<DataImport> getImportStatus(@PathVariable Long importId) {
        return ResponseEntity.ok(fileImportService.getImportStatus(importId));
    }

    @GetMapping("/type/{importType}")
    @Operation(summary = "Get all imports of a specific type")
    public ResponseEntity<List<DataImport>> getImportsByType(@PathVariable DataImport.ImportType importType) {
        return ResponseEntity.ok((List<DataImport>) fileImportService.getImportsByType(importType));
    }

    @GetMapping
    @Operation(summary = "Get recent import jobs")
    public ResponseEntity<List<DataImport>> getRecentImports(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok((List<DataImport>) fileImportService.getRecentImports(limit));
    }

    private ResponseEntity<ImportResponse> handleFileUpload(MultipartFile file, DataImport.ImportType importType) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ImportResponse.error("File cannot be empty"));
        }

        try {
            Long importId = fileImportService.processFileUpload(file, importType);
            return ResponseEntity.accepted()
                    .body(ImportResponse.success(importId, "Import started successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ImportResponse.error("Failed to start import: " + e.getMessage()));
        }
    }

    // Response DTO for import operations
    public record ImportResponse(boolean success, Long importId, String message) {
        public static ImportResponse success(Long importId, String message) {
            return new ImportResponse(true, importId, message);
        }

        public static ImportResponse error(String message) {
            return new ImportResponse(false, null, message);
        }
    }
}
