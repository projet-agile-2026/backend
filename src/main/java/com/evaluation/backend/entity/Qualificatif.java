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
@Table(name = "QUALIFICATIF")
public class Qualificatif implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "qualificatif_seq")
    @SequenceGenerator(name = "qualificatif_seq", sequenceName = "QUA_SEQ", allocationSize = 1)
    @Column(name = "ID_QUALIFICATIF", nullable = false)
    private Long idQualificatif;

    @Column(name = "MAXIMAL", nullable = false, length = 16)
    private String maximal;

    @Column(name = "MINIMAL", nullable = false, length = 16)
    private String minimal;



}