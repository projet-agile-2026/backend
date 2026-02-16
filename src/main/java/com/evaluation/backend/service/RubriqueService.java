package com.evaluation.backend.service;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.dto.Question.ReorderQuestionsRequest;
import com.evaluation.backend.dto.Rubrique.*;
import com.evaluation.backend.entity.*;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.DuplicateResourceException;
import com.evaluation.backend.exception.InvalidOrderException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.RubriqueMapper;
import com.evaluation.backend.repository.RubriqueEvaluationRepository;
import com.evaluation.backend.repository.RubriqueQuestionRepository;
import com.evaluation.backend.repository.RubriqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RubriqueService {

    private final RubriqueRepository rubriqueRepository;
    private final RubriqueQuestionRepository rubriqueQuestionRepository;
    private final RubriqueEvaluationRepository rubriqueEvaluationRepository;
    private final RubriqueMapper rubriqueMapper;
    private final QuestionService questionService;
    @Transactional(readOnly = true)
    
    public List<RubriqueDTO> getAllRubriques(String role, Long noEnseignant) {
        log.debug("Fetching all rubriques for role: {}", role);
        
        List<Rubrique> rubriques;

        log.info("Tentative de récupération pour l'enseignant ID: {}", noEnseignant); // Vérifiez ce log !

        if ("ADM".equals(role)) {
            rubriques = rubriqueRepository.findAll().stream()
                    .filter(q -> "RBS".equals(q.getType()) && q.getNoEnseignant() == null)
                    .sorted((r1, r2) -> Integer.compare(
                        r1.getOrdre() != null ? r1.getOrdre() : 0, 
                        r2.getOrdre() != null ? r2.getOrdre() : 0))
                    .collect(Collectors.toList());
        } else if ("ENS".equals(role)) {

            // Filtrage pour Enseignant : Type RBS et correspond au noEnseignant
            rubriques = rubriqueRepository.findAll().stream()
                    .filter(q -> "RBS".equals(q.getType()) || noEnseignant.equals(q.getNoEnseignant())  )
                    .sorted((r1, r2) -> Integer.compare(
                        r1.getOrdre() != null ? r1.getOrdre() : 0, 
                        r2.getOrdre() != null ? r2.getOrdre() : 0))
                    .collect(Collectors.toList());
        } else {
            rubriques = new ArrayList<>();
        }

    // Conversion en DTO via le mapper injecté
        List<RubriqueDTO> rubriqueDTOs = rubriqueMapper.toDTOList(rubriques);

    // Enrichissement avec les questions ET usedInEval
        for (RubriqueDTO rubriqueDTO : rubriqueDTOs) {
            // Check if rubrique is used in any evaluation
            boolean isUsed = rubriqueEvaluationRepository.existsByIdRubrique(rubriqueDTO.getIdRubrique());
            rubriqueDTO.setUsedInEval(isUsed);

            // Questions
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
        List<QuestionWithQualificatifDTO> questionsWithQualificatifs = getQuestionsWithQualificatifsForRubrique(id);
        rubriqueDTO.setQuestions(questionsWithQualificatifs);

        return rubriqueDTO;
    }

    @Transactional(readOnly = true)
    public List<QuestionWithQualificatifDTO> getQuestionsForRubrique(Long rubriqueId) {
        log.debug("Fetching questions for rubrique id: {}", rubriqueId);

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
            QuestionWithQualificatifDTO dto = questionService.getQuestionWithQualificatifById(rq.getIdQuestion());
            dto.setOrdre(rq.getOrdre());
            result.add(dto);
        }

        return result;
    }

    
    public RubriqueDTO createRubrique(CreateRubriqueRequest request, String role, Long noEnseignant) {
        // Détermination du type
        if ("ADM".equals(role)) {
            request.setType("RBS");
        } else if ("ENS".equals(role)) {
            request.setType("RBP");
            if (noEnseignant == null) throw new BusinessException("ID enseignant manquant");
        } else {
            throw new BusinessException("Rôle non autorisé");
        }

        // Vérification d'unicité via le repository injecté dans le SERVICE
        Optional<Rubrique> existing = ("RBP".equals(request.getType())) 
            ? rubriqueRepository.findByDesignationAndTypeAndNoEnseignant(request.getDesignation(), "RBP", noEnseignant)
            : rubriqueRepository.findByDesignationAndType(request.getDesignation(), "RBS");

        if (existing.isPresent()) {
            throw new DuplicateResourceException("Rubrique", "designation", request.getDesignation());
        }

        // Mapping et Sauvegarde
        Rubrique rubrique = rubriqueMapper.toEntity(request);
        rubrique.setNoEnseignant(noEnseignant);
        
        if (rubrique.getOrdre() == null) {
            Integer maxOrdre = rubriqueRepository.findMaxOrdreByType(rubrique.getType());
            rubrique.setOrdre(maxOrdre == null ? 1 : maxOrdre + 1);
        }

        return rubriqueMapper.toDTO(rubriqueRepository.save(rubrique));
    }

    public RubriqueDTO updateRubrique(Long id, UpdateRubriqueRequest request, String role, Long noEnseignant) {
        log.debug("Updating rubrique with id: {}", id);
        
        // Récupérer la rubrique existante
        Rubrique existingRubrique = rubriqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rubrique", "idRubrique", id));

        // Vérification des droits (Sécurité)
        if ("ENS".equals(role)) {
            // Un enseignant ne peut modifier QUE ses propres RBP
            if (!"RBP".equals(existingRubrique.getType()) || 
                existingRubrique.getNoEnseignant() == null || 
                !existingRubrique.getNoEnseignant().equals(noEnseignant)) {
                throw new BusinessException("Vous n'avez pas le droit de modifier cette rubrique.");
            }
            request.setType("RBP"); // On force le type au cas où il aurait été changé dans le JSON
        } else if ("ADM".equals(role)) {
            // Un admin ne peut modifier QUE les RBS
            if (!"RBS".equals(existingRubrique.getType())) {
                throw new BusinessException("L'administrateur ne peut modifier que les rubriques standards.");
            }
            request.setType("RBS");
        }

        // Vérification d'unicité (pour ne pas renommer vers un nom déjà pris)
        Optional<Rubrique> duplicate;
        if ("RBP".equals(request.getType())) {
            duplicate = rubriqueRepository.findByDesignationAndTypeAndNoEnseignant(
                    request.getDesignation(), "RBP", noEnseignant);
        } else {
            duplicate = rubriqueRepository.findByDesignationAndType(
                    request.getDesignation(), "RBS");
        }

        duplicate.ifPresent(r -> {
            if (!r.getIdRubrique().equals(id)) {
                throw new DuplicateResourceException("Rubrique", "designation", request.getDesignation());
            }
        });

        // Mise à jour et sauvegarde
        rubriqueMapper.updateEntityFromRequest(request, existingRubrique);
        
        // On s'assure que ces champs critiques ne sont pas écrasés par le mapper
        existingRubrique.setType(request.getType());
        if ("ENS".equals(role)) {
            existingRubrique.setNoEnseignant(noEnseignant);
        }

        Rubrique updatedRubrique = rubriqueRepository.save(existingRubrique);
        log.info("Updated rubrique with id: {}", id);
        return rubriqueMapper.toDTO(updatedRubrique);
    } 

    public void deleteRubrique(Long id, String role, Long noEnseignant) {
        log.debug("Deleting rubrique with id: {} by role: {}", id, role);

        // Récupérer la rubrique pour vérifier son type et son propriétaire
        Rubrique rubrique = rubriqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rubrique", "idRubrique", id));

        // Vérification des droits
        if ("ENS".equals(role)) {
            // Un enseignant ne peut supprimer que ses RBP
            if (!"RBP".equals(rubrique.getType()) || 
                rubrique.getNoEnseignant() == null || 
                !rubrique.getNoEnseignant().equals(noEnseignant)) {
                throw new BusinessException("Vous n'êtes pas autorisé à supprimer cette rubrique.");
            }
        } else if ("ADM".equals(role)) {
            // Un admin ne peut supprimer que les RBS
            if (!"RBS".equals(rubrique.getType())) {
                throw new BusinessException("L'administrateur ne peut supprimer que les rubriques standards.");
            }
        } else {
            throw new BusinessException("Rôle non autorisé.");
        }

        // Suppression en cascade des questions liées (si non géré par JPA)
        rubriqueQuestionRepository.deleteByIdRubrique(id);
        
        // Suppression de la rubrique
        rubriqueRepository.delete(rubrique);
        
        log.info("Deleted rubrique with id: {}", id);
    }

    public void addQuestionToRubrique(Long rubriqueId, AddQuestionToRubriqueRequest request) {
        log.debug("Adding question {} to rubrique {}", request.getIdQuestion(), rubriqueId);

        if (!rubriqueRepository.existsById(rubriqueId)) {
            throw new ResourceNotFoundException("Rubrique", "idRubrique", rubriqueId);
        }

        //  Use QuestionService
        if (!questionService.existsById(request.getIdQuestion())) {
            throw new ResourceNotFoundException("Question", "idQuestion", request.getIdQuestion());
        }

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

        if (!rubriqueQuestionRepository.existsByIdRubriqueAndIdQuestion(rubriqueId, questionId)) {
            throw new ResourceNotFoundException("RubriqueQuestion",
                    "rubrique and question", rubriqueId + " - " + questionId);
        }

        rubriqueQuestionRepository.deleteByIdRubriqueAndIdQuestion(rubriqueId, questionId);
        log.info("Removed question {} from rubrique {}", questionId, rubriqueId);
    }

    public void reorderQuestionsInRubrique(Long rubriqueId, ReorderQuestionsRequest request) {
        log.debug("Reordering questions in rubrique {}", rubriqueId);

        if (!rubriqueRepository.existsById(rubriqueId)) {
            throw new ResourceNotFoundException("Rubrique", "idRubrique", rubriqueId);
        }

        List<RubriqueQuestion> currentQuestions = rubriqueQuestionRepository
                .findByIdRubriqueOrderByOrdreAsc(rubriqueId);

        List<Long> currentQuestionIds = currentQuestions.stream()
                .map(RubriqueQuestion::getIdQuestion)
                .collect(Collectors.toList());

        for (ReorderQuestionsRequest.QuestionOrder qo : request.getQuestionOrders()) {
            if (!currentQuestionIds.contains(qo.getIdQuestion())) {
                throw new BusinessException("Question " + qo.getIdQuestion() + " is not in rubrique " + rubriqueId);
            }
        }

        List<Integer> ordres = request.getQuestionOrders().stream()
                .map(ReorderQuestionsRequest.QuestionOrder::getOrdre)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < ordres.size(); i++) {
            if (ordres.get(i) != i + 1) {
                throw new InvalidOrderException("Ordre values must be consecutive starting from 1");
            }
        }

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

    public void reorderRubriques(ReorderRubriquesRequest request, String role, Long noEnseignant) {
        log.debug("Reordering rubriques for role: {}", role);

        for (ReorderRubriquesRequest.RubriqueOrder ro : request.getRubriqueOrders()) {
            // Récupérer la rubrique
            Rubrique rubrique = rubriqueRepository.findById(ro.getIdRubrique())
                    .orElseThrow(() -> new ResourceNotFoundException("Rubrique", "idRubrique", ro.getIdRubrique()));

            // Vérification des droits d'accès
            if ("ENS".equals(role)) {
                // Un enseignant peut ordonner les RBS OU ses propres RBP
                boolean isRBS = "RBS".equals(rubrique.getType());
                boolean isOwnRBP = "RBP".equals(rubrique.getType()) && 
                                noEnseignant != null && 
                                noEnseignant.equals(rubrique.getNoEnseignant());
                
                if (!isRBS && !isOwnRBP) {
                    throw new BusinessException("Vous n'avez pas le droit de modifier l'ordre de la rubrique ID: " + ro.getIdRubrique());
                }
            } else if ("ADM".equals(role)) {
                // Un admin ne peut ordonner QUE les RBS
                if (!"RBS".equals(rubrique.getType())) {
                    throw new BusinessException("L'administrateur ne peut modifier que l'ordre des rubriques standards.");
                }
            }

            // Mise à jour de l'ordre (on accepte les ordres non consécutifs comme demandé)
            rubrique.setOrdre(ro.getOrdre());
            rubriqueRepository.save(rubrique);
        }

        log.info("Successfully reordered {} rubriques", request.getRubriqueOrders().size());
    }
}