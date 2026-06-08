package com.exam.examPortal.repository;

import com.exam.examPortal.entity.Result;
import com.exam.examPortal.entity.User;
import com.exam.examPortal.entity.Exam;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam; // Fixes the RequestParam symbol error

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    //List<Result> findByUser(User user);
    Page<Result> findByUser(User user, Pageable pageable);
    List<Result> findByUser(User user, Sort sort);
    long countByUserAndExam(User user, Exam exam);
}