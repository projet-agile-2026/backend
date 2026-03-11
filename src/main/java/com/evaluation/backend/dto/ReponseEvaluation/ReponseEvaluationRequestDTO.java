package com.evaluation.backend.dto.ReponseEvaluation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReponseEvaluationRequestDTO {
    
    private Long idEvaluation;
    private String commentaire;
    private List<ReponseQuestionDTO> reponses;
    
    @Getter
    @Setter
    @Builder
    public static class ReponseQuestionDTO {
        private Long idQuestionEvaluation;
        private Long positionnement; // 1 à 5
    }
}
