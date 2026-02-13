package com.evaluation.backend.repository;

import com.evaluation.backend.entity.ElementConstitutif;
import com.evaluation.backend.entity.ElementConstitutifId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ElementConstitutifRepository extends JpaRepository<ElementConstitutif, ElementConstitutifId> {


    @Query("""
           select distinct e.codeEc
           from ElementConstitutif e
           where e.codeFormation = :codeFormation
             and e.codeUe = :codeUe
           order by e.codeEc
           """)
    List<String> findDistinctEcsByFormationAndUe(@Param("codeFormation") String codeFormation,
                                                 @Param("codeUe") String codeUe);
}
