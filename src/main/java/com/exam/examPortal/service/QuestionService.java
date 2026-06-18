package com.exam.examPortal.service;

import com.exam.examPortal.entity.Exam;
import com.exam.examPortal.entity.Question;
import com.exam.examPortal.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public void saveQuestion(Question question) {
        questionRepository.save(question);
    }

    public List<Question> getQuestionsByExamId(Long examId) {
        // 1. Fetch the list from the database
        List<Question> questions = questionRepository.findByExam_ExamId(examId);

        // 2. Shuffle the list (this modifies the 'questions' list in-place)
        if (questions != null) {
            Collections.shuffle(questions);
        }
        // 3. Return the shuffled list
        return questions;
    }
}