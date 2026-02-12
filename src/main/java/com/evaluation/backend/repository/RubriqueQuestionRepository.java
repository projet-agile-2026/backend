package com.evaluation.backend.repository;

import com.evaluation.backend.entity.RubriqueQuestion;
import com.evaluation.backend.entity.RubriqueQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RubriqueQuestionRepository extends JpaRepository<RubriqueQuestion, RubriqueQuestionId> {

    @Query("SELECT rq FROM RubriqueQuestion rq WHERE rq.idRubrique = :idRubrique ORDER BY rq.ordre ASC")
    List<RubriqueQuestion> findByIdRubriqueOrderByOrdreAsc(@Param("idRubrique") Long idRubrique);

    @Query("SELECT rq FROM RubriqueQuestion rq WHERE rq.idQuestion = :idQuestion")
    List<RubriqueQuestion> findByIdQuestion(@Param("idQuestion") Long idQuestion);

    @Query("SELECT MAX(rq.ordre) FROM RubriqueQuestion rq WHERE rq.idRubrique = :idRubrique")
    Integer findMaxOrdreByRubrique(@Param("idRubrique") Long idRubrique);

    @Query("SELECT rq FROM RubriqueQuestion rq WHERE rq.idRubrique = :idRubrique AND rq.idQuestion = :idQuestion")
    Optional<RubriqueQuestion> findByIdRubriqueAndIdQuestion(
            @Param("idRubrique") Long idRubrique,
            @Param("idQuestion") Long idQuestion);

    @Modifying
    @Query("DELETE FROM RubriqueQuestion rq WHERE rq.idRubrique = :idRubrique")
    void deleteByIdRubrique(@Param("idRubrique") Long idRubrique);

    @Modifying
    @Query("DELETE FROM RubriqueQuestion rq WHERE rq.idRubrique = :idRubrique AND rq.idQuestion = :idQuestion")
    void deleteByIdRubriqueAndIdQuestion(
            @Param("idRubrique") Long idRubrique,
            @Param("idQuestion") Long idQuestion);

    boolean existsByIdRubriqueAndIdQuestion(Long idRubrique, Long idQuestion);
}