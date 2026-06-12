package com.exam.examPortal.repository;

import com.exam.examPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // This tells Spring Boot to write a custom SQL search query
    // to find a user by their email address!
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);

}