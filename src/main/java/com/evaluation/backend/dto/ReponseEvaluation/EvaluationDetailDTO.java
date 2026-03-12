package com.evaluation.backend.dto.ReponseEvaluation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class EvaluationDetailDTO {
    
    private Long idEvaluation;
    private String nomEnseignant;
    private String prenomEnseignant;
    private String codeUe;
    private String codeEc;
    private Short noEvaluation;
    private String designation;
    private String etat;
    private LocalDate finReponse;
    private List<RubriqueDetailDTO> rubriques;
    
    @Getter
    @Setter
    @Builder
    public static class RubriqueDetailDTO {
        private Long idRubriqueEvaluation;
        private String designation;
        private Integer ordre;
        private List<QuestionDetailDTO> questions;
    }
    
    @Getter
    @Setter
    @Builder
    public static class QuestionDetailDTO {
        private Long idQuestionEvaluation;
        private String intitule;
        private Integer ordre;
        private String minimal;
        private String maximal;
    }
}
