package com.evaluation.backend.dto.Auth;

public class LoginResponseDTO {

    private String role;
    private String nom;

    public LoginResponseDTO(String role, String nom) {
        this.role = role;
        this.nom = nom;
    }
}

