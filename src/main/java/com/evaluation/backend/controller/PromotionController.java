package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Promotions.PromotionCreateUpdateDTO;
import com.evaluation.backend.dto.Promotions.PromotionResponseDTO;
import com.evaluation.backend.service.Promotions.PromotionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public List<PromotionResponseDTO> list(
            @RequestParam(required = false) String anneeUniversitaire,
            @RequestParam(required = false) String diplome,
            @RequestParam(required = false) String nomFormation
    ) {
        return promotionService.list(anneeUniversitaire, diplome, nomFormation);
    }

    @GetMapping("/{codeFormation}/{anneeUniversitaire}")
    public PromotionResponseDTO getOne(
            @PathVariable String codeFormation,
            @PathVariable String anneeUniversitaire
    ) {
        return promotionService.getOne(codeFormation, anneeUniversitaire);
    }

    @PostMapping
    public PromotionResponseDTO create(@Valid @RequestBody PromotionCreateUpdateDTO dto) {
        return promotionService.create(dto);
    }

    @PutMapping("/{codeFormation}/{anneeUniversitaire}")
    public PromotionResponseDTO update(
            @PathVariable String codeFormation,
            @PathVariable String anneeUniversitaire,
            @Valid @RequestBody PromotionCreateUpdateDTO dto
    ) {
        return promotionService.update(codeFormation, anneeUniversitaire, dto);
    }

    @DeleteMapping("/{codeFormation}/{anneeUniversitaire}")
    public void delete(
            @PathVariable String codeFormation,
            @PathVariable String anneeUniversitaire
    ) {
        promotionService.delete(codeFormation, anneeUniversitaire);
    }
}
