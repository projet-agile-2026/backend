package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Question.QuestionDTO;
import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.repository.RubriqueQuestionRepository;
import com.evaluation.backend.service.QuestionService;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.entity.Authentification;
import com.evaluation.backend.repository.AuthentificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;
    private final AuthentificationRepository authentificationRepository;
    private final RubriqueQuestionRepository rubriqueQuestionRepository;

    public QuestionController(QuestionService questionService, AuthentificationRepository authentificationRepository, RubriqueQuestionRepository rubriqueQuestionRepository) {
        this.questionService = questionService;
        this.authentificationRepository = authentificationRepository;
        this.rubriqueQuestionRepository = rubriqueQuestionRepository;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAll(Authentication authentication) {
        String role = extractRole(authentication);
        String noEnseignant = "ROLE_ENS".equals(role) ? getConnectedEnseignantId(authentication) : null;

        if ("ROLE_ENS".equals(role)) {
            return ResponseEntity.ok(questionService.getAllQuestions(noEnseignant));
        } else if ("ROLE_ADM".equals(role)){
            return ResponseEntity.ok(questionService.getQuestionsForAdmin());
        }
        return ResponseEntity.ok(questionService.getAllQuestions(noEnseignant));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createQuestion(@RequestBody Question question, Authentication authentication) {
        if (question.getIntitule() == null || question.getIntitule().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'intitulé de la question est obligatoire.");
        }
        try {
            String role = extractRole(authentication);
            String noEnseignant = "ROLE_ENS".equals(role) ? getConnectedEnseignantId(authentication) : null;
            String simpleRole = role.replace("ROLE_", "");
            Question savedQuestion = questionService.createQuestion(question, simpleRole, noEnseignant);
            return new ResponseEntity<>(savedQuestion, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Question questionDetails, Authentication authentication) {
        if (questionDetails.getIntitule() == null || questionDetails.getIntitule().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'intitulé de la question est obligatoire.");
        }
        try {
            String role = extractRole(authentication);
            String noEnseignant = "ROLE_ENS".equals(role) ? getConnectedEnseignantId(authentication) : null;
            String simpleRole = role.replace("ROLE_", "");
            QuestionWithQualificatifDTO updatedQuestion = questionService.updateQuestion(id, questionDetails, simpleRole, noEnseignant);
            return ResponseEntity.ok(updatedQuestion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        try {
            String role = extractRole(authentication);
            String noEnseignant = "ROLE_ENS".equals(role) ? getConnectedEnseignantId(authentication) : null;
            String simpleRole = role.replace("ROLE_", "");
            questionService.deleteQuestion(id, simpleRole, noEnseignant);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }

    private String getConnectedEnseignantId(Authentication authentication) {
        Authentification auth = authentificationRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (auth.getEnseignant() == null) {
            throw new RuntimeException("L'utilisateur n'est pas lié à un enseignant");
        }
        return String.valueOf(auth.getEnseignant().getId());
    }
}