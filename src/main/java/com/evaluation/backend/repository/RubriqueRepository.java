package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Rubrique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RubriqueRepository extends JpaRepository<Rubrique, Long> {

    List<Rubrique> findByTypeOrderByOrdreAsc(String type);

    List<Rubrique> findByNoEnseignant(Long noEnseignant);

    @Query("SELECT r FROM Rubrique r ORDER BY r.ordre ASC")
    List<Rubrique> findAllOrderByOrdre();

    Optional<Rubrique> findByDesignationAndType(String designation, String type);

    @Query("SELECT MAX(r.ordre) FROM Rubrique r WHERE r.type = :type")
    Integer findMaxOrdreByType(@Param("type") String type);

    /**
     * Pour vérifier l'unicité d'une rubrique personnelle (RBP) pour un enseignant spécifique.
     */
    Optional<Rubrique> findByDesignationAndTypeAndNoEnseignant(String designation, String type, Long noEnseignant);

    /**
     * La requête "Maître" pour récupérer RBS + RBP d'un enseignant en un seul appel SQL.
     * C'est beaucoup plus performant que de faire un .findAll() puis un stream filter.
     */
    @Query("SELECT r FROM Rubrique r WHERE r.type = 'RBS' " +
           "OR (r.type = 'RBP' AND r.noEnseignant = :noEnseignant) " +
           "ORDER BY r.ordre ASC")
    List<Rubrique> findForEnseignant(@Param("noEnseignant") Long noEnseignant);
}