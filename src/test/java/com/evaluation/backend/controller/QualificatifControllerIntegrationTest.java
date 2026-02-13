package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Qualificatif.CreateQualificatifRequest;
import com.evaluation.backend.dto.Qualificatif.UpdateQualificatifRequest;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.repository.QualificatifRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// @WithMockUser simule un utilisateur authentifié pour contourner Spring Security
// → à adapter selon ta config de sécurité (ROLE_ENSEIGNANT, etc.)
@WithMockUser(username = "test@test.com", roles = {"ENSEIGNANT"})
class QualificatifControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QualificatifRepository qualificatifRepository;

    private Qualificatif existingQualificatif;

    @BeforeEach
    void setUp() {
        // Créer un qualificatif de base pour les tests qui en ont besoin
        existingQualificatif = new Qualificatif();
        existingQualificatif.setMaximal("Excellent");
        existingQualificatif.setMinimal("Insuffisant");
        existingQualificatif = qualificatifRepository.save(existingQualificatif);
    }

    // ─────────────────────────────────────────────
    // GET /api/qualificatifs
    // ─────────────────────────────────────────────

    @Test
    void list_ShouldReturnOkAndJsonList() throws Exception {
        mockMvc.perform(get("/api/qualificatifs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void list_ShouldReturnQualificatifWithCountField() throws Exception {
        mockMvc.perform(get("/api/qualificatifs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].mot1", notNullValue()))
                .andExpect(jsonPath("$[0].mot2", notNullValue()))
                .andExpect(jsonPath("$[0].count", isA(Number.class)));
    }

    @Test
    void list_ShouldContainCreatedQualificatif() throws Exception {
        mockMvc.perform(get("/api/qualificatifs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.mot1 == 'Excellent')]", hasSize(greaterThanOrEqualTo(1))));
    }

    // ─────────────────────────────────────────────
    // POST /api/qualificatifs
    // ─────────────────────────────────────────────

    @Test
    void create_WithValidData_ShouldReturnCreated() throws Exception {
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("Très Bien");
        request.setMot2("Passable");

        mockMvc.perform(post("/api/qualificatifs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mot1", is("Très Bien")))
                .andExpect(jsonPath("$.mot2", is("Passable")))
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void create_WithWhitespace_ShouldTrimAndReturnCreated() throws Exception {
        // Given - mots avec espaces
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("  Bien  ");
        request.setMot2("  Mauvais  ");

        mockMvc.perform(post("/api/qualificatifs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mot1", is("Bien")))   // trim appliqué
                .andExpect(jsonPath("$.mot2", is("Mauvais")));
    }

    @Test
    void create_WhenDuplicateCouple_ShouldReturn500() throws Exception {
        // Given - "Excellent" / "Insuffisant" existe déjà (créé dans setUp)
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("Excellent");
        request.setMot2("Insuffisant");

        mockMvc.perform(post("/api/qualificatifs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // RuntimeException → 500 par défaut
    }

    @Test
    void create_WhenDuplicateCaseInsensitive_ShouldReturn500() throws Exception {
        // Given - même couple mais en majuscules
        CreateQualificatifRequest request = new CreateQualificatifRequest();
        request.setMot1("EXCELLENT");
        request.setMot2("INSUFFISANT");

        mockMvc.perform(post("/api/qualificatifs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────
    // PUT /api/qualificatifs/{id}
    // ─────────────────────────────────────────────

    @Test
    void update_WhenValidAndNotUsed_ShouldReturnOk() throws Exception {
        // Given - qualificatif non utilisé par aucune question
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Modifié");
        request.setMot2("Changé");

        mockMvc.perform(put("/api/qualificatifs/" + existingQualificatif.getIdQualificatif())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mot1", is("Modifié")))
                .andExpect(jsonPath("$.mot2", is("Changé")));
    }

    @Test
    void update_WhenNotExists_ShouldReturn500() throws Exception {
        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Test");
        request.setMot2("Test2");

        mockMvc.perform(put("/api/qualificatifs/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void update_WhenDuplicateCoupleExists_ShouldReturn500() throws Exception {
        // Given - créer un second qualificatif, puis essayer de mettre le premier
        //         avec les mêmes mots que le second
        Qualificatif second = new Qualificatif();
        second.setMaximal("Bien");
        second.setMinimal("Mauvais");
        qualificatifRepository.save(second);

        UpdateQualificatifRequest request = new UpdateQualificatifRequest();
        request.setMot1("Bien");
        request.setMot2("Mauvais");

        mockMvc.perform(put("/api/qualificatifs/" + existingQualificatif.getIdQualificatif())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────
    // DELETE /api/qualificatifs/{id}
    // ─────────────────────────────────────────────

    @Test
    void delete_WhenNotUsed_ShouldReturnNoContent() throws Exception {
        // Given - qualificatif sans questions associées
        Qualificatif toDelete = new Qualificatif();
        toDelete.setMaximal("À Supprimer");
        toDelete.setMinimal("Supprimable");
        toDelete = qualificatifRepository.save(toDelete);

        // When
        mockMvc.perform(delete("/api/qualificatifs/" + toDelete.getIdQualificatif()))
                .andExpect(status().isNoContent());

        // Then - vérifier qu'il n'existe plus
        mockMvc.perform(get("/api/qualificatifs"))
                .andExpect(jsonPath("$[?(@.mot1 == 'À Supprimer')]", hasSize(0)));
    }

    @Test
    void delete_WhenNotExists_ShouldReturn500() throws Exception {
        mockMvc.perform(delete("/api/qualificatifs/99999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void delete_WhenAlreadyDeleted_ShouldReturn500() throws Exception {
        // Given - supprimer une première fois
        Long id = existingQualificatif.getIdQualificatif();
        mockMvc.perform(delete("/api/qualificatifs/" + id))
                .andExpect(status().isNoContent());

        // When - supprimer une deuxième fois
        mockMvc.perform(delete("/api/qualificatifs/" + id))
                .andExpect(status().is5xxServerError());
    }
}