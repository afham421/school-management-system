package com.example.school.service;

import com.example.school.dto.StudentDTO;
import com.example.school.dto.StudentProgressDTO;
import com.example.school.entity.Student;
import com.example.school.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    
    List<Student> findAllStudents();
    
    Page<Student> findAllStudents(Pageable pageable);
    
    Student findStudentById(Long id);
    
    Student findStudentByEmail(String email);
    

    Page<Student> searchStudents(String query, Pageable pageable);
    

    List<Student> searchStudents(String query);
    
    Student createStudent(StudentDTO studentDTO);
    
    Student updateStudent(Long id, StudentDTO studentDTO);
    
    void deleteStudent(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsById(Long id);

    StudentProgressDTO getStudentProgress(Long studentId) throws ResourceNotFoundException;
}
