package com.example.school.repository;

import com.example.school.entity.DataImport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface DataImportRepository extends JpaRepository<DataImport, Long> {
    
    List<DataImport> findByStatus(DataImport.ImportStatus status);
    
    List<DataImport> findByImportType(DataImport.ImportType importType);
    
    List<DataImport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT di FROM DataImport di WHERE di.importType = :importType AND di.status = 'COMPLETED' ORDER BY di.completedAt DESC")
    List<DataImport> findRecentSuccessfulImports(@Param("importType") DataImport.ImportType importType);
    
    @Query("SELECT COUNT(di) > 0 FROM DataImport di WHERE di.importType = :importType AND di.status = 'PROCESSING'")
    boolean existsProcessingImportByType(@Param("importType") DataImport.ImportType importType);
    
    @Query("SELECT di FROM DataImport di WHERE di.importType = :importType ORDER BY di.createdAt DESC")
    List<DataImport> findByImportTypeOrderByCreatedAtDesc(@Param("importType") DataImport.ImportType importType);
    
    @Query("SELECT di FROM DataImport di ORDER BY di.createdAt DESC")
    List<DataImport> findTopNByOrderByCreatedAtDesc(Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM DataImport di WHERE di.status = :status AND di.createdAt < :cutoffDate")
    int deleteByStatusAndCreatedAtBefore(
            @Param("status") DataImport.ImportStatus status,
            @Param("cutoffDate") LocalDateTime cutoffDate
    );
}
