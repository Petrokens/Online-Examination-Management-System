package com.exam.examPortal.repository;

import com.exam.examPortal.entity.Question;
import com.exam.examPortal.entity.StudentAnswer;
import com.exam.examPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    // This is a Spring Data JPA query method
    StudentAnswer findByUserAndQuestion(User user, Question question);
}