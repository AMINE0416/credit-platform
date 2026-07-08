package org.eqdom.creditplatform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cin;

    private String nom;
    private String prenom;
    private String telephone;
    private String telephone2;

    @Column(length = 500)
    private String adresse;

    private String ville;
    private String codeAgence;
    private String libAgence;

    @Column(length = 1000)
    private String adresseCadrage;

    // Getters / Setters
    public Long getId() { return id; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getTelephone2() { return telephone2; }
    public void setTelephone2(String telephone2) { this.telephone2 = telephone2; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getCodeAgence() { return codeAgence; }
    public void setCodeAgence(String codeAgence) { this.codeAgence = codeAgence; }
    public String getLibAgence() { return libAgence; }
    public void setLibAgence(String libAgence) { this.libAgence = libAgence; }
    public String getAdresseCadrage() { return adresseCadrage; }
    public void setAdresseCadrage(String adresseCadrage) { this.adresseCadrage = adresseCadrage; }
}