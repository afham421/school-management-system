package com.example.school.service;

import com.example.school.dto.CourseDTO;
import com.example.school.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface CourseService {
    
    List<Course> findAllCourses();
    
    Page<Course> findAllCourses(Pageable pageable);
    
    List<Course> findCoursesWithAvailableCapacity();
    
    Course findCourseById(Long id);
    
    Course findCourseByCode(String code);
    
    Page<Course> searchCourses(String query, Pageable pageable);
    
    Course createCourse(CourseDTO courseDTO);
    
    Course updateCourse(Long id, CourseDTO courseDTO);
    
    void deleteCourse(Long id);
    
    void addPrerequisite(Long courseId, Long prerequisiteId);
    
    void removePrerequisite(Long courseId, Long prerequisiteId);
    
    Set<Course> getPrerequisites(Long courseId);
    
    boolean existsByCode(String code);
    
    boolean existsById(Long id);
    
    boolean hasAvailableCapacity(Long courseId);
}
