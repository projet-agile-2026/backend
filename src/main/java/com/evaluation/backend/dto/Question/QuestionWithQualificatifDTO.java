package com.evaluation.backend.dto.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithQualificatifDTO implements Serializable {

    private Long idQuestion;
    private String type;
    private String noEnseignant;
    private String intitule;
    private Integer ordre;  // Set by RubriqueService (ordre within rubrique)

    // Qualificatif details
    private Long idQualificatif;
    private String maximal;  // mot1
    private String minimal;  // mot2
}