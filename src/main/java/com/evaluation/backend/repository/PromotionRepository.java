package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Promotion;
import com.evaluation.backend.entity.PromotionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, PromotionId> {

    @Query("""
        SELECT p
        FROM Promotion p
        JOIN Formation f ON f.codeFormation = p.codeFormation
        WHERE (:anneeUniversitaire IS NULL OR p.anneeUniversitaire = :anneeUniversitaire)
          AND (:diplome IS NULL OR f.diplome = :diplome)
          AND (:nomFormation IS NULL OR LOWER(f.nomFormation) LIKE LOWER(CONCAT('%', :nomFormation, '%')))
        ORDER BY p.anneeUniversitaire DESC, p.codeFormation ASC
    """)
    List<Promotion> search(
            @Param("anneeUniversitaire") String anneeUniversitaire,
            @Param("diplome") String diplome,
            @Param("nomFormation") String nomFormation
    );
}
