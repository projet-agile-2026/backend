package com.evaluation.backend.repository;

import com.evaluation.backend.entity.ReponseEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReponseEvaluationRepository extends JpaRepository<ReponseEvaluation, Long>, JpaSpecificationExecutor<ReponseEvaluation> {
    
    boolean existsByIdEvaluationAndNoEtudiant(Long idEvaluation, Long noEtudiant);
    
    Optional<ReponseEvaluation> findByIdEvaluationAndNoEtudiant(Long idEvaluation, Long noEtudiant);
    
    @Modifying
    @Query("DELETE FROM ReponseEvaluation re WHERE re.idEvaluation = :idEvaluation AND re.noEtudiant = :noEtudiant")
    void deleteByIdEvaluationAndNoEtudiant(@Param("idEvaluation") Long idEvaluation, @Param("noEtudiant") Long noEtudiant);
    
    @Query(value = "SELECT RPE_SEQ.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextId();

}