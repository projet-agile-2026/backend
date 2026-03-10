package com.evaluation.backend.dto.Rubrique;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubriqueDTO implements Serializable {

    private Long idRubrique;

    @NotBlank(message = "Le type est requis")
    @Size(max = 10, message = "Le type doit comporter au maximum 10 caractères")
    private String type;

    private Long noEnseignant;

    @NotBlank(message = "Designation est requise")
    @Size(max = 32, message = "La designation doit comporter au maximum 32 caractères")
    private String designation;

    private Integer ordre;

    private boolean usedInEval;

    // Include questions with qualificatifs when fetching rubrique details
    private List<QuestionWithQualificatifDTO> questions;
}