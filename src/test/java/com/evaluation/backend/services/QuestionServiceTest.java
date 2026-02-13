package com.evaluation.backend.services;

import com.evaluation.backend.dto.Question.QuestionDTO;
import com.evaluation.backend.dto.Question.QuestionWithQualificatifDTO;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.repository.QuestionRepository;
import com.evaluation.backend.repository.RubriqueQuestionRepository;
import com.evaluation.backend.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QualificatifRepository qualificatifRepository;

    @Mock
    private RubriqueQuestionRepository rubriqueQuestionRepository;

    @InjectMocks
    private QuestionService questionService;

    private Question question;
    private Qualificatif qualificatif;

    @BeforeEach
    void setUp() {
        // rubriqueQuestionRepository est injecté via @Autowired (field injection)
        // @InjectMocks ne le gère pas → injection manuelle nécessaire
        ReflectionTestUtils.setField(questionService, "rubriqueQuestionRepository", rubriqueQuestionRepository);

        qualificatif = new Qualificatif();
        qualificatif.setIdQualificatif(1L);
        qualificatif.setMaximal("Excellent");
        qualificatif.setMinimal("Insuffisant");

        question = new Question();
        question.setIdQuestion(1L);
        question.setType("QCM");
        question.setNoEnseignant("1");
        question.setIdQualificatif("1");
        question.setIntitule("Question test");
    }

    // ─────────────────────────────────────────────
    // getAllQuestions()
    // ─────────────────────────────────────────────

    @Test
    void getAllQuestions_ShouldReturnListWithUsedFlag() {
        // Given
        Question q2 = new Question();
        q2.setIdQuestion(2L);
        q2.setType("QCM");
        q2.setNoEnseignant("1");
        q2.setIdQualificatif("1");
        q2.setIntitule("Question 2");

        when(questionRepository.findAll()).thenReturn(Arrays.asList(question, q2));
        // question 1 est utilisée dans une rubrique, question 2 ne l'est pas
        when(rubriqueQuestionRepository.findAllUsedQuestionIds()).thenReturn(List.of(1L));

        // When
        List<QuestionDTO> result = questionService.getAllQuestions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        QuestionDTO dto1 = result.stream()
                .filter(d -> d.getIdQuestion().equals(1L)).findFirst().orElseThrow();
        assertTrue(dto1.isUsedInRubrique());

        QuestionDTO dto2 = result.stream()
                .filter(d -> d.getIdQuestion().equals(2L)).findFirst().orElseThrow();
        assertFalse(dto2.isUsedInRubrique());

        verify(questionRepository).findAll();
        verify(rubriqueQuestionRepository).findAllUsedQuestionIds();
    }

    @Test
    void getAllQuestions_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(questionRepository.findAll()).thenReturn(Collections.emptyList());
        when(rubriqueQuestionRepository.findAllUsedQuestionIds()).thenReturn(Collections.emptyList());

        // When
        List<QuestionDTO> result = questionService.getAllQuestions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllQuestions_WhenNoneUsedInRubrique_ShouldReturnAllFalse() {
        // Given
        when(questionRepository.findAll()).thenReturn(List.of(question));
        when(rubriqueQuestionRepository.findAllUsedQuestionIds()).thenReturn(Collections.emptyList());

        // When
        List<QuestionDTO> result = questionService.getAllQuestions();

        // Then
        assertEquals(1, result.size());
        assertFalse(result.get(0).isUsedInRubrique());
    }

    // ─────────────────────────────────────────────
    // getQuestionWithQualificatifById()
    // ─────────────────────────────────────────────

    @Test
    void getQuestionWithQualificatifById_WhenExists_ShouldReturnDtoWithQualificatif() {
        // Given
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(qualificatifRepository.findById(1L)).thenReturn(Optional.of(qualificatif));

        // When
        QuestionWithQualificatifDTO result = questionService.getQuestionWithQualificatifById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIdQuestion());
        assertEquals("QCM", result.getType());
        assertEquals("Question test", result.getIntitule());
        assertEquals(1L, result.getIdQualificatif());
        assertEquals("Excellent", result.getMaximal());
        assertEquals("Insuffisant", result.getMinimal());
        assertNull(result.getOrdre()); // ordre est null par défaut
    }

    @Test
    void getQuestionWithQualificatifById_WhenQuestionNotFound_ShouldThrowException() {
        // Given
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.getQuestionWithQualificatifById(999L));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void getQuestionWithQualificatifById_WhenQualificatifNotFound_ShouldThrowException() {
        // Given - question existe mais son qualificatif n'existe pas
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(qualificatifRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.getQuestionWithQualificatifById(1L));
        assertTrue(ex.getMessage().contains("1"));
    }

    // ─────────────────────────────────────────────
    // existsById()
    // ─────────────────────────────────────────────

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        when(questionRepository.existsById(1L)).thenReturn(true);
        assertTrue(questionService.existsById(1L));
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        when(questionRepository.existsById(999L)).thenReturn(false);
        assertFalse(questionService.existsById(999L));
    }

    // ─────────────────────────────────────────────
    // createQuestion()
    // ─────────────────────────────────────────────

    @Test
    void createQuestion_WithValidData_ShouldSaveAndReturn() {
        // Given
        when(qualificatifRepository.existsById(1L)).thenReturn(true);
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // When
        Question result = questionService.createQuestion(question);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIdQuestion());
        verify(questionRepository).save(question);
    }

    @Test
    void createQuestion_WithoutQualificatif_ShouldThrowException() {
        // Given
        question.setIdQualificatif(null);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.createQuestion(question));
        assertTrue(ex.getMessage().contains("qualificatif"));
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestion_WithEmptyQualificatif_ShouldThrowException() {
        // Given
        question.setIdQualificatif("");

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.createQuestion(question));
        assertTrue(ex.getMessage().contains("qualificatif"));
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestion_WhenQualificatifNotFound_ShouldThrowException() {
        // Given
        when(qualificatifRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.createQuestion(question));
        assertTrue(ex.getMessage().contains("1"));
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestion_WithNullType_ShouldDefaultToQST() {
        // Given
        question.setType(null);
        when(qualificatifRepository.existsById(1L)).thenReturn(true);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Question result = questionService.createQuestion(question);

        // Then
        assertEquals("QST", result.getType());
    }

    @Test
    void createQuestion_WithEmptyType_ShouldDefaultToQST() {
        // Given
        question.setType("");
        when(qualificatifRepository.existsById(1L)).thenReturn(true);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Question result = questionService.createQuestion(question);

        // Then
        assertEquals("QST", result.getType());
    }

    // ─────────────────────────────────────────────
    // updateQuestion()
    // ─────────────────────────────────────────────

    @Test
    void updateQuestion_WhenExists_ShouldUpdateAndReturn() {
        // Given
        Question updatedDetails = new Question();
        updatedDetails.setType("QCU");
        updatedDetails.setIdQualificatif("2");
        updatedDetails.setIntitule("Question modifiée");
        updatedDetails.setNoEnseignant("2");

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Question result = questionService.updateQuestion(1L, updatedDetails);

        // Then
        assertNotNull(result);
        assertEquals("QCU", result.getType());
        assertEquals("2", result.getIdQualificatif());
        assertEquals("Question modifiée", result.getIntitule());
        assertEquals("2", result.getNoEnseignant());
        verify(questionRepository).save(any());
    }

    @Test
    void updateQuestion_WhenNotExists_ShouldThrowException() {
        // Given
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.updateQuestion(999L, question));
        assertTrue(ex.getMessage().contains("999"));
        verify(questionRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // deleteQuestion()
    // ─────────────────────────────────────────────

    @Test
    void deleteQuestion_WhenExists_ShouldDelete() {
        // Given
        when(questionRepository.existsById(1L)).thenReturn(true);

        // When
        questionService.deleteQuestion(1L);

        // Then
        verify(questionRepository).deleteById(1L);
    }

    @Test
    void deleteQuestion_WhenNotExists_ShouldThrowException() {
        // Given
        when(questionRepository.existsById(999L)).thenReturn(false);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> questionService.deleteQuestion(999L));
        assertTrue(ex.getMessage().contains("999"));
        verify(questionRepository, never()).deleteById(any());
    }
}