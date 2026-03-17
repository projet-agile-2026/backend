package com.evaluation.backend.dto.Questionnaire;

import lombok.*;

import java.util.List;

@Builder
@Getter
public class QuestionnaireWithRubriquesDTO {

    private Long idQuestionnaire;

    private String designation;

    private List<RubriqueQuestionnaireDTO> rubriques;
}