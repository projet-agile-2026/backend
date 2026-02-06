package com.evaluation.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateQualificatifRequest {
    @NotBlank
    private String mot1;

    @NotBlank
    private String mot2;
}
