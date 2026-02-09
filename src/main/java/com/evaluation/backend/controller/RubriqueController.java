package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.dto.Question.ReorderQuestionsRequest;
import com.evaluation.backend.dto.Rubrique.*;
import com.evaluation.backend.service.RubriqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rubriques")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RubriqueController {

    private final RubriqueService rubriqueService;

    /**
     * Get all rubriques
     */
    @GetMapping
    public ResponseEntity<List<RubriqueDTO>> getAllRubriques() {
        log.info("GET /api/rubriques - Fetching all rubriques");
        List<RubriqueDTO> rubriques = rubriqueService.getAllRubriques();
        return ResponseEntity.ok(rubriques);
    }

    /**
     * Get rubrique by ID with questions and qualificatifs
     */
    @GetMapping("/{id}")
    public ResponseEntity<RubriqueDTO> getRubriqueById(@PathVariable Long id) {
        log.info("GET /api/rubriques/{} - Fetching rubrique by id with questions", id);
        RubriqueDTO rubrique = rubriqueService.getRubriqueById(id);
        return ResponseEntity.ok(rubrique);
    }

    /**
     * Get questions for a specific rubrique (with qualificatifs)
     */
    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionWithQualificatifDTO>> getQuestionsForRubrique(@PathVariable Long id) {
        log.info("GET /api/rubriques/{}/questions - Fetching questions for rubrique", id);
        List<QuestionWithQualificatifDTO> questions = rubriqueService.getQuestionsForRubrique(id);
        return ResponseEntity.ok(questions);
    }

    /**
     * Create a new rubrique
     */
    @PostMapping
    public ResponseEntity<RubriqueDTO> createRubrique(@Valid @RequestBody CreateRubriqueRequest request) {
        log.info("POST /api/rubriques - Creating new rubrique: {}", request.getDesignation());
        RubriqueDTO createdRubrique = rubriqueService.createRubrique(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRubrique);
    }

    /**
     * Update an existing rubrique
     */
    @PutMapping("/{id}")
    public ResponseEntity<RubriqueDTO> updateRubrique(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRubriqueRequest request) {
        log.info("PUT /api/rubriques/{} - Updating rubrique", id);
        RubriqueDTO updatedRubrique = rubriqueService.updateRubrique(id, request);
        return ResponseEntity.ok(updatedRubrique);
    }

    /**
     * Delete a rubrique (hard delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRubrique(@PathVariable Long id) {
        log.info("DELETE /api/rubriques/{} - Deleting rubrique", id);
        rubriqueService.deleteRubrique(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a question to a rubrique
     */
    @PostMapping("/{id}/questions")
    public ResponseEntity<Void> addQuestionToRubrique(
            @PathVariable Long id,
            @Valid @RequestBody AddQuestionToRubriqueRequest request) {
        log.info("POST /api/rubriques/{}/questions - Adding question {} to rubrique", id, request.getIdQuestion());
        rubriqueService.addQuestionToRubrique(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Remove a question from a rubrique
     */
    @DeleteMapping("/{rubriqueId}/questions/{questionId}")
    public ResponseEntity<Void> removeQuestionFromRubrique(
            @PathVariable Long rubriqueId,
            @PathVariable Long questionId) {
        log.info("DELETE /api/rubriques/{}/questions/{} - Removing question from rubrique", rubriqueId, questionId);
        rubriqueService.removeQuestionFromRubrique(rubriqueId, questionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder questions in a rubrique (for drag and drop functionality)
     */
    @PutMapping("/{id}/questions/reorder")
    public ResponseEntity<Void> reorderQuestionsInRubrique(
            @PathVariable Long id,
            @Valid @RequestBody ReorderQuestionsRequest request) {
        log.info("PUT /api/rubriques/{}/questions/reorder - Reordering questions in rubrique", id);
        rubriqueService.reorderQuestionsInRubrique(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reorder/{type}")
    public ResponseEntity<Void> reorderRubriques(
            @PathVariable String type,
            @RequestBody ReorderRubriquesRequest request) {
        try {
            rubriqueService.reorderRubriques(type, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}