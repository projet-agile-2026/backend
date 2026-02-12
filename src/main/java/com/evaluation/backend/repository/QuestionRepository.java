package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByType(String type);

    // ✅ CHANGED: Long → String (because Question.noEnseignant is String)
    List<Question> findByNoEnseignant(String noEnseignant);

    // ✅ CHANGED: Long → String (because Question.idQualificatif is String)
    @Query("SELECT q FROM Question q WHERE q.idQualificatif = :idQualificatif")
    List<Question> findByQualificatifId(@Param("idQualificatif") String idQualificatif);
}