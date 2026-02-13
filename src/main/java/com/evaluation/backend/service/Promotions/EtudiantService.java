package com.evaluation.backend.service.Promotions;

import com.evaluation.backend.dto.Promotions.EtudiantRequestDTO;
import com.evaluation.backend.dto.Promotions.EtudiantResponseDTO;
import com.evaluation.backend.entity.Etudiant;
import com.evaluation.backend.entity.PromotionId;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.EtudiantMapper;
import com.evaluation.backend.repository.EtudiantRepository;
import com.evaluation.backend.repository.PromotionRepository;
import com.evaluation.backend.service.Promotions.PromotionRulesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EtudiantService {

    private final EtudiantRepository etudiantRepository;
    private final PromotionRepository promotionRepository;
    private final EtudiantMapper etudiantMapper;
    private final PromotionRulesService rulesService;

    public EtudiantService(EtudiantRepository etudiantRepository,
                           PromotionRepository promotionRepository,
                           EtudiantMapper etudiantMapper,
                           PromotionRulesService rulesService) {
        this.etudiantRepository = etudiantRepository;
        this.promotionRepository = promotionRepository;
        this.etudiantMapper = etudiantMapper;
        this.rulesService = rulesService;
    }



    public List<EtudiantResponseDTO> listByPromotion(String codeFormation, String anneeUniversitaire) {

        if (!promotionRepository.existsById(new PromotionId(codeFormation, anneeUniversitaire))) {
            throw new ResourceNotFoundException("Promotion introuvable");
        }

        return etudiantRepository
                .findByCodeFormationAndAnneeUniversitaire(codeFormation, anneeUniversitaire)
                .stream()
                .map(etudiantMapper::toDto)
                .toList();
    }


    // Ajouter étudiant (ID auto généré côté application
    public EtudiantResponseDTO addToPromotion(String codeFormation,
                                              String anneeUniversitaire,
                                              EtudiantRequestDTO dto) {

        if (!promotionRepository.existsById(new PromotionId(codeFormation, anneeUniversitaire))) {
            throw new ResourceNotFoundException("Promotion introuvable");
        }

        // Génération ID côté application
        Long newId = generateNextNoEtudiant();

        Etudiant e = new Etudiant();
        e.setNoEtudiant(newId);
        e.setCodeFormation(codeFormation);
        e.setAnneeUniversitaire(anneeUniversitaire);

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



        for (int i = 0; i < 3; i++) {
            try {
                Etudiant saved = etudiantRepository.save(e);
                return etudiantMapper.toDto(saved);
            } catch (Exception ex) {
                newId = generateNextNoEtudiant();
                e.setNoEtudiant(newId);
            }
        }

        throw new BusinessException("Impossible de générer un NO_ETUDIANT unique (réessaye)");
    }




    public EtudiantResponseDTO update(Long noEtudiant, EtudiantRequestDTO dto) {

        Etudiant e = etudiantRepository.findById(noEtudiant)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable"));

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

        Etudiant saved = etudiantRepository.save(e);
        return etudiantMapper.toDto(saved);
    }


    public void delete(Long noEtudiant) {

        if (!etudiantRepository.existsById(noEtudiant)) {
            throw new ResourceNotFoundException("Étudiant introuvable");
        }

        if (rulesService.etudiantADejaRepondu(noEtudiant)) {
            throw new BusinessException("Suppression impossible : l’étudiant a déjà répondu à une évaluation");
        }

        etudiantRepository.deleteById(noEtudiant);
    }


    private Long generateNextNoEtudiant() {

        return etudiantRepository.findAll()
                .stream()
                .map(Etudiant::getNoEtudiant)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}
