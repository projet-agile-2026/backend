package com.evaluation.backend.dto.Droit;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DroitRequestDTO {

    @NotNull
    private Long noEnseignant;

    @NotNull
    private Boolean consultation;

    @NotNull
    private Boolean duplication;
}
