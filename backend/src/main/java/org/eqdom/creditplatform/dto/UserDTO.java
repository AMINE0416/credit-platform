package org.eqdom.creditplatform.dto;

import java.util.List;

public class UserDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
    private List<String> roles;
    private long nbDossiersAssignes;

    public UserDTO(Long id, String nom, String prenom, String email, boolean actif,
                   List<String> roles, long nbDossiersAssignes) {
        this.id = id; this.nom = nom; this.prenom = prenom; this.email = email;
        this.actif = actif; this.roles = roles; this.nbDossiersAssignes = nbDossiersAssignes;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public boolean isActif() { return actif; }
    public List<String> getRoles() { return roles; }
    public long getNbDossiersAssignes() { return nbDossiersAssignes; }
}