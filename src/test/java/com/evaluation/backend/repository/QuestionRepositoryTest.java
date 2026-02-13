package com.evaluation.backend.repository;

import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class QuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    private Qualificatif qualificatif;

    @BeforeEach
    void setUp() {
        // Qualificatif requis par la FK de Question
        qualificatif = new Qualificatif();
        qualificatif.setMaximal("Excellent");
        qualificatif.setMinimal("Insuffisant");
        entityManager.persist(qualificatif);
        entityManager.flush();
    }

    // ─────────────────────────────────────────────
    // findByType()
    // ─────────────────────────────────────────────

    @Test
    void findByType_ShouldReturnMatchingQuestions() {
        createAndPersist("QCM", "1", "Question 1");
        createAndPersist("QCM", "1", "Question 2");
        createAndPersist("QCU", "1", "Question 3");

        List<Question> result = questionRepository.findByType("QCM");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Question::getType).containsOnly("QCM");
    }

    @Test
    void findByType_WhenNoMatch_ShouldReturnEmptyList() {
        List<Question> result = questionRepository.findByType("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // findByNoEnseignant()
    // ─────────────────────────────────────────────

    @Test
    void findByNoEnseignant_ShouldReturnMatchingQuestions() {
        createAndPersist("QCM", "100", "Q1");
        createAndPersist("QCM", "100", "Q2");
        createAndPersist("QCM", "200", "Q3");

        List<Question> result = questionRepository.findByNoEnseignant("100");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Question::getNoEnseignant).containsOnly("100");
    }

    @Test
    void findByNoEnseignant_WhenNoMatch_ShouldReturnEmptyList() {
        List<Question> result = questionRepository.findByNoEnseignant("999");
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // findByQualificatifId()
    // ─────────────────────────────────────────────

    @Test
    void findByQualificatifId_ShouldReturnMatchingQuestions() {
        String qualifId = String.valueOf(qualificatif.getIdQualificatif());
        createAndPersist("QCM", "1", "Q1");
        createAndPersist("QCM", "1", "Q2");

        List<Question> result = questionRepository.findByQualificatifId(qualifId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Question::getIdQualificatif).containsOnly(qualifId);
    }

    @Test
    void findByQualificatifId_WhenNoMatch_ShouldReturnEmptyList() {
        List<Question> result = questionRepository.findByQualificatifId("999");
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // countByIdQualificatif()
    // ─────────────────────────────────────────────

    @Test
    void countByIdQualificatif_ShouldReturnCorrectCount() {
        String qualifId = String.valueOf(qualificatif.getIdQualificatif());
        createAndPersist("QCM", "1", "Q1");
        createAndPersist("QCM", "1", "Q2");
        createAndPersist("QCM", "1", "Q3");

        long count = questionRepository.countByIdQualificatif(qualifId);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void countByIdQualificatif_WhenNoMatch_ShouldReturnZero() {
        long count = questionRepository.countByIdQualificatif("999");
        assertThat(count).isEqualTo(0);
    }

    // ─────────────────────────────────────────────
    // countByIdQualificatifInGroup()
    // ─────────────────────────────────────────────

    @Test
    void countByIdQualificatifInGroup_ShouldReturnCountPerQualificatif() {
        // Given - second qualificatif
        Qualificatif qualif2 = new Qualificatif();
        qualif2.setMaximal("Bien");
        qualif2.setMinimal("Mauvais");
        entityManager.persist(qualif2);
        entityManager.flush();

        String id1 = String.valueOf(qualificatif.getIdQualificatif());
        String id2 = String.valueOf(qualif2.getIdQualificatif());

        createAndPersist("QCM", "1", "Q1"); // → qualif1
        createAndPersist("QCM", "1", "Q2"); // → qualif1
        createAndPersistWithQualif("QCM", "1", "Q3", id2); // → qualif2

        List<Object[]> result = questionRepository.countByIdQualificatifInGroup(List.of(id1, id2));

        assertThat(result).hasSize(2);

        // Vérifier les counts par ID
        long countQ1 = result.stream()
                .filter(r -> r[0].equals(id1))
                .mapToLong(r -> Long.parseLong(r[1].toString()))
                .findFirst().orElse(0L);
        long countQ2 = result.stream()
                .filter(r -> r[0].equals(id2))
                .mapToLong(r -> Long.parseLong(r[1].toString()))
                .findFirst().orElse(0L);

        assertThat(countQ1).isEqualTo(2);
        assertThat(countQ2).isEqualTo(1);
    }

    @Test
    void countByIdQualificatifInGroup_WhenEmptyList_ShouldReturnEmpty() {
        List<Object[]> result = questionRepository.countByIdQualificatifInGroup(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void countByIdQualificatifInGroup_WhenNoMatch_ShouldReturnEmpty() {
        List<Object[]> result = questionRepository.countByIdQualificatifInGroup(List.of("999"));
        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────
    // CRUD standard
    // ─────────────────────────────────────────────

    @Test
    void save_ShouldPersistQuestion() {
        Question q = buildQuestion("QCM", "1", "Nouvelle question");
        Question saved = questionRepository.save(q);
        entityManager.flush();

        assertThat(saved.getIdQuestion()).isNotNull();
        assertThat(saved.getIntitule()).isEqualTo("Nouvelle question");
    }

    @Test
    void findById_WhenExists_ShouldReturnQuestion() {
        Question q = createAndPersist("QCM", "1", "Test");

        assertThat(questionRepository.findById(q.getIdQuestion())).isPresent();
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        assertThat(questionRepository.findById(999L)).isEmpty();
    }

    @Test
    void deleteById_ShouldRemoveQuestion() {
        Question q = createAndPersist("QCM", "1", "À supprimer");
        Long id = q.getIdQuestion();

        questionRepository.deleteById(id);
        entityManager.flush();

        assertThat(questionRepository.findById(id)).isEmpty();
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        Question q = createAndPersist("QCM", "1", "Test");
        assertThat(questionRepository.existsById(q.getIdQuestion())).isTrue();
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        assertThat(questionRepository.existsById(999L)).isFalse();
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private Question createAndPersist(String type, String noEnseignant, String intitule) {
        Question q = buildQuestion(type, noEnseignant, intitule);
        entityManager.persist(q);
        entityManager.flush();
        return q;
    }

    private Question createAndPersistWithQualif(String type, String noEnseignant, String intitule, String qualifId) {
        Question q = new Question();
        q.setType(type);
        q.setNoEnseignant(noEnseignant);
        q.setIntitule(intitule);
        q.setIdQualificatif(qualifId);
        entityManager.persist(q);
        entityManager.flush();
        return q;
    }

    private Question buildQuestion(String type, String noEnseignant, String intitule) {
        Question q = new Question();
        q.setType(type);
        q.setNoEnseignant(noEnseignant);
        q.setIntitule(intitule);
        q.setIdQualificatif(String.valueOf(qualificatif.getIdQualificatif()));
        return q;
    }
}