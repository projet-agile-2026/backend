package com.evaluation.backend.repository;

import com.evaluation.backend.entity.CgRefCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface CgRefCodesRepository extends JpaRepository<CgRefCodes, String>, JpaSpecificationExecutor<CgRefCodes> {
    List<CgRefCodes> findByRvDomainOrderByRvMeaning(String rvDomain);

}