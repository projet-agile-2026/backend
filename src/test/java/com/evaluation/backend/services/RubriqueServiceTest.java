package com.evaluation.backend.service;

import com.evaluation.backend.dto.Rubrique.*;
import com.evaluation.backend.dto.Question.ReorderQuestionsRequest;
import com.evaluation.backend.entity.*;
import com.evaluation.backend.exception.*;
import com.evaluation.backend.mapper.RubriqueMapper;
import com.evaluation.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RubriqueServiceTest {

    @Mock
    private RubriqueRepository rubriqueRepository;

    @Mock
    private RubriqueQuestionRepository rubriqueQuestionRepository;

    @Mock
    private RubriqueMapper rubriqueMapper;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private RubriqueService rubriqueService;

    private Rubrique rubrique;
    private RubriqueDTO rubriqueDTO;
    private CreateRubriqueRequest createRequest;
    private UpdateRubriqueRequest updateRequest;

    @BeforeEach
    void setUp() {
        rubrique = new Rubrique();
        rubrique.setIdRubrique(1L);
        rubrique.setDesignation("TEST RUBRIQUE");
        rubrique.setType("RBS");
        rubrique.setOrdre(1);
        rubrique.setNoEnseignant(1L);

        rubriqueDTO = new RubriqueDTO();
        rubriqueDTO.setIdRubrique(1L);
        rubriqueDTO.setDesignation("TEST RUBRIQUE");
        rubriqueDTO.setType("RBS");
        rubriqueDTO.setOrdre(1);
        rubriqueDTO.setNoEnseignant(1L);

        createRequest = CreateRubriqueRequest.builder()
                .designation("NEW RUBRIQUE")
                .type("RBS")
                .noEnseignant(1L)
                .build();

        updateRequest = UpdateRubriqueRequest.builder()
                .designation("UPDATED RUBRIQUE")
                .type("RBS")
                .noEnseignant(1L)
                .ordre(1)
                .build();
    }

    @Test
    void getAllRubriques_ShouldReturnListOfRubriques() {
        // Given
        List<Rubrique> rubriques = Arrays.asList(rubrique);
        List<RubriqueDTO> rubriqueDTOs = Arrays.asList(rubriqueDTO);

        when(rubriqueRepository.findAllOrderByOrdre()).thenReturn(rubriques);
        when(rubriqueMapper.toDTOList(rubriques)).thenReturn(rubriqueDTOs);
        when(rubriqueQuestionRepository.findByIdRubriqueOrderByOrdreAsc(1L))
                .thenReturn(new ArrayList<>());

        // When
        List<RubriqueDTO> result = rubriqueService.getAllRubriques();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rubriqueRepository).findAllOrderByOrdre();
    }

    @Test
    void getRubriqueById_WhenExists_ShouldReturnRubrique() {
        // Given
        when(rubriqueRepository.findById(1L)).thenReturn(Optional.of(rubrique));
        when(rubriqueMapper.toDTO(rubrique)).thenReturn(rubriqueDTO);
        when(rubriqueQuestionRepository.findByIdRubriqueOrderByOrdreAsc(1L))
                .thenReturn(new ArrayList<>());

        // When
        RubriqueDTO result = rubriqueService.getRubriqueById(1L);

        // Then
        assertNotNull(result);
        assertEquals("TEST RUBRIQUE", result.getDesignation());
        verify(rubriqueRepository).findById(1L);
    }

    @Test
    void getRubriqueById_WhenNotExists_ShouldThrowException() {
        // Given
        when(rubriqueRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> rubriqueService.getRubriqueById(999L));
    }

    @Test
    void createRubrique_ShouldCreateAndReturnRubrique() {

        // Given
        rubrique.setOrdre(null);
        when(rubriqueRepository.findByDesignationAndType(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(rubriqueMapper.toEntity(any(CreateRubriqueRequest.class))).thenReturn(rubrique);
        when(rubriqueRepository.findMaxOrdreByType(anyString())).thenReturn(null);
        when(rubriqueRepository.save(any(Rubrique.class))).thenReturn(rubrique);

        // When
        rubriqueService.createRubrique(createRequest);

        // Then
        verify(rubriqueRepository).save(any(Rubrique.class));
    }

    @Test
    void createRubrique_WhenDuplicate_ShouldThrowException() {
        // Given
        when(rubriqueRepository.findByDesignationAndType("NEW RUBRIQUE", "RBS"))
                .thenReturn(Optional.of(rubrique));

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> rubriqueService.createRubrique(createRequest));
    }

    @Test
    void updateRubrique_ShouldUpdateAndReturnRubrique() {
        // Given
        when(rubriqueRepository.findById(1L)).thenReturn(Optional.of(rubrique));
        when(rubriqueRepository.findByDesignationAndType(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(rubriqueRepository.save(any(Rubrique.class))).thenReturn(rubrique);
        when(rubriqueMapper.toDTO(rubrique)).thenReturn(rubriqueDTO);

        // When
        RubriqueDTO result = rubriqueService.updateRubrique(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(rubriqueMapper).updateEntityFromRequest(updateRequest, rubrique);
        verify(rubriqueRepository).save(rubrique);
    }

    @Test
    void deleteRubrique_WhenExists_ShouldDelete() {
        // Given
        when(rubriqueRepository.existsById(1L)).thenReturn(true);

        // When
        rubriqueService.deleteRubrique(1L);

        // Then
        verify(rubriqueQuestionRepository).deleteByIdRubrique(1L);
        verify(rubriqueRepository).deleteById(1L);
    }

    @Test
    void deleteRubrique_WhenNotExists_ShouldThrowException() {
        // Given
        when(rubriqueRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> rubriqueService.deleteRubrique(999L));
    }

    @Test
    void addQuestionToRubrique_ShouldAddQuestion() {
        // Given
        AddQuestionToRubriqueRequest request = AddQuestionToRubriqueRequest.builder()
                .idQuestion(1L)
                .ordre(1)
                .build();

        when(rubriqueRepository.existsById(1L)).thenReturn(true);
        when(questionService.existsById(1L)).thenReturn(true);
        when(rubriqueQuestionRepository.existsByIdRubriqueAndIdQuestion(1L, 1L))
                .thenReturn(false);

        // When
        rubriqueService.addQuestionToRubrique(1L, request);

        // Then
        verify(rubriqueQuestionRepository).save(any(RubriqueQuestion.class));
    }

    @Test
    void addQuestionToRubrique_WhenDuplicate_ShouldThrowException() {
        // Given
        AddQuestionToRubriqueRequest request = AddQuestionToRubriqueRequest.builder()
                .idQuestion(1L)
                .ordre(1)
                .build();

        when(rubriqueRepository.existsById(1L)).thenReturn(true);
        when(questionService.existsById(1L)).thenReturn(true);
        when(rubriqueQuestionRepository.existsByIdRubriqueAndIdQuestion(1L, 1L))
                .thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class,
                () -> rubriqueService.addQuestionToRubrique(1L, request));
    }

    @Test
    void removeQuestionFromRubrique_ShouldRemoveQuestion() {
        // Given
        when(rubriqueQuestionRepository.existsByIdRubriqueAndIdQuestion(1L, 1L))
                .thenReturn(true);

        // When
        rubriqueService.removeQuestionFromRubrique(1L, 1L);

        // Then
        verify(rubriqueQuestionRepository).deleteByIdRubriqueAndIdQuestion(1L, 1L);
    }

    @Test
    void reorderQuestionsInRubrique_WithValidOrder_ShouldReorder() {
        // Given
        RubriqueQuestion rq1 = new RubriqueQuestion();
        rq1.setIdRubrique(1L);
        rq1.setIdQuestion(1L);
        rq1.setOrdre(1);

        RubriqueQuestion rq2 = new RubriqueQuestion();
        rq2.setIdRubrique(1L);
        rq2.setIdQuestion(2L);
        rq2.setOrdre(2);

        List<RubriqueQuestion> questions = Arrays.asList(rq1, rq2);

        ReorderQuestionsRequest request = ReorderQuestionsRequest.builder()
                .questionOrders(Arrays.asList(
                        ReorderQuestionsRequest.QuestionOrder.builder()
                                .idQuestion(2L).ordre(1).build(),
                        ReorderQuestionsRequest.QuestionOrder.builder()
                                .idQuestion(1L).ordre(2).build()
                ))
                .build();

        when(rubriqueRepository.existsById(1L)).thenReturn(true);
        when(rubriqueQuestionRepository.findByIdRubriqueOrderByOrdreAsc(1L))
                .thenReturn(questions);
        when(rubriqueQuestionRepository.findByIdRubriqueAndIdQuestion(1L, 2L))
                .thenReturn(Optional.of(rq2));
        when(rubriqueQuestionRepository.findByIdRubriqueAndIdQuestion(1L, 1L))
                .thenReturn(Optional.of(rq1));

        // When
        rubriqueService.reorderQuestionsInRubrique(1L, request);

        // Then
        verify(rubriqueQuestionRepository, times(2)).save(any(RubriqueQuestion.class));
    }

    @Test
    void reorderQuestionsInRubrique_WithInvalidOrder_ShouldThrowException() {
        // Given
        RubriqueQuestion rq1 = new RubriqueQuestion();
        rq1.setIdRubrique(1L);
        rq1.setIdQuestion(1L);

        RubriqueQuestion rq2 = new RubriqueQuestion();
        rq2.setIdRubrique(1L);
        rq2.setIdQuestion(2L);

        ReorderQuestionsRequest request = ReorderQuestionsRequest.builder()
                .questionOrders(Arrays.asList(
                        ReorderQuestionsRequest.QuestionOrder.builder()
                                .idQuestion(1L).ordre(1).build(),
                        ReorderQuestionsRequest.QuestionOrder.builder()
                                .idQuestion(2L).ordre(5).build()
                ))
                .build();

        when(rubriqueRepository.existsById(1L)).thenReturn(true);
        when(rubriqueQuestionRepository.findByIdRubriqueOrderByOrdreAsc(1L))
                .thenReturn(Arrays.asList(rq1, rq2));


        // When & Then
        assertThrows(InvalidOrderException.class,
                () -> rubriqueService.reorderQuestionsInRubrique(1L, request));
    }

    @Test
    void reorderRubriques_WithValidOrder_ShouldReorder() {
        // Given
        Rubrique r1 = new Rubrique();
        r1.setIdRubrique(1L);
        r1.setType("RBS");

        Rubrique r2 = new Rubrique();
        r2.setIdRubrique(2L);
        r2.setType("RBS");

        ReorderRubriquesRequest request = new ReorderRubriquesRequest();
        request.setRubriqueOrders(Arrays.asList(
                createRubriqueOrder(2L, 1),
                createRubriqueOrder(1L, 2)
        ));

        when(rubriqueRepository.findByTypeOrderByOrdreAsc("RBS"))
                .thenReturn(Arrays.asList(r1, r2));
        when(rubriqueRepository.findById(anyLong()))
                .thenReturn(Optional.of(r1), Optional.of(r2));

        // When
        rubriqueService.reorderRubriques("RBS", request);

        // Then
        verify(rubriqueRepository, times(2)).save(any(Rubrique.class));
    }

    private ReorderRubriquesRequest.RubriqueOrder createRubriqueOrder(Long idRubrique, Integer ordre) {
        ReorderRubriquesRequest.RubriqueOrder order = new ReorderRubriquesRequest.RubriqueOrder();
        order.setIdRubrique(idRubrique);
        order.setOrdre(ordre);
        return order;
    }
}