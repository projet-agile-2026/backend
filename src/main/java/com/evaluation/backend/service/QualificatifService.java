package com.evaluation.backend.service;

import com.evaluation.backend.dto.CreateQualificatifRequest;
import com.evaluation.backend.dto.QualificatifDto;
import com.evaluation.backend.dto.UpdateQualificatifRequest;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.repository.QualificatifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QualificatifService {

    private final QualificatifRepository repository;
    private final QualificatifUsageCounter counter;

    public List<QualificatifDto> getAll() {
        return repository.findAll().stream()
                .map(q -> toDto(q, counter.countUsage(q.getIdQualificatif())))
                .toList();
    }

    public QualificatifDto create(CreateQualificatifRequest req) {
        String mot1 = req.getMot1().trim();
        String mot2 = req.getMot2().trim();

        // règle : pas de doublon
        if (repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCase(mot1, mot2)) {
            throw new RuntimeException("Ce couple existe déjà.");
        }

        Qualificatif q = new Qualificatif();
        q.setMaximal(mot1);
        q.setMinimal(mot2);

        Qualificatif saved = repository.save(q);
        return toDto(saved, 0L);
    }

    public QualificatifDto update(Long id, UpdateQualificatifRequest req) {
        Qualificatif q = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Couple introuvable."));

        long usage = counter.countUsage(id);

        // règle : modif seulement si count == 0
        if (usage > 0) {
            throw new RuntimeException("Modification impossible : le couple est utilisé (count > 0).");
        }

        String mot1 = req.getMot1().trim();
        String mot2 = req.getMot2().trim();

        // règle : pas de doublon (en excluant l'id courant)
        if (repository.existsByMaximalIgnoreCaseAndMinimalIgnoreCaseAndIdQualificatifNot(mot1, mot2, id)) {
            throw new RuntimeException("Un couple identique existe déjà.");
        }

        q.setMaximal(mot1);
        q.setMinimal(mot2);

        Qualificatif saved = repository.save(q);
        return toDto(saved, 0L);
    }

    public void delete(Long id) {
        Qualificatif q = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Couple introuvable."));

        // ne pas supprimer si utilisé
        long usage = counter.countUsage(id);
        if (usage > 0) {
            throw new RuntimeException("Suppression impossible : le couple est utilisé (count > 0).");
        }

        repository.delete(q);
    }

    private QualificatifDto toDto(Qualificatif q, long count) {
        return QualificatifDto.builder()
                .id(q.getIdQualificatif())
                .mot1(q.getMaximal())
                .mot2(q.getMinimal())
                .count(count)
                .build();
    }
}
