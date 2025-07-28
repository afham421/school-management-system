package com.example.school.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "data_imports")
public class DataImport {
    
    public enum ImportStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
    
    public enum ImportType {
        STUDENTS, COURSES, ENROLLMENTS, GRADES
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportType importType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status = ImportStatus.PENDING;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "total_records")
    private Integer totalRecords;
    
    @Column(name = "processed_records")
    private Integer processedRecords = 0;
    
    @Column(name = "successful_records")
    private Integer successfulRecords = 0;
    
    @Column(name = "failed_records")
    private Integer failedRecords = 0;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Helper methods
    public void startProcessing() {
        this.status = ImportStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete() {
        this.status = ImportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void fail(String error) {
        this.status = ImportStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }
    
    public void incrementProcessed(boolean success) {
        if (this.processedRecords == null) {
            this.processedRecords = 0;
        }
        this.processedRecords++;
        
        if (success) {
            if (this.successfulRecords == null) {
                this.successfulRecords = 0;
            }
            this.successfulRecords++;
        } else {
            if (this.failedRecords == null) {
                this.failedRecords = 0;
            }
            this.failedRecords++;
        }
    }
}
