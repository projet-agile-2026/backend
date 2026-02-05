package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionRepository extends JpaRepository<Question, String>, JpaSpecificationExecutor<Question> {

}