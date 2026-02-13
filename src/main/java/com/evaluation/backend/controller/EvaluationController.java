package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.service.Evaluation.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enseignant/evaluations")
public class EvaluationController {

    private final EvaluationService service;


    @GetMapping
    public List<EvaluationResponseDTO> list(
            @RequestParam(required = false) Long noEnseignant,
            @RequestParam(required = false) String codeFormation,
            @RequestParam(required = false) String anneeUniversitaire
    ) {
        return service.list(noEnseignant, codeFormation, anneeUniversitaire);
    }


    @GetMapping("/formations")
    public List<String> getFormations() {
        return service.getFormations();
    }

    @GetMapping("/formations/{codeFormation}/ues")
    public List<String> getUes(@PathVariable String codeFormation) {
        return service.getCodeUe(codeFormation);
    }

    @GetMapping("/formations/{codeFormation}/ues/{codeUe}/ecs")
    public List<String> getEcs(@PathVariable String codeFormation,
                               @PathVariable String codeUe) {
        return service.getCodeEc(codeFormation, codeUe);
    }



    @GetMapping("/{id}")
    public EvaluationResponseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PostMapping
    public EvaluationResponseDTO create(@Valid @RequestBody EvaluationRequestDTO dto) {
        return service.create(dto);
    }


    @PutMapping("/{id}")
    public EvaluationResponseDTO update(@PathVariable Long id,
                                        @Valid @RequestBody EvaluationRequestDTO dto) {
        return service.update(id, dto);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
