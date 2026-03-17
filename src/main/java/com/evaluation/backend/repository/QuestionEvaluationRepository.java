package com.evaluation.backend.repository;

import com.evaluation.backend.entity.QuestionEvaluation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;

@Repository
public interface QuestionEvaluationRepository extends JpaRepository<QuestionEvaluation, Long> {

    @Query("SELECT qe FROM QuestionEvaluation qe WHERE qe.idRubriqueEvaluation = :idRubriqueEvaluation ORDER BY qe.ordre ASC")
    List<QuestionEvaluation> findByIdRubriqueEvaluationOrderByOrdreAsc(@Param("idRubriqueEvaluation") Long idRubriqueEvaluation);

    boolean existsByIdRubriqueEvaluationAndIdQuestion(Long idRubriqueEvaluation, Long idQuestion);

    @Query("SELECT MAX(qe.ordre) FROM QuestionEvaluation qe WHERE qe.idRubriqueEvaluation = :idRubriqueEvaluation")
    Integer findMaxOrdreByRubriqueEvaluation(@Param("idRubriqueEvaluation") Long idRubriqueEvaluation);


    @Modifying
    @Transactional
    @Query(value = "UPDATE question_evaluation SET id_qualificatif = :idQualificatif WHERE id_question_evaluation = :id", nativeQuery = true)
    void updateQualificatifOnly(@Param("id") Long id, @Param("idQualificatif") Long idQualificatif);
}