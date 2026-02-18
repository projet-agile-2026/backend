package com.evaluation.backend.dto.Droit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DroitResponseDTO {

    private Long idEvaluation;
    private Long noEnseignant;

    // valeurs BD : "O" / "N"
    private String consultation;
    private String duplication;
}
