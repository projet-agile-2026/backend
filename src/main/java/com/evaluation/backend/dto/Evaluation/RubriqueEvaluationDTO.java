package com.evaluation.backend.dto.Evaluation;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubriqueEvaluationDTO {

    private Long idRubriqueEvaluation;
    private Long idEvaluation;
    private Long idRubrique;
    private Integer ordre;
    private String designation;

    // Informations de la rubrique source (si applicable)
    private String type; // RBS, RBP, ou null si rubrique composée personnalisée

    // Questions de cette rubrique dans l'évaluation
    private List<QuestionWithQualificatifDTO> questions;
}