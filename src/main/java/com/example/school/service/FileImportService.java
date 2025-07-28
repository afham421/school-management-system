package com.example.school.service;

import com.example.school.entity.DataImport;
import org.springframework.web.multipart.MultipartFile;

public interface FileImportService {
    

    Long processFileUpload(MultipartFile file, DataImport.ImportType importType);
    

    DataImport getImportStatus(Long importId);

    Iterable<DataImport> getImportsByType(DataImport.ImportType importType);

    Iterable<DataImport> getRecentImports(int limit);
    

    int cleanupOldImports(int daysOlderThan);
}
