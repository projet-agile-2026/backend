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

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "CG_REF_CODES")
public class CgRefCodes implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_CGRC", nullable = false)
    private Long idCgrc;

    @Column(name = "RV_DOMAIN", nullable = false)
    private String rvDomain;

    @Column(name = "RV_LOW_VALUE", nullable = false)
    private String rvLowValue;

    @Column(name = "RV_HIGH_VALUE")
    private String rvHighValue;

    @Column(name = "RV_ABBREVIATION")
    private String rvAbbreviation;

    @Column(name = "RV_MEANING")
    private String rvMeaning;

}
