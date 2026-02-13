package com.evaluation.backend.services;

import com.evaluation.backend.dto.Qualificatif.CreateQualificatifRequest;
import com.evaluation.backend.dto.Qualificatif.QualificatifDto;
import com.evaluation.backend.dto.Qualificatif.UpdateQualificatifRequest;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.service.Qualificatif.QualificatifService;
import com.evaluation.backend.service.Qualificatif.QualificatifUsageCounter;
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

@ExtendWith(MockitoExtension.class)
class QualificatifServiceTest {

    @Mock
    private QualificatifRepository repository;

    // On mock l'interface, pas l'implémentation concrète
    // → isole QualificatifService de QuestionRepository
    @Mock
    private QualificatifUsageCounter counter;

    @InjectMocks
    private QualificatifService qualificatifService;

    private Qualificatif qualificatif;

    @BeforeEach
    void setUp() {
        qualificatif = new Qualificatif();
        qualificatif.setIdQualificatif(1L);
        qualificatif.setMaximal("Excellent");
        qualificatif.setMinimal("Insuffisant");
    }

    // ─────────────────────────────────────────────
    // getAll()
    // ─────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnListWithCount() {
        // Given
        Qualificatif q2 = new Qualificatif();
        q2.setIdQualificatif(2L);
        q2.setMaximal("Bien");
        q2.setMinimal("Mauvais");

        List<Qualificatif> qualificatifs = Arrays.asList(qualificatif, q2);
        Map<Long, Long> counts = Map.of(1L, 3L, 2L, 0L);

        when(repository.findAll()).thenReturn(qualificatifs);
        when(counter.countUsage(anySet())).thenReturn(counts);

        // When
        List<QualificatifDto> result = qualificatifService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        QualificatifDto dto1 = result.stream()
                .filter(d -> d.getId().equals(1L)).findFirst().orElseThrow();
        assertEquals("Excellent", dto1.getMot1());
        assertEquals("Insuffisant", dto1.getMot2());
        assertEquals(3L, dto1.getCount());

        QualificatifDto dto2 = result.stream()
                .filter(d -> d.getId().equals(2L)).findFirst().orElseThrow();
        assertEquals(0L, dto2.getCount());

