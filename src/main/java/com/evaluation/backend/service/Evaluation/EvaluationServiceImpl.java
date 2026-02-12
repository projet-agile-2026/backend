package com.evaluation.backend.service.Evaluation;

import com.evaluation.backend.dto.Evaluation.EvaluationRequestDTO;
import com.evaluation.backend.dto.Evaluation.EvaluationResponseDTO;
import com.evaluation.backend.entity.Evaluation;
import com.evaluation.backend.exception.BusinessException;
import com.evaluation.backend.exception.ResourceNotFoundException;
import com.evaluation.backend.mapper.EvaluationMapper;
import com.evaluation.backend.repository.ElementConstitutifRepository;
import com.evaluation.backend.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.evaluation.backend.repository.FormationRepository;
import com.evaluation.backend.repository.UniteEnseignementRepository;



import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository repository;
    private final EvaluationMapper mapper;
    private final ElementConstitutifRepository elementConstitutifRepository;
    private final FormationRepository formationRepository;
    private final UniteEnseignementRepository uniteEnseignementRepository;



    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponseDTO> list(Long noEnseignant, String codeFormation, String anneeUniversitaire) {

        List<Evaluation> res;

        if (noEnseignant != null) {
            res = repository.findByNoEnseignant(noEnseignant);
        } else if (codeFormation != null && anneeUniversitaire != null) {
            res = repository.findByCodeFormationAndAnneeUniversitaire(codeFormation, anneeUniversitaire);
        } else {
            res = repository.findAll();
        }

        return res.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationResponseDTO getById(Long id) {
        Evaluation e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + id));
        return mapper.toResponse(e);
    }

    @Override
    public EvaluationResponseDTO create(EvaluationRequestDTO dto) {
        validateEtat(dto.getEtat());

        Evaluation e = mapper.toEntity(dto);

        Evaluation saved = repository.save(e);

        return mapper.toResponse(saved);
    }

    @Override
    public EvaluationResponseDTO update(Long id, EvaluationRequestDTO dto) {
        validateEtat(dto.getEtat());

        Evaluation e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation introuvable : id=" + id));

        mapper.updateEntity(e, dto);

        Evaluation saved = repository.save(e);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Evaluation introuvable : id=" + id);
        }
        repository.deleteById(id);
    }

    private void validateEtat(String etat) {
        if (etat == null) {
            throw new BusinessException("Etat obligatoire (ELA, DIS, CLO)");
        }
        String v = etat.trim().toUpperCase();
        if (!v.equals("ELA") && !v.equals("DIS") && !v.equals("CLO")) {
            throw new BusinessException("Etat invalide. Valeurs possibles: ELA, DIS, CLO");
        }
    }


    @Override
    public List<String> getFormations() {
        return formationRepository.findAllCodeFormations();
    }

    @Override
    public List<String> getCodeUe(String codeFormation) {
        return uniteEnseignementRepository.findCodeUeByCodeFormation(codeFormation);
    }

    @Override
    public List<String> getCodeEc(String codeFormation, String codeUe) {
        return elementConstitutifRepository.findDistinctEcsByFormationAndUe(codeFormation, codeUe);
    }

}
