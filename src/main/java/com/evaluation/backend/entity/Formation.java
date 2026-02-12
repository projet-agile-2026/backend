package com.evaluation.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "FORMATION")
public class Formation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CODE_FORMATION", nullable = false)
    private String codeFormation;

    @Column(name = "DIPLOME", nullable = false)
    private String diplome;

    @Column(name = "N0_ANNEE", nullable = false)
    private String n0Annee;

    @Column(name = "NOM_FORMATION", nullable = false)
    private String nomFormation;

    @Column(name = "DOUBLE_DIPLOME", nullable = false)
    private String doubleDiplome;

    @Column(name = "DEBUT_ACCREDITATION")
    private Date debutAccreditation;

    @Column(name = "FIN_ACCREDITATION")
    private Date finAccreditation;

}
