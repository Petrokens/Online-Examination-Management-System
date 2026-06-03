package com.exam.examPortal.repository;

import com.exam.examPortal.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    // We don't need to add any custom search functions here right now.
    // Spring Boot automatically gives us save(), delete(), and findById() for exams!

}