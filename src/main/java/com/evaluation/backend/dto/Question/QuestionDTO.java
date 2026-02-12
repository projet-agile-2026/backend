package com.evaluation.backend.dto.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private Long idQuestion;

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    private String noEnseignant;

    @NotBlank(message = "L'identifiant du qualificatif est obligatoire")
    private String idQualificatif;

    @NotBlank(message = "L'intitulé est obligatoire")
    private String intitule;

    private boolean usedInRubrique;

    public QuestionDTO(Long idQuestion, String type, String noEnseignant, String idQualificatif, String intitule) {
        this.idQuestion = idQuestion;
        this.type = type;
        this.noEnseignant = noEnseignant;
        this.idQualificatif = idQualificatif;
        this.intitule = intitule;
    }
}