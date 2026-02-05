package com.evaluation.backend.dto;

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
    private Long noEnseignant;
    private String intitule;
    private Integer ordre;

    // Qualificatif details
    private Long idQualificatif;
    private String maximal;
    private String minimal;
}