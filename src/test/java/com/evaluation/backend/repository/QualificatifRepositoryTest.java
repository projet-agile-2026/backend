package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Qualificatif;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QualificatifRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QualificatifRepository qualificatifRepository;

    @Test
    void existsByMaximalIgnoreCaseAndMinimalIgnoreCase_WhenExactMatch_ShouldReturnTrue() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Excellent", "Insuffisant")).isTrue();
    }

    @Test
    void existsByMaximalIgnoreCaseAndMinimalIgnoreCase_WhenUpperCase_ShouldReturnTrue() {
        Qualificatif q = createQualificatif("excellent", "insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCase("EXCELLENT", "INSUFFISANT")).isTrue();
    }

    @Test
    void existsByMaximalIgnoreCaseAndMinimalIgnoreCase_WhenMixedCase_ShouldReturnTrue() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCase("eXcElLeNt", "iNsUfFiSaNt")).isTrue();
    }

    @Test
    void existsByMaximalIgnoreCaseAndMinimalIgnoreCase_WhenNotExists_ShouldReturnFalse() {
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Excellent", "Insuffisant")).isFalse();
    }

    @Test
    void existsByMaximalIgnoreCaseAndMinimalIgnoreCase_WhenOnlyMaximalMatches_ShouldReturnFalse() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Excellent", "Mauvais")).isFalse();
    }

    @Test
    void existsByMaximalIgnoreCaseAndMinimalIgnoreCase_WhenOnlyMinimalMatches_ShouldReturnFalse() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCase("Bien", "Insuffisant")).isFalse();
    }

    @Test
    void existsByMaximalAndMinimalAndIdNot_WhenSameIdProvided_ShouldReturnFalse() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                        "Excellent", "Insuffisant", q.getIdQualificatif())).isFalse();
    }

    @Test
    void existsByMaximalAndMinimalAndIdNot_WhenDifferentIdExists_ShouldReturnTrue() {
        Qualificatif q1 = createQualificatif("Excellent", "Insuffisant");
        Qualificatif q2 = createQualificatif("Bien", "Mauvais");
        entityManager.persist(q1);
        entityManager.persist(q2);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                        "Excellent", "Insuffisant", q2.getIdQualificatif())).isTrue();
    }

    @Test
    void existsByMaximalAndMinimalAndIdNot_WhenNotExists_ShouldReturnFalse() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                        "Nouveau", "Autre", q.getIdQualificatif())).isFalse();
    }

    @Test
    void existsByMaximalAndMinimalAndIdNot_IsCaseInsensitive() {
        Qualificatif q1 = createQualificatif("Excellent", "Insuffisant");
        Qualificatif q2 = createQualificatif("Bien", "Mauvais");
        entityManager.persist(q1);
        entityManager.persist(q2);
        entityManager.flush();
        assertThat(qualificatifRepository
                .existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(
                        "EXCELLENT", "INSUFFISANT", q2.getIdQualificatif())).isTrue();
    }

    @Test
    void save_ShouldPersistQualificatif() {
        Qualificatif saved = qualificatifRepository.save(createQualificatif("Très Bien", "Passable"));
        entityManager.flush();
        assertThat(saved.getIdQualificatif()).isNotNull();
        assertThat(saved.getMaximal()).isEqualTo("Très Bien");
    }

    @Test
    void findById_WhenExists_ShouldReturnQualificatif() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        Optional<Qualificatif> found = qualificatifRepository.findById(q.getIdQualificatif());
        assertThat(found).isPresent();
        assertThat(found.get().getMaximal()).isEqualTo("Excellent");
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        assertThat(qualificatifRepository.findById(999L)).isEmpty();
    }

    @Test
    void delete_ShouldRemoveQualificatif() {
        Qualificatif q = createQualificatif("Excellent", "Insuffisant");
        entityManager.persist(q);
        entityManager.flush();
        Long id = q.getIdQualificatif();
        qualificatifRepository.delete(q);
        entityManager.flush();
        assertThat(qualificatifRepository.findById(id)).isEmpty();
    }

    @Test
    void save_ShouldUpdateExistingQualificatif() {
        Qualificatif q = createQualificatif("Original", "Opposé");
        entityManager.persist(q);
        entityManager.flush();
        entityManager.clear();
        Qualificatif found = qualificatifRepository.findById(q.getIdQualificatif()).orElseThrow();
        found.setMaximal("Modifié");
        found.setMinimal("Changé");
        qualificatifRepository.save(found);
        entityManager.flush();
        entityManager.clear();
        Qualificatif updated = qualificatifRepository.findById(q.getIdQualificatif()).orElseThrow();
        assertThat(updated.getMaximal()).isEqualTo("Modifié");
        assertThat(updated.getMinimal()).isEqualTo("Changé");
    }

    private Qualificatif createQualificatif(String maximal, String minimal) {
        Qualificatif q = new Qualificatif();
        q.setMaximal(maximal);
        q.setMinimal(minimal);
        return q;
    }
}
