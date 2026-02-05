package com.evaluation.backend.service;

import com.evaluation.backend.dto.QuestionDTO;
import com.evaluation.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.evaluation.backend.entity.Question;

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

    public Question updateQuestion(Long id, Question questionDetails) {

    return questionRepository.findById(id).map(existingQuestion -> {

        existingQuestion.setType(questionDetails.getType());
        existingQuestion.setIdQualificatif(questionDetails.getIdQualificatif());
        existingQuestion.setIntitule(questionDetails.getIntitule());
        existingQuestion.setNoEnseignant(questionDetails.getNoEnseignant());
        
        return questionRepository.save(existingQuestion);
        
    }).orElseThrow(() -> new RuntimeException("Question non trouvée avec l'id : " + id));
}
}