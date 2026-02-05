package com.evaluation.backend.service;

import com.evaluation.backend.dto.QuestionDTO;
import com.evaluation.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(question -> new QuestionDTO(
                        question.getIdQuestion(), 
                        question.getType(),
                        question.getNoEnseignant(),
                        question.getIdQualificatif(),
                        question.getIntitule()
                ))
                .toList();
    }
}