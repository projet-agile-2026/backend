package com.evaluation.backend.dto.Evaluation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRubriqueSpecifiqueRequest {

    @NotBlank
    private String designation;

    private Integer ordre;
}