package com.evaluation.backend.service.Promotions;

import org.springframework.stereotype.Service;

@Service
public class PromotionRulesService {

    /**
     * Vérifie si une promotion a déjà fait l'objet d'une évaluation
     * nhtajoha f sprint 2
     */
    public boolean promotionDejaEvaluee(String codeFormation, String anneeUniversitaire) {
        return false; // temporaire
    }

    /**
     * Vérifie si un étudiant a déjà répondu à une évaluation
     * nhtajoha f sprint 2
     */
    public boolean etudiantADejaRepondu(Long noEtudiant) {
        return false; // temporaire
    }
}
