package com.evaluation.backend.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DroitId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idEvaluation;
    private Long noEnseignant;
}
