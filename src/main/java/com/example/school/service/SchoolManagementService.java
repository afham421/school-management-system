package com.example.school.service;

import com.example.school.dto.EnrollmentRequestDTO;
import com.example.school.dto.GradeRequestDTO;
import com.example.school.entity.Course;
import com.example.school.entity.Enrollment;
import com.example.school.entity.Grade;
import com.example.school.entity.Student;

import java.util.List;

public interface SchoolManagementService {
    

    List<Enrollment> enrollStudentInCourses(EnrollmentRequestDTO enrollmentRequest);
    

    Enrollment transferStudentCourse(Long studentId, Long fromCourseId, Long toCourseId);
    

    Grade recordGradeAndUpdateEnrollment(GradeRequestDTO gradeRequest);
    

    void dropStudentFromCourse(Long studentId, Long courseId);
    

    void processStudentWithdrawal(Long studentId, String reason);
    

    Course updateCourseCapacity(Long courseId, int newCapacity);
}
