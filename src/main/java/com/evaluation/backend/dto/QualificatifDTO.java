package com.evaluation.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualificatifDTO implements Serializable {

    private Long idQualificatif;

    @NotBlank(message = "Maximal value is required")
    @Size(max = 16, message = "Maximal value must not exceed 16 characters")
    private String maximal;

    @NotBlank(message = "Minimal value is required")
    @Size(max = 16, message = "Minimal value must not exceed 16 characters")
    private String minimal;
}