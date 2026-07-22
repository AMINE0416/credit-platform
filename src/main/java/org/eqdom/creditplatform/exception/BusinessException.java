package org.eqdom.creditplatform.exception;

/**
 * Exception générique pour les erreurs métier
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
