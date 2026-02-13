package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.Promotions.PromotionCreateUpdateDTO;
import com.evaluation.backend.dto.Promotions.PromotionResponseDTO;
import com.evaluation.backend.entity.Formation;
import com.evaluation.backend.entity.Promotion;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    public Promotion toEntity(PromotionCreateUpdateDTO dto) {
        Promotion p = new Promotion();
        p.setCodeFormation(dto.getCodeFormation());
        p.setAnneeUniversitaire(dto.getAnneeUniversitaire());
        p.setNoEnseignant(dto.getNoEnseignant());
        p.setSiglePromotion(dto.getSiglePromotion());
        p.setNbMaxEtudiant(dto.getNbMaxEtudiant());
        p.setDateReponseLp(dto.getDateReponseLp());
        p.setDateReponseLalp(dto.getDateReponseLalp());
        p.setDateRentree(dto.getDateRentree());
        p.setLieuRentree(dto.getLieuRentree());
        p.setProcessusStage(dto.getProcessusStage());
        p.setCommentaire(dto.getCommentaire());
        return p;
    }

    public void copyToEntity(PromotionCreateUpdateDTO dto, Promotion p) {
        p.setNoEnseignant(dto.getNoEnseignant());
        p.setSiglePromotion(dto.getSiglePromotion());
        p.setNbMaxEtudiant(dto.getNbMaxEtudiant());
        p.setDateReponseLp(dto.getDateReponseLp());
        p.setDateReponseLalp(dto.getDateReponseLalp());
        p.setDateRentree(dto.getDateRentree());
        p.setLieuRentree(dto.getLieuRentree());
        p.setProcessusStage(dto.getProcessusStage());
        p.setCommentaire(dto.getCommentaire());
    }

    public PromotionResponseDTO toDto(Promotion p, Formation fOrNull) {
        return PromotionResponseDTO.builder()
                .codeFormation(p.getCodeFormation())
                .anneeUniversitaire(p.getAnneeUniversitaire())
                .noEnseignant(p.getNoEnseignant())
                .siglePromotion(p.getSiglePromotion())
                .nbMaxEtudiant(p.getNbMaxEtudiant())
                .dateReponseLp(p.getDateReponseLp())
                .dateReponseLalp(p.getDateReponseLalp())
                .dateRentree(p.getDateRentree())
                .lieuRentree(p.getLieuRentree())
                .processusStage(p.getProcessusStage())
                .commentaire(p.getCommentaire())
                .enseignantNom(p.getEnseignant() != null ? p.getEnseignant().getNom() : null)
                .enseignantPrenom(p.getEnseignant() != null ? p.getEnseignant().getPrenom() : null)
                .nomFormation(fOrNull != null ? fOrNull.getNomFormation() : null)
                .diplome(fOrNull != null ? fOrNull.getDiplome() : null)
                .build();
    }
}
