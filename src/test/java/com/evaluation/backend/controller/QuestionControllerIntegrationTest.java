package com.evaluation.backend.controller;

import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.entity.Question;
import com.evaluation.backend.repository.QualificatifRepository;
import com.evaluation.backend.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@test.com", roles = {"ENSEIGNANT"})
class QuestionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QualificatifRepository qualificatifRepository;

    private Qualificatif qualificatif;
    private Question existingQuestion;

    @BeforeEach
    void setUp() {
        qualificatif = new Qualificatif();
        qualificatif.setMaximal("Excellent");
        qualificatif.setMinimal("Insuffisant");
        qualificatif = qualificatifRepository.save(qualificatif);

        existingQuestion = new Question();
        existingQuestion.setType("QCM");
        existingQuestion.setNoEnseignant("1");
        existingQuestion.setIdQualificatif(String.valueOf(qualificatif.getIdQualificatif()));
        existingQuestion.setIntitule("Question test");
        existingQuestion = questionRepository.save(existingQuestion);
    }

    // ─────────────────────────────────────────────
    // GET /api/questions
    // ─────────────────────────────────────────────

    @Test
    void getAll_ShouldReturnOkAndList() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void getAll_ShouldReturnQuestionWithUsedInRubriqueField() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idQuestion", notNullValue()))
                .andExpect(jsonPath("$[0].type", notNullValue()))
                .andExpect(jsonPath("$[0].intitule", notNullValue()))
                .andExpect(jsonPath("$[0].usedInRubrique", isA(Boolean.class)));
    }

    @Test
    void getAll_ShouldContainCreatedQuestion() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.intitule == 'Question test')]",
                        hasSize(greaterThanOrEqualTo(1))));
    }

    // ─────────────────────────────────────────────
    // POST /api/questions/create
    // ─────────────────────────────────────────────

    @Test
    void createQuestion_WithValidData_ShouldReturnCreated() throws Exception {
        Question newQuestion = new Question();
        newQuestion.setType("QCU");
        newQuestion.setNoEnseignant("1");
        newQuestion.setIdQualificatif(String.valueOf(qualificatif.getIdQualificatif()));
        newQuestion.setIntitule("Nouvelle question");

        mockMvc.perform(post("/api/questions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newQuestion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.intitule", is("Nouvelle question")))
                .andExpect(jsonPath("$.type", is("QCU")))
                .andExpect(jsonPath("$.idQuestion", notNullValue()));
    }

    @Test
    void createQuestion_WithValidQualificatif_ShouldReturnCreated() throws Exception {
        // Note: le controller utilise questionRepository.save() directement
        // → pas de validation service, la DB gère les contraintes FK
        Question newQuestion = new Question();
        newQuestion.setType("QCM");
        newQuestion.setNoEnseignant("1");
        newQuestion.setIdQualificatif(String.valueOf(qualificatif.getIdQualificatif()));
        newQuestion.setIntitule("Question avec qualificatif valide");

        mockMvc.perform(post("/api/questions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newQuestion)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.intitule", is("Question avec qualificatif valide")));
    }

    // ─────────────────────────────────────────────
    // PUT /api/questions/update/{id}
    // ─────────────────────────────────────────────

    @Test
    void update_WhenExists_ShouldReturnOk() throws Exception {
        Question updatedDetails = new Question();
        updatedDetails.setType("QCU");
        updatedDetails.setNoEnseignant("2");
        updatedDetails.setIdQualificatif(String.valueOf(qualificatif.getIdQualificatif()));
        updatedDetails.setIntitule("Question modifiée");

        mockMvc.perform(put("/api/questions/update/" + existingQuestion.getIdQuestion())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("QCU")))
                .andExpect(jsonPath("$.intitule", is("Question modifiée")));
    }

    @Test
    void update_WhenNotExists_ShouldReturn404() throws Exception {
        Question updatedDetails = new Question();
        updatedDetails.setType("QCM");
        updatedDetails.setNoEnseignant("1");
        updatedDetails.setIdQualificatif(String.valueOf(qualificatif.getIdQualificatif()));
        updatedDetails.setIntitule("Test");

        mockMvc.perform(put("/api/questions/update/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────
    // DELETE /api/questions/delete/{id}
    // ─────────────────────────────────────────────

    @Test
    void delete_WhenExists_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/questions/delete/" + existingQuestion.getIdQuestion()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/questions/delete/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_WhenAlreadyDeleted_ShouldReturn404() throws Exception {
        Long id = existingQuestion.getIdQuestion();

        mockMvc.perform(delete("/api/questions/delete/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/questions/delete/" + id))
                .andExpect(status().isNotFound());
    }
}