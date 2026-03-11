package com.evaluation.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "REPONSE_QUESTION")
public class ReponseQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ReponseQuestionId id;

    @Column(name = "POSITIONNEMENT")
    private Long positionnement;

}
