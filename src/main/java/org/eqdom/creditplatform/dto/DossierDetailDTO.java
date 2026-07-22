package org.eqdom.creditplatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eqdom.creditplatform.exception.BusinessException;
import org.eqdom.creditplatform.model.Client;
import org.eqdom.creditplatform.model.Credit;
import org.eqdom.creditplatform.model.Dossier;
import java.math.BigDecimal;

public class DossierDetailDTO {
    private Long dossierId;
    private String statut;
    private ClientDTO client;
    private CreditDTO credit;
    private Long nbActions;

    public DossierDetailDTO(Dossier dossier, long nbActions) {
        this.dossierId = dossier.getId();
        this.statut = dossier.getStatutActuel().name();
        this.nbActions = nbActions;
        
        try {
            Credit credit = dossier.getCredit();
            if (credit == null) {
                throw new BusinessException("Crédit non trouvé pour le dossier " + dossier.getId());
            }
            this.credit = new CreditDTO(credit);
            
            Client client = credit.getClient();
            if (client == null) {
                throw new BusinessException("Client non trouvé pour le crédit " + credit.getId());
            }
            this.client = new ClientDTO(client);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors du chargement du dossier: " + e.getMessage(), e);
        }
    }

    public Long getDossierId() { return dossierId; }
    public String getStatut() { return statut; }
    public ClientDTO getClient() { return client; }
    public CreditDTO getCredit() { return credit; }
    public Long getNbActions() { return nbActions; }

    public static class ClientDTO {
        private Long id;
        private String cin;
        private String nom;
        private String prenom;
        private String telephone;
        private String telephone2;
        private String adresse;
        private String ville;
        private String codeAgence;
        private String libAgence;
        private String adresseCadrage;

        public ClientDTO(Client client) {
            this.id = client.getId();
            this.cin = client.getCin();
            this.nom = client.getNom();
            this.prenom = client.getPrenom();
            this.telephone = client.getTelephone();
            this.telephone2 = client.getTelephone2();
            this.adresse = client.getAdresse();
            this.ville = client.getVille();
            this.codeAgence = client.getCodeAgence();
            this.libAgence = client.getLibAgence();
            this.adresseCadrage = client.getAdresseCadrage();
        }

        public Long getId() { return id; }
        public String getCin() { return cin; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getTelephone() { return telephone; }
        public String getTelephone2() { return telephone2; }
        public String getAdresse() { return adresse; }
        public String getVille() { return ville; }
        public String getCodeAgence() { return codeAgence; }
        public String getLibAgence() { return libAgence; }
        public String getAdresseCadrage() { return adresseCadrage; }
    }

    public static class CreditDTO {
        private Long id;
        private String referenceContrat;
        private String fromProv;
        private String gestCin;
        private String typeCredit;
        private String segment;
        private Integer nbrImpaye;
        private BigDecimal mensualite;
        private BigDecimal encours;
        private BigDecimal impayeMontant;
        private BigDecimal prvCum;
        private String stage;
        private String dvr;
        private String reecheance;
        private String immatriculation;

        public CreditDTO(Credit credit) {
            this.id = credit.getId();
            this.referenceContrat = credit.getReferenceContrat();
            this.fromProv = credit.getFromProv();
            this.gestCin = credit.getGestCin();
            this.typeCredit = credit.getTypeCredit();
            this.segment = credit.getSegment();
            this.nbrImpaye = credit.getNbrImpaye();
            this.mensualite = credit.getMensualite();
            this.encours = credit.getEncours();
            this.impayeMontant = credit.getImpayeMontant();
            this.prvCum = credit.getPrvCum();
            this.stage = credit.getStage();
            this.dvr = credit.getDvr();
            this.reecheance = credit.getReecheance();
            this.immatriculation = credit.getImmatriculation();
        }

        public Long getId() { return id; }
        public String getReferenceContrat() { return referenceContrat; }
        public String getFromProv() { return fromProv; }
        public String getGestCin() { return gestCin; }
        public String getTypeCredit() { return typeCredit; }
        public String getSegment() { return segment; }
        public Integer getNbrImpaye() { return nbrImpaye; }
        public BigDecimal getMensualite() { return mensualite; }
        public BigDecimal getEncours() { return encours; }
        public BigDecimal getImpayeMontant() { return impayeMontant; }
        public BigDecimal getPrvCum() { return prvCum; }
        public String getStage() { return stage; }
        public String getDvr() { return dvr; }
        public String getReecheance() { return reecheance; }
        public String getImmatriculation() { return immatriculation; }
    }
}