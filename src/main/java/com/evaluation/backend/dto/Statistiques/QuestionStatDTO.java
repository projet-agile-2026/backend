package com.evaluation.backend.dto.Statistiques;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QuestionStatDTO {

    private Long    idQuestionEvaluation;
    private Integer ordre;
    private String  intitule;

    // Qualificatifs
    private String  minimal;
    private String  maximal;

    // Agrégats
    private Long    nbRepondants;
    private Double  moyenne;
    private Long    minimum;
    private Long    maximum;
    private Double  ecartType;
    private Double  mediane;

    // Distribution 1 → 5
    private Long    nb1;
    private Long    nb2;
    private Long    nb3;
    private Long    nb4;
    private Long    nb5;
}