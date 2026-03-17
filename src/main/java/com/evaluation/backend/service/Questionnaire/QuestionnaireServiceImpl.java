package com.evaluation.backend.service.Questionnaire;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.dto.Question.QuestionnaireRequestDTO;
import com.evaluation.backend.dto.Questionnaire.*;
import com.evaluation.backend.dto.Rubrique.RubriqueDTO;
import com.evaluation.backend.entity.QuestionQuestionnaire;
import com.evaluation.backend.entity.Questionnaire;
import com.evaluation.backend.entity.RubriqueQuestionnaire;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.repository.QuestionQuestionnaireRepository;
import com.evaluation.backend.repository.QuestionnaireRepository;
import com.evaluation.backend.repository.RubriqueQuestionnaireRepository;
import com.evaluation.backend.service.QuestionService;
import com.evaluation.backend.service.RubriqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {


    private final QuestionnaireRepository questionnaireRepository;
    private final RubriqueQuestionnaireRepository rubriqueQuestionnaireRepository;
    private final QuestionQuestionnaireRepository questionQuestionnaireRepository;

    private final RubriqueService rubriqueService;
    private final QuestionService questionService;

    @Override
    @Transactional(readOnly = true)
    public List<QuestionnaireResponseDTO> list() {

        return questionnaireRepository.findAllByOrderByDesignationAsc()
                .stream()
                .map(q -> QuestionnaireResponseDTO.builder()
                        .idQuestionnaire(q.getIdQuestionnaire())
                        .designation(q.getDesignation())
                        .build())
                .toList();
    }

    @Override
    public QuestionnaireResponseDTO create(QuestionnaireRequestDTO dto) {

        Questionnaire questionnaire = Questionnaire.builder()
                .designation(dto.getDesignation())
                .build();

        Questionnaire saved = questionnaireRepository.save(questionnaire);

        return QuestionnaireResponseDTO.builder()
                .idQuestionnaire(saved.getIdQuestionnaire())
                .designation(saved.getDesignation())
                .build();
    }

    @Override
    public QuestionnaireResponseDTO update(Long id, QuestionnaireRequestDTO dto) {

        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire introuvable : id=" + id));

        questionnaire.setDesignation(dto.getDesignation());

        Questionnaire saved = questionnaireRepository.save(questionnaire);

        return QuestionnaireResponseDTO.builder()
                .idQuestionnaire(saved.getIdQuestionnaire())
                .designation(saved.getDesignation())
                .build();
    }

    @Override
    public void delete(Long id) {

        if (!questionnaireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Questionnaire introuvable : id=" + id);
        }

        questionnaireRepository.deleteById(id);
    }

    @Override
    public RubriqueQuestionnaireDTO addRubriqueToQuestionnaire(Long questionnaireId,
                                                               AddRubriqueToQuestionnaireRequest request) {

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Questionnaire introuvable : id=" + questionnaireId));

        RubriqueDTO rubriqueDTO = rubriqueService.getRubriqueById(request.getIdRubrique());

        Integer ordre = request.getOrdre();

        if (ordre == null) {
            Integer maxOrdre = rubriqueQuestionnaireRepository.findMaxOrdreByQuestionnaire(questionnaireId);
            ordre = maxOrdre == null ? 1 : maxOrdre + 1;
        }

        RubriqueQuestionnaire rq = RubriqueQuestionnaire.builder()
                .idQuestionnaire(questionnaireId)
                .idRubrique(request.getIdRubrique())
                .ordre(ordre)
                .designation(request.getDesignation())
                .build();

        RubriqueQuestionnaire saved = rubriqueQuestionnaireRepository.save(rq);

    /* --------------------------------------------------
       COPIER LES QUESTIONS DE LA RUBRIQUE
    -------------------------------------------------- */

        int questionOrdre = 1;

        for (QuestionWithQualificatifDTO q : rubriqueDTO.getQuestions()) {

            QuestionQuestionnaire qq = QuestionQuestionnaire.builder()
                    .idRubriqueQuestionnaire(saved.getIdRubriqueQuestionnaire())
                    .idQuestion(q.getIdQuestion())
                    .idQualificatif(q.getIdQualificatif())
                    .ordre(questionOrdre++)
                    .build();

            questionQuestionnaireRepository.save(qq);
        }

        /* -------------------------------------------------- */

        return RubriqueQuestionnaireDTO.builder()
                .idRubriqueQuestionnaire(saved.getIdRubriqueQuestionnaire())
                .idQuestionnaire(saved.getIdQuestionnaire())
                .idRubrique(saved.getIdRubrique())
                .ordre(saved.getOrdre())
                .designation(saved.getDesignation())
                .type(rubriqueDTO.getType())
                .questions(rubriqueDTO.getQuestions())
                .build();
    }

    @Override
    public RubriqueQuestionnaireDTO addQuestionToRubriqueQuestionnaire(
            Long questionnaireId,
            Long rubriqueQuestionnaireId,
            AddQuestionToRubriqueQuestionnaireRequest request) {

        RubriqueQuestionnaire rubriqueQuestionnaire = rubriqueQuestionnaireRepository.findById(rubriqueQuestionnaireId)
                .orElseThrow(() -> new ResourceNotFoundException("RubriqueQuestionnaire introuvable"));

        if (!rubriqueQuestionnaire.getIdQuestionnaire().equals(questionnaireId)) {
            throw new BusinessException("RubriqueQuestionnaire does not belong to this questionnaire");
        }

        if (!questionService.existsById(request.getIdQuestion())) {
            throw new ResourceNotFoundException("Question introuvable");
        }

        Integer ordre = request.getOrdre();

        if (ordre == null) {
            Integer maxOrdre = questionQuestionnaireRepository
                    .findMaxOrdreByRubriqueQuestionnaire(rubriqueQuestionnaireId);

            ordre = maxOrdre == null ? 1 : maxOrdre + 1;
        }

        QuestionQuestionnaire qq = QuestionQuestionnaire.builder()
                .idRubriqueQuestionnaire(rubriqueQuestionnaireId)
                .idQuestion(request.getIdQuestion())
                .idQualificatif(request.getIdQualificatif())
                .ordre(ordre)
                .build();

        questionQuestionnaireRepository.save(qq);

        return getRubriqueQuestionnaireDTO(questionnaireId, rubriqueQuestionnaireId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionnaireWithRubriquesDTO getByIdWithRubriques(Long id) {

        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Questionnaire introuvable : id=" + id));

        List<RubriqueQuestionnaire> rubriques =
                rubriqueQuestionnaireRepository.findByIdQuestionnaireOrderByOrdreAsc(id);

        List<RubriqueQuestionnaireDTO> rubriqueDTOs = new ArrayList<>();

        for (RubriqueQuestionnaire rq : rubriques) {

            RubriqueDTO rubriqueDTO = rubriqueService.getRubriqueById(rq.getIdRubrique());

            List<QuestionQuestionnaire> questions =
                    questionQuestionnaireRepository
                            .findByIdRubriqueQuestionnaireOrderByOrdreAsc(
                                    rq.getIdRubriqueQuestionnaire());

            List<QuestionWithQualificatifDTO> questionDTOs = new ArrayList<>();

            for (QuestionQuestionnaire qq : questions) {

                QuestionWithQualificatifDTO q =
                        questionService.getQuestionWithQualificatifById(qq.getIdQuestion());

                q.setOrdre(qq.getOrdre());
                q.setIdQuestionQuestionnaire(qq.getIdQuestionQuestionnaire());

                questionDTOs.add(q);
            }

            rubriqueDTOs.add(
                    RubriqueQuestionnaireDTO.builder()
                            .idRubriqueQuestionnaire(rq.getIdRubriqueQuestionnaire())
                            .idQuestionnaire(rq.getIdQuestionnaire())
                            .idRubrique(rq.getIdRubrique())
                            .ordre(rq.getOrdre())
                            .designation(
                                    rq.getDesignation() != null
                                            ? rq.getDesignation()
                                            : rubriqueDTO.getDesignation()
                            )
                            .type(rubriqueDTO.getType())
                            .questions(questionDTOs)
                            .build()
            );
        }

        return QuestionnaireWithRubriquesDTO.builder()
                .idQuestionnaire(questionnaire.getIdQuestionnaire())
                .designation(questionnaire.getDesignation())
                .rubriques(rubriqueDTOs)
                .build();
    }

    @Override
    public void removeRubriqueFromQuestionnaire(Long questionnaireId, Long rubriqueQuestionnaireId) {

        RubriqueQuestionnaire rubriqueQuestionnaire =
                rubriqueQuestionnaireRepository.findById(rubriqueQuestionnaireId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "RubriqueQuestionnaire introuvable : id=" + rubriqueQuestionnaireId));

        if (!rubriqueQuestionnaire.getIdQuestionnaire().equals(questionnaireId)) {
            throw new BusinessException(
                    "Cette rubrique n'appartient pas à ce questionnaire");
        }

        /* vérifier si la rubrique contient encore des questions */

        if (questionQuestionnaireRepository
                .existsByIdRubriqueQuestionnaire(rubriqueQuestionnaireId)) {

            throw new BusinessException(
                    "Cette rubrique contient encore des questions. Supprimez d'abord les questions associées.");
        }

        rubriqueQuestionnaireRepository.deleteById(rubriqueQuestionnaireId);
    }

    private RubriqueQuestionnaireDTO getRubriqueQuestionnaireDTO(Long questionnaireId,
                                                                 Long rubriqueQuestionnaireId) {

        QuestionnaireWithRubriquesDTO questionnaire = getByIdWithRubriques(questionnaireId);

        return questionnaire.getRubriques()
                .stream()
                .filter(r -> r.getIdRubriqueQuestionnaire().equals(rubriqueQuestionnaireId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RubriqueQuestionnaire introuvable"));
    }

    @Override
    public void removeQuestionFromRubriqueQuestionnaire(Long questionnaireId,
                                                        Long rubriqueQuestionnaireId,
                                                        Long questionQuestionnaireId) {

        RubriqueQuestionnaire rubriqueQuestionnaire =
                rubriqueQuestionnaireRepository.findById(rubriqueQuestionnaireId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "RubriqueQuestionnaire introuvable : id=" + rubriqueQuestionnaireId));

        if (!rubriqueQuestionnaire.getIdQuestionnaire().equals(questionnaireId)) {
            throw new BusinessException(
                    "Cette rubrique n'appartient pas à ce questionnaire");
        }

        QuestionQuestionnaire questionQuestionnaire =
                questionQuestionnaireRepository.findById(questionQuestionnaireId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "QuestionQuestionnaire introuvable : id=" + questionQuestionnaireId));

        if (!questionQuestionnaire.getIdRubriqueQuestionnaire().equals(rubriqueQuestionnaireId)) {
            throw new BusinessException(
                    "Cette question n'appartient pas à cette rubrique");
        }

        questionQuestionnaireRepository.deleteById(questionQuestionnaireId);
    }

}
