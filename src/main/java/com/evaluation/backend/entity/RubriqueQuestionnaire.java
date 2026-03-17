package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "RUBRIQUE_QUESTIONNAIRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RubriqueQuestionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rubrique_questionnaire_seq_gen")
    @SequenceGenerator(
            name = "rubrique_questionnaire_seq_gen",
            sequenceName = "RQUE_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_RUBRIQUE_QUESTIONNAIRE", nullable = false)
    private Long idRubriqueQuestionnaire;

    @Column(name = "ID_QUESTIONNAIRE", nullable = false)
    private Long idQuestionnaire;

    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;

    @Column(name = "ID_RUBRIQUE", nullable = false)
    private Long idRubrique;

    @Column(name = "DESIGNATION")
    private String designation;

    /**
     * FK -> QUESTIONNAIRE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_QUESTIONNAIRE", insertable = false, updatable = false)
    @ToString.Exclude
    private Questionnaire questionnaire;

    /**
     * FK -> RUBRIQUE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RUBRIQUE", insertable = false, updatable = false)
    @ToString.Exclude
    private Rubrique rubrique;

    /**
     * Une rubrique contient plusieurs questions
     */
    @OneToMany(mappedBy = "rubriqueQuestionnaire", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<QuestionQuestionnaire> questionsQuestionnaire;
}