package com.example.school.controller;

import com.example.school.dto.StudentProgressDTO;
import com.example.school.dto.CourseGradeDTO;
import com.example.school.entity.Enrollment;
import com.example.school.entity.Student;
import com.example.school.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/progress")
public class StudentProgressController {

    private final StudentService studentService;

    public StudentProgressController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{studentId}")
    public StudentProgressDTO getStudentProgress(@PathVariable Long studentId) {
        Student student = studentService.findStudentById(studentId);
        double totalPoints = 0;
        int totalCourses = 0;
        List<CourseGradeDTO> courseList = new ArrayList<>();

        for (Enrollment e : student.getEnrollments()) {
            if (e.getGrade() != null) {
                totalCourses++;
                String gradeVal = e.getGrade().getGradeValue();
                switch (gradeVal) {
                    case "A" -> totalPoints += 4.0;
                    case "B" -> totalPoints += 3.0;
                    case "C" -> totalPoints += 2.0;
                    case "D" -> totalPoints += 1.0;
                    case "F" -> totalPoints += 0.0;
                }
                courseList.add(new CourseGradeDTO(
                        e.getCourse().getTitle(),
                        gradeVal,
                        e.getGrade().isCourseCompleted()
                ));
            }
        }

        double gpa = totalCourses > 0 ? totalPoints / totalCourses : 0.0;
        return new StudentProgressDTO(student.getFullName(), gpa, courseList);
    }
}