        verify(repository).findAll();
        verify(counter).countUsage(anySet());
    }

    @Test
    void getAll_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(repository.findAll()).thenReturn(Collections.emptyList());
        when(counter.countUsage(anySet())).thenReturn(Map.of());

        // When
        List<QualificatifDto> result = qualificatifService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void getAll_WhenCountNotInMap_ShouldDefaultToZero() {
        // Given - counter retourne une map vide (aucun qualificatif utilisé)
        when(repository.findAll()).thenReturn(List.of(qualificatif));
        when(counter.countUsage(anySet())).thenReturn(Map.of());

        // When
        List<QualificatifDto> result = qualificatifService.getAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(0L, result.get(0).getCount()); // getOrDefault → 0
    }

    // ─────────────────────────────────────────────
    // create()
    // ─────────────────────────────────────────────

    @Test
    void create_WithValidData_ShouldReturnDto() {
        // Given
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("  Excellent  ");
        request.setMot2("  Insuffisant  ");

        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Excellent", "Insuffisant"))
                .thenReturn(false);
        when(repository.save(any(Qualificatif.class))).thenReturn(qualificatif);

        // When
        QualificatifDto result = qualificatifService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Excellent", result.getMot1());
        assertEquals("Insuffisant", result.getMot2());
        assertEquals(0L, result.getCount()); // nouveau → count = 0
        verify(repository).save(any(Qualificatif.class));
    }

    @Test
    void create_ShouldTrimWhitespace() {
        // Given - les mots ont des espaces autour
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("  Bien  ");
        request.setMot2("  Mauvais  ");

        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Bien", "Mauvais"))
                .thenReturn(false);
        when(repository.save(any(Qualificatif.class))).thenAnswer(inv -> {
            Qualificatif saved = inv.getArgument(0);
            // Vérifier que le trim a été appliqué avant le save
            assertEquals("Bien", saved.getMaximal());
            assertEquals("Mauvais", saved.getMinimal());
            saved.setIdQualificatif(2L);
            return saved;
        });

        // When
        QualificatifDto result = qualificatifService.create(request);

        // Then
        assertNotNull(result);
    }

    @Test
    void create_WhenDuplicateCouple_ShouldThrowRuntimeException() {
        // Given - un couple identique existe déjà
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("Excellent");
        request.setMot2("Insuffisant");

        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Excellent", "Insuffisant"))
                .thenReturn(true);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> qualificatifService.create(request));
        assertEquals("Ce couple existe déjà.", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void create_ShouldBeCaseInsensitiveDuplicateCheck() {
        // Given - même couple en minuscules
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("excellent");
        request.setMot2("insuffisant");

        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCase("excellent", "insuffisant"))
                .thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> qualificatifService.create(request));
    }

    // ─────────────────────────────────────────────
    // update()
    // ─────────────────────────────────────────────

    @Test
    void update_WhenNotUsed_ShouldUpdateAndReturnDto() {
        // Given
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Très Bien");
        request.setMot2("Passable");

        Qualificatif updated = new Qualificatif();
        updated.setIdQualificatif(1L);
        updated.setMaximal("Très Bien");
        updated.setMinimal("Passable");

        when(repository.findById(1L)).thenReturn(Optional.of(qualificatif));
        when(counter.countUsage(1L)).thenReturn(0L); // pas utilisé → modifiable
        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                "Très Bien", "Passable", 1L)).thenReturn(false);
        when(repository.save(any(Qualificatif.class))).thenReturn(updated);

        // When
        QualificatifDto result = qualificatifService.update(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("Très Bien", result.getMot1());
        assertEquals("Passable", result.getMot2());
        assertEquals(0L, result.getCount());
        verify(repository).save(any(Qualificatif.class));
    }

    @Test
    void update_WhenUsedByQuestions_ShouldThrowRuntimeException() {
        // Given - le qualificatif est référencé par des questions
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Nouveau");
        request.setMot2("Ancien");

        when(repository.findById(1L)).thenReturn(Optional.of(qualificatif));
        when(counter.countUsage(1L)).thenReturn(5L); // utilisé 5 fois

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> qualificatifService.update(1L, request));
        assertTrue(ex.getMessage().contains("Modification impossible"));
        assertTrue(ex.getMessage().contains("count > 0"));
        verify(repository, never()).save(any());
    }

    @Test
    void update_WhenNotExists_ShouldThrowRuntimeException() {
        // Given
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Test");
        request.setMot2("Test2");

        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> qualificatifService.update(999L, request));
        assertEquals("Couple introuvable.", ex.getMessage());
    }

    @Test
    void update_WhenDuplicateCoupleExists_ShouldThrowRuntimeException() {
        // Given - les nouveaux mots correspondent à un autre couple existant
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Bien");
        request.setMot2("Mauvais");

        when(repository.findById(1L)).thenReturn(Optional.of(qualificatif));
        when(counter.countUsage(1L)).thenReturn(0L);
        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                "Bien", "Mauvais", 1L)).thenReturn(true);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> qualificatifService.update(1L, request));
        assertEquals("Un couple identique existe déjà.", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void update_ShouldTrimWhitespace() {
        // Given
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("  Bien  ");
        request.setMot2("  Mauvais  ");

        when(repository.findById(1L)).thenReturn(Optional.of(qualificatif));
        when(counter.countUsage(1L)).thenReturn(0L);
        when(repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                "Bien", "Mauvais", 1L)).thenReturn(false);
        when(repository.save(any(Qualificatif.class))).thenAnswer(inv -> {
            Qualificatif saved = inv.getArgument(0);
            assertEquals("Bien", saved.getMaximal());
            assertEquals("Mauvais", saved.getMinimal());
            return saved;
        });

        // When
        qualificatifService.update(1L, request);

        // Then - vérifié dans le thenAnswer
        verify(repository).save(any());
    }

    // ─────────────────────────────────────────────
    // delete()
    // ─────────────────────────────────────────────

    @Test
    void delete_WhenNotUsed_ShouldDelete() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(qualificatif));
        when(counter.countUsage(1L)).thenReturn(0L); // pas utilisé → supprimable

        // When
        qualificatifService.delete(1L);

        // Then
        verify(repository).delete(qualificatif);
    }

    @Test
    void delete_WhenUsedByQuestions_ShouldThrowRuntimeException() {
        // Given - le qualificatif est encore référencé
        when(repository.findById(1L)).thenReturn(Optional.of(qualificatif));
        when(counter.countUsage(1L)).thenReturn(2L);

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> qualificatifService.delete(1L));
        assertTrue(ex.getMessage().contains("Suppression impossible"));
        assertTrue(ex.getMessage().contains("count > 0"));
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_WhenNotExists_ShouldThrowRuntimeException() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> qualificatifService.delete(999L));
        assertEquals("Couple introuvable.", ex.getMessage());
        verify(repository, never()).delete(any());
    }
}