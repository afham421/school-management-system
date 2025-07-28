package com.example.school.service;

import com.example.school.dto.StudentDTO;
import com.example.school.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {
    
    List<Student> findAllStudents();
    
    Page<Student> findAllStudents(Pageable pageable);
    
    Student findStudentById(Long id);
    
    Student findStudentByEmail(String email);
    
    /**
     * Search for students by name or email with pagination
     * @param query Search term to look for in student names or emails
     * @param pageable Pagination information
     * @return Page of students matching the search criteria
     */
    Page<Student> searchStudents(String query, Pageable pageable);
    
    /**
     * Search for students by name or email (non-paginated)
     * @param query Search term to look for in student names or emails
     * @return List of students matching the search criteria
     */
    List<Student> searchStudents(String query);
    
    Student createStudent(StudentDTO studentDTO);
    
    Student updateStudent(Long id, StudentDTO studentDTO);
    
    void deleteStudent(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsById(Long id);
}
