package com.evaluation.backend.mapper;

import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.entity.Evaluation;
import org.springframework.stereotype.Component;

@Component
public class EvaluationMapper {

    public Evaluation toEntity(EvaluationRequestDTO dto) {
        Evaluation e = new Evaluation();
        updateEntity(e, dto);
        return e;
    }

    public void updateEntity(Evaluation e, EvaluationRequestDTO dto) {
        e.setNoEnseignant(dto.getNoEnseignant());
        e.setCodeFormation(dto.getCodeFormation());
        e.setAnneeUniversitaire(dto.getAnneeUniversitaire());
        e.setCodeUe(dto.getCodeUe());
        e.setCodeEc(dto.getCodeEc());
        e.setNoEvaluation(dto.getNoEvaluation());
        e.setDesignation(dto.getDesignation());
        e.setEtat(dto.getEtat() == null ? null : dto.getEtat().trim().toUpperCase());
        e.setPeriode(dto.getPeriode());
        e.setDebutReponse(dto.getDebutReponse());
        e.setFinReponse(dto.getFinReponse());
    }

    public EvaluationResponseDTO toResponse(Evaluation e) {
        return EvaluationResponseDTO.builder()
                .idEvaluation(e.getIdEvaluation())
                .noEnseignant(e.getNoEnseignant())
                .codeFormation(e.getCodeFormation())
                .anneeUniversitaire(e.getAnneeUniversitaire())
                .codeUe(e.getCodeUe())
                .codeEc(e.getCodeEc())
                .noEvaluation(e.getNoEvaluation())
                .designation(e.getDesignation())
                .etat(e.getEtat())
                .periode(e.getPeriode())
                .debutReponse(e.getDebutReponse())
                .finReponse(e.getFinReponse())
                .build();
    }
}
