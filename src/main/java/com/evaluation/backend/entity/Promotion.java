package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PROMOTION")
@IdClass(PromotionId.class)
public class Promotion implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "CODE_FORMATION", nullable = false)
    private String codeFormation;

    @Id
    @Column(name = "ANNEE_UNIVERSITAIRE", nullable = false)
    private String anneeUniversitaire;


    @Column(name = "NO_ENSEIGNANT")
    private Long noEnseignant;

    @Column(name = "SIGLE_PROMOTION")
    private String siglePromotion;

    @Column(name = "NB_MAX_ETUDIANT", nullable = false)
    private Integer nbMaxEtudiant;

    @Column(name = "DATE_REPONSE_LP")
    private Date dateReponseLp;

    @Column(name = "DATE_REPONSE_LALP")
    private Date dateReponseLalp;

    @Column(name = "DATE_RENTREE")
    private Date dateRentree;

    @Column(name = "LIEU_RENTREE")
    private String lieuRentree;

    @Column(name = "PROCESSUS_STAGE")
    private String processusStage;

    @Column(name = "COMMENTAIRE")
    private String commentaire;



    /**
     * FK PRO_ENS_FK : NO_ENSEIGNANT -> ENSEIGNANT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_ENSEIGNANT", insertable = false, updatable = false)
    @ToString.Exclude
    private Enseignant enseignant;


}
