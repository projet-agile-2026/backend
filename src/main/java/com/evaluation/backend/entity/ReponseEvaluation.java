package com.evaluation.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "REPONSE_EVALUATION")
public class ReponseEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_REPONSE_EVALUATION", nullable = false)
    private Long idReponseEvaluation;

    @Column(name = "ID_EVALUATION", nullable = false)
    private Long idEvaluation;

    @Column(name = "NO_ETUDIANT")
    private Long noEtudiant;

    @Column(name = "COMMENTAIRE", length = 512)
    private String commentaire;

    @Column(name = "NOM")
    private String nom;

    @Column(name = "PRENOM")
    private String prenom;

}
