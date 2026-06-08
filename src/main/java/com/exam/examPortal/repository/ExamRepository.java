package com.exam.examPortal.repository;

import com.exam.examPortal.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    // We don't need to add any custom search functions here right now.
    // Spring Boot automatically gives us save(), delete(), and findById() for exams!
    @Modifying
    @Query("UPDATE Exam e SET e.maxAttempts = :limit WHERE e.id = :id")
    void updateMaxAttempts(@Param("id") Long id, @Param("limit") int limit);
}