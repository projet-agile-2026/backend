package com.evaluation.backend.service.Evaluation;

import com.evaluation.backend.dto.Droit.DroitRequestDTO;
import com.evaluation.backend.dto.Droit.DroitResponseDTO;
import com.evaluation.backend.dto.Droit.DroitTousRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.dto.Evaluation.*;
import java.util.List;

public interface EvaluationService {

    List<EvaluationResponseDTO> list(Long noEnseignant, String codeFormation, String anneeUniversitaire);

    EvaluationResponseDTO getById(Long id);

    EvaluationWithRubriquesDTO getByIdWithRubriques(Long id);

    EvaluationResponseDTO create(EvaluationRequestDTO dto, Long noEnseignant);

    EvaluationResponseDTO update(Long id, EvaluationRequestDTO dto);

    void delete(Long id);

    List<String> getFormations();

    List<String> getCodeUe(String codeFormation);

    List<String> getCodeEc(String codeFormation, String codeUe);

    // US 6.5: Définir les rubriques d'une évaluation
    RubriqueEvaluationDTO addRubriqueToEvaluation(Long evaluationId, AddRubriqueToEvaluationRequest request, Long noEnseignant);

    void removeRubriqueFromEvaluation(Long evaluationId, Long rubriqueEvaluationId, Long noEnseignant);

    void reorderRubriquesInEvaluation(Long evaluationId, ReorderRubriquesInEvaluationRequest request, Long noEnseignant);

    // US 6.6: Définir les questions d'une rubrique d'évaluation

    RubriqueEvaluationDTO addQuestionToRubriqueEvaluation(Long evaluationId, Long rubriqueEvaluationId,
                                         AddQuestionToRubriqueEvaluationRequest request, Long noEnseignant);

    void removeQuestionFromRubriqueEvaluation(Long evaluationId, Long rubriqueEvaluationId,
                                              Long questionEvaluationId, Long noEnseignant);

    void reorderQuestionsInRubriqueEvaluation(Long evaluationId, Long rubriqueEvaluationId,
                                              ReorderQuestionsInRubriqueEvaluationRequest request, Long noEnseignant);


    // us droit + duplication
    // US 6.10
    List<EvaluationResponseDTO> listEvaluationsPartagees();

    // US 6.11
    EvaluationResponseDTO dupliquerEvaluation(Long idEvaluation);

    // US 6.9
    List<DroitResponseDTO> listDroits(Long idEvaluation);
    DroitResponseDTO upsertDroit(Long idEvaluation, DroitRequestDTO dto);
    void deleteDroit(Long idEvaluation, Long noEnseignantCible);
    DroitResponseDTO donnerDroitATous(Long idEvaluation, DroitTousRequestDTO dto);



    List<String> getAnneesUniversitaires(String codeFormation);

}
