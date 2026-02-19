package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Promotions.EnseignantLightDTO;

import com.evaluation.backend.service.Promotions.EnseignantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enseignants")
public class EnseignantController {

    private final EnseignantService enseignantService;

    public EnseignantController(EnseignantService enseignantService) {
        this.enseignantService = enseignantService;
    }

    @GetMapping
    public List<EnseignantLightDTO> getAll() {
        return enseignantService.getAllLight();
    }
}
