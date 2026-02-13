package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Rubrique.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Arrays;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class RubriqueControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllRubriques_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/rubriques"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void createRubrique_WithValidData_ShouldReturnCreated() throws Exception {
        CreateRubriqueRequest request = CreateRubriqueRequest.builder()
                .designation("TEST INTEGRATION")
                .type("RBS")
                .noEnseignant(1L)
                .build();

        mockMvc.perform(post("/api/rubriques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.designation", is("TEST INTEGRATION")))
                .andExpect(jsonPath("$.type", is("RBS")));
    }

    @Test
    void createRubrique_WithoutType_ShouldDefaultToRBS() throws Exception {
        CreateRubriqueRequest request = CreateRubriqueRequest.builder()
                .designation("TEST DEFAULT TYPE")
                .noEnseignant(1L)
                .build();

        mockMvc.perform(post("/api/rubriques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type", is("RBS")));
    }

    @Test
    void getRubriqueById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/rubriques/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRubrique_WithValidData_ShouldReturnOk() throws Exception {
        // First create
        CreateRubriqueRequest createRequest = CreateRubriqueRequest.builder()
                .designation("ORIGINAL")
                .type("RBS")
                .noEnseignant(1L)
                .build();

        String createResponse = mockMvc.perform(post("/api/rubriques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();

        RubriqueDTO created = objectMapper.readValue(createResponse, RubriqueDTO.class);

        // Then update
        UpdateRubriqueRequest updateRequest = UpdateRubriqueRequest.builder()
                .designation("UPDATED")
                .type("RBS")
                .noEnseignant(1L)
                .ordre(1)
                .build();

        mockMvc.perform(put("/api/rubriques/" + created.getIdRubrique())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation", is("UPDATED")));
    }

    @Test
    void deleteRubrique_WithoutQuestions_ShouldReturnNoContent() throws Exception {
        // Create rubrique
        CreateRubriqueRequest request = CreateRubriqueRequest.builder()
                .designation("TO DELETE")
                .type("RBS")
                .noEnseignant(1L)
                .build();

        String response = mockMvc.perform(post("/api/rubriques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        RubriqueDTO created = objectMapper.readValue(response, RubriqueDTO.class);

        // Delete
        mockMvc.perform(delete("/api/rubriques/" + created.getIdRubrique()))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/rubriques/" + created.getIdRubrique()))
                .andExpect(status().isNotFound());
    }

    @Test
    void reorderRubriques_WithValidData_ShouldReturnOk() throws Exception {
        // Create 2 rubriques
        CreateRubriqueRequest req1 = CreateRubriqueRequest.builder()
                .designation("FIRST").type("RBS").noEnseignant(1L).build();
        CreateRubriqueRequest req2 = CreateRubriqueRequest.builder()
                .designation("SECOND").type("RBS").noEnseignant(1L).build();

        String resp1 = mockMvc.perform(post("/api/rubriques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andReturn().getResponse().getContentAsString();

        String resp2 = mockMvc.perform(post("/api/rubriques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andReturn().getResponse().getContentAsString();

        RubriqueDTO r1 = objectMapper.readValue(resp1, RubriqueDTO.class);
        RubriqueDTO r2 = objectMapper.readValue(resp2, RubriqueDTO.class);

        // Reorder
        ReorderRubriquesRequest reorderRequest = new ReorderRubriquesRequest();
        reorderRequest.setRubriqueOrders(java.util.Arrays.asList(
                createRubriqueOrder(r2.getIdRubrique(), 1),
                createRubriqueOrder(r1.getIdRubrique(), 2)
        ));

        mockMvc.perform(put("/api/rubriques/reorder/RBS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderRequest)))
                .andDo(print())
                .andExpect(status().isOk());

    }

    private ReorderRubriquesRequest.RubriqueOrder createRubriqueOrder(Long idRubrique, Integer ordre) {
        ReorderRubriquesRequest.RubriqueOrder order = new ReorderRubriquesRequest.RubriqueOrder();
        order.setIdRubrique(idRubrique);
        order.setOrdre(ordre);
        return order;
    }
}