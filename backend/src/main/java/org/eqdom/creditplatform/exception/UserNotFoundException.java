package org.eqdom.creditplatform.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Utilisateur introuvable avec l'ID : " + id);
    }
    
    public UserNotFoundException(String email) {
        super("Utilisateur introuvable avec l'email : " + email);
    }
}
