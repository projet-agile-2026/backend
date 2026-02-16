package com.evaluation.backend.repository;

import com.evaluation.backend.entity.QuestionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionEvaluationRepository extends JpaRepository<QuestionEvaluation, Long> {

    @Query("SELECT qe FROM QuestionEvaluation qe WHERE qe.idRubriqueEvaluation = :idRubriqueEvaluation ORDER BY qe.ordre ASC")
    List<QuestionEvaluation> findByIdRubriqueEvaluationOrderByOrdreAsc(@Param("idRubriqueEvaluation") Long idRubriqueEvaluation);

    boolean existsByIdRubriqueEvaluationAndIdQuestion(Long idRubriqueEvaluation, Long idQuestion);
}