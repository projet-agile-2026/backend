package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EvaluationRepository extends JpaRepository<Evaluation, Void>, JpaSpecificationExecutor<Evaluation> {

}