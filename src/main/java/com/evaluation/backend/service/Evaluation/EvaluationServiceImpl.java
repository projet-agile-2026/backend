package com.evaluation.backend.service.Evaluation;

import com.evaluation.backend.dto.Droit.DroitRequestDTO;
import com.evaluation.backend.dto.Droit.DroitResponseDTO;
import com.evaluation.backend.dto.Droit.DroitTousRequestDTO;
import com.evaluation.backend.dto.Evaluation.*;
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


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.evaluation.backend.repository.AuthentificationRepository;






import java.util.List;

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
                dto.setType(rubrique.getType());

                // Récupérer les questions de QUESTION_EVALUATION
                List<QuestionEvaluation> questionsEval = questionEvaluationRepository
                        .findByIdRubriqueEvaluationOrderByOrdreAsc(re.getIdRubriqueEvaluation());

                if (!questionsEval.isEmpty()) {
                    // Si des questions spécifiques à l'évaluation existent, les utiliser
                    List<QuestionWithQualificatifDTO> questions = new ArrayList<>();
                    for (QuestionEvaluation qe : questionsEval) {
                        QuestionWithQualificatifDTO q = questionService.getQuestionWithQualificatifById(qe.getIdQuestion());
                        q.setIdQuestionEvaluation(qe.getIdQuestionEvaluation());
                        q.setOrdre(qe.getOrdre());
                        questions.add(q);
                    }
                    dto.setQuestions(questions);
                } else {
                    // Sinon, utiliser les questions par défaut de la rubrique
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
                    q.setIdQuestionEvaluation(qe.getIdQuestionEvaluation());
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

        // Vérifier que l'évaluation existe et appartient à l'enseignant
        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        // Vérifier que la rubrique existe
        RubriqueDTO rubriqueDTO = rubriqueService.getRubriqueById(request.getIdRubrique());

        // Vérifier qu'elle n'est pas déjà dans l'évaluation
        if (rubriqueEvaluationRepository.existsByIdEvaluationAndIdRubrique(evaluationId, request.getIdRubrique())) {
            throw new DuplicateResourceException("RubriqueEvaluation", "evaluation and rubrique",
                    evaluationId + " - " + request.getIdRubrique());
        }

        //  Auto-incrémenter l'ordre si non fourni
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
        log.info("Added rubrique {} to evaluation {} with ordre {}", request.getIdRubrique(), evaluationId, ordre);

        // Retourner le DTO
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

        // Vérifier que l'évaluation existe et appartient à l'enseignant
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

        // Vérifier que l'évaluation existe et appartient à l'enseignant
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

        // Vérifier que l'évaluation existe et appartient à l'enseignant
        Evaluation evaluation = repository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + evaluationId));

        if (!evaluation.getNoEnseignant().equals(noEnseignant)) {
            throw new BusinessException("Vous n'avez pas le droit de modifier cette évaluation");
        }

        // Vérifier que la rubrique evaluation existe
        RubriqueEvaluation rubriqueEvaluation = rubriqueEvaluationRepository.findById(rubriqueEvaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("RubriqueEvaluation", "id", rubriqueEvaluationId));

        if (!rubriqueEvaluation.getIdEvaluation().equals(evaluationId)) {
            throw new BusinessException("RubriqueEvaluation does not belong to this evaluation");
        }

        // Vérifier que la question existe
        if (!questionService.existsById(request.getIdQuestion())) {
            throw new ResourceNotFoundException("Question", "idQuestion", request.getIdQuestion());
        }

        // Vérifier que la question n'est pas déjà dans cette rubrique
        if (questionEvaluationRepository.existsByIdRubriqueEvaluationAndIdQuestion(
                rubriqueEvaluationId, request.getIdQuestion())) {
            throw new DuplicateResourceException("QuestionEvaluation", "rubrique and question",
                    rubriqueEvaluationId + " - " + request.getIdQuestion());
        }

        //  Auto-incrémenter l'ordre si non fourni
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

        //  Retourner le DTO complet avec toutes les questions
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

        // Vérifier que l'évaluation existe et appartient à l'enseignant
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

        // Vérifier que l'évaluation existe et appartient à l'enseignant
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

        String email = authentication.getName(); // subject JWT = email
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

    // ---------------- US 6.10 : lister évaluations partagées ----------------

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
                // optionnel : exclure ses propres évaluations si tu veux
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
        copy.setNoEvaluation(source.getNoEvaluation());
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
        boolean cons = dup || Boolean.TRUE.equals(dto.getConsultation()); // duplication => consultation

        List<Integer> allIds = EnseignantRepository.findAllIds();

        for (Integer idEns : allIds) {
            Long cible = Long.valueOf(idEns);

            if (cible.equals(owner)) continue; // pas à soi-même

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
                .noEnseignant(-1L) // convention "tous"
                .consultation(cons ? "O" : "N")
                .duplication(dup ? "O" : "N")
                .build();
    }


}
