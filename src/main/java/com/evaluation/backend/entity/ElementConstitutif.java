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
@Table(name = "ELEMENT_CONSTITUTIF")
@IdClass(ElementConstitutifId.class)
public class ElementConstitutif implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== Clé primaire composite =====

    @Id
    @Column(name = "CODE_FORMATION", nullable = false)
    private String codeFormation;

    @Id
    @Column(name = "CODE_UE", nullable = false)
    private String codeUe;

    @Id
    @Column(name = "CODE_EC", nullable = false)
    private String codeEc;

    // ===== Colonnes métier =====

    @Column(name = "NO_ENSEIGNANT", nullable = false)
    private String noEnseignant;

    @Column(name = "DESIGNATION", nullable = false)
    private String designation;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NBH_CM")
    private Integer nbhCm;

    @Column(name = "NBH_TD")
    private Integer nbhTd;

    @Column(name = "NBH_TP")
    private Integer nbhTp;

    // Relations JPA

    /**
     * FK (CODE_FORMATION, CODE_UE) -> UNITE_ENSEIGNEMENT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CODE_FORMATION", insertable = false, updatable = false),
            @JoinColumn(name = "CODE_UE", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private UniteEnseignement uniteEnseignement;

    /**
     * FK NO_ENSEIGNANT -> ENSEIGNANT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_ENSEIGNANT", insertable = false, updatable = false)
    @ToString.Exclude
    private Enseignant enseignant;
}
