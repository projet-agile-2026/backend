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
@Table(name = "RUBRIQUE_EVALUATION")
public class RubriqueEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_RUBRIQUE_EVALUATION", nullable = false)
    private String idRubriqueEvaluation;

    @Column(name = "ID_EVALUATION", nullable = false)
    private String idEvaluation;

    @Column(name = "ID_RUBRIQUE")
    private String idRubrique;

    @Column(name = "ORDRE", nullable = false)
    private String ordre;

    @Column(name = "DESIGNATION")
    private String designation;

}
