package com.evaluation.backend.service;

import com.evaluation.backend.dto.Admin.AdminUserDTO;
import com.evaluation.backend.entity.Authentification;
import com.evaluation.backend.entity.Enseignant;
import com.evaluation.backend.entity.Etudiant;
import com.evaluation.backend.repository.AuthentificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class AdminService {

    private final AuthentificationRepository authRepository;

    public AdminService(AuthentificationRepository authRepository) {
        this.authRepository = authRepository;
    }

    public List<AdminUserDTO> getEnseignants() {

        List<Authentification> users = authRepository.findByRole("ENS");

        return users.stream().map(auth -> {

            Enseignant e = auth.getEnseignant();

            return new AdminUserDTO(
                    auth.getId(),
                    e.getNom(),
                    e.getPrenom(),
                    auth.getEmail(),
                    e.getMobile(),
                    auth.getRole(),
                    auth.getActive()
            );

        }).toList();
    }

    public List<AdminUserDTO> getEtudiants() {

        List<Authentification> users = authRepository.findByRole("ETU");

        return users.stream()
                .map(auth -> {

                    Etudiant e = auth.getEtudiant();

                    return new AdminUserDTO(
                            auth.getId(),
                            e.getNom(),
                            e.getPrenom(),
                            auth.getEmail(),
                            e.getMobile(),
                            auth.getRole(),
                            auth.getActive()
                    );
                })
                .toList();
    }

    public void toggleUser(@PathVariable Long idAuth) {
        Authentification auth = authRepository.findById(idAuth)
                .orElseThrow();

        auth.setActive(!auth.getActive());

        authRepository.save(auth);
    }
}
