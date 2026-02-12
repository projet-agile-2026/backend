package com.evaluation.backend.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UniteEnseignementId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codeFormation;
    private String codeUe;
}
