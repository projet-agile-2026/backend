package com.evaluation.backend.dto.Evaluation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EvaluationResponseDTO {

    private Long idEvaluation;

    private Long noEnseignant;
    private String nomEnseignant;
    private String prenomEnseignant;
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
    
    private Boolean dejaRepondu;
}
