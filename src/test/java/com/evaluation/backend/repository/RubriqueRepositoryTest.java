package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Rubrique;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RubriqueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RubriqueRepository rubriqueRepository;

    @Test
    void findByDesignationAndType_WhenExists_ShouldReturnRubrique() {
        // Given
        Rubrique rubrique = createRubrique("TEST", "RBS", 1, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();

        // When
        Optional<Rubrique> found = rubriqueRepository.findByDesignationAndType("TEST", "RBS");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDesignation()).isEqualTo("TEST");
        assertThat(found.get().getType()).isEqualTo("RBS");
    }

    @Test
    void findByDesignationAndType_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Rubrique> found = rubriqueRepository.findByDesignationAndType("NONEXISTENT", "RBS");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByDesignationAndType_WhenDifferentType_ShouldReturnEmpty() {
        // Given
        Rubrique rubrique = createRubrique("TEST", "RBS", 1, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();

        // When
        Optional<Rubrique> found = rubriqueRepository.findByDesignationAndType("TEST", "OTHER");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByTypeOrderByOrdreAsc_ShouldReturnOrderedList() {
        // Given
        Rubrique r1 = createRubrique("SECOND", "RBS", 2, 1L);
        Rubrique r2 = createRubrique("FIRST", "RBS", 1, 1L);
        Rubrique r3 = createRubrique("THIRD", "RBS", 3, 1L);
        Rubrique r4 = createRubrique("OTHER", "OTHER_TYPE", 1, 1L);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.persist(r4);
        entityManager.flush();

        // When
        List<Rubrique> result = rubriqueRepository.findByTypeOrderByOrdreAsc("RBS");

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrdre()).isEqualTo(1);
        assertThat(result.get(0).getDesignation()).isEqualTo("FIRST");
        assertThat(result.get(1).getOrdre()).isEqualTo(2);
        assertThat(result.get(1).getDesignation()).isEqualTo("SECOND");
        assertThat(result.get(2).getOrdre()).isEqualTo(3);
        assertThat(result.get(2).getDesignation()).isEqualTo("THIRD");
    }

    @Test
    void findByTypeOrderByOrdreAsc_WhenNoMatches_ShouldReturnEmptyList() {
        // When
        List<Rubrique> result = rubriqueRepository.findByTypeOrderByOrdreAsc("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findMaxOrdreByType_ShouldReturnMax() {
        // Given
        Rubrique r1 = createRubrique("TEST1", "RBS", 3, 1L);
        Rubrique r2 = createRubrique("TEST2", "RBS", 5, 1L);
        Rubrique r3 = createRubrique("TEST3", "RBS", 1, 1L);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.flush();

        // When
        Integer maxOrdre = rubriqueRepository.findMaxOrdreByType("RBS");

        // Then
        assertThat(maxOrdre).isEqualTo(5);
    }

    @Test
    void findMaxOrdreByType_WhenNoMatches_ShouldReturnNull() {
        // When
        Integer maxOrdre = rubriqueRepository.findMaxOrdreByType("NONEXISTENT");

        // Then
        assertThat(maxOrdre).isNull();
    }

    @Test
    void findMaxOrdreByType_ShouldOnlyConsiderMatchingType() {
        // Given
        Rubrique r1 = createRubrique("RBS1", "RBS", 5, 1L);
        Rubrique r2 = createRubrique("OTHER1", "OTHER", 10, 1L);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.flush();

        // When
        Integer maxOrdreRBS = rubriqueRepository.findMaxOrdreByType("RBS");
        Integer maxOrdreOther = rubriqueRepository.findMaxOrdreByType("OTHER");

        // Then
        assertThat(maxOrdreRBS).isEqualTo(5);
        assertThat(maxOrdreOther).isEqualTo(10);
    }

    @Test
    void findAllOrderByOrdre_ShouldReturnAllRubriquesOrdered() {
        // Given
        Rubrique r1 = createRubrique("THIRD", "RBS", 3, 1L);
        Rubrique r2 = createRubrique("FIRST", "OTHER", 1, 1L);
        Rubrique r3 = createRubrique("SECOND", "RBS", 2, 1L);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.flush();

        // When
        List<Rubrique> result = rubriqueRepository.findAllOrderByOrdre();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrdre()).isEqualTo(1);
        assertThat(result.get(0).getDesignation()).isEqualTo("FIRST");
        assertThat(result.get(1).getOrdre()).isEqualTo(2);
        assertThat(result.get(2).getOrdre()).isEqualTo(3);
    }

    @Test
    void findAllOrderByOrdre_WhenEmpty_ShouldReturnEmptyList() {
        // When
        List<Rubrique> result = rubriqueRepository.findAllOrderByOrdre();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByNoEnseignant_ShouldReturnMatchingRubriques() {
        // Given
        Rubrique r1 = createRubrique("ENG1", "RBS", 1, 100L);
        Rubrique r2 = createRubrique("ENG2", "RBS", 2, 100L);
        Rubrique r3 = createRubrique("OTHER", "RBS", 3, 200L);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.flush();

        // When
        List<Rubrique> result = rubriqueRepository.findByNoEnseignant(100L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Rubrique::getNoEnseignant)
                .containsOnly(100L);
    }

    @Test
    void findByNoEnseignant_WhenNoMatches_ShouldReturnEmptyList() {
        // When
        List<Rubrique> result = rubriqueRepository.findByNoEnseignant(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistNewRubrique() {
        // Given
        Rubrique rubrique = createRubrique("NEW", "RBS", 1, 1L);

        // When
        Rubrique saved = rubriqueRepository.save(rubrique);
        entityManager.flush();

        // Then
        assertThat(saved.getIdRubrique()).isNotNull();
        assertThat(saved.getDesignation()).isEqualTo("NEW");
    }

    @Test
    void save_ShouldUpdateExistingRubrique() {
        // Given
        Rubrique rubrique = createRubrique("ORIGINAL", "RBS", 1, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();
        entityManager.clear();

        // When
        Rubrique found = rubriqueRepository.findById(rubrique.getIdRubrique()).get();
        found.setDesignation("UPDATED");
        Rubrique updated = rubriqueRepository.save(found);
        entityManager.flush();

        // Then
        assertThat(updated.getDesignation()).isEqualTo("UPDATED");
    }

    @Test
    void deleteById_ShouldRemoveRubrique() {
        // Given
        Rubrique rubrique = createRubrique("TO_DELETE", "RBS", 1, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();
        Long id = rubrique.getIdRubrique();

        // When
        rubriqueRepository.deleteById(id);
        entityManager.flush();

        // Then
        Optional<Rubrique> found = rubriqueRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        // Given
        Rubrique rubrique = createRubrique("TEST", "RBS", 1, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();

        // When
        boolean exists = rubriqueRepository.existsById(rubrique.getIdRubrique());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        // When
        boolean exists = rubriqueRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findById_WhenExists_ShouldReturnRubrique() {
        // Given
        Rubrique rubrique = createRubrique("TEST", "RBS", 1, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();

        // When
        Optional<Rubrique> found = rubriqueRepository.findById(rubrique.getIdRubrique());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDesignation()).isEqualTo("TEST");
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        // When
        Optional<Rubrique> found = rubriqueRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByTypeOrderByOrdreAsc_ShouldHandleMultipleTypes() {
        // Given
        Rubrique r1 = createRubrique("RBS1", "RBS", 1, 1L);
        Rubrique r2 = createRubrique("RBS2", "RBS", 2, 1L);
        Rubrique r3 = createRubrique("OTHER1", "OTHER", 1, 1L);
        Rubrique r4 = createRubrique("OTHER2", "OTHER", 2, 1L);

        entityManager.persist(r1);
        entityManager.persist(r2);
        entityManager.persist(r3);
        entityManager.persist(r4);
        entityManager.flush();

        // When
        List<Rubrique> rbsResult = rubriqueRepository.findByTypeOrderByOrdreAsc("RBS");
        List<Rubrique> otherResult = rubriqueRepository.findByTypeOrderByOrdreAsc("OTHER");

        // Then
        assertThat(rbsResult).hasSize(2);
        assertThat(otherResult).hasSize(2);
        assertThat(rbsResult).extracting(Rubrique::getType).containsOnly("RBS");
        assertThat(otherResult).extracting(Rubrique::getType).containsOnly("OTHER");
    }

    @Test
    void findMaxOrdreByType_WithSingleRubrique_ShouldReturnThatOrdre() {
        // Given
        Rubrique rubrique = createRubrique("ONLY", "RBS", 7, 1L);
        entityManager.persist(rubrique);
        entityManager.flush();

        // When
        Integer maxOrdre = rubriqueRepository.findMaxOrdreByType("RBS");

        // Then
        assertThat(maxOrdre).isEqualTo(7);
    }

    // Helper method
    private Rubrique createRubrique(String designation, String type, Integer ordre, Long noEnseignant) {
        Rubrique rubrique = new Rubrique();
        rubrique.setDesignation(designation);
        rubrique.setType(type);
        rubrique.setOrdre(ordre);
        rubrique.setNoEnseignant(noEnseignant);
        return rubrique;
    }
}