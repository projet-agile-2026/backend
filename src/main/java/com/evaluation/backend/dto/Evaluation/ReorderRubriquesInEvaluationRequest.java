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
public class ReorderRubriquesInEvaluationRequest {

    @NotEmpty(message = "Rubrique order list cannot be empty")
    private List<RubriqueEvaluationOrder> rubriqueOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RubriqueEvaluationOrder {
        @NotNull(message = "Rubrique Evaluation ID is required")
        private Long idRubriqueEvaluation;

        @NotNull(message = "Ordre is required")
        private Integer ordre;
    }
}