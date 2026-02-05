package com.evaluation.backend.controller;

import com.evaluation.backend.dto.QuestionDTO;
import com.evaluation.backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        log.info("GET /api/questions - Fetching all questions");
        List<QuestionDTO> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable Long id) {
        log.info("GET /api/questions/{} - Fetching question by id", id);
        QuestionDTO question = questionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByType(@PathVariable String type) {
        log.info("GET /api/questions/type/{} - Fetching questions by type", type);
        List<QuestionDTO> questions = questionService.getQuestionsByType(type);
        return ResponseEntity.ok(questions);
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> createQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        log.info("POST /api/questions - Creating new question");
        QuestionDTO createdQuestion = questionService.createQuestion(questionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionDTO questionDTO) {
        log.info("PUT /api/questions/{} - Updating question", id);
        QuestionDTO updatedQuestion = questionService.updateQuestion(id, questionDTO);
        return ResponseEntity.ok(updatedQuestion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        log.info("DELETE /api/questions/{} - Deleting question", id);
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}