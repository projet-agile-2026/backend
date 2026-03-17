package com.evaluation.backend.dto.Questionnaire;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionToRubriqueQuestionnaireRequest {

    @NotNull
    private Long idQuestion;

    private Long idQualificatif;

    private Integer ordre;
}