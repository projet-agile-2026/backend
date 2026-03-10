package com.evaluation.backend.dto.Rubrique;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddQuestionToRubriqueRequest {

    @NotNull(message = "Question Id est requis")
    private Long idQuestion;

    @NotNull(message = "Ordre est requis")
    private Integer ordre;
}