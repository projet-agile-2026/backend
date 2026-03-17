package com.evaluation.backend.dto.Questionnaire;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
public class RubriqueQuestionnaireDTO {

    private Long idRubriqueQuestionnaire;

    private Long idQuestionnaire;

    private Long idRubrique;

    private Integer ordre;

    private String designation;

    private String type;

    private List<QuestionWithQualificatifDTO> questions;
}