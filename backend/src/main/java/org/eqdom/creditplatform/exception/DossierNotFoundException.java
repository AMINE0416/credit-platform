package org.eqdom.creditplatform.exception;

public class DossierNotFoundException extends RuntimeException {
    public DossierNotFoundException(Long id) {
        super("Dossier introuvable avec l'ID : " + id);
    }
    
    public DossierNotFoundException(String message) {
        super(message);
    }
}
