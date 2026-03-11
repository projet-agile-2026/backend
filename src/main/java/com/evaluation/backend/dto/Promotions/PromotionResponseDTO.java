package com.evaluation.backend.dto.Promotions;

import lombok.Builder;
import lombok.Data;

import java.sql.Date;

@Data
@Builder
public class PromotionResponseDTO {

    private String codeFormation;
    private String anneeUniversitaire;
    private Integer nbEtudiantActuel;

    private Long noEnseignant;
    private String siglePromotion;
    private Integer nbMaxEtudiant;

    private Date dateReponseLp;
    private Date dateReponseLalp;
    private Date dateRentree;

    private String lieuRentree;
    private String processusStage;
    private String commentaire;

    private String enseignantNom;
    private String enseignantPrenom;

    private String nomFormation;
    private String diplome;
}
