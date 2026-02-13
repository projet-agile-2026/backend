package com.evaluation.backend.dto.Promotions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Date;

@Data
public class PromotionCreateUpdateDTO {

    @NotBlank
    private String codeFormation;

    @NotBlank
    private String anneeUniversitaire;

    private Long noEnseignant;

    private String siglePromotion;

    @NotNull
    private Integer nbMaxEtudiant;

    private Date dateReponseLp;
    private Date dateReponseLalp;
    private Date dateRentree;

    private String lieuRentree;
    private String processusStage;
    private String commentaire;
}
