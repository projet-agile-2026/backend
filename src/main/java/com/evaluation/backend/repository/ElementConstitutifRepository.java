package com.evaluation.backend.repository;

import com.evaluation.backend.entity.ElementConstitutif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ElementConstitutifRepository extends JpaRepository<ElementConstitutif, String>, JpaSpecificationExecutor<ElementConstitutif> {

}