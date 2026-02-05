package com.evaluation.backend.controller;

import com.evaluation.backend.dto.QuestionDTO;
import com.evaluation.backend.service.QuestionService;
import com.evaluation.backend.repository.QuestionRepository;
import org.springframework.http.ResponseEntity;
import com.evaluation.backend.entity.Question;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionRepository questionRepository;

    public QuestionController(QuestionService questionService, QuestionRepository questionRepository) {
        this.questionService = questionService;
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAll() {
        List<QuestionDTO> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/create")
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        try {
            Question savedQuestion = questionRepository.save(question);
            return new ResponseEntity<>(savedQuestion, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}