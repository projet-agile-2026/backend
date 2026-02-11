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
@Table(name = "DROIT")
@IdClass(DroitId.class)
public class Droit implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "ID_EVALUATION", nullable = false)
    private Long idEvaluation;

    @Id
    @Column(name = "NO_ENSEIGNANT", nullable = false)
    private Long noEnseignant;



    @Column(name = "CONSULTATION", nullable = false)
    private String consultation;

    @Column(name = "DUPLICATION", nullable = false)
    private String duplication;



    /**
     * FK ID_EVALUATION -> EVALUATION
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVALUATION", insertable = false, updatable = false)
    @ToString.Exclude
    private Evaluation evaluation;

    /**
     * FK NO_ENSEIGNANT -> ENSEIGNANT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_ENSEIGNANT", insertable = false, updatable = false)
    @ToString.Exclude
    private Enseignant enseignant;
}
