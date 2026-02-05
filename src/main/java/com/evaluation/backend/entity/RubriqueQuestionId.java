package com.evaluation.backend.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RubriqueQuestionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRubrique;
    private Long idQuestion;

}