package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FormationRepository extends JpaRepository<Formation, String>, JpaSpecificationExecutor<Formation> {

}