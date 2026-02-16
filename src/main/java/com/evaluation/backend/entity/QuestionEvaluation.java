package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "QUESTION_EVALUATION")
public class QuestionEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_eval_seq_gen")
    @SequenceGenerator(
            name = "question_eval_seq_gen",
            sequenceName = "QEV_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_QUESTION_EVALUATION", nullable = false)
    private Long idQuestionEvaluation;


    @Column(name = "ID_RUBRIQUE_EVALUATION", nullable = false)
    private Long idRubriqueEvaluation;

    @Column(name = "ID_QUESTION")
    private Long idQuestion;

    @Column(name = "ID_QUALIFICATIF")
    private Long idQualificatif;


    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;

    @Column(name = "INTITULE")
    private String intitule;



    /**
     * FK -> RUBRIQUE_EVALUATION
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RUBRIQUE_EVALUATION", insertable = false, updatable = false)
    @ToString.Exclude
    private RubriqueEvaluation rubriqueEvaluation;

    /**
     * FK -> QUESTION
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_QUESTION", insertable = false, updatable = false)
    @ToString.Exclude
    private Question question;

    /**
     * FK -> QUALIFICATIF
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_QUALIFICATIF", insertable = false, updatable = false)
    @ToString.Exclude
    private Qualificatif qualificatif;


    /* on gere le XOR entre questiion et qualif dans le front */
}
