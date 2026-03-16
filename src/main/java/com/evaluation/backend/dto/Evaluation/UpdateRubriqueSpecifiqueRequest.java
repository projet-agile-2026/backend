package com.evaluation.backend.dto.Evaluation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRubriqueSpecifiqueRequest {
    @NotBlank
    private String designation;
}