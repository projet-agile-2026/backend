package com.evaluation.backend.dto.ReponseEvaluation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class ReponseEvaluationResultDTO {
    
    private Long idEvaluation;
    private String nomEnseignant;
    private String prenomEnseignant;
    private String codeUe;
    private String codeEc;
    private Short noEvaluation;
    private String designation;
    private String etat;
    private LocalDate finReponse;
    private String commentaire;
    private List<RubriqueResultDTO> rubriques;
    
    @Getter
    @Setter
    @Builder
    public static class RubriqueResultDTO {
        private Long idRubriqueEvaluation;
        private String designation;
        private Integer ordre;
        private List<QuestionResultDTO> questions;
    }
    
    @Getter
    @Setter
    @Builder
    public static class QuestionResultDTO {
        private Long idQuestionEvaluation;
        private String intitule;
        private Integer ordre;
        private String minimal;
        private String maximal;
        private Long positionnement; // La réponse de l'étudiant (1-5)
    }
}
