package com.evaluation.backend.service;

import com.evaluation.backend.dto.Question.QuestionDTO;
import com.evaluation.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.entity.Question;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QualificatifRepository qualificatifRepository;

    public QuestionService(QuestionRepository questionRepository,
                           QualificatifRepository qualificatifRepository) {
        this.questionRepository = questionRepository;
        this.qualificatifRepository = qualificatifRepository;  // Added at rubrique work
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
    public QuestionWithQualificatifDTO getQuestionWithQualificatifById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question non trouvée avec l'id : " + questionId));

        Long qualificatifId = Long.parseLong(question.getIdQualificatif());

        Qualificatif qualificatif = qualificatifRepository.findById(qualificatifId)
                .orElseThrow(() -> new RuntimeException("Qualificatif non trouvé avec l'id : " + qualificatifId));

        return QuestionWithQualificatifDTO.builder()
                .idQuestion(question.getIdQuestion())
                .type(question.getType())
                .noEnseignant(question.getNoEnseignant())
                .intitule(question.getIntitule())
                .idQualificatif(qualificatif.getIdQualificatif())
                .maximal(qualificatif.getMaximal())
                .minimal(qualificatif.getMinimal())
                .ordre(null)
                .build();
    }
    public boolean existsById(Long questionId) {
        return questionRepository.existsById(questionId);
    }
    public Question createQuestion(Question question) {
        // Validate that the qualificatif exists
        if (question.getIdQualificatif() != null && !question.getIdQualificatif().isEmpty()) {
            Long qualificatifId = Long.parseLong(question.getIdQualificatif());
            if (!qualificatifRepository.existsById(qualificatifId)) {
                throw new RuntimeException("Qualificatif non trouvé avec l'id : " + qualificatifId);
            }
        } else {
            throw new RuntimeException("Un qualificatif doit être spécifié pour créer une question");
        }

        // Set default type if not provided
        if (question.getType() == null || question.getType().isEmpty()) {
            question.setType("QST");
        }

        return questionRepository.save(question);
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

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Impossible de supprimer : Question non trouvée avec l'id : " + id);
        }
        questionRepository.deleteById(id);
    }
}