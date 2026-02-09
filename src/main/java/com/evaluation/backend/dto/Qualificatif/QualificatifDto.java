package com.evaluation.backend.dto.Qualificatif;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QualificatifDto {
    private Long id;
    private String mot1;
    private String mot2;
    private long count;
}
