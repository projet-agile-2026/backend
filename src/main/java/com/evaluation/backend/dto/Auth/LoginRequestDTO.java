package com.evaluation.backend.dto.Auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    private String email;
    private String motPasse;
}

