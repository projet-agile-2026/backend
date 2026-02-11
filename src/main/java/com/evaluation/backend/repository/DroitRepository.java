package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Droit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DroitRepository extends JpaRepository<Droit, String>, JpaSpecificationExecutor<Droit> {

}