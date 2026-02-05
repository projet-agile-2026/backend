package com.evaluation.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "QUESTION_EVALUATION")
public class QuestionEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_QUESTION_EVALUATION", nullable = false)
    private String idQuestionEvaluation;

    @Column(name = "ID_RUBRIQUE_EVALUATION", nullable = false)
    private String idRubriqueEvaluation;

    @Column(name = "ID_QUESTION")
    private String idQuestion;

    @Column(name = "ID_QUALIFICATIF")
    private String idQualificatif;

    @Column(name = "ORDRE", nullable = false)
    private String ordre;

    @Column(name = "INTITULE")
    private String intitule;

}
