package com.evaluation.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "QUESTION")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
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

}
