package com.evaluation.backend.service.Questionnaire;

import com.evaluation.backend.dto.Question.QuestionnaireRequestDTO;
import com.evaluation.backend.dto.Questionnaire.*;
import com.evaluation.backend.entity.Questionnaire;

import java.util.List;

public interface QuestionnaireService {

    List<QuestionnaireResponseDTO> list();

    QuestionnaireResponseDTO create(QuestionnaireRequestDTO dto);

    QuestionnaireResponseDTO update(Long id, QuestionnaireRequestDTO dto);

    void delete(Long id);

    QuestionnaireWithRubriquesDTO getByIdWithRubriques(Long id);

    RubriqueQuestionnaireDTO addRubriqueToQuestionnaire(Long questionnaireId,
                                                        AddRubriqueToQuestionnaireRequest request);

    void removeRubriqueFromQuestionnaire(Long questionnaireId, Long rubriqueQuestionnaireId);

    RubriqueQuestionnaireDTO addQuestionToRubriqueQuestionnaire(Long questionnaireId,
                                                                Long rubriqueQuestionnaireId,
                                                                AddQuestionToRubriqueQuestionnaireRequest request);

    void removeQuestionFromRubriqueQuestionnaire(Long questionnaireId,
                                                 Long rubriqueQuestionnaireId,
                                                 Long questionQuestionnaireId);
}