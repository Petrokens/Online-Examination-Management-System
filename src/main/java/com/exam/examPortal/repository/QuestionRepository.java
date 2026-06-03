package com.exam.examPortal.repository;

import com.exam.examPortal.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // This custom search acts like a filter.
    // It tells MySQL: "Get me a List of all questions where the exam_id matches this number!"
    List<Question> findByExam_ExamId(Long examId);

}