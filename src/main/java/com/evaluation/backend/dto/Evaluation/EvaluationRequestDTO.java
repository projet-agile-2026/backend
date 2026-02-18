package com.evaluation.backend.dto.Evaluation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EvaluationRequestDTO {

    @NotBlank
    private String codeFormation;

    @NotBlank
    private String anneeUniversitaire;

    @NotBlank
    private String codeUe;

    @NotBlank
    private String codeEc;

    @NotNull
    private Short noEvaluation;

    @NotBlank
    private String designation;


    @NotBlank
    private String etat;

    @NotBlank
    private String periode;

    private LocalDate debutReponse;
    private LocalDate finReponse;
}
