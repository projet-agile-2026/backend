package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "RUBRIQUE_EVALUATION")
public class RubriqueEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rubrique_eval_seq_gen")
    @SequenceGenerator(
            name = "rubrique_eval_seq_gen",
            sequenceName = "REV_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_RUBRIQUE_EVALUATION", nullable = false)
    private Long idRubriqueEvaluation;


    @Column(name = "ID_EVALUATION", nullable = false)
    private Long idEvaluation;

    @Column(name = "ID_RUBRIQUE")
    private Long idRubrique;


    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;

    @Column(name = "DESIGNATION")
    private String designation;



    /**
     * FK -> EVALUATION(ID_EVALUATION)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVALUATION", insertable = false, updatable = false)
    @ToString.Exclude
    private Evaluation evaluation;

    /**
     * FK -> RUBRIQUE(ID_RUBRIQUE)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RUBRIQUE", insertable = false, updatable = false)
    @ToString.Exclude
    private Rubrique rubrique;

    /**
     * Référence inverse : une rubrique_evaluation contient plusieurs question_evaluation
     */
    @OneToMany(mappedBy = "rubriqueEvaluation", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<QuestionEvaluation> questionsEvaluation;
}
