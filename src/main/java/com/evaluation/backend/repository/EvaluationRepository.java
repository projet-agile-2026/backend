package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByNoEnseignant(Long noEnseignant);

    List<Evaluation> findByCodeFormationAndAnneeUniversitaire(String codeFormation, String anneeUniversitaire);
}
