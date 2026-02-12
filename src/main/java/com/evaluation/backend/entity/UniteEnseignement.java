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
@Table(name = "UNITE_ENSEIGNEMENT")
@IdClass(UniteEnseignementId.class)
public class UniteEnseignement implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "CODE_FORMATION", nullable = false)
    private String codeFormation;

    @Id
    @Column(name = "CODE_UE", nullable = false)
    private String codeUe;


    @Column(name = "NO_ENSEIGNANT", nullable = false)
    private String noEnseignant;

    @Column(name = "DESIGNATION", nullable = false)
    private String designation;

    @Column(name = "SEMESTRE", nullable = false)
    private String semestre;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NBH_CM")
    private Integer nbhCm;

    @Column(name = "NBH_TD")
    private Integer nbhTd;

    @Column(name = "NBH_TP")
    private Integer nbhTp;


    /**
     * FK UE_ENS_FK : NO_ENSEIGNANT -> ENSEIGNANT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_ENSEIGNANT", insertable = false, updatable = false)
    @ToString.Exclude
    private Enseignant enseignant;


}
