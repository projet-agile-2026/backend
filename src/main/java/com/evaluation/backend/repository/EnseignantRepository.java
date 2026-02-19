package com.evaluation.backend.repository;

import com.evaluation.backend.dto.Promotions.EnseignantLightDTO;
import com.evaluation.backend.entity.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnseignantRepository extends JpaRepository<Enseignant, Integer> {

    @Query("select e.id from Enseignant e")
    List<Integer> findAllIds();


    @Query("""
        select new com.evaluation.backend.dto.Promotions.EnseignantLightDTO(
            e.id,
            e.nom,
            e.prenom,
            e.emailUbo
        )
        from Enseignant e
        order by e.nom, e.prenom
    """)
    List<EnseignantLightDTO> findAllLight();
}
