package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Droit;
import com.evaluation.backend.entity.DroitId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroitRepository extends JpaRepository<Droit, DroitId> {

    List<Droit> findByNoEnseignant(Long noEnseignant);

    List<Droit> findByIdEvaluation(Long idEvaluation);

    Optional<Droit> findByIdEvaluationAndNoEnseignant(Long idEvaluation, Long noEnseignant);
}
