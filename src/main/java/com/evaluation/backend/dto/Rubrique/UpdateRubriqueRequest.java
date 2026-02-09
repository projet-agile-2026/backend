package com.evaluation.backend.dto.Rubrique;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRubriqueRequest {

    @NotBlank(message = "Type is required")
    @Size(max = 10, message = "Type must not exceed 10 characters")
    private String type;

    private Long noEnseignant;

    @NotBlank(message = "Designation is required")
    @Size(max = 32, message = "Designation must not exceed 32 characters")
    private String designation;

    private Integer ordre;
}