package com.example.school.service.impl;

import com.example.school.entity.DataImport;
import com.example.school.exception.ImportInProgressException;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.repository.DataImportRepository;
import com.example.school.service.FileImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileImportServiceImpl implements FileImportService {

    private final DataImportRepository dataImportRepository;
    
    @Value("${app.upload.dir:${user.home}/.school-imports}")
    private String uploadDir;

    @Override
    @Transactional
    public Long processFileUpload(MultipartFile file, DataImport.ImportType importType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (dataImportRepository.existsProcessingImportByType(importType)) {
            throw new ImportInProgressException("An import is already in progress");
        }
        
        DataImport dataImport = new DataImport();
        dataImport.setImportType(importType);
        dataImport.setStatus(DataImport.ImportStatus.PENDING);
        dataImport.setFileName(file.getOriginalFilename());
        dataImport = dataImportRepository.save(dataImport);
        
        processFileAsync(file, dataImport);
        return dataImport.getId();
    }

    @Async
    protected void processFileAsync(MultipartFile file, DataImport dataImport) {
        try {
            dataImport.startProcessing();
            dataImport = dataImportRepository.save(dataImport);
            
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String fileExt = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                fileExt = originalName.substring(originalName.lastIndexOf("."));
            }
            
            String newFilename = "import_" + dataImport.getId() + "_" + 
                               UUID.randomUUID() + fileExt;
            
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            dataImport.setFilePath(filePath.toString());
            
            // Process file content here
            processFileContent(filePath, dataImport);
            
            dataImport.complete();
        } catch (Exception e) {
            log.error("Import failed: " + e.getMessage(), e);
            dataImport.fail(e.getMessage());
        } finally {
            dataImportRepository.save(dataImport);
        }
    }
    
    private void processFileContent(Path filePath, DataImport dataImport) throws IOException {
        // Implement file processing logic here
        // Example: Parse CSV/Excel and import data
        dataImport.setTotalRecords(0);
        dataImport.setSuccessfulRecords(0);
    }

    @Override
    @Transactional(readOnly = true)
    public DataImport getImportStatus(Long importId) {
        return dataImportRepository.findById(importId)
                .orElseThrow(() -> new ResourceNotFoundException("Import not found: " + importId));
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<DataImport> getImportsByType(DataImport.ImportType importType) {
        return dataImportRepository.findByImportTypeOrderByCreatedAtDesc(importType);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<DataImport> getRecentImports(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        return dataImportRepository.findTopNByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public int cleanupOldImports(int daysOlderThan) {
        if (daysOlderThan <= 0) {
            daysOlderThan = 30; // Default to 30 days if invalid value is provided
        }
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOlderThan);
        return dataImportRepository.deleteByStatusAndCreatedAtBefore(
            DataImport.ImportStatus.COMPLETED, cutoffDate);
    }
}
