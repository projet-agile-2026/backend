package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.dto.ReponseEvaluation.EvaluationDetailDTO;
import com.evaluation.backend.dto.ReponseEvaluation.ReponseEvaluationRequestDTO;
import com.evaluation.backend.dto.ReponseEvaluation.ReponseEvaluationResultDTO;
import com.evaluation.backend.entity.Authentification;
import com.evaluation.backend.repository.AuthentificationRepository;
import com.evaluation.backend.service.EvaluationReponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation-reponses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EvaluationReponseController {

    private final EvaluationReponseService evaluationReponseService;
    private final AuthentificationRepository authentificationRepository;

    /**
     * Récupère toutes les évaluations disponibles pour l'étudiant connecté
     * (basé sur sa promotion: codeFormation + anneeUniversitaire)
     */
    @GetMapping("/evaluations")
    public ResponseEntity<List<EvaluationResponseDTO>> getEvaluationsForConnectedEtudiant(
            Authentication authentication) {
        
        // Récupérer l'ID de l'étudiant connecté
        Long noEtudiant = getConnectedEtudiantId(authentication);
        
        List<EvaluationResponseDTO> evaluations = 
            evaluationReponseService.getEvaluationsForEtudiant(noEtudiant);
        
        return ResponseEntity.ok(evaluations);
    }

    /**
     * Récupère les détails d'une évaluation (rubriques + questions) pour l'étudiant connecté
     */
    @GetMapping("/evaluations/{idEvaluation}")
    public ResponseEntity<EvaluationDetailDTO> getEvaluationDetail(
            @PathVariable Long idEvaluation,
            Authentication authentication) {
        
        Long noEtudiant = getConnectedEtudiantId(authentication);
        EvaluationDetailDTO detail = evaluationReponseService.getEvaluationDetail(idEvaluation, noEtudiant);
        
        return ResponseEntity.ok(detail);
    }

    /**
     * Soumet les réponses d'un étudiant à une évaluation
     */
    @PostMapping("/reponses")
    public ResponseEntity<Void> submitReponses(
            @RequestBody ReponseEvaluationRequestDTO request,
            Authentication authentication) {
        
        Long noEtudiant = getConnectedEtudiantId(authentication);
        evaluationReponseService.submitReponses(request, noEtudiant);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Récupère les résultats d'une évaluation pour l'étudiant connecté
     */
    @GetMapping("/evaluations/{idEvaluation}/resultat")
    public ResponseEntity<ReponseEvaluationResultDTO> getEvaluationResult(
            @PathVariable Long idEvaluation,
            Authentication authentication) {
        
        Long noEtudiant = getConnectedEtudiantId(authentication);
        ReponseEvaluationResultDTO result = evaluationReponseService.getEvaluationResult(idEvaluation, noEtudiant);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Récupère l'ID de l'étudiant connecté depuis l'authentification
     */
    private Long getConnectedEtudiantId(Authentication authentication) {
        Authentification auth = authentificationRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (auth.getEtudiant() == null) {
            throw new RuntimeException("L'utilisateur n'est pas lié à un étudiant");
        }
        
        return auth.getEtudiant().getNoEtudiant();
    }

    /**
     * Extrait le rôle de l'utilisateur connecté
     */
    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
