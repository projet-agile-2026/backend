package com.evaluation.backend.dto.Question;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class QuestionnaireRequestDTO {

    @NotBlank
    private String designation;
}