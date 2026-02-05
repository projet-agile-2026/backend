package com.evaluation.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubriqueQuestionDTO implements Serializable {

    @NotNull(message = "Rubrique ID is required")
    private Long idRubrique;

    @NotNull(message = "Question ID is required")
    private Long idQuestion;

    @NotNull(message = "Ordre is required")
    private Integer ordre;
}