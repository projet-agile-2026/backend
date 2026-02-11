package com.evaluation.backend.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "QUALIFICATIF",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MAXIMAL", "MINIMAL"})
)
public class Qualificatif implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qualificatif_seq")
    @SequenceGenerator(
            name = "qualificatif_seq",
            sequenceName = "QUALIFICATIF_SEQ",
            allocationSize = 1
    )
    @Column(name = "ID_QUALIFICATIF", nullable = false)
    private Long idQualificatif;


    @Column(name = "MAXIMAL", nullable = false)
    private String maximal;

    @Column(name = "MINIMAL", nullable = false)
    private String minimal;

}

