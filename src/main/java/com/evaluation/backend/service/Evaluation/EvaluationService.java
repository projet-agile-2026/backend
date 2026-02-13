package com.evaluation.backend.service.Evaluation;

import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;

import java.util.List;

public interface EvaluationService {

    List<EvaluationResponseDTO> list(Long noEnseignant, String codeFormation, String anneeUniversitaire);

    EvaluationResponseDTO getById(Long id);

    EvaluationResponseDTO create(EvaluationRequestDTO dto);

    EvaluationResponseDTO update(Long id, EvaluationRequestDTO dto);

    void delete(Long id);

    List<String> getFormations();

    List<String> getCodeUe(String codeFormation);

    List<String> getCodeEc(String codeFormation, String codeUe);

}
