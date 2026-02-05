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
@Table(name = "QUESTION")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_seq")
    @SequenceGenerator(name = "question_seq", sequenceName = "QUE_SEQ", allocationSize = 1)
    @Column(name = "ID_QUESTION", nullable = false)
    private Long idQuestion;

    @Column(name = "TYPE", nullable = false, length = 10)
    private String type;

    @Column(name = "NO_ENSEIGNANT")
    private Long noEnseignant;

    @Column(name = "ID_QUALIFICATIF", nullable = false)
    private Long idQualificatif;

    @Column(name = "INTITULE", nullable = false, length = 64)
    private String intitule;


    // JPA Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_QUALIFICATIF", insertable = false, updatable = false)
    private Qualificatif qualificatif;

}