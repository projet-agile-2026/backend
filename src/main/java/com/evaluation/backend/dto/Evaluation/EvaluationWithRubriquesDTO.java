package com.evaluation.backend.dto.Evaluation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class EvaluationWithRubriquesDTO {

    private Long idEvaluation;
    private Long noEnseignant;
    private String codeFormation;
    private String anneeUniversitaire;
    private String codeUe;
    private String codeEc;
    private Short noEvaluation;
    private String designation;
    private String etat;
    private String periode;
    private LocalDate debutReponse;
    private LocalDate finReponse;

    // Liste des rubriques avec leurs questions
    private List<RubriqueEvaluationDTO> rubriques;
}