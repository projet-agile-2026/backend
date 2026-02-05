package com.evaluation.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO implements Serializable {

    private Long idQuestion;

    @Size(max = 10, message = "Type must not exceed 10 characters")
    private String type;

    private Long noEnseignant;

    @NotNull(message = "Qualificatif ID is required")
    private Long idQualificatif;

    @NotBlank(message = "Intitule is required")
    @Size(max = 64, message = "Intitule must not exceed 64 characters")
    private String intitule;

    // Include qualificatif details in response
    private QualificatifDTO qualificatif;
}