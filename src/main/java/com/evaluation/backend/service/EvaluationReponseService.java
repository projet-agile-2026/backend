package com.evaluation.backend.service;

import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.dto.ReponseEvaluation.EvaluationDetailDTO;
import com.evaluation.backend.dto.ReponseEvaluation.ReponseEvaluationRequestDTO;
import com.evaluation.backend.dto.ReponseEvaluation.ReponseEvaluationResultDTO;
import com.evaluation.backend.entity.*;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationReponseService {

    private final EvaluationRepository evaluationRepository;
    private final EtudiantRepository etudiantRepository;
    private final RubriqueEvaluationRepository rubriqueEvaluationRepository;
    private final QuestionEvaluationRepository questionEvaluationRepository;
    private final ReponseEvaluationRepository reponseEvaluationRepository;
    private final ReponseQuestionRepository reponseQuestionRepository;
    private final RubriqueQuestionRepository rubriqueQuestionRepository;
    private final QualificatifRepository qualificatifRepository;

    /**
     * Récupère toutes les évaluations de la promotion d'un étudiant
     */
    @Transactional(readOnly = true)
    public List<EvaluationResponseDTO> getEvaluationsForEtudiant(Long noEtudiant) {
        log.debug("Fetching evaluations for student with id: {}", noEtudiant);

        // 1. Récupérer l'étudiant pour obtenir sa promotion (codeFormation + anneeUniversitaire)
        Etudiant etudiant = etudiantRepository.findById(noEtudiant)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant", "noEtudiant", noEtudiant));

        // 2. Récupérer toutes les évaluations de cette promotion
        List<Evaluation> evaluations = evaluationRepository
                .findByCodeFormationAndAnneeUniversitaire(
                        etudiant.getCodeFormation(),
                        etudiant.getAnneeUniversitaire()
                );

        log.info("Found {} evaluations for student {} in promotion {}-{}",
                evaluations.size(), noEtudiant, etudiant.getCodeFormation(), etudiant.getAnneeUniversitaire());

        // 3. Filtrer uniquement les évaluations DIS (mise à disposition) et CLO (clôturées)
        // et convertir en DTO
        return evaluations.stream()
                .filter(eval -> "DIS".equals(eval.getEtat()) || "CLO".equals(eval.getEtat()))
                .map(eval -> toResponseDTO(eval, noEtudiant))
                .collect(Collectors.toList());
    }

    private EvaluationResponseDTO toResponseDTO(Evaluation evaluation, Long noEtudiant) {
        // Récupérer les informations de l'enseignant
        String nomEnseignant = null;
        String prenomEnseignant = null;
        if (evaluation.getEnseignant() != null) {
            nomEnseignant = evaluation.getEnseignant().getNom();
            prenomEnseignant = evaluation.getEnseignant().getPrenom();
        }
        
        // Vérifier si l'étudiant a déjà répondu
        boolean dejaRepondu = reponseEvaluationRepository
                .existsByIdEvaluationAndNoEtudiant(evaluation.getIdEvaluation(), noEtudiant);

        return EvaluationResponseDTO.builder()
                .idEvaluation(evaluation.getIdEvaluation())
                .noEnseignant(evaluation.getNoEnseignant())
                .nomEnseignant(nomEnseignant)
                .prenomEnseignant(prenomEnseignant)
                .codeFormation(evaluation.getCodeFormation())
                .anneeUniversitaire(evaluation.getAnneeUniversitaire())
                .codeUe(evaluation.getCodeUe())
                .codeEc(evaluation.getCodeEc())
                .noEvaluation(evaluation.getNoEvaluation())
                .designation(evaluation.getDesignation())
                .etat(evaluation.getEtat())
                .periode(evaluation.getPeriode())
                .debutReponse(evaluation.getDebutReponse())
                .finReponse(evaluation.getFinReponse())
                .dejaRepondu(dejaRepondu)
                .build();
    }

    /**
     * Récupère les détails d'une évaluation avec ses rubriques et questions pour un étudiant
     */
    @Transactional(readOnly = true)
    public EvaluationDetailDTO getEvaluationDetail(Long idEvaluation, Long noEtudiant) {
        log.debug("Fetching evaluation detail for idEvaluation: {} and student: {}", idEvaluation, noEtudiant);

        // Vérifier que l'étudiant existe
        Etudiant etudiant = etudiantRepository.findById(noEtudiant)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant", "noEtudiant", noEtudiant));

        // Récupérer l'évaluation
        Evaluation evaluation = evaluationRepository.findById(idEvaluation)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", "idEvaluation", idEvaluation));

        // Vérifier que l'évaluation appartient à la promotion de l'étudiant
        if (!evaluation.getCodeFormation().equals(etudiant.getCodeFormation()) ||
            !evaluation.getAnneeUniversitaire().equals(etudiant.getAnneeUniversitaire())) {
            throw new RuntimeException("Cette évaluation n'est pas accessible pour cet étudiant");
        }

        // Vérifier que l'évaluation est en état DIS
        if (!"DIS".equals(evaluation.getEtat())) {
            throw new RuntimeException("Cette évaluation n'est pas disponible pour répondre");
        }

        // Récupérer les rubriques de l'évaluation
        List<RubriqueEvaluation> rubriques = rubriqueEvaluationRepository
                .findByIdEvaluationOrderByOrdreAsc(idEvaluation);
        
        log.info("📋 Found {} rubriques for evaluation {}", rubriques.size(), idEvaluation);

        // Construire le DTO avec les rubriques et questions
        List<EvaluationDetailDTO.RubriqueDetailDTO> rubriquesDTO = rubriques.stream()
                .map(rubrique -> {
                    // Récupérer le nom de la rubrique : priorité à designation de RubriqueEvaluation, sinon depuis Rubrique
                    String designationRubrique = rubrique.getDesignation();
                    if ((designationRubrique == null || designationRubrique.isEmpty()) && rubrique.getIdRubrique() != null) {
                        if (rubrique.getRubrique() != null) {
                            designationRubrique = rubrique.getRubrique().getDesignation();
                            log.debug("  📝 Récupération du nom depuis RUBRIQUE: {}", designationRubrique);
                        }
                    }
                    
                    log.debug("  🔍 Processing rubrique {} (ID: {})", designationRubrique, rubrique.getIdRubriqueEvaluation());
                    
                    List<EvaluationDetailDTO.QuestionDetailDTO> questionsDTO = new ArrayList<>();
                    int ordre = 1;
                    
                    // 1. Récupérer les questions depuis RUBRIQUE_QUESTION (questions de base de la rubrique)
                    if (rubrique.getIdRubrique() != null) {
                        List<RubriqueQuestion> rubriqueQuestions = rubriqueQuestionRepository
                                .findByIdRubriqueOrderByOrdreAsc(rubrique.getIdRubrique());
                        
                        log.info("    📚 Found {} questions from RUBRIQUE_QUESTION for rubrique {}", 
                                rubriqueQuestions.size(), rubrique.getIdRubrique());
                        
                        for (RubriqueQuestion rq : rubriqueQuestions) {
                            if (rq.getQuestion() != null) {
                                String minimal = null;
                                String maximal = null;
                                
                                // Récupérer les qualificatifs si disponibles
                                if (rq.getQuestion().getQualificatif() != null) {
                                    minimal = rq.getQuestion().getQualificatif().getMinimal();
                                    maximal = rq.getQuestion().getQualificatif().getMaximal();
                                }
                                
                                questionsDTO.add(EvaluationDetailDTO.QuestionDetailDTO.builder()
                                        .idQuestionEvaluation(rq.getIdQuestion()) // Utiliser l'ID de la question de base
                                        .intitule(rq.getQuestion().getIntitule())
                                        .ordre(ordre++)
                                        .minimal(minimal)
                                        .maximal(maximal)
                                        .build());
                            }
                        }
                    }
                    
                    // 2. Ajouter les questions supplémentaires depuis QUESTION_EVALUATION (questions ad-hoc)
                    List<QuestionEvaluation> questionsEvaluation = questionEvaluationRepository
                            .findByIdRubriqueEvaluationOrderByOrdreAsc(rubrique.getIdRubriqueEvaluation());
                    
                    log.info("    ➕ Found {} additional questions from QUESTION_EVALUATION for rubrique evaluation {}", 
                            questionsEvaluation.size(), rubrique.getIdRubriqueEvaluation());
                    
                    for (QuestionEvaluation qe : questionsEvaluation) {
                        String minimal = null;
                        String maximal = null;
                        
                        // Récupérer les qualificatifs si disponibles
                        if (qe.getIdQualificatif() != null) {
                            try {
                                Qualificatif qual = qualificatifRepository.findById(qe.getIdQualificatif()).orElse(null);
                                if (qual != null) {
                                    minimal = qual.getMinimal();
                                    maximal = qual.getMaximal();
                                }
                            } catch (Exception e) {
                                log.warn("Could not fetch qualificatif {} for question {}", qe.getIdQualificatif(), qe.getIdQuestionEvaluation());
                            }
                        }
                        
                        questionsDTO.add(EvaluationDetailDTO.QuestionDetailDTO.builder()
                                .idQuestionEvaluation(qe.getIdQuestionEvaluation())
                                .intitule(qe.getIntitule())
                                .ordre(ordre++)
                                .minimal(minimal)
                                .maximal(maximal)
                                .build());
                    }
                    
                    log.info("    ✅ Total {} questions for rubrique {}", questionsDTO.size(), rubrique.getIdRubriqueEvaluation());
                    
                    if (questionsDTO.isEmpty()) {
                        log.warn("    ⚠️ No questions found for rubrique {} - this rubrique will be empty", rubrique.getIdRubriqueEvaluation());
                    }

                    return EvaluationDetailDTO.RubriqueDetailDTO.builder()
                            .idRubriqueEvaluation(rubrique.getIdRubriqueEvaluation())
                            .designation(designationRubrique)
                            .ordre(rubrique.getOrdre())
                            .questions(questionsDTO)
                            .build();
                })
                .collect(Collectors.toList());

        // Informations de l'enseignant
        String nomEnseignant = null;
        String prenomEnseignant = null;
        if (evaluation.getEnseignant() != null) {
            nomEnseignant = evaluation.getEnseignant().getNom();
            prenomEnseignant = evaluation.getEnseignant().getPrenom();
        }

        return EvaluationDetailDTO.builder()
                .idEvaluation(evaluation.getIdEvaluation())
                .nomEnseignant(nomEnseignant)
                .prenomEnseignant(prenomEnseignant)
                .codeUe(evaluation.getCodeUe())
                .codeEc(evaluation.getCodeEc())
                .noEvaluation(evaluation.getNoEvaluation())
                .designation(evaluation.getDesignation())
                .etat(evaluation.getEtat())
                .finReponse(evaluation.getFinReponse())
                .rubriques(rubriquesDTO)
                .build();
    }

    /**
     * Soumet les réponses d'un étudiant à une évaluation
     */
    public void submitReponses(ReponseEvaluationRequestDTO request, Long noEtudiant) {
        log.debug("Submitting responses for evaluation {} by student {}", request.getIdEvaluation(), noEtudiant);

        // Vérifier que l'étudiant existe
        Etudiant etudiant = etudiantRepository.findById(noEtudiant)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant", "noEtudiant", noEtudiant));

        // Vérifier que l'évaluation existe et est en état DIS
        Evaluation evaluation = evaluationRepository.findById(request.getIdEvaluation())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", "idEvaluation", request.getIdEvaluation()));

        if (!"DIS".equals(evaluation.getEtat())) {
            throw new RuntimeException("Cette évaluation n'est pas disponible pour répondre");
        }

        // Vérifier si l'étudiant a déjà répondu à cette évaluation
        Optional<ReponseEvaluation> existingReponse = reponseEvaluationRepository
                .findByIdEvaluationAndNoEtudiant(request.getIdEvaluation(), noEtudiant);
        
        if (existingReponse.isPresent()) {
            log.info("🔄 Étudiant {} a déjà répondu à l'évaluation {}. Écrasement des anciennes réponses.", 
                    noEtudiant, request.getIdEvaluation());
            
            Long oldIdReponseEvaluation = existingReponse.get().getIdReponseEvaluation();
            
            // Supprimer d'abord les réponses aux questions
            reponseQuestionRepository.deleteByIdReponseEvaluation(oldIdReponseEvaluation);
            log.debug("  ✓ Anciennes réponses questions supprimées");
            
            // Supprimer la réponse évaluation
            reponseEvaluationRepository.deleteByIdEvaluationAndNoEtudiant(request.getIdEvaluation(), noEtudiant);
            log.debug("  ✓ Ancienne réponse évaluation supprimée");
        }

        // Vérifier que l'évaluation contient des rubriques et questions
        List<RubriqueEvaluation> rubriques = rubriqueEvaluationRepository
                .findByIdEvaluationOrderByOrdreAsc(request.getIdEvaluation());
        
        if (rubriques.isEmpty()) {
            throw new RuntimeException("Cette évaluation ne contient aucune rubrique. Impossible de répondre.");
        }

        // Vérifier que la liste de réponses est présente et non vide
        if (request.getReponses() == null || request.getReponses().isEmpty()) {
            throw new RuntimeException("Aucune réponse fournie. Veuillez répondre à toutes les questions.");
        }

        // Générer l'ID depuis la séquence Oracle
        Long idReponseEvaluation = reponseEvaluationRepository.getNextId();
        log.debug("Generated ID from sequence RPE_SEQ: {}", idReponseEvaluation);

        // Créer l'entité ReponseEvaluation avec l'ID généré
        ReponseEvaluation reponseEvaluation = ReponseEvaluation.builder()
                .idReponseEvaluation(idReponseEvaluation)
                .idEvaluation(request.getIdEvaluation())
                .noEtudiant(noEtudiant)
                .commentaire(request.getCommentaire())
                .nom(etudiant.getNom())
                .prenom(etudiant.getPrenom())
                .build();

        reponseEvaluation = reponseEvaluationRepository.save(reponseEvaluation);
        
        // L'ID devrait maintenant être celui qu'on a généré
        idReponseEvaluation = reponseEvaluation.getIdReponseEvaluation();

        // Créer les réponses aux questions
        for (ReponseEvaluationRequestDTO.ReponseQuestionDTO reponseDTO : request.getReponses()) {
            // Valider le positionnement (1 à 5)
            if (reponseDTO.getPositionnement() < 1 || reponseDTO.getPositionnement() > 5) {
                throw new IllegalArgumentException("Le positionnement doit être entre 1 et 5");
            }

            ReponseQuestionId id = new ReponseQuestionId(idReponseEvaluation, reponseDTO.getIdQuestionEvaluation());
            
            ReponseQuestion reponseQuestion = ReponseQuestion.builder()
                    .id(id)
                    .positionnement(reponseDTO.getPositionnement())
                    .build();

            reponseQuestionRepository.save(reponseQuestion);
        }

        log.info("Successfully saved responses for evaluation {} by student {}", request.getIdEvaluation(), noEtudiant);
    }

    /**
     * Récupère les résultats d'un étudiant pour une évaluation avec les réponses
     */
    @Transactional(readOnly = true)
    public ReponseEvaluationResultDTO getEvaluationResult(Long idEvaluation, Long noEtudiant) {
        log.debug("Fetching evaluation result for idEvaluation: {} and student: {}", idEvaluation, noEtudiant);

        // Vérifier que l'étudiant existe
        Etudiant etudiant = etudiantRepository.findById(noEtudiant)
                .orElseThrow(() -> new ResourceNotFoundException("Etudiant", "noEtudiant", noEtudiant));

        // Récupérer l'évaluation
        Evaluation evaluation = evaluationRepository.findById(idEvaluation)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", "idEvaluation", idEvaluation));

        // Récupérer la réponse de l'étudiant
        ReponseEvaluation reponseEvaluation = reponseEvaluationRepository
                .findByIdEvaluationAndNoEtudiant(idEvaluation, noEtudiant)
                .orElseThrow(() -> new ResourceNotFoundException("ReponseEvaluation", "idEvaluation", idEvaluation));

        // Récupérer toutes les réponses aux questions
        List<ReponseQuestion> reponsesQuestions = reponseQuestionRepository
                .findByIdReponseEvaluation(reponseEvaluation.getIdReponseEvaluation());

        // Créer une map pour accéder facilement aux réponses par idQuestionEvaluation
        var reponsesMap = reponsesQuestions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        rq -> rq.getId().getIdQuestionEvaluation(),
                        ReponseQuestion::getPositionnement
                ));

        // Récupérer les rubriques de l'évaluation
        List<RubriqueEvaluation> rubriques = rubriqueEvaluationRepository
                .findByIdEvaluationOrderByOrdreAsc(idEvaluation);
        
        log.info("📋 Found {} rubriques for evaluation {}", rubriques.size(), idEvaluation);

        // Construire le DTO avec les rubriques, questions et réponses
        List<ReponseEvaluationResultDTO.RubriqueResultDTO> rubriquesDTO = rubriques.stream()
                .map(rubrique -> {
                    String designationRubrique = rubrique.getDesignation();
                    if ((designationRubrique == null || designationRubrique.isEmpty()) && rubrique.getIdRubrique() != null) {
                        if (rubrique.getRubrique() != null) {
                            designationRubrique = rubrique.getRubrique().getDesignation();
                        }
                    }
                    
                    List<ReponseEvaluationResultDTO.QuestionResultDTO> questionsDTO = new ArrayList<>();
                    int ordre = 1;
                    
                    // 1. Récupérer les questions depuis RUBRIQUE_QUESTION
                    if (rubrique.getIdRubrique() != null) {
                        List<RubriqueQuestion> rubriqueQuestions = rubriqueQuestionRepository
                                .findByIdRubriqueOrderByOrdreAsc(rubrique.getIdRubrique());
                        
                        for (RubriqueQuestion rq : rubriqueQuestions) {
                            if (rq.getQuestion() != null) {
                                String minimal = null;
                                String maximal = null;
                                
                                if (rq.getQuestion().getQualificatif() != null) {
                                    minimal = rq.getQuestion().getQualificatif().getMinimal();
                                    maximal = rq.getQuestion().getQualificatif().getMaximal();
                                }
                                
                                Long idQuestion = rq.getIdQuestion();
                                Long positionnement = reponsesMap.get(idQuestion);
                                
                                questionsDTO.add(ReponseEvaluationResultDTO.QuestionResultDTO.builder()
                                        .idQuestionEvaluation(idQuestion)
                                        .intitule(rq.getQuestion().getIntitule())
                                        .ordre(ordre++)
                                        .minimal(minimal)
                                        .maximal(maximal)
                                        .positionnement(positionnement)
                                        .build());
                            }
                        }
                    }
                    
                    // 2. Ajouter les questions supplémentaires depuis QUESTION_EVALUATION
                    List<QuestionEvaluation> questionsEvaluation = questionEvaluationRepository
                            .findByIdRubriqueEvaluationOrderByOrdreAsc(rubrique.getIdRubriqueEvaluation());
                    
                    for (QuestionEvaluation qe : questionsEvaluation) {
                        String minimal = null;
                        String maximal = null;
                        
                        if (qe.getIdQualificatif() != null) {
                            try {
                                Qualificatif qual = qualificatifRepository.findById(qe.getIdQualificatif()).orElse(null);
                                if (qual != null) {
                                    minimal = qual.getMinimal();
                                    maximal = qual.getMaximal();
                                }
                            } catch (Exception e) {
                                log.warn("Could not fetch qualificatif {} for question {}", qe.getIdQualificatif(), qe.getIdQuestionEvaluation());
                            }
                        }
                        
                        Long positionnement = reponsesMap.get(qe.getIdQuestionEvaluation());
                        
                        questionsDTO.add(ReponseEvaluationResultDTO.QuestionResultDTO.builder()
                                .idQuestionEvaluation(qe.getIdQuestionEvaluation())
                                .intitule(qe.getIntitule())
                                .ordre(ordre++)
                                .minimal(minimal)
                                .maximal(maximal)
                                .positionnement(positionnement)
                                .build());
                    }
                    
                    return ReponseEvaluationResultDTO.RubriqueResultDTO.builder()
                            .idRubriqueEvaluation(rubrique.getIdRubriqueEvaluation())
                            .designation(designationRubrique)
                            .ordre(rubrique.getOrdre())
                            .questions(questionsDTO)
                            .build();
                })
                .collect(Collectors.toList());

        // Informations de l'enseignant
        String nomEnseignant = null;
        String prenomEnseignant = null;
        if (evaluation.getEnseignant() != null) {
            nomEnseignant = evaluation.getEnseignant().getNom();
            prenomEnseignant = evaluation.getEnseignant().getPrenom();
        }

        return ReponseEvaluationResultDTO.builder()
                .idEvaluation(evaluation.getIdEvaluation())
                .nomEnseignant(nomEnseignant)
                .prenomEnseignant(prenomEnseignant)
                .codeUe(evaluation.getCodeUe())
                .codeEc(evaluation.getCodeEc())
                .noEvaluation(evaluation.getNoEvaluation())
                .designation(evaluation.getDesignation())
                .etat(evaluation.getEtat())
                .finReponse(evaluation.getFinReponse())
                .commentaire(reponseEvaluation.getCommentaire())
                .rubriques(rubriquesDTO)
                .build();
    }
}
