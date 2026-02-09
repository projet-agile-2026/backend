package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Qualificatif.CreateQualificatifRequest;
import com.evaluation.backend.dto.Qualificatif.QualificatifDto;
import com.evaluation.backend.dto.Qualificatif.UpdateQualificatifRequest;
import com.evaluation.backend.service.Qualificatif.QualificatifService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qualificatifs")
@CrossOrigin(origins = "*")
public class QualificatifController {

    private final QualificatifService service;

    @GetMapping
    public List<QualificatifDto> list() {
        return service.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QualificatifDto create(@Valid @RequestBody CreateQualificatifRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public QualificatifDto update(@PathVariable Long id, @Valid @RequestBody UpdateQualificatifRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
