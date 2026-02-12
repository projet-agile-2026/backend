package com.evaluation.backend.service;

import com.evaluation.backend.dto.Question.QuestionDTO;
import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.repository.QuestionRepository;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.repository.RubriqueQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QualificatifRepository qualificatifRepository;
    @Autowired
    private RubriqueQuestionRepository rubriqueQuestionRepository;


    public QuestionService(QuestionRepository questionRepository, QualificatifRepository qualificatifRepository) {
        this.questionRepository = questionRepository;
        this.qualificatifRepository = qualificatifRepository;
    }

    public List<QuestionDTO> getAllQuestions() {

        List<Question> questions = questionRepository.findAll();

        List<Long> usedQuestionIds = rubriqueQuestionRepository.findAllUsedQuestionIds();
        Set<Long> usedSet = new HashSet<>(usedQuestionIds);

        return questions.stream()
                .map(question -> new QuestionDTO(
                        question.getIdQuestion(),
                        question.getType(),
                        question.getNoEnseignant(),
                        question.getIdQualificatif(),
                        question.getIntitule(),
                        usedSet.contains(question.getIdQuestion())
                ))
                .toList();
    }

    public QuestionWithQualificatifDTO getQuestionWithQualificatifById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question non trouvée"));
        Qualificatif qualificatif = qualificatifRepository.findById(Long.parseLong(question.getIdQualificatif()))
                .orElseThrow(() -> new RuntimeException("Qualificatif non trouvé"));

        return QuestionWithQualificatifDTO.builder()
                .idQuestion(question.getIdQuestion())
                .type(question.getType())
                .noEnseignant(question.getNoEnseignant())
                .intitule(question.getIntitule())
                .idQualificatif(qualificatif.getIdQualificatif())
                .maximal(qualificatif.getMaximal())
                .minimal(qualificatif.getMinimal())
                .build();
    }

    public boolean existsById(Long questionId) {
        return questionRepository.existsById(questionId);
    }

    public List<QuestionDTO> getAllQuestions(String noEnseignant) {
        return questionRepository.findAll().stream()
                .filter(q -> ("QUS".equals(q.getType()) || isOwner(q, noEnseignant) ))
                .map(this::mapToDTO).toList();
    }

    public List<QuestionDTO> getQuestionsForAdmin() {
        return questionRepository.findAll().stream()
                .filter(q -> ("QUS".equals(q.getType()) && q.getNoEnseignant() == null) )
                .map(this::mapToDTO).toList();
    }

    public Question createQuestion(Question question, String role, String noEnseignant) {
        if ("ENS".equals(role)) {
            question.setNoEnseignant(noEnseignant);
            question.setType("QUP");
        } else if ("ADM".equals(role)){
            question.setNoEnseignant(null);
            question.setType("QUS");
        }
        return questionRepository.save(question);
    }

    public Question updateQuestion(Long id, Question details, String role, String noEnseignant) {
        return questionRepository.findById(id).map(q -> {
            if ("ENS".equals(role) && !isOwner(q, noEnseignant)) {
                throw new RuntimeException("Action interdite : propriétaire différent");
            }
            else if ("ENS".equals(role) && details.getType().equals("QUS")){
                throw new RuntimeException("l'enseignant ne peut pas modifier une question standard" );

            }
            else if ("ADM".equals(role) && details.getType().equals("QUP")){
                throw new RuntimeException("l'admin  ne peut pas modifier une question personnelle" );

            }
            q.setIntitule(details.getIntitule());
            q.setIdQualificatif(details.getIdQualificatif());
            return questionRepository.save(q);
        }).orElseThrow(() -> new RuntimeException("Question introuvable"));
    }

    public void deleteQuestion(Long id, String role, String noEnseignant) {
        Question q = questionRepository.findById(id).orElseThrow(() -> new RuntimeException("Introuvable"));
        if ("ENS".equals(role) && !isOwner(q, noEnseignant)) {
            throw new RuntimeException("Action interdite");
        }
        else if ("ADM".equals(role) && q.getType().equals("QUP")) {
            throw new RuntimeException("l'admin  ne peut pas supprimer une question personnelle");
        }
        else if ("ENS".equals(role) && q.getType().equals("QUS")) {
            throw new RuntimeException("l'enseignant ne peut pas supprimer une question standart");
        }
        questionRepository.deleteById(id);
    }

    // Méthode utilitaire pour gérer les espaces d'Oracle
    private boolean isOwner(Question q, String noEnseignant) {
        if (q.getNoEnseignant() == null || noEnseignant == null) return false;
        return q.getNoEnseignant().trim().equals(noEnseignant.trim());
    }

    private QuestionDTO mapToDTO(Question q) {
        return new QuestionDTO(q.getIdQuestion(), q.getType(), q.getNoEnseignant(), q.getIdQualificatif(), q.getIntitule());
    }
}