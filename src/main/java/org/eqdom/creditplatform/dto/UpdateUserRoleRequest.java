package org.eqdom.creditplatform.dto;

/**
 * DTO pour la mise à jour du rôle d'un utilisateur.
 * Réservé aux administrateurs uniquement.
 */
public class UpdateUserRoleRequest {
    private String role;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
