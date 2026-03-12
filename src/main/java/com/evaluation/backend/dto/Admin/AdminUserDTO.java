package com.evaluation.backend.dto.Admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class AdminUserDTO {

    private Long authId;

    private String nom;

    private String prenom;

    private String email;

    private String telephone;

    private String role;

    private Boolean active;

}
