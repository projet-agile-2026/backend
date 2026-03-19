package com.evaluation.backend.dto.Evaluation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderQuestionsInRubriqueEvaluationRequest {

    @NotEmpty(message = "Question order list cannot be empty")
    private List<QuestionEvaluationOrder> questionOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionEvaluationOrder {
        @NotNull(message = "Question Evaluation ID is required")
            private Long idQuestionEvaluation;

        @NotNull(message = "Ordre is required")
        private Integer ordre;
    }
}