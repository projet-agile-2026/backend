package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    List<Etudiant> findByCodeFormationAndAnneeUniversitaire(String codeFormation, String anneeUniversitaire);

    boolean existsByCodeFormationAndAnneeUniversitaire(String codeFormation, String anneeUniversitaire);

    int countByCodeFormationAndAnneeUniversitaire(String codeFormation, String anneeUniversitaire);
}
