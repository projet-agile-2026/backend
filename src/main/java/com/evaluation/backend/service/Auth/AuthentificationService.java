package com.evaluation.backend.service;

import com.evaluation.backend.dto.Login.LoginResponseDTO;
import com.evaluation.backend.entity.Authentification;
import com.evaluation.backend.entity.Enseignant;
import com.evaluation.backend.entity.Etudiant;
import com.evaluation.backend.repository.AuthentificationRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthentificationService {

    private final AuthentificationRepository repository;

    public AuthentificationService(AuthentificationRepository repository) {
        this.repository = repository;
    }

    public LoginResponseDTO login(String email, String motPasse) {

        Authentification auth = repository
                .findByEmailAndMotPasse(email, motPasse)
                .orElseThrow(() ->
                        new RuntimeException("Identifiants incorrects")
                );

        if ("ETUDIANT".equals(auth.getRole())) {
            Etudiant e = auth.getEtudiant();
            return new LoginResponseDTO(
                    "ETUDIANT",
                    e.getNom()
            );
        }

        if ("ENSEIGNANT".equals(auth.getRole())) {
            Enseignant ens = auth.getEnseignant();
            return new LoginResponseDTO(
                    "ENSEIGNANT",
                    ens.getNom()
            );
        }

        throw new RuntimeException("Rôle inconnu");
    }
}

