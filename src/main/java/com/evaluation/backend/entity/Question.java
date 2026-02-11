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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_generator")
    @SequenceGenerator(
            name = "question_generator",
            sequenceName = "QUE_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_QUESTION", nullable = false)
    private Long idQuestion;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Column(name = "NO_ENSEIGNANT")
    private String noEnseignant;

    @Column(name = "ID_QUALIFICATIF", nullable = false)
    private String idQualificatif;

    @Column(name = "INTITULE", nullable = false)
    private String intitule;



    /**
     * FK QUE_QUA_FK : ID_QUALIFICATIF -> QUALIFICATIF(ID_QUALIFICATIF)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_QUALIFICATIF", insertable = false, updatable = false)
    @ToString.Exclude
    private Qualificatif qualificatif;
}
