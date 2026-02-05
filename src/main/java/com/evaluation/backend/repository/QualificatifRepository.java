package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Qualificatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualificatifRepository extends JpaRepository<Qualificatif, Long> {
}