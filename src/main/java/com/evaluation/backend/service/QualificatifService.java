package com.evaluation.backend.service;

import com.evaluation.backend.dto.QualificatifDTO;
import com.evaluation.backend.entity.Qualificatif;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.QualificatifMapper;
import com.evaluation.backend.repository.QualificatifRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QualificatifService {

    private final QualificatifRepository qualificatifRepository;
    private final QualificatifMapper qualificatifMapper;

    @Transactional(readOnly = true)
    public List<QualificatifDTO> getAllQualificatifs() {
        log.debug("Fetching all qualificatifs");
        List<Qualificatif> qualificatifs = qualificatifRepository.findAll();
        return qualificatifMapper.toDTOList(qualificatifs);
    }

    @Transactional(readOnly = true)
    public QualificatifDTO getQualificatifById(Long id) {
        log.debug("Fetching qualificatif with id: {}", id);
        Qualificatif qualificatif = qualificatifRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Qualificatif", "idQualificatif", id));
        return qualificatifMapper.toDTO(qualificatif);
    }

    public QualificatifDTO createQualificatif(QualificatifDTO qualificatifDTO) {
        log.debug("Creating new qualificatif: {}", qualificatifDTO);
        Qualificatif qualificatif = qualificatifMapper.toEntity(qualificatifDTO);
        Qualificatif savedQualificatif = qualificatifRepository.save(qualificatif);
        log.info("Created qualificatif with id: {}", savedQualificatif.getIdQualificatif());
        return qualificatifMapper.toDTO(savedQualificatif);
    }

    public QualificatifDTO updateQualificatif(Long id, QualificatifDTO qualificatifDTO) {
        log.debug("Updating qualificatif with id: {}", id);
        Qualificatif existingQualificatif = qualificatifRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Qualificatif", "idQualificatif", id));

        existingQualificatif.setMaximal(qualificatifDTO.getMaximal());
        existingQualificatif.setMinimal(qualificatifDTO.getMinimal());

        Qualificatif updatedQualificatif = qualificatifRepository.save(existingQualificatif);
        log.info("Updated qualificatif with id: {}", id);
        return qualificatifMapper.toDTO(updatedQualificatif);
    }

    public void deleteQualificatif(Long id) {
        log.debug("Deleting qualificatif with id: {}", id);
        if (!qualificatifRepository.existsById(id)) {
            throw new ResourceNotFoundException("Qualificatif", "idQualificatif", id);
        }
        qualificatifRepository.deleteById(id);
        log.info("Deleted qualificatif with id: {}", id);
    }
}