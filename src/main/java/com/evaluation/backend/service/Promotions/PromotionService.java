package com.evaluation.backend.service.Promotions;

import com.evaluation.backend.dto.Promotions.PromotionCreateUpdateDTO;
import com.evaluation.backend.dto.Promotions.PromotionResponseDTO;
import com.evaluation.backend.entity.Formation;
import com.evaluation.backend.entity.Promotion;
import com.evaluation.backend.entity.PromotionId;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.DuplicateResourceException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.PromotionMapper;
import com.evaluation.backend.repository.EtudiantRepository;
import com.evaluation.backend.repository.FormationRepository;
import com.evaluation.backend.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final FormationRepository formationRepository;
    private final EtudiantRepository etudiantRepository;
    private final PromotionMapper promotionMapper;
    private final PromotionRulesService guardService;

    public PromotionService(PromotionRepository promotionRepository,
                            FormationRepository formationRepository,
                            EtudiantRepository etudiantRepository,
                            PromotionMapper promotionMapper,
                            PromotionRulesService guardService) {
        this.promotionRepository = promotionRepository;
        this.formationRepository = formationRepository;
        this.etudiantRepository = etudiantRepository;
        this.promotionMapper = promotionMapper;
        this.guardService = guardService;
    }

public List<PromotionResponseDTO> list(String anneeUniversitaire, String diplome, String nomFormation) {
    return promotionRepository.search(anneeUniversitaire, diplome, nomFormation)
            .stream()
            .map(p -> {
                Formation f = formationRepository.findById(p.getCodeFormation()).orElse(null);
                int count = etudiantRepository.countByCodeFormationAndAnneeUniversitaire(
                    p.getCodeFormation(), p.getAnneeUniversitaire());
                return promotionMapper.toDto(p, f, count);
            })
            .toList();
}

public PromotionResponseDTO getOne(String codeFormation, String anneeUniversitaire) {
    Promotion p = promotionRepository.findById(new PromotionId(codeFormation, anneeUniversitaire))
            .orElseThrow(() -> new ResourceNotFoundException("Promotion introuvable"));
    Formation f = formationRepository.findById(codeFormation).orElse(null);
    int count = etudiantRepository.countByCodeFormationAndAnneeUniversitaire(codeFormation, anneeUniversitaire);
    return promotionMapper.toDto(p, f, count);
}

public PromotionResponseDTO create(PromotionCreateUpdateDTO dto) {
    formationRepository.findById(dto.getCodeFormation())
            .orElseThrow(() -> new ResourceNotFoundException("Formation inexistante"));
    PromotionId id = new PromotionId(dto.getCodeFormation(), dto.getAnneeUniversitaire());
    if (promotionRepository.existsById(id)) {
        throw new DuplicateResourceException("La promotion existe déjà");
    }
    Promotion saved = promotionRepository.save(promotionMapper.toEntity(dto));
    Formation f = formationRepository.findById(saved.getCodeFormation()).orElse(null);
    return promotionMapper.toDto(saved, f, 0);
}

public PromotionResponseDTO update(String codeFormation, String anneeUniversitaire, PromotionCreateUpdateDTO dto) {
    PromotionId id = new PromotionId(codeFormation, anneeUniversitaire);
    Promotion p = promotionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion introuvable"));
    if (!codeFormation.equals(dto.getCodeFormation()) || !anneeUniversitaire.equals(dto.getAnneeUniversitaire())) {
        throw new BusinessException("Impossible de modifier la clé de la promotion");
    }
    promotionMapper.copyToEntity(dto, p);
    Promotion saved = promotionRepository.save(p);
    Formation f = formationRepository.findById(saved.getCodeFormation()).orElse(null);
    int count = etudiantRepository.countByCodeFormationAndAnneeUniversitaire(codeFormation, anneeUniversitaire);
    return promotionMapper.toDto(saved, f, count);
}

    public void delete(String codeFormation, String anneeUniversitaire) {
        PromotionId id = new PromotionId(codeFormation, anneeUniversitaire);

        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion introuvable");
        }


        if (etudiantRepository.existsByCodeFormationAndAnneeUniversitaire(codeFormation, anneeUniversitaire)) {
            throw new BusinessException("Suppression impossible : la promotion contient des étudiants");
        }


        if (guardService.promotionDejaEvaluee(codeFormation, anneeUniversitaire)) {
            throw new BusinessException("Suppression impossible : la promotion a déjà fait l’objet d’une évaluation");
        }

        promotionRepository.deleteById(id);
    }
}
