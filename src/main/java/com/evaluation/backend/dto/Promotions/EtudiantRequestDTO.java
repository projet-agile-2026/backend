package com.evaluation.backend.dto.Promotions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EtudiantRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le sexe est obligatoire")
    private String sexe;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateNaissance;

    @NotBlank(message = "Le lieu de naissance est obligatoire")
    private String lieuNaissance;

    @NotBlank(message = "La nationalité est obligatoire")
    private String nationalite;

    private String telephone;

    private String mobile;

    @NotBlank(message = "L'email est obligatoire")
    private String email;

    private String emailUbo;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    private String codePostal;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotBlank(message = "Le pays d'origine est obligatoire")
    private String paysOrigine;

    @NotBlank(message = "L'université d'origine est obligatoire")
    private String universiteOrigine;

    private Long groupeTp;

    private Long groupeAnglais;
}
