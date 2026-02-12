package com.evaluation.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ENSEIGNANT")
public class Enseignant {
    @Id
    @Column(name = "NO_ENSEIGNANT")
    private Integer id;


    @NotNull
    @Column(name = "TYPE")
    private String type;


    @NotNull
    @Column(name = "SEXE")
    private String sexe;

    @NotNull
    @Column(name = "NOM")
    private String nom;

    @NotNull
    @Column(name = "PRENOM")
    private String prenom;

    @NotNull
    @Column(name = "ADRESSE")
    private String adresse;

    @NotNull
    @Column(name = "CODE_POSTAL")
    private String codePostal;

    @NotNull
    @Column(name = "VILLE")
    private String ville;


    @NotNull
    @Column(name = "PAYS")
    private String pays;

    @NotNull
    @Column(name = "MOBILE")
    private String mobile;

    @Column(name = "TELEPHONE")
    private String telephone;

    @NotNull
    @Column(name = "EMAIL_UBO")
    private String emailUbo;

    @Column(name = "EMAIL_PERSO")
    private String emailPerso;

}