package com.evaluation.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "RUBRIQUE")
public class Rubrique implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rubrique_seq")
    @SequenceGenerator(name = "rubrique_seq", sequenceName = "RUB_SEQ", allocationSize = 1)
    @Column(name = "ID_RUBRIQUE", nullable = false)
    private Long idRubrique;

    @Column(name = "TYPE", nullable = false, length = 10)
    private String type;

    @Column(name = "NO_ENSEIGNANT")
    private Long noEnseignant;

    @Column(name = "DESIGNATION", nullable = false, length = 32)
    private String designation;

    @Column(name = "ORDRE")
    private Integer ordre;



}