package com.evaluation.backend.dto.Evaluation;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionToRubriqueEvaluationRequest {

    @NotNull(message = "Question ID is required")
    private Long idQuestion;

    @NotNull(message = "Ordre is required")
    private Integer ordre;
}