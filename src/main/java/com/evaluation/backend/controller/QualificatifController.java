package com.evaluation.backend.controller;

import com.evaluation.backend.dto.QualificatifDTO;
import com.evaluation.backend.service.QualificatifService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qualificatifs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QualificatifController {

    private final QualificatifService qualificatifService;

    @GetMapping
    public ResponseEntity<List<QualificatifDTO>> getAllQualificatifs() {
        log.info("GET /api/qualificatifs - Fetching all qualificatifs");
        List<QualificatifDTO> qualificatifs = qualificatifService.getAllQualificatifs();
        return ResponseEntity.ok(qualificatifs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QualificatifDTO> getQualificatifById(@PathVariable Long id) {
        log.info("GET /api/qualificatifs/{} - Fetching qualificatif by id", id);
        QualificatifDTO qualificatif = qualificatifService.getQualificatifById(id);
        return ResponseEntity.ok(qualificatif);
    }

    @PostMapping
    public ResponseEntity<QualificatifDTO> createQualificatif(@Valid @RequestBody QualificatifDTO qualificatifDTO) {
        log.info("POST /api/qualificatifs - Creating new qualificatif");
        QualificatifDTO createdQualificatif = qualificatifService.createQualificatif(qualificatifDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQualificatif);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QualificatifDTO> updateQualificatif(
            @PathVariable Long id,
            @Valid @RequestBody QualificatifDTO qualificatifDTO) {
        log.info("PUT /api/qualificatifs/{} - Updating qualificatif", id);
        QualificatifDTO updatedQualificatif = qualificatifService.updateQualificatif(id, qualificatifDTO);
        return ResponseEntity.ok(updatedQualificatif);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQualificatif(@PathVariable Long id) {
        log.info("DELETE /api/qualificatifs/{} - Deleting qualificatif", id);
        qualificatifService.deleteQualificatif(id);
        return ResponseEntity.noContent().build();
    }
}