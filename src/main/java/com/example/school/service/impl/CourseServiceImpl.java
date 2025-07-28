package com.example.school.service.impl;

import com.example.school.dto.CourseDTO;
import com.example.school.entity.Course;
import com.example.school.exception.ResourceAlreadyExistsException;
import com.example.school.exception.ResourceNotFoundException;
import com.example.school.repository.CourseRepository;
import com.example.school.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public List<Course> findCoursesWithAvailableCapacity() {
        return courseRepository.findCoursesWithAvailableCapacity();
    }

    @Override
    public Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Override
    public Course findCourseByCode(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + code));
    }

    @Override
    public Page<Course> searchCourses(String query, Pageable pageable) {
        return courseRepository.search(query, pageable);
    }

    @Override
    @Transactional
    public Course createCourse(CourseDTO courseDTO) {
        // Check if course code already exists
        if (courseRepository.existsByCode(courseDTO.getCode())) {
            throw new ResourceAlreadyExistsException("Course code already in use: " + courseDTO.getCode());
        }

        // Map DTO to entity
        Course course = modelMapper.map(courseDTO, Course.class);
        
        // Save the course first to get an ID
        Course savedCourse = courseRepository.save(course);
        
        // Add prerequisites if any
        if (courseDTO.getPrerequisiteIds() != null && !courseDTO.getPrerequisiteIds().isEmpty()) {
            for (Long prerequisiteId : courseDTO.getPrerequisiteIds()) {
                Course prerequisite = findCourseById(prerequisiteId);
                savedCourse.addPrerequisite(prerequisite);
            }
            savedCourse = courseRepository.save(savedCourse);
        }
        
        return savedCourse;
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, CourseDTO courseDTO) {
        // Find existing course
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // Check if the new code is already in use by another course
        if (!existingCourse.getCode().equals(courseDTO.getCode()) && 
            courseRepository.existsByCode(courseDTO.getCode())) {
            throw new ResourceAlreadyExistsException("Course code already in use: " + courseDTO.getCode());
        }

        // Map DTO to existing entity
        modelMapper.map(courseDTO, existingCourse);
        
        // Clear existing prerequisites and add new ones
        existingCourse.getPrerequisites().clear();
        if (courseDTO.getPrerequisiteIds() != null) {
            for (Long prerequisiteId : courseDTO.getPrerequisiteIds()) {
                if (prerequisiteId.equals(id)) {
                    throw new IllegalArgumentException("A course cannot be a prerequisite for itself");
                }
                Course prerequisite = findCourseById(prerequisiteId);
                existingCourse.addPrerequisite(prerequisite);
            }
        }
        
        // Save and return the updated course
        return courseRepository.save(existingCourse);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        // Check if the course exists
        Course course = findCourseById(id);
        
        // Check if the course is a prerequisite for any other courses
        List<Course> dependentCourses = courseRepository.findCoursesThatRequireCourse(id);
        if (!dependentCourses.isEmpty()) {
            throw new IllegalStateException("Cannot delete course as it is a prerequisite for other courses");
        }
        
        // Delete the course
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addPrerequisite(Long courseId, Long prerequisiteId) {
        if (courseId.equals(prerequisiteId)) {
            throw new IllegalArgumentException("A course cannot be a prerequisite for itself");
        }
        
        Course course = findCourseById(courseId);
        Course prerequisite = findCourseById(prerequisiteId);
        
        if (course.getPrerequisites().contains(prerequisite)) {
            throw new ResourceAlreadyExistsException("Course already has this prerequisite");
        }
        
        course.addPrerequisite(prerequisite);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void removePrerequisite(Long courseId, Long prerequisiteId) {
        Course course = findCourseById(courseId);
        Course prerequisite = findCourseById(prerequisiteId);
        
        if (!course.getPrerequisites().contains(prerequisite)) {
            throw new ResourceNotFoundException("Prerequisite not found for this course");
        }
        
        course.removePrerequisite(prerequisite);
        courseRepository.save(course);
    }

    @Override
    public Set<Course> getPrerequisites(Long courseId) {
        Course course = findCourseById(courseId);
        return new HashSet<>(course.getPrerequisites());
    }

    @Override
    public boolean existsByCode(String code) {
        return courseRepository.existsByCode(code);
    }

    @Override
    public boolean existsById(Long id) {
        return courseRepository.existsById(id);
    }

    @Override
    public boolean hasAvailableCapacity(Long courseId) {
        Course course = findCourseById(courseId);
        return course.hasAvailableCapacity();
    }
}
