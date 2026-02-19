package com.evaluation.backend.dto.Promotions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnseignantLightDTO {

    private Integer noEnseignant;
    private String nom;
    private String prenom;
    private String emailUbo;
}
