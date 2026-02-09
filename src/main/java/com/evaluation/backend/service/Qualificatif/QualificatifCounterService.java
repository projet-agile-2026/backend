package com.evaluation.backend.service.Qualificatif;

import org.springframework.stereotype.Service;

@Service
public class QualificatifCounterService implements QualificatifUsageCounter {

    @Override
    public long countUsage(Long qualificatifId) {
        // traitement dyal counter hna
        return 0L;
    }
}
