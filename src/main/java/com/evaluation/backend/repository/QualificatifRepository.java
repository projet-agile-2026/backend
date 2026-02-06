package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Qualificatif;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificatifRepository extends JpaRepository<Qualificatif, Long> {

    boolean existsByMaximalIgnoreCaseAndMinimalIgnoreCase(String maximal, String minimal);

    boolean existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
            String maximal,
            String minimal,
            Long idQualificatif
    );
}
