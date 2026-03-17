package com.evaluation.backend.repository;

import com.evaluation.backend.entity.RubriqueQuestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RubriqueQuestionnaireRepository extends JpaRepository<RubriqueQuestionnaire, Long> {

    @Query("""
    SELECT MAX(r.ordre)
    FROM RubriqueQuestionnaire r
    WHERE r.idQuestionnaire = :idQuestionnaire
""")
    Integer findMaxOrdreByQuestionnaire(@Param("idQuestionnaire") Long idQuestionnaire);
    List<RubriqueQuestionnaire> findByIdQuestionnaireOrderByOrdreAsc(Long idQuestionnaire);

}