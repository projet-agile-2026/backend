package com.evaluation.backend.dto.Auth;

public record UserInfoDTO(
        Long id,
        String role,
        String nom,
        String prenom,
        String email
) {}
