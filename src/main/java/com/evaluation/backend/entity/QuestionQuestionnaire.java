package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "QUESTION_QUESTIONNAIRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class QuestionQuestionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_questionnaire_seq_gen")
    @SequenceGenerator(
            name = "question_questionnaire_seq_gen",
            sequenceName = "QQUE_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_QUESTION_QUESTIONNAIRE", nullable = false)
    private Long idQuestionQuestionnaire;

    @Column(name = "ID_RUBRIQUE_QUESTIONNAIRE", nullable = false)
    private Long idRubriqueQuestionnaire;

    @Column(name = "ID_QUALIFICATIF")
    private Long idQualificatif;

    @Column(name = "ID_QUESTION")
    private Long idQuestion;

    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;

    @Column(name = "INTITULE")
    private String intitule;

    /**
     * FK -> RUBRIQUE_QUESTIONNAIRE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RUBRIQUE_QUESTIONNAIRE", insertable = false, updatable = false)
    @ToString.Exclude
    private RubriqueQuestionnaire rubriqueQuestionnaire;

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
}