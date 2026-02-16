package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.repository.AuthentificationRepository;
import com.evaluation.backend.service.Evaluation.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.evaluation.backend.dto.Evaluation.*;
import com.evaluation.backend.entity.Authentification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enseignant/evaluations")
public class EvaluationController {

    private final EvaluationService service;
    private final AuthentificationRepository authentificationRepository;


    @GetMapping
    public List<EvaluationResponseDTO> list(
            @RequestParam(required = false) Long noEnseignant,
            @RequestParam(required = false) String codeFormation,
            @RequestParam(required = false) String anneeUniversitaire
    ) {
        return service.list(noEnseignant, codeFormation, anneeUniversitaire);
    }


    @GetMapping("/formations")
    public List<String> getFormations() {
        return service.getFormations();
    }

    @GetMapping("/formations/{codeFormation}/ues")
    public List<String> getUes(@PathVariable String codeFormation) {
        return service.getCodeUe(codeFormation);
    }

    @GetMapping("/formations/{codeFormation}/ues/{codeUe}/ecs")
    public List<String> getEcs(@PathVariable String codeFormation,
                               @PathVariable String codeUe) {
        return service.getCodeEc(codeFormation, codeUe);
    }



    @GetMapping("/{id}")
    public EvaluationResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PostMapping
    public EvaluationResponseDTO create(@Valid @RequestBody EvaluationRequestDTO dto) {
        return service.create(dto);
    }


    @PutMapping("/{id}")
    public EvaluationResponseDTO update(@PathVariable Long id,
                                        @Valid @RequestBody EvaluationRequestDTO dto) {
        return service.update(id, dto);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }


    @GetMapping("/{id}/rubriques")
    public EvaluationWithRubriquesDTO getEvaluationWithRubriques(
            @PathVariable Long id) {
        return service.getByIdWithRubriques(id);
    }

    // US 6.5: Gestion des rubriques d'une évaluation

    @PostMapping("/{id}/rubriques")
    public ResponseEntity<Void> addRubriqueToEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody AddRubriqueToEvaluationRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        service.addRubriqueToEvaluation(id, request, noEnseignant);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{evaluationId}/rubriques/{rubriqueEvaluationId}")
    public ResponseEntity<Void> removeRubriqueFromEvaluation(
            @PathVariable Long evaluationId,
            @PathVariable Long rubriqueEvaluationId,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        service.removeRubriqueFromEvaluation(evaluationId, rubriqueEvaluationId, noEnseignant);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/rubriques/reorder")
    public ResponseEntity<Void> reorderRubriquesInEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody ReorderRubriquesInEvaluationRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        service.reorderRubriquesInEvaluation(id, request, noEnseignant);
        return ResponseEntity.ok().build();
    }

    // US 6.6: Gestion des questions d'une rubrique d'évaluation

    @PostMapping("/{evaluationId}/rubriques/{rubriqueEvaluationId}/questions")
    public ResponseEntity<Void> addQuestionToRubriqueEvaluation(
            @PathVariable Long evaluationId,
            @PathVariable Long rubriqueEvaluationId,
            @Valid @RequestBody AddQuestionToRubriqueEvaluationRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        service.addQuestionToRubriqueEvaluation(evaluationId, rubriqueEvaluationId, request, noEnseignant);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{evaluationId}/rubriques/{rubriqueEvaluationId}/questions/{questionEvaluationId}")
    public ResponseEntity<Void> removeQuestionFromRubriqueEvaluation(
            @PathVariable Long evaluationId,
            @PathVariable Long rubriqueEvaluationId,
            @PathVariable Long questionEvaluationId,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        service.removeQuestionFromRubriqueEvaluation(evaluationId, rubriqueEvaluationId, questionEvaluationId, noEnseignant);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{evaluationId}/rubriques/{rubriqueEvaluationId}/questions/reorder")
    public ResponseEntity<Void> reorderQuestionsInRubriqueEvaluation(
            @PathVariable Long evaluationId,
            @PathVariable Long rubriqueEvaluationId,
            @Valid @RequestBody ReorderQuestionsInRubriqueEvaluationRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        service.reorderQuestionsInRubriqueEvaluation(evaluationId, rubriqueEvaluationId, request, noEnseignant);
        return ResponseEntity.ok().build();
    }

    private Long getConnectedEnseignantId(Authentication authentication) {
        Authentification auth = authentificationRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (auth.getEnseignant() == null) {
            throw new RuntimeException("L'utilisateur n'est pas lié à un enseignant");
        }
        return Long.valueOf(auth.getEnseignant().getId());
    }
}
