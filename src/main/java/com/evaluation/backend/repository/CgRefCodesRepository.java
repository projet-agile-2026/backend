package com.evaluation.backend.repository;

import com.evaluation.backend.entity.CgRefCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CgRefCodesRepository extends JpaRepository<CgRefCodes, String>, JpaSpecificationExecutor<CgRefCodes> {

}