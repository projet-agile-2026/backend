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
import com.evaluation.backend.entity.Authentification;
import com.evaluation.backend.repository.AuthentificationRepository;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/rubriques") 
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RubriqueController {

    private final RubriqueService rubriqueService;

    private final AuthentificationRepository authentificationRepository;

    /**
     * Get all rubriques
     */
    @GetMapping
    public ResponseEntity<List<RubriqueDTO>> getAllRubriques(
            Authentication authentication,
            @RequestParam(required = false) Long evaluationId) {

        String role = extractRole(authentication);
        String noEnseignant = "ROLE_ENS".equals(role) ? getConnectedEnseignantId(authentication) : null;
        String simpleRole = role.replace("ROLE_", "");

        Long noEnseignantLong = (noEnseignant != null) ? Long.valueOf(noEnseignant) : null;

        List<RubriqueDTO> rubriques = rubriqueService.getAllRubriques(simpleRole, noEnseignantLong, evaluationId);

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
    public ResponseEntity<RubriqueDTO> createRubrique(@Valid @RequestBody CreateRubriqueRequest request, Authentication authentication) {
       String role = extractRole(authentication);
        String simpleRole = role.replace("ROLE_", "");
        
        //On récupère l'ID si c'est un enseignant
        Long noEnseignant = "ENS".equals(simpleRole) ? Long.valueOf(getConnectedEnseignantId(authentication)) : null;

        log.info("POST /api/rubriques - User role: {} creating rubrique", simpleRole);
        
        RubriqueDTO createdRubrique = rubriqueService.createRubrique(request, simpleRole, noEnseignant);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRubrique);
    }


    /**
     * Update an existing rubrique
     */
    @PutMapping("/{id}")
    public ResponseEntity<RubriqueDTO> updateRubrique(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRubriqueRequest request,
            Authentication authentication) { 
        
        // Extraire les infos de l'utilisateur connecté
        String role = extractRole(authentication);
        String simpleRole = role.replace("ROLE_", "");
        
        String idStr = getConnectedEnseignantId(authentication);
        Long noEnseignant = (idStr != null) ? Long.valueOf(idStr) : null;

        log.info("PUT /api/rubriques/{} - Updating rubrique by role: {}", id, simpleRole);
        
        // On passe maintenant les 4 arguments demandés par le service
        RubriqueDTO updatedRubrique = rubriqueService.updateRubrique(id, request, simpleRole, noEnseignant);
        
        return ResponseEntity.ok(updatedRubrique);
    }


    /**
     * Delete a rubrique (hard delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRubrique(@PathVariable Long id, Authentication authentication) {
        String role = extractRole(authentication);
        String simpleRole = role.replace("ROLE_", "");
        Long noEnseignant = null;
        if ("ENS".equals(simpleRole)) {
            String idStr = getConnectedEnseignantId(authentication);
            if (idStr != null) {
                noEnseignant = Long.valueOf(idStr);
            }
        }
        
        log.info("DELETE /api/rubriques/{} - Deleting by role: {}", id, simpleRole);
        
        // On passe les infos de sécurité au service
        rubriqueService.deleteRubrique(id, simpleRole, noEnseignant);
        
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

    // @PutMapping("/reorder/{type}")
    // public ResponseEntity<Void> reorderRubriques(
    //         @PathVariable String type,
    //         @RequestBody ReorderRubriquesRequest request) {
    //     try {
    //         rubriqueService.reorderRubriques(type, request);
    //         return ResponseEntity.ok().build();
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //     }
    // }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderRubriques(
            @RequestBody ReorderRubriquesRequest request, 
            Authentication authentication) {
        
        String role = extractRole(authentication);
        String simpleRole = role.replace("ROLE_", "");
        
        Long noEnseignant = null;
        if ("ENS".equals(simpleRole)) {
            String idStr = getConnectedEnseignantId(authentication);
            noEnseignant = (idStr != null) ? Long.valueOf(idStr) : null;
        }

        log.info("PUT /api/rubriques/reorder - User: {}", simpleRole);
        rubriqueService.reorderRubriques(request, simpleRole, noEnseignant);
        return ResponseEntity.ok().build();
    }


    private String getConnectedEnseignantId(Authentication authentication) {
        Authentification auth = authentificationRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (auth.getEnseignant() == null) {
            throw new RuntimeException("L'utilisateur n'est pas lié à un enseignant");
        }
        return String.valueOf(auth.getEnseignant().getId());
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
    
    
}