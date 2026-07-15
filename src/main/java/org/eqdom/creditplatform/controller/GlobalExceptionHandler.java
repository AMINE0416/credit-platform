package org.eqdom.creditplatform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        System.err.println("=== ERROR: HttpMessageNotReadableException ===");
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Most specific cause: " + ex.getMostSpecificCause().getMessage());
        ex.printStackTrace();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erreur de format de données");
        errorResponse.put("message", ex.getMostSpecificCause().getMessage());
        errorResponse.put("hint", "Vérifiez que tous les champs enum utilisent les bonnes valeurs");

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        System.err.println("=== ERROR: IllegalArgumentException ===");
        System.err.println("Message: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Argument invalide");
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        System.err.println("=== ERROR: Generic Exception ===");
        System.err.println("Type: " + ex.getClass().getName());
        System.err.println("Message: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erreur serveur");
        errorResponse.put("type", ex.getClass().getSimpleName());
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
