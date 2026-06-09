package com.exam.examPortal.repository;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByTeacher(User teacher);
    // Spring Data JPA magic: find all exams where the 'allowedStudents' list contains this student
    List<Exam> findByAllowedStudentsContaining(User student);

    // We don't need to add any custom search functions here right now.
    // Spring Boot automatically gives us save(), delete(), and findById() for exams!
    @Modifying
    @Query("UPDATE Exam e SET e.maxAttempts = :limit WHERE e.id = :id")
    void updateMaxAttempts(@Param("id") Long id, @Param("limit") int limit);


}