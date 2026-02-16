package com.evaluation.backend.service;

import com.evaluation.backend.dto.Question.QuestionDTO;
import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.repository.QuestionRepository;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.repository.RubriqueQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;




@Service

public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QualificatifRepository qualificatifRepository;
    private final RubriqueQuestionRepository rubriqueQuestionRepository;


    public QuestionService(QuestionRepository questionRepository, QualificatifRepository qualificatifRepository, RubriqueQuestionRepository rubriqueQuestionRepository) {
        this.questionRepository = questionRepository;
        this.qualificatifRepository = qualificatifRepository;
        this.rubriqueQuestionRepository = rubriqueQuestionRepository;
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

        List<Question> questions = questionRepository.findAll();

        List<Long> usedIds = rubriqueQuestionRepository.findAllUsedQuestionIds();

        return questions.stream()
                .filter(q -> ("QUS".equals(q.getType()) || isOwner(q, noEnseignant)))
                .map(q -> mapToDTO(q, usedIds))
                .toList();
    }


    public List<QuestionDTO> getQuestionsForAdmin() {

        List<Long> usedIds = rubriqueQuestionRepository.findAllUsedQuestionIds();

        return questionRepository.findAll().stream()
                .filter(q -> ("QUS".equals(q.getType()) && q.getNoEnseignant() == null))
                .map(q -> mapToDTO(q, usedIds))
                .toList();
    }

    //public Question createQuestion(Question question, String role, String noEnseignant) {
        //if ("ENS".equals(role)) {
            //question.setNoEnseignant(noEnseignant);
            //question.setType("QUP");
        //} else if ("ADM".equals(role)){
            //question.setNoEnseignant(null);
            //question.setType("QUS");
        //}
       // return questionRepository.save(question);
    //}//
    @Transactional
    public Question createQuestion(Question question, String role, String noEnseignant) {

        if ("ENS".equals(role)) {
            question.setNoEnseignant(noEnseignant);
            question.setType("QUP");
        } else if ("ADM".equals(role)) {
            question.setNoEnseignant(null);
            question.setType("QUS");
        }

        Question saved = questionRepository.save(question);

        // 🔥 Charger manuellement le qualificatif
        if (saved.getIdQualificatif() != null) {
            Qualificatif qual = qualificatifRepository
                    .findById(Long.valueOf(saved.getIdQualificatif()))
                    .orElse(null);

            saved.setQualificatif(qual);
        }

        return saved;
    }
    @Transactional
    public QuestionWithQualificatifDTO updateQuestion(Long id, Question details, String role, String noEnseignant) {
        Question updated = questionRepository.findById(id).map(q -> {

            // Vérifications de rôle
            if ("ENS".equals(role) && !isOwner(q, noEnseignant)) {
                throw new RuntimeException("Action interdite : propriétaire différent");
            } else if ("ENS".equals(role) && "QUS".equals(details.getType())) {
                throw new RuntimeException("l'enseignant ne peut pas modifier une question standard");
            } else if ("ADM".equals(role) && "QUP".equals(details.getType())) {
                throw new RuntimeException("l'admin ne peut pas modifier une question personnelle");
            }

            // Mise à jour
            q.setIntitule(details.getIntitule());
            q.setIdQualificatif(details.getIdQualificatif());

            return questionRepository.save(q);
        }).orElseThrow(() -> new RuntimeException("Question introuvable"));

        // 🔥 Charger le Qualificatif
        Qualificatif qual = qualificatifRepository
                .findById(Long.valueOf(updated.getIdQualificatif()))
                .orElseThrow(() -> new RuntimeException("Qualificatif introuvable"));

        // 🔥 Mapper vers le DTO
        return QuestionWithQualificatifDTO.builder()
                .idQuestion(updated.getIdQuestion())
                .type(updated.getType())
                .noEnseignant(updated.getNoEnseignant())
                .intitule(updated.getIntitule())
                .idQualificatif(qual.getIdQualificatif())
                .minimal(qual.getMinimal())
                .maximal(qual.getMaximal())
                .ordre(updated.getIdQuestion().intValue()) // exemple si tu veux ordre temporaire
                .build();
    }
//    public Question updateQuestion(Long id, Question details, String role, String noEnseignant) {
//        return questionRepository.findById(id).map(q -> {
//            if ("ENS".equals(role) && !isOwner(q, noEnseignant)) {
//                throw new RuntimeException("Action interdite : propriétaire différent");
//            }
//            else if ("ENS".equals(role) && "QUS".equals(details.getType())){
//                throw new RuntimeException("l'enseignant ne peut pas modifier une question standard" );
//
//            }
//            else if ("ADM".equals(role) && "QUP".equals(details.getType())){
//                throw new RuntimeException("l'admin  ne peut pas modifier une question personnelle" );
//
//            }
//            q.setIntitule(details.getIntitule());
//            q.setIdQualificatif(details.getIdQualificatif());
//            return questionRepository.save(q);
//        }).orElseThrow(() -> new RuntimeException("Question introuvable"));
//    }

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

    private QuestionDTO mapToDTO(Question question, List<Long> usedIds) {

        QuestionDTO dto = new QuestionDTO();

        dto.setIdQuestion(question.getIdQuestion());
        dto.setType(question.getType());
        dto.setNoEnseignant(question.getNoEnseignant());
        dto.setIdQualificatif(question.getIdQualificatif());
        dto.setIntitule(question.getIntitule());
        dto.setUsedInRubrique(usedIds.contains(question.getIdQuestion()));

        return dto;
    }

}