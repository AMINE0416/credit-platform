package org.eqdom.creditplatform.dto;

/**
 * DTO pour la mise à jour du profil utilisateur (sans modification de rôle).
 * Utilisable par tous les utilisateurs authentifiés.
 */
public class UpdateUserProfileRequest {
    private String nom;
    private String prenom;
    private String email;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
