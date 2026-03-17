package com.evaluation.backend.service.Evaluation;

import com.evaluation.backend.dto.Droit.DroitRequestDTO;
import com.evaluation.backend.dto.Droit.DroitResponseDTO;
import com.evaluation.backend.dto.Droit.DroitTousRequestDTO;
import com.evaluation.backend.dto.Evaluation.*;
import com.evaluation.backend.dto.Questionnaire.CreateEvaluationFromQuestionnaireRequest;
import com.evaluation.backend.entity.*;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.DroitMapper;
import com.evaluation.backend.mapper.EvaluationMapper;
import com.evaluation.backend.repository.*;
import com.evaluation.backend.service.QuestionService;
import com.evaluation.backend.service.RubriqueService;
import com.evaluation.backend.repository.QuestionEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.dto.Rubrique.RubriqueDTO;
import com.evaluation.backend.exception.DuplicateResourceException;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.dto.Statistiques.QuestionStatDTO;
import com.evaluation.backend.dto.Statistiques.RubriqueStatDTO;
import com.evaluation.backend.dto.Statistiques.StatistiquesEvaluationDTO;
import java.util.HashMap;
import java.util.Map;
import com.evaluation.backend.repository.ReponseQuestionRepository;
import com.evaluation.backend.repository.QualificatifRepository;

