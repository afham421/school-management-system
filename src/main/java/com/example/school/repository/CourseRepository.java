package com.example.school.repository;

import com.example.school.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByCode(String code);
    
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Course> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE c.capacity > c.enrolledStudents")
    List<Course> findCoursesWithAvailableCapacity();
    
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Course c JOIN c.prerequisites p WHERE p.id = :courseId")
    List<Course> findCoursesThatRequireCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.prerequisites WHERE c.id = :courseId")
    Optional<Course> findByIdWithPrerequisites(@Param("courseId") Long courseId);
}
