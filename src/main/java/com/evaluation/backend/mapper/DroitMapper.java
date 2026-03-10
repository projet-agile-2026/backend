package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.Droit.DroitRequestDTO;
import com.evaluation.backend.dto.Droit.DroitResponseDTO;
import com.evaluation.backend.entity.Droit;
import org.springframework.stereotype.Component;

@Component
public class DroitMapper {

    public void apply(Droit droit, DroitRequestDTO dto) {
        boolean dup = Boolean.TRUE.equals(dto.getDuplication());
        boolean cons = dup || Boolean.TRUE.equals(dto.getConsultation()); // duplication => consultation

        droit.setNoEnseignant(dto.getNoEnseignant());
        droit.setConsultation(cons ? "O" : "N");
        droit.setDuplication(dup ? "O" : "N");
    }

    public DroitResponseDTO toResponse(Droit d) {
        return DroitResponseDTO.builder()
                .idEvaluation(d.getIdEvaluation())
                .noEnseignant(d.getNoEnseignant())
                .consultation(d.getConsultation())
                .duplication(d.getDuplication())
                .nom(d.getEnseignant() != null ? d.getEnseignant().getNom() : null)
                .prenom(d.getEnseignant() != null ? d.getEnseignant().getPrenom() : null)
                .emailUbo(d.getEnseignant() != null ? d.getEnseignant().getEmailUbo() : null)
                .build();
    }
}
