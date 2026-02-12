package com.evaluation.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "ETUDIANT")
public class Etudiant {
    @Id
    @Column(name = "NO_ETUDIANT")
    private String noEtudiant;

    @NotNull
    @Column(name = "NOM")
    private String nom;

    @NotNull
    @Column(name = "PRENOM")
    private String prenom;

    @NotNull
    @Column(name = "SEXE")
    private String sexe;

    @NotNull
    @Column(name = "DATE_NAISSANCE")
    private LocalDate dateNaissance;

    @NotNull
    @Column(name = "LIEU_NAISSANCE")
    private String lieuNaissance;

    @NotNull
    @Column(name = "NATIONALITE")
    private String nationalite;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Column(name = "MOBILE")
    private String mobile;

    @NotNull
    @Column(name = "EMAIL")
    private String email;


    @Column(name = "EMAIL_UBO")
    private String emailUbo;

    @NotNull
    @Column(name = "ADRESSE")
    private String adresse;

    @Column(name = "CODE_POSTAL")
    private String codePostal;

    @NotNull
    @Column(name = "VILLE")
    private String ville;


    @NotNull
    @Column(name = "PAYS_ORIGINE")
    private String paysOrigine;


    @NotNull
    @Column(name = "UNIVERSITE_ORIGINE")
    private String universiteOrigine;

    @Column(name = "GROUPE_TP")
    private Long groupeTp;

    @Column(name = "GROUPE_ANGLAIS")
    private Long groupeAnglais;

}