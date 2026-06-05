package com.exam.examPortal.service;

import com.exam.examPortal.entity.StudentAnswer;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.entity.Question;
import com.exam.examPortal.repository.StudentAnswerRepository;
import com.exam.examPortal.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentAnswerService {

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public void saveOrUpdate(User user, Long questionId, String selectedOption) {
        System.out.println("DEBUG: Service is saving question " + questionId + " for user " + user.getEmail());
        // 1. Find the question from DB
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // 2. Check if an answer already exists for this user/question
        StudentAnswer answer = studentAnswerRepository.findByUserAndQuestion(user, question);

        // 3. If no answer, create a new object; if exists, update it
        if (answer == null) {
            answer = new StudentAnswer();
            answer.setUser(user);
            answer.setQuestion(question);
        }
        if (question.getExam() != null) {
            answer.setExam(question.getExam());
        }
        answer.setSelectedOption(selectedOption);
        studentAnswerRepository.save(answer);
        System.out.println("DEBUG: Save command executed.");
    }
}