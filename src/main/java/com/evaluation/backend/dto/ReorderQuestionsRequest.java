package com.evaluation.backend.dto;

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
public class ReorderQuestionsRequest {

    @NotEmpty(message = "Question order list cannot be empty")
    private List<QuestionOrder> questionOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOrder {
        @NotNull(message = "Question ID is required")
        private Long idQuestion;

        @NotNull(message = "Ordre is required")
        private Integer ordre;
    }
}