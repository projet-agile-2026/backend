package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnseignantRepository extends JpaRepository<Enseignant, Integer> {

    @Query("select e.id from Enseignant e")
    List<Integer> findAllIds();
}
