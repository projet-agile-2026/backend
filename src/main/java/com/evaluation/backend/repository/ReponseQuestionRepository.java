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

    @Query(value = """
        SELECT
            qe.ID_QUESTION_EVALUATION                               AS idQuestionEvaluation,
            qe.ORDRE                                                AS ordre,
            qe.INTITULE                                             AS intitule,
            qe.ID_QUALIFICATIF                                      AS idQualificatif,
            COUNT(rq.POSITIONNEMENT)                                AS nbRepondants,
            ROUND(AVG(rq.POSITIONNEMENT), 2)                        AS moyenne,
            MIN(rq.POSITIONNEMENT)                                  AS minimum,
            MAX(rq.POSITIONNEMENT)                                  AS maximum,
            ROUND(STDDEV(rq.POSITIONNEMENT), 2)                     AS ecartType,
            PERCENTILE_CONT(0.5)
                WITHIN GROUP (ORDER BY rq.POSITIONNEMENT)           AS mediane,
            SUM(CASE WHEN rq.POSITIONNEMENT = 1 THEN 1 ELSE 0 END)  AS nb1,
            SUM(CASE WHEN rq.POSITIONNEMENT = 2 THEN 1 ELSE 0 END)  AS nb2,
            SUM(CASE WHEN rq.POSITIONNEMENT = 3 THEN 1 ELSE 0 END)  AS nb3,
            SUM(CASE WHEN rq.POSITIONNEMENT = 4 THEN 1 ELSE 0 END)  AS nb4,
            SUM(CASE WHEN rq.POSITIONNEMENT = 5 THEN 1 ELSE 0 END)  AS nb5
        FROM QUESTION_EVALUATION qe
        JOIN RUBRIQUE_EVALUATION rev
            ON rev.ID_RUBRIQUE_EVALUATION = qe.ID_RUBRIQUE_EVALUATION
        LEFT JOIN REPONSE_EVALUATION re
            ON re.ID_EVALUATION = rev.ID_EVALUATION
        LEFT JOIN REPONSE_QUESTION rq
            ON  rq.ID_REPONSE_EVALUATION  = re.ID_REPONSE_EVALUATION
            AND rq.ID_QUESTION_EVALUATION = qe.ID_QUESTION_EVALUATION
        WHERE rev.ID_EVALUATION = :idEvaluation
        GROUP BY
            qe.ID_QUESTION_EVALUATION,
            qe.ORDRE,
            qe.INTITULE,
            qe.ID_QUALIFICATIF
        ORDER BY
            MIN(rev.ORDRE) ASC,
            qe.ORDRE       ASC
        """, nativeQuery = true)
    List<Object[]> findRawStatsByEvaluation(@Param("idEvaluation") Long idEvaluation);

    @Query(value = """
        SELECT COUNT(*)
        FROM REPONSE_EVALUATION
        WHERE ID_EVALUATION = :idEvaluation
        """, nativeQuery = true)
    Long countRepondantsByEvaluation(@Param("idEvaluation") Long idEvaluation);
}