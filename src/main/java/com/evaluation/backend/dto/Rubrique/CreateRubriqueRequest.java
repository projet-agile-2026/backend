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
public class CreateRubriqueRequest {

    @NotBlank(message = "Le type est requis")
    @Size(max = 10, message = "Le type doit comporter au maximum 10 caractères")
    private String type;

    private Long noEnseignant;

    @NotBlank(message = "Designation est requise")
    @Size(max = 32, message = "La designation doit comporter au maximum 32 caractères")
    private String designation;

    private Integer ordre;
}