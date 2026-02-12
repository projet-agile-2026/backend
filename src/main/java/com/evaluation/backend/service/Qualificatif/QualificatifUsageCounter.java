package com.evaluation.backend.service.Qualificatif;

import java.util.Map;
import java.util.Set;

public interface QualificatifUsageCounter {
    long countUsage(Long qualificatifId);


    Map<Long, Long> countUsage(Set<Long> qualificatifIds);
}
