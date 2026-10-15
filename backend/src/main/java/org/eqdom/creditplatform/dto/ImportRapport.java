package org.eqdom.creditplatform.dto;

import java.util.List;
import java.util.Map;

public class ImportRapport {
    private long jobId;
    private String statut;
    private int nbLignesTotal;
    private int nbInserees;
    private int nbMisesAJour;
    private int nbRejetees;
    private List<Map<String, String>> erreurs;

    public ImportRapport() {
        this.jobId = System.currentTimeMillis();
        this.erreurs = new java.util.ArrayList<>();
    }

    public long getJobId() { return jobId; }
    public void setJobId(long jobId) { this.jobId = jobId; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getNbLignesTotal() { return nbLignesTotal; }
    public void setNbLignesTotal(int nbLignesTotal) { this.nbLignesTotal = nbLignesTotal; }
    public int getNbInserees() { return nbInserees; }
    public void setNbInserees(int nbInserees) { this.nbInserees = nbInserees; }
    public int getNbMisesAJour() { return nbMisesAJour; }
    public void setNbMisesAJour(int nbMisesAJour) { this.nbMisesAJour = nbMisesAJour; }
    public int getNbRejetees() { return nbRejetees; }
    public void setNbRejetees(int nbRejetees) { this.nbRejetees = nbRejetees; }
    public List<Map<String, String>> getErreurs() { return erreurs; }
    public void setErreurs(List<Map<String, String>> erreurs) { this.erreurs = erreurs; }
}
