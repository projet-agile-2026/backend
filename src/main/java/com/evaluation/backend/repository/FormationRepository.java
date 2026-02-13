package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, String> {

    @Query("select f.codeFormation from Formation f order by f.codeFormation")
    List<String> findAllCodeFormations();
}
