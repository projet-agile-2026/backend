package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Formation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormationRepository extends JpaRepository<Formation, String> {
}
