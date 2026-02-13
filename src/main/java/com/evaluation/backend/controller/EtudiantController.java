package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Promotions.EtudiantRequestDTO;
import com.evaluation.backend.dto.Promotions.EtudiantResponseDTO;
import com.evaluation.backend.service.Promotions.EtudiantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EtudiantController {

    private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    @GetMapping("/promotions/{codeFormation}/{anneeUniversitaire}/etudiants")
    public List<EtudiantResponseDTO> listByPromotion(
            @PathVariable String codeFormation,
            @PathVariable String anneeUniversitaire
    ) {
        return etudiantService.listByPromotion(codeFormation, anneeUniversitaire);
    }

    @PostMapping("/promotions/{codeFormation}/{anneeUniversitaire}/etudiants")
    public EtudiantResponseDTO addToPromotion(
            @PathVariable String codeFormation,
            @PathVariable String anneeUniversitaire,
            @Valid @RequestBody EtudiantRequestDTO dto
    ) {
        return etudiantService.addToPromotion(codeFormation, anneeUniversitaire, dto);
    }

    @PutMapping("/etudiants/{noEtudiant}")
    public EtudiantResponseDTO update(
            @PathVariable Long noEtudiant,
            @Valid @RequestBody EtudiantRequestDTO dto
    ) {
        return etudiantService.update(noEtudiant, dto);
    }

    @DeleteMapping("/etudiants/{noEtudiant}")
    public void delete(@PathVariable Long noEtudiant) {
        etudiantService.delete(noEtudiant);
    }
}