import com.evaluation.backend.repository.PromotionRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.evaluation.backend.repository.AuthentificationRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository repository;
    private final EvaluationMapper mapper;
    private final ElementConstitutifRepository elementConstitutifRepository;
    private final FormationRepository formationRepository;
    private final UniteEnseignementRepository uniteEnseignementRepository;
    private final RubriqueEvaluationRepository rubriqueEvaluationRepository;
    private final QuestionEvaluationRepository questionEvaluationRepository;
    private final RubriqueService rubriqueService;
    private final QuestionService questionService;

    private final AuthentificationRepository authentificationRepository;
    private final DroitRepository DroitRepository;
    private final DroitMapper droitMapper;
    private final EnseignantRepository EnseignantRepository;

    private final PromotionRepository promotionRepository;

    private final ReponseQuestionRepository reponseQuestionRepository;
    private final QualificatifRepository    qualificatifRepository;
    private final QuestionRepository questionRepository;

    private final QuestionnaireRepository questionnaireRepository;
    private final RubriqueQuestionnaireRepository rubriqueQuestionnaireRepository;
    private final QuestionQuestionnaireRepository questionQuestionnaireRepository;




    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponseDTO> list(Long noEnseignant, String codeFormation, String anneeUniversitaire) {

        List<Evaluation> res;

        if (noEnseignant != null) {
            res = repository.findByNoEnseignant(noEnseignant);
        } else if (codeFormation != null && anneeUniversitaire != null) {
            res = repository.findByCodeFormationAndAnneeUniversitaire(codeFormation, anneeUniversitaire);
        } else {
            res = repository.findAll();
        }

        return res.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationResponseDTO getById(Long id) {
        Evaluation e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + id));
        return mapper.toResponse(e);
    }

    @Override
    public EvaluationResponseDTO create(EvaluationRequestDTO dto, Long noEnseignant) {
        validateEtat(dto.getEtat());

        Evaluation e = mapper.toEntity(dto);
        e.setNoEnseignant(noEnseignant);

        Short maxNoEvaluation = repository.findMaxNoEvaluation(
                dto.getAnneeUniversitaire(),
                noEnseignant,
                dto.getCodeFormation(),
                dto.getCodeUe()
        );

        short nextNoEvaluation = (short) (maxNoEvaluation + 1);
        e.setNoEvaluation(nextNoEvaluation);

        Evaluation saved = repository.save(e);

        return mapper.toResponse(saved);
    }

    @Override
    public EvaluationResponseDTO update(Long id, EvaluationRequestDTO dto) {
        validateEtat(dto.getEtat());

        Evaluation e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + id));

        mapper.updateEntity(e, dto);

        Evaluation saved = repository.save(e);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Evaluation introuvable : id=" + id);
        }
        repository.deleteById(id);
    }

    private void validateEtat(String etat) {
        if (etat == null) {
            throw new BusinessException("Etat obligatoire (ELA, DIS, CLO)");
        }
        String v = etat.trim().toUpperCase();
        if (!v.equals("ELA") && !v.equals("DIS") && !v.equals("CLO")) {
            throw new BusinessException("Etat invalide. Valeurs possibles: ELA, DIS, CLO");
        }
    }

    @Override
    public List<String> getFormations() {
        return formationRepository.findAllCodeFormations();
    }

    @Override
    public List<String> getCodeUe(String codeFormation) {
        return uniteEnseignementRepository.findCodeUeByCodeFormation(codeFormation);
    }

    @Override
    public List<String> getCodeEc(String codeFormation, String codeUe) {
        return elementConstitutifRepository.findDistinctEcsByFormationAndUe(codeFormation, codeUe);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationWithRubriquesDTO getByIdWithRubriques(Long id) {
        Evaluation evaluation = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + id));

        List<RubriqueEvaluation> rubriquesEvaluation = rubriqueEvaluationRepository
                .findByIdEvaluationOrderByOrdreAsc(id);

        List<RubriqueEvaluationDTO> rubriqueDTOs = new ArrayList<>();

        for (RubriqueEvaluation re : rubriquesEvaluation) {

            // ← MODIFIÉ : on passe designation(re.getDesignation()) dans le builder
            RubriqueEvaluationDTO dto = RubriqueEvaluationDTO.builder()
                    .idRubriqueEvaluation(re.getIdRubriqueEvaluation())
                    .idEvaluation(re.getIdEvaluation())
                    .idRubrique(re.getIdRubrique())
                    .ordre(re.getOrdre())
                    .designation(re.getDesignation())
                    .build();

            // Si c'est une rubrique standard ou personnelle (pas composée)
            if (re.getIdRubrique() != null) {
                RubriqueDTO rubrique = rubriqueService.getRubriqueById(re.getIdRubrique());

                // ← MODIFIÉ : on utilise la designation de RubriqueEvaluation si elle existe,
                //             sinon on tombe back sur celle de la rubrique source
                dto.setDesignation(re.getDesignation() != null ? re.getDesignation() : rubrique.getDesignation());
                dto.setType(rubrique.getType());

                // Récupérer les questions de QUESTION_EVALUATION
                List<QuestionEvaluation> questionsEval = questionEvaluationRepository
                        .findByIdRubriqueEvaluationOrderByOrdreAsc(re.getIdRubriqueEvaluation());

                if (!questionsEval.isEmpty()) {
                    List<QuestionWithQualificatifDTO> questions = new ArrayList<>();
                    for (QuestionEvaluation qe : questionsEval) {
                        QuestionWithQualificatifDTO q = questionService.getQuestionWithQualificatifById(qe.getIdQuestion());
                        q.setIdQuestionQuestionnaire(qe.getIdQuestionEvaluation());
                        q.setOrdre(qe.getOrdre());
                        questions.add(q);
                    }
                    dto.setQuestions(questions);
                } else {
                    dto.setQuestions(rubrique.getQuestions());
                }
            } else {
                // Rubrique composée : récupérer les questions via QuestionEvaluation
                List<QuestionEvaluation> questionsEval = questionEvaluationRepository
                        .findByIdRubriqueEvaluationOrderByOrdreAsc(re.getIdRubriqueEvaluation());

                List<QuestionWithQualificatifDTO> questions = new ArrayList<>();
                for (QuestionEvaluation qe : questionsEval) {
                    QuestionWithQualificatifDTO q = questionService.getQuestionWithQualificatifById(qe.getIdQuestion());
                    q.setOrdre(qe.getOrdre());
                    q.setIdQuestionQuestionnaire(qe.getIdQuestionEvaluation());
                    questions.add(q);
                }
                dto.setQuestions(questions);
            }

            rubriqueDTOs.add(dto);
        }

        return EvaluationWithRubriquesDTO.builder()
                .idEvaluation(evaluation.getIdEvaluation())
                .noEnseignant(evaluation.getNoEnseignant())
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
                .rubriques(rubriqueDTOs)
                .build();
    }

    @Override
    public RubriqueEvaluationDTO addRubriqueToEvaluation(Long evaluationId, AddRubriqueToEvaluationRequest request, Long noEnseignant) {
        log.debug("Adding rubrique {} to evaluation {}", request.getIdRubrique(), evaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        RubriqueDTO rubriqueDTO = rubriqueService.getRubriqueById(request.getIdRubrique());

        if (rubriqueEvaluationRepository.existsByIdEvaluationAndIdRubrique(evaluationId, request.getIdRubrique())) {
            throw new DuplicateResourceException("RubriqueEvaluation", "evaluation and rubrique",
                    evaluationId + " - " + request.getIdRubrique());
        }

        Integer ordre = request.getOrdre();
        if (ordre == null) {
            Integer maxOrdre = rubriqueEvaluationRepository.findMaxOrdreByEvaluation(evaluationId);
            ordre = maxOrdre == null ? 1 : maxOrdre + 1;
        }

        RubriqueEvaluation rubriqueEvaluation = RubriqueEvaluation.builder()
                .idEvaluation(evaluationId)
                .idRubrique(request.getIdRubrique())
                .ordre(ordre)
                .designation(request.getDesignation())
                .build();

        RubriqueEvaluation saved = rubriqueEvaluationRepository.save(rubriqueEvaluation);

        List<QuestionWithQualificatifDTO> questionsRubrique = rubriqueDTO.getQuestions();
        if (questionsRubrique != null && !questionsRubrique.isEmpty()) {
            int ordreQuestion = 1;
            for (QuestionWithQualificatifDTO q : questionsRubrique) {
                QuestionEvaluation qe = QuestionEvaluation.builder()
                        .idRubriqueEvaluation(saved.getIdRubriqueEvaluation())
                        .idQuestion(q.getIdQuestion())
                        .ordre(ordreQuestion++)
                        .build();
                questionEvaluationRepository.save(qe);
            }
            log.info("Copied {} questions from rubrique {} to question_evaluation",
                    questionsRubrique.size(), request.getIdRubrique());
        }
        log.info("Added rubrique {} to evaluation {} with ordre {}", request.getIdRubrique(), evaluationId, ordre);

        return RubriqueEvaluationDTO.builder()
                .idRubriqueEvaluation(saved.getIdRubriqueEvaluation())
                .idEvaluation(saved.getIdEvaluation())
                .idRubrique(saved.getIdRubrique())
                .ordre(saved.getOrdre())
                .designation(saved.getDesignation())
                .type(rubriqueDTO.getType())
                .questions(rubriqueDTO.getQuestions())
                .build();
    }

    @Override
    public void removeRubriqueFromEvaluation(Long evaluationId, Long rubriqueEvaluationId, Long noEnseignant) {
        log.debug("Removing rubrique evaluation {} from evaluation {}", rubriqueEvaluationId, evaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        rubriqueEvaluationRepository.deleteByIdEvaluationAndIdRubriqueEvaluation(evaluationId, rubriqueEvaluationId);
        log.info("Removed rubrique evaluation {} from evaluation {}", rubriqueEvaluationId, evaluationId);
    }

    @Override
    public void reorderRubriquesInEvaluation(Long evaluationId, ReorderRubriquesInEvaluationRequest request, Long noEnseignant) {
        log.debug("Reordering rubriques in evaluation {}", evaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        for (ReorderRubriquesInEvaluationRequest.RubriqueEvaluationOrder reo : request.getRubriqueOrders()) {
            RubriqueEvaluation re = rubriqueEvaluationRepository
                    .findById(reo.getIdRubriqueEvaluation())
                    .orElseThrow(() -> new ResourceNotFoundException("RubriqueEvaluation",
                            "idRubriqueEvaluation", reo.getIdRubriqueEvaluation()));

            if (!re.getIdEvaluation().equals(evaluationId)) {
                throw new BusinessException("Rubrique evaluation " + reo.getIdRubriqueEvaluation() +
                        " does not belong to evaluation " + evaluationId);
            }

            re.setOrdre(reo.getOrdre());
            rubriqueEvaluationRepository.save(re);
        }

        log.info("Reordered {} rubriques in evaluation {}", request.getRubriqueOrders().size(), evaluationId);
    }

    @Override
    public RubriqueEvaluationDTO addQuestionToRubriqueEvaluation(Long evaluationId, Long rubriqueEvaluationId,
                                                                 AddQuestionToRubriqueEvaluationRequest request, Long noEnseignant) {
        log.debug("Adding question {} to rubrique evaluation {}", request.getIdQuestion(), rubriqueEvaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        RubriqueEvaluation rubriqueEvaluation = rubriqueEvaluationRepository.findById(rubriqueEvaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("RubriqueEvaluation", "id", rubriqueEvaluationId));

        if (!rubriqueEvaluation.getIdEvaluation().equals(evaluationId)) {
            throw new BusinessException("RubriqueEvaluation does not belong to this evaluation");
        }

        if (!questionService.existsById(request.getIdQuestion())) {
            throw new ResourceNotFoundException("Question", "idQuestion", request.getIdQuestion());
        }

        if (questionEvaluationRepository.existsByIdRubriqueEvaluationAndIdQuestion(
                rubriqueEvaluationId, request.getIdQuestion())) {
            throw new DuplicateResourceException("QuestionEvaluation", "rubrique and question",
                    rubriqueEvaluationId + " - " + request.getIdQuestion());
        }

        Integer ordre = request.getOrdre();
        if (ordre == null) {
            Integer maxOrdre = questionEvaluationRepository.findMaxOrdreByRubriqueEvaluation(rubriqueEvaluationId);
            ordre = maxOrdre == null ? 1 : maxOrdre + 1;
        }

        QuestionEvaluation questionEvaluation = QuestionEvaluation.builder()
                .idRubriqueEvaluation(rubriqueEvaluationId)
                .idQuestion(request.getIdQuestion())
                .ordre(ordre)
                .build();

        questionEvaluationRepository.save(questionEvaluation);
        log.info("Added question {} to rubrique evaluation {}", request.getIdQuestion(), rubriqueEvaluationId);

        EvaluationWithRubriquesDTO evalWithRub = getByIdWithRubriques(evaluationId);
        return evalWithRub.getRubriques().stream()
                .filter(r -> r.getIdRubriqueEvaluation().equals(rubriqueEvaluationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("RubriqueEvaluation", "id", rubriqueEvaluationId));
    }

    @Override
    public void removeQuestionFromRubriqueEvaluation(Long evaluationId, Long rubriqueEvaluationId,
                                                     Long questionEvaluationId, Long noEnseignant) {
        log.debug("Removing question evaluation {} from rubrique evaluation {}", questionEvaluationId, rubriqueEvaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        questionEvaluationRepository.deleteById(questionEvaluationId);
        log.info("Removed question evaluation {} from rubrique evaluation {}", questionEvaluationId, rubriqueEvaluationId);
    }

    @Override
    public void reorderQuestionsInRubriqueEvaluation(Long evaluationId, Long rubriqueEvaluationId,
                                                     ReorderQuestionsInRubriqueEvaluationRequest request, Long noEnseignant) {
        log.debug("Reordering questions in rubrique evaluation {}", rubriqueEvaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        for (ReorderQuestionsInRubriqueEvaluationRequest.QuestionEvaluationOrder qeo : request.getQuestionOrders()) {
            QuestionEvaluation qe = questionEvaluationRepository.findById(qeo.getIdQuestionEvaluation())
                    .orElseThrow(() -> new ResourceNotFoundException("QuestionEvaluation",
                            "idQuestionEvaluation", qeo.getIdQuestionEvaluation()));

            if (!qe.getIdRubriqueEvaluation().equals(rubriqueEvaluationId)) {
                throw new BusinessException("Question evaluation " + qeo.getIdQuestionEvaluation() +
                        " does not belong to rubrique evaluation " + rubriqueEvaluationId);
            }

            qe.setOrdre(qeo.getOrdre());
            questionEvaluationRepository.save(qe);
        }

        log.info("Reordered {} questions in rubrique evaluation {}", request.getQuestionOrders().size(), rubriqueEvaluationId);
    }

    private Long currentNoEnseignant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException("Utilisateur non authentifié");
        }

        String email = authentication.getName();
        Authentification auth = authentificationRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé : " + email));

        if (auth.getEnseignant() == null || auth.getEnseignant().getId() == null) {
            throw new BusinessException("Accès interdit : utilisateur non enseignant");
        }

        return auth.getEnseignant().getId().longValue();
    }

    private Evaluation getOwnedEvaluationOrThrow(Long idEvaluation) {
        Evaluation eval = repository.findById(idEvaluation)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + idEvaluation));

        Long owner = currentNoEnseignant();
        if (eval.getNoEnseignant() == null || !eval.getNoEnseignant().equals(owner)) {
            throw new BusinessException("Accès interdit : vous n'êtes pas propriétaire de cette évaluation.");
        }
        return eval;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponseDTO> listEvaluationsPartagees() {

        Long noEnseignant = currentNoEnseignant();
        List<Droit> droits = DroitRepository.findByNoEnseignant(noEnseignant);

        return droits.stream()
                .filter(d ->
                        "O".equalsIgnoreCase(d.getConsultation()) ||
                                "O".equalsIgnoreCase(d.getDuplication())
                )
                .map(d -> repository.findById(d.getIdEvaluation()).orElse(null))
                .filter(e -> e != null)
                .filter(e -> e.getNoEnseignant() == null || !e.getNoEnseignant().equals(noEnseignant))
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public EvaluationResponseDTO dupliquerEvaluation(Long idEvaluation) {

        Long noEnseignant = currentNoEnseignant();

        Evaluation source = repository.findById(idEvaluation)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + idEvaluation));

        boolean isOwner = source.getNoEnseignant() != null && source.getNoEnseignant().equals(noEnseignant);

        boolean hasDupRight = DroitRepository.findByIdEvaluationAndNoEnseignant(idEvaluation, noEnseignant)
                .map(d -> "O".equalsIgnoreCase(d.getDuplication()))
                .orElse(false);

        if (!isOwner && !hasDupRight) {
            throw new BusinessException("Duplication interdite : vous n'avez pas le droit de duplication sur cette évaluation.");
        }

        Evaluation copy = new Evaluation();
        copy.setIdEvaluation(null);
        copy.setNoEnseignant(noEnseignant);
        copy.setCodeFormation(source.getCodeFormation());
        copy.setAnneeUniversitaire(source.getAnneeUniversitaire());
        copy.setCodeUe(source.getCodeUe());
        copy.setCodeEc(source.getCodeEc());

        Short maxNoEval = repository.findMaxNoEvaluation(
                source.getAnneeUniversitaire(),
                noEnseignant,
                source.getCodeFormation(),
                source.getCodeUe()
        );

        short nextNoEval = (short) ((maxNoEval != null ? maxNoEval : 0) + 1);

        copy.setNoEvaluation(nextNoEval);
        copy.setDesignation(source.getDesignation());
        copy.setEtat(source.getEtat());
        copy.setPeriode(source.getPeriode());
        copy.setDebutReponse(source.getDebutReponse());
        copy.setFinReponse(source.getFinReponse());

        return mapper.toResponse(repository.save(copy));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DroitResponseDTO> listDroits(Long idEvaluation) {
        getOwnedEvaluationOrThrow(idEvaluation);
        return DroitRepository.findByIdEvaluation(idEvaluation)
                .stream()
                .map(droitMapper::toResponse)
                .toList();
    }

    @Override
    public DroitResponseDTO upsertDroit(Long idEvaluation, DroitRequestDTO dto) {

        Evaluation eval = getOwnedEvaluationOrThrow(idEvaluation);
        Long owner = eval.getNoEnseignant();

        if (dto.getNoEnseignant().equals(owner)) {
            throw new BusinessException("Impossible de vous attribuer un droit à vous-même.");
        }

        Droit droit = DroitRepository.findByIdEvaluationAndNoEnseignant(idEvaluation, dto.getNoEnseignant())
                .orElseGet(() -> {
                    Droit d = new Droit();
                    d.setIdEvaluation(idEvaluation);
                    d.setNoEnseignant(dto.getNoEnseignant());
                    return d;
                });

        droitMapper.apply(droit, dto);

        return droitMapper.toResponse(DroitRepository.save(droit));
    }

    @Override
    public void deleteDroit(Long idEvaluation, Long noEnseignantCible) {
        getOwnedEvaluationOrThrow(idEvaluation);

        DroitId id = new DroitId(idEvaluation, noEnseignantCible);
        if (!DroitRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Droit introuvable (idEvaluation=" + idEvaluation + ", noEnseignant=" + noEnseignantCible + ")"
            );
        }
        DroitRepository.deleteById(id);
    }

    @Override
    public DroitResponseDTO donnerDroitATous(Long idEvaluation, DroitTousRequestDTO dto) {

        Evaluation eval = getOwnedEvaluationOrThrow(idEvaluation);
        Long owner = eval.getNoEnseignant();

        boolean dup = Boolean.TRUE.equals(dto.getDuplication());
        boolean cons = dup || Boolean.TRUE.equals(dto.getConsultation());

        List<Integer> allIds = EnseignantRepository.findAllIds();

        for (Integer idEns : allIds) {
            Long cible = Long.valueOf(idEns);

            if (cible.equals(owner)) continue;

            Droit droit = DroitRepository.findByIdEvaluationAndNoEnseignant(idEvaluation, cible)
                    .orElseGet(() -> {
                        Droit d = new Droit();
                        d.setIdEvaluation(idEvaluation);
                        d.setNoEnseignant(cible);
                        return d;
                    });

            droit.setConsultation(cons ? "O" : "N");
            droit.setDuplication(dup ? "O" : "N");

            DroitRepository.save(droit);
        }

        return DroitResponseDTO.builder()
                .idEvaluation(idEvaluation)
                .noEnseignant(-1L)
                .consultation(cons ? "O" : "N")
                .duplication(dup ? "O" : "N")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAnneesUniversitaires(String codeFormation) {
        return promotionRepository.findAnneesUniversitairesByCodeFormation(codeFormation);
    }

    // Changer l'etat d'evaluation - Achraf EL AIDI IDRISSI
    @Override
    public EvaluationResponseDTO updateEtat(Long evaluationId, String etat) {
        Evaluation eval = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        System.out.println("Etat actuel = " + eval.getEtat());
        System.out.println("Etat demandé = " + etat);

        if (eval.getEtat().equals("ELA") && etat.equals("DIS")) {
            eval.setEtat("DIS");
        } else if (eval.getEtat().equals("DIS") && etat.equals("CLO")) {
            eval.setEtat("CLO");
        } else {
            throw new RuntimeException("Transition d'état non autorisée");
        }

        repository.save(eval);
        return mapper.toResponse(eval);
    }

    // ranya
    @Override
    public RubriqueEvaluationDTO updateDesignationRubriqueEvaluation(
            Long evaluationId,
            Long rubriqueEvaluationId,
            String designation,
            Long noEnseignant) {

        log.debug("Updating designation of rubrique evaluation {} in evaluation {}",
                rubriqueEvaluationId, evaluationId);

        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException(
                    "Vous n'avez pas le droit de modifier cette évaluation");
        }

        RubriqueEvaluation rubriqueEvaluation = rubriqueEvaluationRepository
                .findById(rubriqueEvaluationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RubriqueEvaluation introuvable : id=" + rubriqueEvaluationId));

        if (!rubriqueEvaluation.getIdEvaluation().equals(evaluationId)) {
            throw new BusinessException(
                    "Cette rubrique n'appartient pas à cette évaluation");
        }

        rubriqueEvaluation.setDesignation(designation);
        rubriqueEvaluationRepository.save(rubriqueEvaluation);

        log.info("Updated designation of rubrique evaluation {} to '{}'",
                rubriqueEvaluationId, designation);

        EvaluationWithRubriquesDTO evalWithRub = getByIdWithRubriques(evaluationId);
        return evalWithRub.getRubriques().stream()
                .filter(r -> r.getIdRubriqueEvaluation().equals(rubriqueEvaluationId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "RubriqueEvaluation", "id", rubriqueEvaluationId));
    }

    @Override
    @Transactional(readOnly = true)
    public StatistiquesEvaluationDTO getStatistiques(Long idEvaluation) {

        Evaluation evaluation = repository.findById(idEvaluation)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + idEvaluation));

        if (!"CLO".equals(evaluation.getEtat())) {
            throw new BusinessException("Les statistiques sont disponibles uniquement pour une évaluation clôturée.");
        }

        Long totalRepondants = reponseQuestionRepository.countRepondantsByEvaluation(idEvaluation);

        List<RubriqueEvaluation> rubriques = rubriqueEvaluationRepository
                .findByIdEvaluationOrderByOrdreAsc(idEvaluation);

        List<Object[]> rawStats = reponseQuestionRepository.findRawStatsByEvaluation(idEvaluation);

        Map<Long, Object[]> statsMap = new HashMap<>();
        for (Object[] row : rawStats) {
            Long idQE = ((Number) row[0]).longValue();
            statsMap.put(idQE, row);
        }

        List<RubriqueStatDTO> rubriqueDTOs = new ArrayList<>();

        for (RubriqueEvaluation rubrique : rubriques) {

            // ── Désignation depuis RUBRIQUE.DESIGNATION ──────────────────────
            String designationRubrique = null;
            if (rubrique.getIdRubrique() != null) {
                try {
                    designationRubrique = rubriqueService
                            .getRubriqueById(rubrique.getIdRubrique())
                            .getDesignation();
                } catch (Exception ignored) {}
            }
            if (designationRubrique == null) {
                designationRubrique = rubrique.getDesignation();
            }

            List<QuestionEvaluation> questions = questionEvaluationRepository
                    .findByIdRubriqueEvaluationOrderByOrdreAsc(rubrique.getIdRubriqueEvaluation());


            List<QuestionStatDTO> questionDTOs = new ArrayList<>();

            for (QuestionEvaluation qe : questions) {

                // ── Intitulé + Qualificatif depuis QUESTION ───────────────────
                String intitule = qe.getIntitule();
                String minimal  = null;
                String maximal  = null;

                if (qe.getIdQuestion() != null) {
                    questionRepository.findById(qe.getIdQuestion()).ifPresent(question -> {
                        // stocker dans tableau pour accès depuis lambda
                    });

                    var questionOpt = questionRepository.findById(qe.getIdQuestion());
                    if (questionOpt.isPresent()) {
                        var question = questionOpt.get();

                        if (intitule == null) {
                            intitule = question.getIntitule();
                        }

                        // Qualificatif : d'abord sur QE, sinon sur QUESTION
                        Long idQualificatif = qe.getIdQualificatif();
                        if (idQualificatif == null && question.getIdQualificatif() != null) {
                            try {
                                idQualificatif = Long.valueOf(question.getIdQualificatif());
                            } catch (NumberFormatException ignored) {}
                        }

                        if (idQualificatif != null) {
                            var qualOpt = qualificatifRepository.findById(idQualificatif);
                            if (qualOpt.isPresent()) {
                                minimal = qualOpt.get().getMinimal();
                                maximal = qualOpt.get().getMaximal();
                            }
                        }
                    }
                }

                // ── Stats depuis la Map ───────────────────────────────────────
                Object[] row = statsMap.get(qe.getIdQuestionEvaluation());

                QuestionStatDTO dto;
                if (row != null) {
                    dto = QuestionStatDTO.builder()
                            .idQuestionEvaluation(qe.getIdQuestionEvaluation())
                            .ordre(qe.getOrdre())
                            .intitule(intitule)
                            .minimal(minimal)
                            .maximal(maximal)
                            .nbRepondants(row[4]  != null ? ((Number) row[4]).longValue()   : 0L)
                            .moyenne(     row[5]  != null ? ((Number) row[5]).doubleValue() : null)
                            .minimum(     row[6]  != null ? ((Number) row[6]).longValue()   : null)
                            .maximum(     row[7]  != null ? ((Number) row[7]).longValue()   : null)
                            .ecartType(   row[8]  != null ? ((Number) row[8]).doubleValue() : null)
                            .mediane(     row[9]  != null ? ((Number) row[9]).doubleValue() : null)
                            .nb1(         row[10] != null ? ((Number) row[10]).longValue()  : 0L)
                            .nb2(         row[11] != null ? ((Number) row[11]).longValue()  : 0L)
                            .nb3(         row[12] != null ? ((Number) row[12]).longValue()  : 0L)
                            .nb4(         row[13] != null ? ((Number) row[13]).longValue()  : 0L)
                            .nb5(         row[14] != null ? ((Number) row[14]).longValue()  : 0L)
                            .build();
                } else {
                    dto = QuestionStatDTO.builder()
                            .idQuestionEvaluation(qe.getIdQuestionEvaluation())
                            .ordre(qe.getOrdre())
                            .intitule(intitule)
                            .minimal(minimal)
                            .maximal(maximal)
                            .nbRepondants(0L)
                            .nb1(0L).nb2(0L).nb3(0L).nb4(0L).nb5(0L)
                            .build();
                }

                questionDTOs.add(dto);
            }

            rubriqueDTOs.add(RubriqueStatDTO.builder()
                    .idRubriqueEvaluation(rubrique.getIdRubriqueEvaluation())
                    .ordre(rubrique.getOrdre())
                    .designation(designationRubrique)
                    .questions(questionDTOs)
                    .build());
        }
        String emailEnseignant = authentificationRepository
                .findByEnseignantId(evaluation.getNoEnseignant().intValue())
                .map(Authentification::getEmail)
                .orElse("");


        return StatistiquesEvaluationDTO.builder()
                .idEvaluation(evaluation.getIdEvaluation())
                .designation(evaluation.getDesignation())
                .codeFormation(evaluation.getCodeFormation())
                .anneeUniversitaire(evaluation.getAnneeUniversitaire())
                .codeUe(evaluation.getCodeUe())
                .codeEc(evaluation.getCodeEc())
                .noEvaluation(evaluation.getNoEvaluation())
                .etat(evaluation.getEtat())
                .periode(evaluation.getPeriode())
                .debutReponse(evaluation.getDebutReponse())
                .finReponse(evaluation.getFinReponse())
                .totalRepondants(totalRepondants)
                .rubriques(rubriqueDTOs)
                .emailEnseignant(emailEnseignant)
                .build();
    }

    public byte[] generateStatistiquesPdf(Long idEvaluation) throws Exception {
        StatistiquesEvaluationDTO stats = getStatistiques(idEvaluation);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 100, 60);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        Font bold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
        Font small = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

        String today = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.FRENCH)
                .format(new java.util.Date());
        String email = stats.getEmailEnseignant() != null ? stats.getEmailEnseignant() : "";

        // ── Header + Footer sur chaque page ──────────────────────────────────
        writer.setPageEvent(new PdfPageEventHelper() {

            private void drawHeader(PdfContentByte cb, Document doc) {
                try {
                    float pageWidth = doc.getPageSize().getWidth();
                    float top = doc.getPageSize().getHeight() - 20;

                    // Ligne 1 : M2DOSI | titre | année
                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false), 11);
                    cb.beginText();
                    cb.showTextAligned(Element.ALIGN_LEFT,  stats.getCodeFormation(), 36, top, 0);
                    cb.showTextAligned(Element.ALIGN_CENTER, "Evaluation d'un enseignement", pageWidth / 2, top, 0);
                    cb.showTextAligned(Element.ALIGN_RIGHT, stats.getAnneeUniversitaire(), pageWidth - 36, top, 0);
                    cb.endText();

                    // Ligne séparatrice
                    cb.setLineWidth(0.5f);
                    cb.moveTo(36, top - 6);
                    cb.lineTo(pageWidth - 36, top - 6);
                    cb.stroke();

                    // Ligne 2 : UE | EC | Période (petite police)
                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false), 8);
                    cb.beginText();
                    cb.showTextAligned(Element.ALIGN_LEFT,
                            "UE: " + stats.getCodeUe() + "   EC: " + (stats.getCodeEc() != null ? stats.getCodeEc() : "—") + "   Période: " + (stats.getPeriode() != null ? stats.getPeriode() : "—"),
                            36, top - 16, 0);
                    cb.endText();

                } catch (Exception ignored) {}
            }

            private void drawFooter(PdfContentByte cb, Document doc, int pageNumber) {
                try {
                    float pageWidth = doc.getPageSize().getWidth();
                    float bottom = 20;

                    cb.setLineWidth(0.3f);
                    cb.moveTo(36, bottom + 10);
                    cb.lineTo(pageWidth - 36, bottom + 10);
                    cb.stroke();

                    cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false), 8);
                    cb.beginText();
                    cb.showTextAligned(Element.ALIGN_LEFT, email + "  —  " + today, 36, bottom, 0);
                    cb.showTextAligned(Element.ALIGN_RIGHT, "Page " + pageNumber, pageWidth - 36, bottom, 0);
                    cb.endText();

                } catch (Exception ignored) {}
            }

            @Override
            public void onEndPage(PdfWriter w, Document doc) {
                PdfContentByte cb = w.getDirectContent();
                drawHeader(cb, doc);
                drawFooter(cb, doc, w.getPageNumber());
            }
        });

        document.open();

        // ── Métadonnées (première page seulement) ────────────────────────────
        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(55);
        meta.setHorizontalAlignment(Element.ALIGN_LEFT);
        meta.setWidths(new float[]{3f, 3f});
        meta.setSpacingBefore(10f);
        addMetaRow(meta, "Unité d'Enseignement", stats.getCodeUe(), bold, normal);
        addMetaRow(meta, "Elément Constitutif", stats.getCodeEc() != null ? stats.getCodeEc() : "—", bold, normal);
        addMetaRow(meta, "Période", stats.getPeriode() != null ? stats.getPeriode() : "—", bold, normal);
        document.add(meta);
        document.add(Chunk.NEWLINE);

        // ── Rubriques ────────────────────────────────────────────────────────
        for (RubriqueStatDTO rubrique : stats.getRubriques()) {
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5f, 2.5f, 1f, 1f, 1f, 1f, 1f, 2.5f, 1.5f});
            table.setSpacingBefore(8f);
            table.setKeepTogether(true); // ← ne pas couper une rubrique

            // Titre rubrique
            PdfPCell rubCell = new PdfPCell(new Phrase(rubrique.getDesignation(), bold));
            rubCell.setColspan(9);
            rubCell.setBackgroundColor(new BaseColor(170, 170, 170));
            rubCell.setPadding(4);
            table.addCell(rubCell);

            // Sous-header
            table.addCell(makeHeaderCell("", bold));
            table.addCell(makeHeaderCell("Minimum", small));
            for (String n : new String[]{"1","2","3","4","5"})
                table.addCell(makeHeaderCell(n, bold));
            table.addCell(makeHeaderCell("Maximum", small));
            table.addCell(makeHeaderCell("Moyen", small));

            // Questions
            for (QuestionStatDTO q : rubrique.getQuestions()) {
                table.addCell(makeCell(q.getIntitule(), normal, Element.ALIGN_LEFT));
                table.addCell(makeCell(q.getMinimal(), normal, Element.ALIGN_LEFT));
                table.addCell(makeCell(str(q.getNb1()), normal, Element.ALIGN_CENTER));
                table.addCell(makeCell(str(q.getNb2()), normal, Element.ALIGN_CENTER));
                table.addCell(makeCell(str(q.getNb3()), normal, Element.ALIGN_CENTER));
                table.addCell(makeCell(str(q.getNb4()), normal, Element.ALIGN_CENTER));
                table.addCell(makeCell(str(q.getNb5()), normal, Element.ALIGN_CENTER));
                table.addCell(makeCell(q.getMaximal(), normal, Element.ALIGN_LEFT));

                PdfPCell moyCell = new PdfPCell(new Phrase(
                        q.getMoyenne() != null ? String.format("%.1f", q.getMoyenne()) : "—", bold));
                moyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                moyCell.setPadding(3);
                if (q.getMoyenne() != null) moyCell.setBackgroundColor(moyenneColor(q.getMoyenne()));
                table.addCell(moyCell);
            }

            document.add(table);
        }

        // ── Commentaires ─────────────────────────────────────────────────────
        PdfPTable comm = new PdfPTable(1);
        comm.setWidthPercentage(100);
        comm.setSpacingBefore(10f);
        comm.setKeepTogether(true);
        PdfPCell commHead = new PdfPCell(new Phrase("Commentaires", bold));
        commHead.setBackgroundColor(new BaseColor(170, 170, 170));
        commHead.setPadding(4);
        comm.addCell(commHead);
        PdfPCell commBody = new PdfPCell(new Phrase(" "));
        commBody.setMinimumHeight(50f);
        comm.addCell(commBody);
        document.add(comm);

        document.close();
        return baos.toByteArray();
    }
    private void addMetaRow(PdfPTable t, String label, String val, Font bold, Font normal) {
        PdfPCell l = new PdfPCell(new Phrase(label, bold)); l.setPadding(3); t.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(val, normal)); v.setPadding(3); t.addCell(v);
    }

    private PdfPCell makeCell(String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "—", font));
        c.setHorizontalAlignment(align); c.setPadding(3); return c;
    }

    private PdfPCell makeHeaderCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new BaseColor(210, 210, 210));
        c.setHorizontalAlignment(Element.ALIGN_CENTER); c.setPadding(3); return c;
    }

    private String str(Long v) { return v != null ? String.valueOf(v) : "0"; }

    private BaseColor moyenneColor(double m) {
        double ratio = Math.max(0, Math.min(1, (m - 1) / 4.0));
        double h = ratio * 120.0 / 360.0, s = 0.75, l = 0.40;
        double q = l < 0.5 ? l*(1+s) : l+s-l*s, p = 2*l-q;
        return new BaseColor(
                (int)(h2rgb(p,q,h+1.0/3)*255),
                (int)(h2rgb(p,q,h)*255),
                (int)(h2rgb(p,q,h-1.0/3)*255));
    }

    private double h2rgb(double p, double q, double t) {
        if (t<0) t+=1; if (t>1) t-=1;
        if (t<1.0/6) return p+(q-p)*6*t;
        if (t<1.0/2) return q;
        if (t<2.0/3) return p+(q-p)*(2.0/3-t)*6;
        return p;
    }

    @Override
    @Transactional
    public EvaluationResponseDTO createFromQuestionnaire(
            CreateEvaluationFromQuestionnaireRequest dto,
            Long noEnseignant
    ) {

        Questionnaire questionnaire = questionnaireRepository
                .findById(dto.getIdQuestionnaire())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Questionnaire introuvable"
                ));

        Evaluation e = new Evaluation();

        e.setNoEnseignant(noEnseignant);
        e.setCodeFormation(dto.getCodeFormation());
        e.setAnneeUniversitaire(dto.getAnneeUniversitaire());
        e.setCodeUe(dto.getCodeUe());
        e.setCodeEc(dto.getCodeEc());
        e.setDesignation(dto.getDesignation());
        e.setPeriode(dto.getPeriode());
        e.setEtat("ELA");
        e.setDebutReponse(dto.getDebutReponse());
        e.setFinReponse(dto.getFinReponse());

        Short maxNoEvaluation = repository.findMaxNoEvaluation(
                dto.getAnneeUniversitaire(),
                noEnseignant,
                dto.getCodeFormation(),
                dto.getCodeUe()
        );

        short nextNoEvaluation = (short) (maxNoEvaluation + 1);
        e.setNoEvaluation(nextNoEvaluation);

        Evaluation savedEvaluation = repository.save(e);

        copyQuestionnaireStructure(questionnaire, savedEvaluation);

        return mapper.toResponse(savedEvaluation);
    }

    private void copyQuestionnaireStructure(
            Questionnaire questionnaire,
            Evaluation evaluation
    ) {

        List<RubriqueQuestionnaire> rubriques =
                rubriqueQuestionnaireRepository
                        .findByIdQuestionnaireOrderByOrdreAsc(
                                questionnaire.getIdQuestionnaire()
                        );

        for (RubriqueQuestionnaire rq : rubriques) {

            RubriqueEvaluation re = new RubriqueEvaluation();

            re.setIdEvaluation(evaluation.getIdEvaluation());
            re.setIdRubrique(rq.getIdRubrique());
            re.setDesignation(rq.getDesignation());
            re.setOrdre(rq.getOrdre());

            RubriqueEvaluation savedRubrique =
                    rubriqueEvaluationRepository.save(re);

            copyQuestions(rq, savedRubrique);
        }
    }

    private void copyQuestions(
            RubriqueQuestionnaire rq,
            RubriqueEvaluation re
    ) {

        List<QuestionQuestionnaire> questions =
                questionQuestionnaireRepository
                        .findByIdRubriqueQuestionnaireOrderByOrdreAsc(
                                rq.getIdRubriqueQuestionnaire()
                        );

        for (QuestionQuestionnaire qq : questions) {

            QuestionEvaluation qe = new QuestionEvaluation();

            qe.setIdRubriqueEvaluation(re.getIdRubriqueEvaluation());
            qe.setIdQuestion(qq.getIdQuestion());
            qe.setIdQualificatif(qq.getIdQualificatif());
            qe.setOrdre(qq.getOrdre());
            qe.setIntitule(qq.getIntitule());

            questionEvaluationRepository.save(qe);
        }
    }

}