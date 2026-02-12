package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "RUBRIQUE_QUESTION")
@IdClass(RubriqueQuestionId.class)
public class RubriqueQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_RUBRIQUE", nullable = false)
    private Long idRubrique;

    @Id
    @Column(name = "ID_QUESTION", nullable = false)
    private Long idQuestion;

    @Column(name = "ORDRE", nullable = false)
    private Integer ordre;



    // JPA Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RUBRIQUE", insertable = false, updatable = false)
    private Rubrique rubrique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_QUESTION", insertable = false, updatable = false)
    private Question question;


}