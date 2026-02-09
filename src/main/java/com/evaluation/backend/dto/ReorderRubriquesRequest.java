package com.evaluation.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderRubriquesRequest {

    private List<RubriqueOrder> rubriqueOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RubriqueOrder {
        private Long idRubrique;
        private Integer ordre;
    }
}