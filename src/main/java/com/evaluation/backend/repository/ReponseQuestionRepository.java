package com.evaluation.backend.repository;

import com.evaluation.backend.entity.ReponseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReponseQuestionRepository extends JpaRepository<ReponseQuestion, Void>, JpaSpecificationExecutor<ReponseQuestion> {
    
    @Modifying
    @Query("DELETE FROM ReponseQuestion rq WHERE rq.id.idReponseEvaluation = :idReponseEvaluation")
    void deleteByIdReponseEvaluation(@Param("idReponseEvaluation") Long idReponseEvaluation);

    @Query("SELECT rq FROM ReponseQuestion rq WHERE rq.id.idReponseEvaluation = :idReponseEvaluation")
    List<ReponseQuestion> findByIdReponseEvaluation(@Param("idReponseEvaluation") Long idReponseEvaluation);

}