package com.evaluation.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // Exceptions métier applicatives
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderException(
            InvalidOrderException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    // Validation @Valid (niveau DTO)
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation invalides")
                .message("Données invalides. Veuillez corriger les erreurs de validation.")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // =========================================================
    // RuntimeException métier
    // QualificatifService, QuestionService, EvaluationService
    // utilisent des RuntimeException avec messages en français
    // =========================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        String msg = ex.getMessage();
        if (msg == null) msg = "Erreur inattendue.";

        // --- QualificatifService ---
        if (msg.contains("Ce couple existe déjà")) {
            return buildError(HttpStatus.CONFLICT,
                    "Ce qualificatif (mot1/mot2) existe déjà. Veuillez saisir un couple différent.",
                    request);
        }
        if (msg.contains("Modification impossible") && msg.contains("count")) {
            return buildError(HttpStatus.CONFLICT,
                    "Modification impossible : ce qualificatif est déjà utilisé dans une ou plusieurs questions.",
                    request);
        }
        if (msg.contains("Un couple identique existe déjà")) {
            return buildError(HttpStatus.CONFLICT,
                    "Un qualificatif identique existe déjà. Veuillez choisir un autre couple de mots.",
                    request);
        }
        if (msg.contains("Suppression impossible") && msg.contains("count")) {
            return buildError(HttpStatus.CONFLICT,
                    "Suppression impossible : ce qualificatif est utilisé dans une ou plusieurs questions. Retirez-le d'abord.",
                    request);
        }
        if (msg.contains("Couple introuvable")) {
            return buildError(HttpStatus.NOT_FOUND,
                    "Qualificatif introuvable. Vérifiez l'identifiant saisi.",
                    request);
        }

        // --- QuestionService ---
        if (msg.contains("Action interdite") || msg.contains("propriétaire différent")) {
            return buildError(HttpStatus.FORBIDDEN,
                    "Action interdite : vous ne pouvez pas modifier ou supprimer une question qui ne vous appartient pas.",
                    request);
        }
        if (msg.contains("enseignant ne peut pas modifier une question standard")) {
            return buildError(HttpStatus.FORBIDDEN,
                    "Action interdite : un enseignant ne peut pas modifier une question standard (QUS).",
                    request);
        }
        if (msg.contains("admin ne peut pas modifier une question personnelle")) {
            return buildError(HttpStatus.FORBIDDEN,
                    "Action interdite : un administrateur ne peut pas modifier une question personnelle (QUP).",
                    request);
        }
        if (msg.contains("enseignant ne peut pas supprimer une question standart")) {
            return buildError(HttpStatus.FORBIDDEN,
                    "Action interdite : un enseignant ne peut pas supprimer une question standard (QUS).",
                    request);
        }
        if (msg.contains("admin ne peut pas supprimer une question personnelle")) {
            return buildError(HttpStatus.FORBIDDEN,
                    "Action interdite : un administrateur ne peut pas supprimer une question personnelle (QUP).",
                    request);
        }
        if (msg.contains("Question non trouvée") || msg.contains("Question introuvable")) {
            return buildError(HttpStatus.NOT_FOUND,
                    "Question introuvable. Vérifiez l'identifiant saisi.",
                    request);
        }
        if (msg.contains("Qualificatif non trouvé") || msg.contains("Qualificatif introuvable")) {
            return buildError(HttpStatus.NOT_FOUND,
                    "Qualificatif associé introuvable. La question référence un qualificatif inexistant.",
                    request);
        }

        // --- Auth / Security (EvaluationController, RubriqueController) ---
        if (msg.contains("Utilisateur non trouvé")) {
            return buildError(HttpStatus.UNAUTHORIZED,
                    "Utilisateur non authentifié ou introuvable. Veuillez vous reconnecter.",
                    request);
        }
        if (msg.contains("n'est pas lié à un enseignant")) {
            return buildError(HttpStatus.FORBIDDEN,
                    "Votre compte n'est pas associé à un profil enseignant. Accès refusé.",
                    request);
        }

        // Fallback
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue s'est produite : " + msg,
                request);
    }

    // =========================================================
    // Erreurs base de données Oracle (Promotion / Etudiant) : ADM KOLO
    // =========================================================

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            JpaSystemException.class,
            TransactionSystemException.class,
            ConstraintViolationException.class,
            DataException.class
    })
    public ResponseEntity<ErrorResponse> handleDatabaseExceptions(Exception ex, HttpServletRequest request) {

        String msg = getRootCauseMessage(ex);
        String oracleCode = extractOraCode(msg);
        String constraint = extractConstraintName(msg);

        // ORA-12899 : valeur trop grande pour colonne
        if ("ORA-12899".equals(oracleCode)) {
            return buildError(HttpStatus.BAD_REQUEST,
                    "Texte trop long. Veuillez raccourcir la valeur saisie.",
                    request);
        }

        // ORA-01400 : null sur champ NOT NULL
        if ("ORA-01400".equals(oracleCode)) {
            return buildError(HttpStatus.BAD_REQUEST,
                    "Veuillez remplir tous les champs obligatoires.",
                    request);
        }

        // ORA-02290 : CHECK constraint
        if ("ORA-02290".equals(oracleCode)) {
            if ("CK_RUB_TYPE".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Type de rubrique invalide. Les valeurs acceptées sont : 'RBP' (rubrique personnelle) ou 'RBS' (rubrique standard).",
                        request);
            }
            if ("CK_QUE_TYPE".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Type de question invalide. Les valeurs acceptées sont : 'QUP' (question personnelle) ou 'QUS' (question standard).",
                        request);
            }
            if ("CK_RPQ_POSITIONNEMENT".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Positionnement invalide. La valeur doit être comprise entre 1 et 5.",
                        request);
            }
            return buildError(HttpStatus.BAD_REQUEST,
                    "Valeur invalide. Veuillez vérifier les champs.",
                    request);
        }

        // ORA-00001 : unique constraint / PK
        if ("ORA-00001".equals(oracleCode)) {
            if ("PRO_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette promotion existe déjà pour cette formation et cette année universitaire.",
                        request);
            }
            if ("EVE_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette évaluation existe déjà (identifiant en double).",
                        request);
            }
            if ("EVE_EVE_UK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Une évaluation avec le même numéro existe déjà pour cet enseignant, cette formation, cette unité d'enseignement et cette année universitaire.",
                        request);
            }
            if ("QUA_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Ce qualificatif existe déjà (identifiant en double).",
                        request);
            }
            if ("RUB_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette rubrique existe déjà (identifiant en double).",
                        request);
            }
            if ("RBQ_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette question est déjà associée à cette rubrique.",
                        request);
            }
            if ("REV_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette rubrique d'évaluation existe déjà (identifiant en double).",
                        request);
            }
            if ("QUE_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette question existe déjà (identifiant en double).",
                        request);
            }
            if ("QEV_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette question d'évaluation existe déjà (identifiant en double).",
                        request);
            }
            if ("RPE_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cette session de réponse existe déjà (identifiant en double).",
                        request);
            }
            if ("RPQ_PK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Une réponse existe déjà pour cette question dans cette session de réponse.",
                        request);
            }
            if ("DRT_PK".equals(constraint) || "DRT_DRT_UK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Cet enseignant possède déjà un droit d'accès défini pour cette évaluation.",
                        request);
            }
            return buildError(HttpStatus.CONFLICT,
                    "Un enregistrement identique existe déjà. Veuillez vérifier vos données.",
                    request);
        }

        // ORA-02291 : FK parent inexistante
        if ("ORA-02291".equals(oracleCode)) {
            if ("PRO_FRM_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Formation invalide. Merci de sélectionner une formation existante.",
                        request);
            }
            if ("PRO_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Enseignant invalide. Merci de sélectionner un enseignant existant.",
                        request);
            }
            if ("EVE_UE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Unité d'enseignement introuvable. Veuillez sélectionner une UE valide pour la formation indiquée.",
                        request);
            }
            if ("EVE_PRO_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Promotion introuvable. Aucune promotion ne correspond à cette formation et cette année universitaire.",
                        request);
            }
            if ("EVE_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Enseignant introuvable. Veuillez sélectionner un enseignant existant pour cette évaluation.",
                        request);
            }
            if ("EVE_EC_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Élément constitutif (EC) introuvable. Veuillez sélectionner un EC valide pour cette UE et cette formation.",
                        request);
            }
            if ("QEV_QUA_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Qualificatif introuvable. Veuillez sélectionner un qualificatif existant pour cette question d'évaluation.",
                        request);
            }
            if ("QEV_QUE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Question introuvable. Veuillez sélectionner une question existante pour cette rubrique d'évaluation.",
                        request);
            }
            if ("QEV_REV_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Rubrique d'évaluation introuvable. Impossible d'associer une question à une rubrique inexistante.",
                        request);
            }
            if ("RBQ_QUE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Question introuvable. Impossible d'associer une question inexistante à cette rubrique.",
                        request);
            }
            if ("RBQ_RUB_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Rubrique introuvable. Impossible d'associer une question à une rubrique inexistante.",
                        request);
            }
            if ("RUB_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Enseignant introuvable. Impossible d'associer cette rubrique à un enseignant inexistant.",
                        request);
            }
            if ("REV_RUB_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Rubrique introuvable. Veuillez sélectionner une rubrique existante pour cette évaluation.",
                        request);
            }
            if ("REV_EVE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Évaluation introuvable. Impossible d'associer une rubrique à une évaluation inexistante.",
                        request);
            }
            if ("DRT_EVE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Évaluation introuvable. Impossible d'attribuer un droit d'accès à une évaluation inexistante.",
                        request);
            }
            if ("DRT_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Enseignant introuvable. Impossible d'attribuer un droit d'accès à un enseignant inexistant.",
                        request);
            }
            if ("QUE_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Enseignant introuvable. Impossible d'associer cette question à un enseignant inexistant.",
                        request);
            }
            if ("QUE_QUA_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Qualificatif introuvable. Veuillez sélectionner un qualificatif existant pour cette question.",
                        request);
            }
            if ("RPQ_RPE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Session de réponse introuvable. Impossible d'enregistrer une réponse sans session valide.",
                        request);
            }
            if ("RPQ_QEV_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Question d'évaluation introuvable. Impossible d'enregistrer une réponse pour une question inexistante.",
                        request);
            }
            if ("RPE_ETU_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Étudiant introuvable. Impossible d'enregistrer une réponse pour un étudiant inexistant.",
                        request);
            }
            if ("RPE_EVE_FK".equals(constraint)) {
                return buildError(HttpStatus.BAD_REQUEST,
                        "Évaluation introuvable. Impossible d'enregistrer une réponse pour une évaluation inexistante.",
                        request);
            }
            return buildError(HttpStatus.BAD_REQUEST,
                    "Référence invalide. Un ou plusieurs identifiants liés sont introuvables en base de données.",
                    request);
        }

        // ORA-02292 : suppression impossible (enfants existent)
        if ("ORA-02292".equals(oracleCode)) {
            if ("ETU_PRO_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette promotion : elle contient des étudiants inscrits.",
                        request);
            }
            if ("REV_EVE_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette évaluation : des rubriques lui sont associées. Retirez-les d'abord.",
                        request);
            }
            if ("DRT_EVE_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette évaluation : des droits d'accès y sont définis. Retirez-les d'abord.",
                        request);
            }
            if ("RPE_EVE_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette évaluation : des réponses d'étudiants y sont enregistrées.",
                        request);
            }
            if ("RBQ_RUB_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette rubrique : des questions y sont associées. Retirez-les d'abord.",
                        request);
            }
            if ("REV_RUB_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette rubrique : elle est utilisée dans une ou plusieurs évaluations.",
                        request);
            }
            if ("RBQ_QUE_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette question : elle est associée à une ou plusieurs rubriques.",
                        request);
            }
            if ("QEV_QUE_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette question : elle est utilisée dans une ou plusieurs évaluations.",
                        request);
            }
            if ("QEV_QUA_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer ce qualificatif : il est utilisé dans une ou plusieurs questions d'évaluation.",
                        request);
            }
            if ("QUE_QUA_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer ce qualificatif : il est référencé par une ou plusieurs questions.",
                        request);
            }
            if ("QEV_REV_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette rubrique d'évaluation : des questions d'évaluation y sont rattachées.",
                        request);
            }
            if ("RPQ_RPE_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette session de réponse : des réponses aux questions y sont enregistrées.",
                        request);
            }
            if ("RPQ_QEV_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cette question d'évaluation : des réponses lui sont associées.",
                        request);
            }
            if ("EVE_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cet enseignant : il est responsable d'une ou plusieurs évaluations.",
                        request);
            }
            if ("RUB_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cet enseignant : il est propriétaire d'une ou plusieurs rubriques.",
                        request);
            }
            if ("DRT_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cet enseignant : des droits d'accès lui sont attribués.",
                        request);
            }
            if ("QUE_ENS_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cet enseignant : il est auteur d'une ou plusieurs questions.",
                        request);
            }
            if ("RPE_ETU_FK".equals(constraint)) {
                return buildError(HttpStatus.CONFLICT,
                        "Impossible de supprimer cet étudiant : des réponses à des évaluations lui sont associées.",
                        request);
            }
            return buildError(HttpStatus.CONFLICT,
                    "Suppression impossible : cet enregistrement est encore référencé par d'autres données. Supprimez d'abord les données liées.",
                    request);
        }

        return buildError(HttpStatus.BAD_REQUEST,
                "Erreur de saisie. Veuillez vérifier vos données.",
                request);
    }

    // =========================================================
    // Filet de sécurité final
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // =========================================================
    // Helpers privés
    // =========================================================

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }

    private String getRootCauseMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() != null ? root.getMessage() : ex.getMessage();
    }

    private String extractOraCode(String message) {
        if (message == null) return null;
        int idx = message.indexOf("ORA-");
        if (idx < 0) return null;
        int end = Math.min(idx + 9, message.length()); // "ORA-12899"
        return message.substring(idx, end);
    }

    private String extractConstraintName(String message) {
        if (message == null) return null;

        // Format avec parenthèses : constraint (DOSI.QUE_QUA_FK)
        int start = message.indexOf("constraint (");
        if (start >= 0) {
            start += "constraint (".length();
            int end = message.indexOf(")", start);
            if (end >= 0) {
                String full = message.substring(start, end);
                int dot = full.indexOf('.');
                return dot >= 0 ? full.substring(dot + 1) : full;
            }
        }

        // Format avec crochets : constraint [DOSI.QUE_QUA_FK]
        start = message.indexOf("constraint [");
        if (start >= 0) {
            start += "constraint [".length();
            int end = message.indexOf("]", start);
            if (end >= 0) {
                String full = message.substring(start, end);
                int dot = full.indexOf('.');
                return dot >= 0 ? full.substring(dot + 1) : full;
            }
        }

        // Fallback Oracle : parenthèses simples
        int open = message.indexOf('(');
        int close = message.indexOf(')', open + 1);
        if (open < 0 || close < 0) return null;
        String inside = message.substring(open + 1, close).trim()
                .replace("\"", "").replace(" ", "");
        int dot = inside.lastIndexOf('.');
        return dot >= 0 ? inside.substring(dot + 1) : inside;
    }
}