package com.evaluation.backend.service.Qualificatif;

import com.evaluation.backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualificatifCounterService implements QualificatifUsageCounter {

    private final QuestionRepository questionRepository;

    @Override
    public long countUsage(Long qualificatifId) {
        if (qualificatifId == null) return 0L;
        return questionRepository.countByIdQualificatif(String.valueOf(qualificatifId));
    }

    @Override
    public Map<Long, Long> countUsage(Set<Long> qualificatifIds) {
        if (qualificatifIds == null || qualificatifIds.isEmpty()) return Map.of();

        List<String> idsAsString = qualificatifIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        List<Object[]> rows = questionRepository.countByIdQualificatifInGroup(idsAsString);

        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            String idStr = (String) row[0];
            Object countObj = row[1];

            Long id = Long.valueOf(idStr);


            long count = (countObj instanceof Long l) ? l
                    : (countObj instanceof BigDecimal bd) ? bd.longValue()
                    : Long.parseLong(countObj.toString());

            result.put(id, count);
        }
        return result;
    }
}
