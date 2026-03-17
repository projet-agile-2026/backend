package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "QUESTIONNAIRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Questionnaire implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "questionnaire_seq_gen")
    @SequenceGenerator(
            name = "questionnaire_seq_gen",
            sequenceName = "QUE_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_QUESTIONNAIRE", nullable = false)
    private Long idQuestionnaire;

    @Column(name = "DESIGNATION", nullable = false)
    private String designation;

    /**
     * Un questionnaire contient plusieurs rubriques
     */
    @OneToMany(mappedBy = "questionnaire", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<RubriqueQuestionnaire> rubriquesQuestionnaire;
}
