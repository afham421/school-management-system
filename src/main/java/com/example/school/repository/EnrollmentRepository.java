package com.example.school.repository;

import com.example.school.entity.Enrollment;
import com.example.school.entity.Enrollment.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId")
    Optional<Enrollment> findByStudentIdAndCourseId(
            @Param("studentId") Long studentId, 
            @Param("courseId") Long courseId
    );
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId AND e.status = 'ACTIVE'")
    boolean existsActiveEnrollment(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course c WHERE e.student.id = :studentId AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedCoursesByStudentId(@Param("studentId") Long studentId);
    
    @Transactional
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = :status WHERE e.id = :enrollmentId")
    int updateEnrollmentStatus(@Param("enrollmentId") Long enrollmentId, @Param("status") EnrollmentStatus status);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId")
    void deleteByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    int countActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course c JOIN FETCH e.student s WHERE e.id = :enrollmentId")
    Optional<Enrollment> findByIdWithCourseAndStudent(@Param("enrollmentId") Long enrollmentId);
}
