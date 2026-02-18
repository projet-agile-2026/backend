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
import org.springframework.http.HttpStatus;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
                .error("Validation Failed")
                .message("Invalid input data")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }




    // ERREURS BD (Promotion / Etudiant) : ADM KOLO

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
            return buildError(HttpStatus.BAD_REQUEST,
                    "Valeur invalide. Veuillez vérifier les champs.",
                    request);
        }

        // ORA-00001 : unique constraint / PK
        if ("ORA-00001".equals(oracleCode) && "PRO_PK".equals(constraint)) {
            return buildError(HttpStatus.CONFLICT,
                    "Cette promotion existe déjà pour cette formation et cette année.",
                    request);
        }

        // ORA-02291 : FK parent inexistante
        if ("ORA-02291".equals(oracleCode) && "PRO_FRM_FK".equals(constraint)) {
            return buildError(HttpStatus.BAD_REQUEST,
                    "Formation invalide. Merci de sélectionner une formation existante.",
                    request);
        }

        if ("ORA-02291".equals(oracleCode) && "PRO_ENS_FK".equals(constraint)) {
            return buildError(HttpStatus.BAD_REQUEST,
                    "Enseignant invalide. Merci de sélectionner un enseignant existant.",
                    request);
        }

        // ORA-02292 : suppression impossible (enfants existent)
        if ("ORA-02292".equals(oracleCode) && "ETU_PRO_FK".equals(constraint)) {
            return buildError(HttpStatus.CONFLICT,
                    "Impossible de supprimer cette promotion : elle contient des étudiants.",
                    request);
        }

        return buildError(HttpStatus.BAD_REQUEST,
                "Erreur de saisie. Veuillez vérifier vos données.",
                request);
    }


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



    // helpers dyal les err d partie promotion


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

        // Oracle met souvent le nom de contrainte entre parenthèses :
        int open = message.indexOf('(');
        int close = message.indexOf(')', open + 1);

        if (open < 0 || close < 0) return null;

        String inside = message.substring(open + 1, close).trim();

        // Nettoyage : enlever " et espaces
        inside = inside.replace("\"", "").replace(" ", "");

        // Si format SCHEMA.CONSTRAINT → on garde seulement CONSTRAINT
        int dot = inside.lastIndexOf('.');
        return dot >= 0 ? inside.substring(dot + 1) : inside;
    }



}