package com.evaluation.backend.dto.Promotions;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EtudiantResponseDTO {

    private Long noEtudiant;
    private String codeFormation;
    private String anneeUniversitaire;

    private String nom;
    private String prenom;
    private String sexe;

    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String nationalite;

    private String telephone;
    private String mobile;

    private String email;
    private String emailUbo;

    private String adresse;
    private String codePostal;
    private String ville;

    private String paysOrigine;
    private String universiteOrigine;

    private Long groupeTp;
    private Long groupeAnglais;
}
