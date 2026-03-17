package com.evaluation.backend.dto.Questionnaire;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateEvaluationFromQuestionnaireRequest {

    @NotNull
    private Long idQuestionnaire;

    @NotBlank
    private String codeFormation;

    @NotBlank
    private String anneeUniversitaire;

    @NotBlank
    private String codeUe;

    private String codeEc;

    @NotBlank
    private String designation;

    private String periode;

    @NotNull
    private LocalDate debutReponse;

    @NotNull
    private LocalDate finReponse;
}