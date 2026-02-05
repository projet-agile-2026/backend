package com.evaluation.backend.service;

import com.evaluation.backend.dto.*;
import com.evaluation.backend.entity.*;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.DuplicateResourceException;
import com.evaluation.backend.exception.InvalidOrderException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.RubriqueMapper;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.repository.QuestionRepository;
import com.evaluation.backend.repository.RubriqueQuestionRepository;
import com.evaluation.backend.repository.RubriqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RubriqueService {

    private final RubriqueRepository rubriqueRepository;
    private final QuestionRepository questionRepository;
    private final QualificatifRepository qualificatifRepository;
    private final RubriqueQuestionRepository rubriqueQuestionRepository;
    private final RubriqueMapper rubriqueMapper;

    @Transactional(readOnly = true)
    public List<RubriqueDTO> getAllRubriques() {
        log.debug("Fetching all rubriques");
        List<Rubrique> rubriques = rubriqueRepository.findAllOrderByOrdre();
        List<RubriqueDTO> rubriqueDTOs = rubriqueMapper.toDTOList(rubriques);

        // Populate questions for each rubrique
        for (RubriqueDTO rubriqueDTO : rubriqueDTOs) {
            List<QuestionWithQualificatifDTO> questions =
                    getQuestionsWithQualificatifsForRubrique(rubriqueDTO.getIdRubrique());
            rubriqueDTO.setQuestions(questions);
        }

        return rubriqueDTOs;
    }

    @Transactional(readOnly = true)
    public RubriqueDTO getRubriqueById(Long id) {
        log.debug("Fetching rubrique with id: {}", id);
        Rubrique rubrique = rubriqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rubrique", "idRubrique", id));

        RubriqueDTO rubriqueDTO = rubriqueMapper.toDTO(rubrique);

        // Fetch questions with their qualificatifs
        List<QuestionWithQualificatifDTO> questionsWithQualificatifs = getQuestionsWithQualificatifsForRubrique(id);
        rubriqueDTO.setQuestions(questionsWithQualificatifs);

        return rubriqueDTO;
    }

    @Transactional(readOnly = true)
    public List<QuestionWithQualificatifDTO> getQuestionsForRubrique(Long rubriqueId) {
        log.debug("Fetching questions for rubrique id: {}", rubriqueId);

        // Verify rubrique exists
        if (!rubriqueRepository.existsById(rubriqueId)) {
            throw new ResourceNotFoundException("Rubrique", "idRubrique", rubriqueId);
        }

        return getQuestionsWithQualificatifsForRubrique(rubriqueId);
    }

    private List<QuestionWithQualificatifDTO> getQuestionsWithQualificatifsForRubrique(Long rubriqueId) {
        List<RubriqueQuestion> rubriqueQuestions = rubriqueQuestionRepository
                .findByIdRubriqueOrderByOrdreAsc(rubriqueId);

        List<QuestionWithQualificatifDTO> result = new ArrayList<>();

        for (RubriqueQuestion rq : rubriqueQuestions) {
            Question question = questionRepository.findById(rq.getIdQuestion())
                    .orElseThrow(() -> new ResourceNotFoundException("Question", "idQuestion", rq.getIdQuestion()));

            Qualificatif qualificatif = qualificatifRepository.findById(question.getIdQualificatif())
                    .orElseThrow(() -> new ResourceNotFoundException("Qualificatif", "idQualificatif", question.getIdQualificatif()));

            QuestionWithQualificatifDTO dto = QuestionWithQualificatifDTO.builder()
                    .idQuestion(question.getIdQuestion())
                    .type(question.getType())
                    .noEnseignant(question.getNoEnseignant())
                    .intitule(question.getIntitule())
                    .ordre(rq.getOrdre())
                    .idQualificatif(qualificatif.getIdQualificatif())
                    .maximal(qualificatif.getMaximal())
                    .minimal(qualificatif.getMinimal())
                    .build();

            result.add(dto);
        }

        return result;
    }

    public RubriqueDTO createRubrique(CreateRubriqueRequest request) {
        log.debug("Creating new rubrique: {}", request);
        if (request.getType() == null || request.getType().isBlank()) {
            request.setType("RBS");
        }

        // Check for duplicate designation and type
        rubriqueRepository.findByDesignationAndType(request.getDesignation(), request.getType())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Rubrique", "designation and type",
                            request.getDesignation() + " - " + request.getType());
                });

        Rubrique rubrique = rubriqueMapper.toEntity(request);

        // If ordre is not provided, set it to max + 1
        if (rubrique.getOrdre() == null) {
            Integer maxOrdre = rubriqueRepository.findMaxOrdreByType(rubrique.getType());
            rubrique.setOrdre(maxOrdre == null ? 1 : maxOrdre + 1);
        }

        Rubrique savedRubrique = rubriqueRepository.save(rubrique);
        log.info("Created rubrique with id: {}", savedRubrique.getIdRubrique());
        return rubriqueMapper.toDTO(savedRubrique);
    }

    public RubriqueDTO updateRubrique(Long id, UpdateRubriqueRequest request) {
        log.debug("Updating rubrique with id: {}", id);
        Rubrique existingRubrique = rubriqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rubrique", "idRubrique", id));

        // Check for duplicate designation and type (excluding current rubrique)
        rubriqueRepository.findByDesignationAndType(request.getDesignation(), request.getType())
                .ifPresent(existing -> {
                    if (!existing.getIdRubrique().equals(id)) {
                        throw new DuplicateResourceException("Rubrique", "designation and type",
                                request.getDesignation() + " - " + request.getType());
                    }
                });

        rubriqueMapper.updateEntityFromRequest(request, existingRubrique);

        Rubrique updatedRubrique = rubriqueRepository.save(existingRubrique);
        log.info("Updated rubrique with id: {}", id);
        return rubriqueMapper.toDTO(updatedRubrique);
    }

    public void deleteRubrique(Long id) {
        log.debug("Deleting rubrique with id: {}", id);
        if (!rubriqueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rubrique", "idRubrique", id);
        }

        // Delete all associated rubrique_question entries first
        rubriqueQuestionRepository.deleteByIdRubrique(id);

        rubriqueRepository.deleteById(id);
        log.info("Deleted rubrique with id: {}", id);
    }

    public void addQuestionToRubrique(Long rubriqueId, AddQuestionToRubriqueRequest request) {
        log.debug("Adding question {} to rubrique {}", request.getIdQuestion(), rubriqueId);

        // Verify rubrique exists
        if (!rubriqueRepository.existsById(rubriqueId)) {
            throw new ResourceNotFoundException("Rubrique", "idRubrique", rubriqueId);
        }

        // Verify question exists
        if (!questionRepository.existsById(request.getIdQuestion())) {
            throw new ResourceNotFoundException("Question", "idQuestion", request.getIdQuestion());
        }

        // Check if question already exists in rubrique
        if (rubriqueQuestionRepository.existsByIdRubriqueAndIdQuestion(rubriqueId, request.getIdQuestion())) {
            throw new DuplicateResourceException("RubriqueQuestion", "rubrique and question",
                    rubriqueId + " - " + request.getIdQuestion());
        }

        RubriqueQuestion rubriqueQuestion = RubriqueQuestion.builder()
                .idRubrique(rubriqueId)
                .idQuestion(request.getIdQuestion())
                .ordre(request.getOrdre())
                .build();

        rubriqueQuestionRepository.save(rubriqueQuestion);
        log.info("Added question {} to rubrique {} with ordre {}", request.getIdQuestion(), rubriqueId, request.getOrdre());
    }

    public void removeQuestionFromRubrique(Long rubriqueId, Long questionId) {
        log.debug("Removing question {} from rubrique {}", questionId, rubriqueId);

        // Verify association exists
        if (!rubriqueQuestionRepository.existsByIdRubriqueAndIdQuestion(rubriqueId, questionId)) {
            throw new ResourceNotFoundException("RubriqueQuestion",
                    "rubrique and question", rubriqueId + " - " + questionId);
        }

        rubriqueQuestionRepository.deleteByIdRubriqueAndIdQuestion(rubriqueId, questionId);
        log.info("Removed question {} from rubrique {}", questionId, rubriqueId);
    }

    public void reorderQuestionsInRubrique(Long rubriqueId, ReorderQuestionsRequest request) {
        log.debug("Reordering questions in rubrique {}", rubriqueId);

        // Verify rubrique exists
        if (!rubriqueRepository.existsById(rubriqueId)) {
            throw new ResourceNotFoundException("Rubrique", "idRubrique", rubriqueId);
        }

        // Get current questions in rubrique
        List<RubriqueQuestion> currentQuestions = rubriqueQuestionRepository
                .findByIdRubriqueOrderByOrdreAsc(rubriqueId);

        // Validate that all questions in request exist in rubrique
        List<Long> currentQuestionIds = currentQuestions.stream()
                .map(RubriqueQuestion::getIdQuestion)
                .collect(Collectors.toList());

        for (ReorderQuestionsRequest.QuestionOrder qo : request.getQuestionOrders()) {
            if (!currentQuestionIds.contains(qo.getIdQuestion())) {
                throw new BusinessException("Question " + qo.getIdQuestion() + " is not in rubrique " + rubriqueId);
            }
        }

        // Validate ordre values (should be consecutive starting from 1)
        List<Integer> ordres = request.getQuestionOrders().stream()
                .map(ReorderQuestionsRequest.QuestionOrder::getOrdre)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < ordres.size(); i++) {
            if (ordres.get(i) != i + 1) {
                throw new InvalidOrderException("Ordre values must be consecutive starting from 1");
            }
        }

        // Update ordre for each question
        for (ReorderQuestionsRequest.QuestionOrder qo : request.getQuestionOrders()) {
            RubriqueQuestion rq = rubriqueQuestionRepository
                    .findByIdRubriqueAndIdQuestion(rubriqueId, qo.getIdQuestion())
                    .orElseThrow(() -> new ResourceNotFoundException("RubriqueQuestion",
                            "rubrique and question", rubriqueId + " - " + qo.getIdQuestion()));

            rq.setOrdre(qo.getOrdre());
            rubriqueQuestionRepository.save(rq);
        }

        log.info("Reordered {} questions in rubrique {}", request.getQuestionOrders().size(), rubriqueId);
    }
}