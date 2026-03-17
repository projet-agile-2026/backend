package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Question.QuestionnaireRequestDTO;
import com.evaluation.backend.dto.Questionnaire.*;
import com.evaluation.backend.entity.Questionnaire;
import com.evaluation.backend.service.Questionnaire.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/questionnaires")
public class QuestionnaireController {

    private final QuestionnaireService service;

    @GetMapping
    public List<QuestionnaireResponseDTO> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public QuestionnaireWithRubriquesDTO getById(@PathVariable Long id) {
        return service.getByIdWithRubriques(id);
    }

    @PostMapping
    public QuestionnaireResponseDTO create(@Valid @RequestBody QuestionnaireRequestDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public QuestionnaireResponseDTO update(@PathVariable Long id,
                                           @Valid @RequestBody QuestionnaireRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/rubriques")
    public ResponseEntity<RubriqueQuestionnaireDTO> addRubriqueToQuestionnaire(
            @PathVariable Long id,
            @Valid @RequestBody AddRubriqueToQuestionnaireRequest request) {

        RubriqueQuestionnaireDTO result = service.addRubriqueToQuestionnaire(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{questionnaireId}/rubriques/{rubriqueQuestionnaireId}")
    public ResponseEntity<Void> removeRubriqueFromQuestionnaire(
            @PathVariable Long questionnaireId,
            @PathVariable Long rubriqueQuestionnaireId) {

        service.removeRubriqueFromQuestionnaire(questionnaireId, rubriqueQuestionnaireId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{questionnaireId}/rubriques/{rubriqueQuestionnaireId}/questions")
    public ResponseEntity<RubriqueQuestionnaireDTO> addQuestionToRubriqueQuestionnaire(
            @PathVariable Long questionnaireId,
            @PathVariable Long rubriqueQuestionnaireId,
            @Valid @RequestBody AddQuestionToRubriqueQuestionnaireRequest request) {

        RubriqueQuestionnaireDTO result =
                service.addQuestionToRubriqueQuestionnaire(questionnaireId, rubriqueQuestionnaireId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{questionnaireId}/rubriques/{rubriqueQuestionnaireId}/questions/{questionQuestionnaireId}")
    public ResponseEntity<Void> removeQuestionFromRubriqueQuestionnaire(
            @PathVariable Long questionnaireId,
            @PathVariable Long rubriqueQuestionnaireId,
            @PathVariable Long questionQuestionnaireId) {

        service.removeQuestionFromRubriqueQuestionnaire(
                questionnaireId,
                rubriqueQuestionnaireId,
                questionQuestionnaireId
        );

        return ResponseEntity.noContent().build();
    }
}
