package com.example.school.service.impl;

import com.example.school.dto.CourseGradeDTO;
import com.example.school.dto.StudentDTO;
import com.example.school.dto.StudentProgressDTO;
import com.example.school.entity.Enrollment;
import com.example.school.entity.Student;
import com.example.school.exception.ResourceAlreadyExistsException;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.repository.StudentRepository;
import com.example.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Page<Student> findAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    @Override
    public Student findStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
    }

    @Override
    public Page<Student> searchStudents(String query, Pageable pageable) {
        return new PageImpl<>(studentRepository.searchByName(query), pageable, studentRepository.searchByName(query).size());
    }
    
    @Override
    public List<Student> searchStudents(String query) {
        return studentRepository.searchByName(query);
    }

    @Override
    @Transactional
    public Student createStudent(StudentDTO studentDTO) {
        // Check if email already exists
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use: " + studentDTO.getEmail());
        }

        // Map DTO to entity
        Student student = modelMapper.map(studentDTO, Student.class);
        
        // Save and return the new student
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, StudentDTO studentDTO) {
        // Find existing student
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Check if the new email is already in use by another student
        if (!existingStudent.getEmail().equals(studentDTO.getEmail()) && 
            studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already in use: " + studentDTO.getEmail());
        }

        // Update the student entity with DTO values
        modelMapper.map(studentDTO, existingStudent);
        
        // Save and return the updated student
        return studentRepository.save(existingStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        // Check if student exists
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        
        // Delete the student
        studentRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        return studentRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StudentProgressDTO getStudentProgress(Long studentId) throws ResourceNotFoundException {
//        log.debug("Fetching progress for student with id: {}", studentId);
        
        // Find the student with their enrollments and grades
        Student student = studentRepository.findByIdWithEnrollmentsAndGrades(studentId)
                .orElseThrow(() -> {
//                    log.warn("Student not found with id: {}", studentId);
                    return new ResourceNotFoundException("Student not found with id: " + studentId);
                });
        
        double totalPoints = 0.0;
        int totalGradedCourses = 0;
        List<CourseGradeDTO> courseGrades = new ArrayList<>();
        
        // Calculate GPA and collect course grades
        for (Enrollment enrollment : student.getEnrollments()) {
            if (enrollment.getGrade() != null) {
                String gradeValue = enrollment.getGrade().getGradeValue();
                boolean isCompleted = enrollment.getGrade().isCourseCompleted();
                
                if (isCompleted) {
                    // Convert letter grade to grade points (A=4.0, B=3.0, etc.)
                    double gradePoints = switch (gradeValue.charAt(0)) {
                        case 'A' -> 4.0;
                        case 'B' -> 3.0;
                        case 'C' -> 2.0;
                        case 'D' -> 1.0;
                        default -> 0.0; // F or other grades count as 0.0
                    };
                    
                    // Handle + and - grades if present
                    if (gradeValue.length() > 1) {
                        if (gradeValue.endsWith("+") && gradePoints > 0.3) {
                            gradePoints += 0.3; // A+ = 4.3, B+ = 3.3, etc.
                        } else if (gradeValue.endsWith("-")) {
                            gradePoints -= 0.3; // A- = 3.7, B- = 2.7, etc.
                        }
                    }
                    
                    totalPoints += gradePoints;
                    totalGradedCourses++;
                }
                
                // Add to course grades list regardless of completion status
                courseGrades.add(new CourseGradeDTO(
                    enrollment.getCourse().getTitle(),
                    gradeValue,
                    isCompleted
                ));
            }
        }
        
        // Calculate GPA (on a 4.0 scale)
        double gpa = totalGradedCourses > 0 ? Math.round((totalPoints / totalGradedCourses) * 100.0) / 100.0 : 0.0;
        
//        log.debug("Calculated GPA: {} for student: {}", gpa, student.getFullName());
        
        return new StudentProgressDTO(
            student.getFullName(),
            gpa,
            courseGrades
        );
    }
}
