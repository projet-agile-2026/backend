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
public class AddRubriqueToEvaluationRequest {

    @NotNull(message = "Rubrique ID is required")
    private Long idRubrique;

    @NotNull(message = "Ordre is required")
    private Integer ordre;

    // Optionnel : pour les rubriques composées personnalisées
    private String designation;
}