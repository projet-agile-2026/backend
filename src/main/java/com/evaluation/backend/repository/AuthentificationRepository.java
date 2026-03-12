package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Authentification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthentificationRepository
        extends JpaRepository<Authentification, Long> {

    List<Authentification> findByRole(String role);

    Optional<Authentification> findByEmailAndMotPasse(
            String email,
            String motPasse
    );

    Optional<Authentification> findByEmail(
            String email
    );
}

