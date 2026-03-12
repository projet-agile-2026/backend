package com.evaluation.backend.dto.Statistiques;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class StatistiquesEvaluationDTO {

    // Métadonnées de l'évaluation
    private Long      idEvaluation;
    private String    designation;
    private String    codeFormation;
    private String    anneeUniversitaire;
    private String    codeUe;
    private String    codeEc;
    private Short     noEvaluation;
    private String    etat;
    private String    periode;
    private LocalDate debutReponse;
    private LocalDate finReponse;
    private String emailEnseignant;

    // Statistiques globales
    private Long      totalRepondants;

    // Rubriques avec leurs questions et stats
    private List<RubriqueStatDTO> rubriques;
}
