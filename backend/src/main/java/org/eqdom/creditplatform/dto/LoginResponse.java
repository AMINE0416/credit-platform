package org.eqdom.creditplatform.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private String nom;
    private String prenom;
    private List<String> roles;

    public LoginResponse(String token, String nom, String prenom, List<String> roles) {
        this.token = token;
        this.nom = nom;
        this.prenom = prenom;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public List<String> getRoles() { return roles; }
}