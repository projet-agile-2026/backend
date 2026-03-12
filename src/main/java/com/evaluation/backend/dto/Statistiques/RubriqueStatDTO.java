package com.evaluation.backend.dto.Statistiques;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RubriqueStatDTO {

    private Long              idRubriqueEvaluation;
    private Integer           ordre;
    private String            designation;
    private List<QuestionStatDTO> questions;
}