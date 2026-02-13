package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EVALUATION")
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evaluation_seq_gen")
    @SequenceGenerator(
            name = "evaluation_seq_gen",
            sequenceName = "EVALUATION_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_EVALUATION", nullable = false)
    private Long idEvaluation;



    @Column(name = "NO_ENSEIGNANT", nullable = false)
    private Long noEnseignant;

    @Column(name = "CODE_FORMATION", nullable = false)
    private String codeFormation;

    @Column(name = "ANNEE_UNIVERSITAIRE", nullable = false)
    private String anneeUniversitaire;

    @Column(name = "CODE_UE", nullable = false)
    private String codeUe;

    @Column(name = "CODE_EC", nullable = false)
    private String codeEc;

    @Column(name = "NO_EVALUATION", nullable = false)
    private Short noEvaluation; // si VARCHAR en BD -> String

    @Column(name = "DESIGNATION", nullable = false)
    private String designation;

    @Column(name = "ETAT", nullable = false)
    private String etat;

    @Column(name = "PERIODE", nullable = false)
    private String periode;

    @Column(name = "DEBUT_REPONSE")
    private LocalDate debutReponse;

    @Column(name = "FIN_REPONSE")
    private LocalDate finReponse;



    /**
     * FK EVE_ENS_FK : NO_ENSEIGNANT -> ENSEIGNANT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_ENSEIGNANT", insertable = false, updatable = false)
    @ToString.Exclude
    private Enseignant enseignant;

    /**
     * FK EVE_PRO_FK : (CODE_FORMATION, ANNEE_UNIVERSITAIRE) -> PROMOTION
     * (sur ton schéma, Promotion est liée à Formation + Année universitaire)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CODE_FORMATION", insertable = false, updatable = false),
            @JoinColumn(name = "ANNEE_UNIVERSITAIRE", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private Promotion promotion;

    /**
     * FK EVE_UE_FK : (CODE_FORMATION, CODE_UE) -> UNITE_ENSEIGNEMENT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CODE_FORMATION", insertable = false, updatable = false),
            @JoinColumn(name = "CODE_UE", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private UniteEnseignement uniteEnseignement;

    /**
     * FK EVE_EC_FK : (CODE_FORMATION, CODE_UE, CODE_EC) -> ELEMENT_CONSTITUTIF
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "CODE_FORMATION", insertable = false, updatable = false),
            @JoinColumn(name = "CODE_UE", insertable = false, updatable = false),
            @JoinColumn(name = "CODE_EC", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private ElementConstitutif elementConstitutif;


}
