package com.evaluation.backend.service;

import com.evaluation.backend.dto.QuestionDTO;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.QuestionMapper;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QualificatifRepository qualificatifRepository;
    private final QuestionMapper questionMapper;

    @Transactional(readOnly = true)
    public List<QuestionDTO> getAllQuestions() {
        log.debug("Fetching all questions");
        List<Question> questions = questionRepository.findAll();
        return questionMapper.toDTOList(questions);
    }

    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(Long id) {
        log.debug("Fetching question with id: {}", id);
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "idQuestion", id));
        return questionMapper.toDTO(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByType(String type) {
        log.debug("Fetching questions with type: {}", type);
        List<Question> questions = questionRepository.findByType(type);
        return questionMapper.toDTOList(questions);
    }

    public QuestionDTO createQuestion(QuestionDTO questionDTO) {
        log.debug("Creating new question: {}", questionDTO);

        // Verify qualificatif exists
        Qualificatif qualificatif = qualificatifRepository.findById(questionDTO.getIdQualificatif())
                .orElseThrow(() -> new ResourceNotFoundException("Qualificatif", "idQualificatif", questionDTO.getIdQualificatif()));

        if (questionDTO.getType() == null || questionDTO.getType().isBlank()) {
            questionDTO.setType("QUS");
        }

        Question question = questionMapper.toEntity(questionDTO);
        Question savedQuestion = questionRepository.save(question);
        log.info("Created question with id: {}", savedQuestion.getIdQuestion());

        return questionMapper.toDTO(savedQuestion);
    }

    public QuestionDTO updateQuestion(Long id, QuestionDTO questionDTO) {
        log.debug("Updating question with id: {}", id);
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "idQuestion", id));

        // Verify qualificatif exists
        qualificatifRepository.findById(questionDTO.getIdQualificatif())
                .orElseThrow(() -> new ResourceNotFoundException("Qualificatif", "idQualificatif", questionDTO.getIdQualificatif()));
        existingQuestion.setNoEnseignant(questionDTO.getNoEnseignant());
        existingQuestion.setIdQualificatif(questionDTO.getIdQualificatif());
        existingQuestion.setIntitule(questionDTO.getIntitule());


        if (questionDTO.getType() != null && !questionDTO.getType().isBlank()) {
            existingQuestion.setType(questionDTO.getType());
        }

        Question updatedQuestion = questionRepository.save(existingQuestion);
        log.info("Updated question with id: {}", id);
        return questionMapper.toDTO(updatedQuestion);
    }

    public void deleteQuestion(Long id) {
        log.debug("Deleting question with id: {}", id);
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question", "idQuestion", id);
        }
        questionRepository.deleteById(id);
        log.info("Deleted question with id: {}", id);
    }
}