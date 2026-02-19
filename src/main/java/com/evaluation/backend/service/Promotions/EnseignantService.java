package com.evaluation.backend.service.Promotions;



import com.evaluation.backend.dto.Promotions.EnseignantLightDTO;
import com.evaluation.backend.repository.EnseignantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnseignantService {

    private final EnseignantRepository enseignantRepository;

    public EnseignantService(EnseignantRepository enseignantRepository) {
        this.enseignantRepository = enseignantRepository;
    }

    public List<EnseignantLightDTO> getAllLight() {
        return enseignantRepository.findAllLight();
    }
}

