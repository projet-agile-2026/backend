package com.evaluation.backend.dto.Questionnaire;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddRubriqueToQuestionnaireRequest {

    @NotNull(message = "Rubrique ID is required")
    private Long idRubrique;

    private Integer ordre;

    private String designation;
}