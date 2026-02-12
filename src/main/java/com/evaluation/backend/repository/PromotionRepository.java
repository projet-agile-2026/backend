package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PromotionRepository extends JpaRepository<Promotion, String>, JpaSpecificationExecutor<Promotion> {

}