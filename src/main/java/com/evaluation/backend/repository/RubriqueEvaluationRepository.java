package com.evaluation.backend.repository;

import com.evaluation.backend.entity.RubriqueEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RubriqueEvaluationRepository extends JpaRepository<RubriqueEvaluation, Long> {

    @Query("SELECT re FROM RubriqueEvaluation re WHERE re.idEvaluation = :idEvaluation ORDER BY re.ordre ASC")
    List<RubriqueEvaluation> findByIdEvaluationOrderByOrdreAsc(@Param("idEvaluation") Long idEvaluation);

    @Query("SELECT MAX(re.ordre) FROM RubriqueEvaluation re WHERE re.idEvaluation = :idEvaluation")
    Integer findMaxOrdreByEvaluation(@Param("idEvaluation") Long idEvaluation);

    boolean existsByIdEvaluationAndIdRubrique(Long idEvaluation, Long idRubrique);

    @Modifying
    @Query("DELETE FROM RubriqueEvaluation re WHERE re.idEvaluation = :idEvaluation AND re.idRubriqueEvaluation = :idRubriqueEvaluation")
    void deleteByIdEvaluationAndIdRubriqueEvaluation(
            @Param("idEvaluation") Long idEvaluation,
            @Param("idRubriqueEvaluation") Long idRubriqueEvaluation);

    @Modifying
    @Query("DELETE FROM RubriqueEvaluation re WHERE re.idEvaluation = :idEvaluation")
    void deleteByIdEvaluation(@Param("idEvaluation") Long idEvaluation);

    Optional<RubriqueEvaluation> findByIdEvaluationAndIdRubriqueEvaluation(Long idEvaluation, Long idRubriqueEvaluation);
}