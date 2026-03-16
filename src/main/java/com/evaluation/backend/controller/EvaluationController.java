package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Droit.DroitRequestDTO;
import com.evaluation.backend.dto.Droit.DroitResponseDTO;
import com.evaluation.backend.dto.Droit.DroitTousRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.repository.AuthentificationRepository;
import com.evaluation.backend.service.Evaluation.EvaluationPdfService;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enseignant/evaluations")
public class EvaluationController {

    private final EvaluationService service;
    private final AuthentificationRepository authentificationRepository;

    private final EvaluationPdfService evaluationPdfService;


    @GetMapping
    public List<EvaluationResponseDTO> list(
            @RequestParam(required = false) String codeFormation,
            @RequestParam(required = false) String anneeUniversitaire,
            Authentication authentication
    ) {
        Long noEnseignant = getConnectedEnseignantId(authentication);
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


    @GetMapping("/formations/{codeFormation}/annees")
    public List<String> getAnneesUniversitaires(@PathVariable String codeFormation) {
        return service.getAnneesUniversitaires(codeFormation);
    }



    @GetMapping("/{id}")
    public EvaluationResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PostMapping
    public EvaluationResponseDTO create(@Valid @RequestBody EvaluationRequestDTO dto, Authentication authentication) {
        Long noEnseignant = getConnectedEnseignantId(authentication);
        System.out.println("noEnseignant : " + noEnseignant);
        return service.create(dto, noEnseignant);
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
    public ResponseEntity<RubriqueEvaluationDTO> addRubriqueToEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody AddRubriqueToEvaluationRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        RubriqueEvaluationDTO result = service.addRubriqueToEvaluation(id, request, noEnseignant);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
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
    public ResponseEntity<RubriqueEvaluationDTO> addQuestionToRubriqueEvaluation(
            @PathVariable Long evaluationId,
            @PathVariable Long rubriqueEvaluationId,
            @Valid @RequestBody AddQuestionToRubriqueEvaluationRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        RubriqueEvaluationDTO result = service.addQuestionToRubriqueEvaluation(evaluationId, rubriqueEvaluationId, request, noEnseignant);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
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


    // us droit + duplication

    @GetMapping("/partagees")
    public List<EvaluationResponseDTO> listPartagees() {
        return service.listEvaluationsPartagees();
    }

    // ---------------- US 6.11 ----------------
    @PostMapping("/{idEvaluation:\\d+}/dupliquer")
    public EvaluationResponseDTO dupliquer(@PathVariable Long idEvaluation) {
        return service.dupliquerEvaluation(idEvaluation);
    }

    // ---------------- US 6.9 ----------------
    @GetMapping("/{idEvaluation:\\d+}/droits")
    public List<DroitResponseDTO> listDroits(@PathVariable Long idEvaluation) {
        return service.listDroits(idEvaluation);
    }

    @PostMapping("/{idEvaluation:\\d+}/droits")
    public DroitResponseDTO upsertDroit(@PathVariable Long idEvaluation,
                                        @Valid @RequestBody DroitRequestDTO dto) {
        return service.upsertDroit(idEvaluation, dto);
    }

    @DeleteMapping("/{idEvaluation:\\d+}/droits/{noEnseignantCible:\\d+}")
    public void deleteDroit(@PathVariable Long idEvaluation,
                            @PathVariable Long noEnseignantCible) {
        service.deleteDroit(idEvaluation, noEnseignantCible);
    }

    @PostMapping("/{idEvaluation:\\d+}/droits/tous")
    public DroitResponseDTO donnerDroitATous(@PathVariable Long idEvaluation,
                                             @Valid @RequestBody DroitTousRequestDTO dto) {
        return service.donnerDroitATous(idEvaluation, dto);
    }


    //Changer l'etat d'evaluation - Achraf EL AIDI IDRISSI
    @PutMapping("/{id}/etat")
    public EvaluationResponseDTO updateEtat(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        String etat = payload.get("etat");
        return service.updateEtat(id, etat);
    }


    @PostMapping("/{id}/rubriques/specifique")
    public ResponseEntity<RubriqueEvaluationDTO> addRubriqueSpecifique(
            @PathVariable Long id,
            @Valid @RequestBody AddRubriqueSpecifiqueRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        RubriqueEvaluationDTO result = service.addRubriqueSpecifiqueToEvaluation(
                id, request, noEnseignant);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }



    @PutMapping("/{id}/rubriques/{rubriqueEvaluationId}/specifique")
    public ResponseEntity<RubriqueEvaluationDTO> updateRubriqueSpecifique(
            @PathVariable Long id,
            @PathVariable Long rubriqueEvaluationId,
            @Valid @RequestBody UpdateRubriqueSpecifiqueRequest request,
            Authentication authentication) {

        Long noEnseignant = getConnectedEnseignantId(authentication);
        RubriqueEvaluationDTO result = service.updateRubriqueSpecifique(
                id, rubriqueEvaluationId, request, noEnseignant);
        return ResponseEntity.ok(result);
    }



    @GetMapping(value = "/{id}/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        EvaluationWithRubriquesDTO evaluation = service.getByIdWithRubriques(id);
        byte[] pdf = evaluationPdfService.generateEvaluationPdf(evaluation);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"evaluation-" + id + ".pdf\"")
                .body(pdf);
    }
}