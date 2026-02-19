package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByNoEnseignant(Long noEnseignant);

    @Query("""
        SELECT COALESCE(MAX(e.noEvaluation), 0)
        FROM Evaluation e
        WHERE e.anneeUniversitaire = :annee
          AND e.noEnseignant = :noEns
          AND e.codeFormation = :codeFormation
          AND e.codeUe = :codeUe
    """)
        Short findMaxNoEvaluation(
            @Param("annee") String annee,
            @Param("noEns") Long noEns,
            @Param("codeFormation") String codeFormation,
            @Param("codeUe") String codeUe
    );

    List<Evaluation> findByCodeFormationAndAnneeUniversitaire(String codeFormation, String anneeUniversitaire);
}
