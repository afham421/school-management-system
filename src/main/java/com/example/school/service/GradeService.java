package com.example.school.service;

import com.example.school.dto.GradeRequestDTO;
import com.example.school.entity.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GradeService {
    
    Grade findGradeById(Long id);
    
    List<Grade> findGradesByStudentId(Long studentId);
    
    List<Grade> findGradesByCourseId(Long courseId);
    
    Page<Grade> findAllGrades(Pageable pageable);
    
    Grade recordGrade(GradeRequestDTO gradeRequestDTO);
    
    Grade updateGrade(Long gradeId, GradeRequestDTO gradeRequestDTO);
    
    void deleteGrade(Long gradeId);
    
    Double calculateStudentGPA(Long studentId);
    
    boolean isCourseCompleted(Long enrollmentId);
    
    boolean isGradeValid(String gradeValue);
    
    double convertGradeToPoints(String gradeValue);
}
