package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.Promotions.EtudiantRequestDTO;
import com.evaluation.backend.dto.Promotions.EtudiantResponseDTO;
import com.evaluation.backend.entity.Etudiant;
import org.springframework.stereotype.Component;

@Component
public class EtudiantMapper {

    public Etudiant toEntity(EtudiantRequestDTO dto, String codeFormation, String anneeUniversitaire) {
        Etudiant e = new Etudiant();

        e.setCodeFormation(codeFormation);
        e.setAnneeUniversitaire(anneeUniversitaire);

        copyCreateFields(dto, e);
        return e;
    }

    public void copyToEntity(EtudiantRequestDTO dto, Etudiant e) {
        e.setNom(dto.getNom());
        e.setPrenom(dto.getPrenom());
        e.setSexe(dto.getSexe());
        e.setDateNaissance(dto.getDateNaissance());
        e.setLieuNaissance(dto.getLieuNaissance());
        e.setNationalite(dto.getNationalite());
        e.setTelephone(dto.getTelephone());
        e.setMobile(dto.getMobile());
        e.setEmail(dto.getEmail());
        e.setEmailUbo(dto.getEmailUbo());
        e.setAdresse(dto.getAdresse());
        e.setCodePostal(dto.getCodePostal());
        e.setVille(dto.getVille());
        e.setPaysOrigine(dto.getPaysOrigine());
        e.setUniversiteOrigine(dto.getUniversiteOrigine());
        e.setGroupeTp(dto.getGroupeTp());
        e.setGroupeAnglais(dto.getGroupeAnglais());
    }

    public EtudiantResponseDTO toDto(Etudiant e) {
        return EtudiantResponseDTO.builder()
                .noEtudiant(e.getNoEtudiant())
                .codeFormation(e.getCodeFormation())
                .anneeUniversitaire(e.getAnneeUniversitaire())
                .nom(e.getNom())
                .prenom(e.getPrenom())
                .sexe(e.getSexe())
                .dateNaissance(e.getDateNaissance())
                .lieuNaissance(e.getLieuNaissance())
                .nationalite(e.getNationalite())
                .telephone(e.getTelephone())
                .mobile(e.getMobile())
                .email(e.getEmail())
                .emailUbo(e.getEmailUbo())
                .adresse(e.getAdresse())
                .codePostal(e.getCodePostal())
                .ville(e.getVille())
                .paysOrigine(e.getPaysOrigine())
                .universiteOrigine(e.getUniversiteOrigine())
                .groupeTp(e.getGroupeTp())
                .groupeAnglais(e.getGroupeAnglais())
                .build();
    }

    private void copyCreateFields(EtudiantRequestDTO dto, Etudiant e) {
        e.setNom(dto.getNom());
        e.setPrenom(dto.getPrenom());
        e.setSexe(dto.getSexe());
        e.setDateNaissance(dto.getDateNaissance());
        e.setLieuNaissance(dto.getLieuNaissance());
        e.setNationalite(dto.getNationalite());
        e.setTelephone(dto.getTelephone());
        e.setMobile(dto.getMobile());
        e.setEmail(dto.getEmail());
        e.setEmailUbo(dto.getEmailUbo());
        e.setAdresse(dto.getAdresse());
        e.setCodePostal(dto.getCodePostal());
        e.setVille(dto.getVille());
        e.setPaysOrigine(dto.getPaysOrigine());
        e.setUniversiteOrigine(dto.getUniversiteOrigine());
        e.setGroupeTp(dto.getGroupeTp());
        e.setGroupeAnglais(dto.getGroupeAnglais());
    }
}
