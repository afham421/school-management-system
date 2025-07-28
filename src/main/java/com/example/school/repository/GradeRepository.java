package com.example.school.repository;

import com.example.school.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    @Query("SELECT g FROM Grade g JOIN g.enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId")
    Optional<Grade> findByStudentIdAndCourseId(
            @Param("studentId") Long studentId, 
            @Param("courseId") Long courseId
    );
    
    @Query("SELECT g FROM Grade g JOIN g.enrollment e WHERE e.student.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT g FROM Grade g JOIN g.enrollment e WHERE e.course.id = :courseId")
    List<Grade> findByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT g FROM Grade g JOIN g.enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId AND g.isCourseCompleted = true")
    Optional<Grade> findCompletedGradeByStudentAndCourse(
            @Param("studentId") Long studentId, 
            @Param("courseId") Long courseId
    );
    
    @Query("SELECT AVG(g.gradeValue) FROM Grade g JOIN g.enrollment e WHERE e.student.id = :studentId AND g.isCourseCompleted = true")
    Double calculateGPA(@Param("studentId") Long studentId);
    
    @Query("SELECT g FROM Grade g JOIN FETCH g.enrollment e JOIN FETCH e.course c WHERE e.student.id = :studentId")
    List<Grade> findGradesWithCourseByStudentId(@Param("studentId") Long studentId);
}
