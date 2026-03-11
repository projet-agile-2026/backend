package com.evaluation.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReponseQuestionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "ID_REPONSE_EVALUATION", nullable = false)
    private Long idReponseEvaluation;

    @Column(name = "ID_QUESTION_EVALUATION", nullable = false)
    private Long idQuestionEvaluation;
}
