package com.evaluation.backend.repository;

import com.evaluation.backend.entity.UniteEnseignement;
import com.evaluation.backend.entity.UniteEnseignementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UniteEnseignementRepository extends JpaRepository<UniteEnseignement, UniteEnseignementId> {

    @Query("""
           select distinct ue.codeUe
           from UniteEnseignement ue
           where ue.codeFormation = :codeFormation
           order by ue.codeUe
           """)
    List<String> findCodeUeByCodeFormation(@Param("codeFormation") String codeFormation);
}
