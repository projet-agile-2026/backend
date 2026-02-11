package com.evaluation.backend.repository;

import com.evaluation.backend.entity.UniteEnseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UniteEnseignementRepository extends JpaRepository<UniteEnseignement, String>, JpaSpecificationExecutor<UniteEnseignement> {

}