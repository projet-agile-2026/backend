package com.evaluation.backend.repository;

import com.evaluation.backend.entity.QuestionQuestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionQuestionnaireRepository extends JpaRepository<QuestionQuestionnaire, Long> {

    @Query("""
        SELECT MAX(q.ordre)
        FROM QuestionQuestionnaire q
        WHERE q.idRubriqueQuestionnaire = :idRubriqueQuestionnaire
    """)
    Integer findMaxOrdreByRubriqueQuestionnaire(@Param("idRubriqueQuestionnaire") Long idRubriqueQuestionnaire);

    List<QuestionQuestionnaire> findByIdRubriqueQuestionnaireOrderByOrdreAsc(Long idRubriqueQuestionnaire);

    boolean existsByIdRubriqueQuestionnaire(Long idRubriqueQuestionnaire);

}
