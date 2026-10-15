package org.eqdom.creditplatform.dto;

import java.util.Set;

/**
 * Informations sur les transitions autorisées et les champs requis
 */
public class TransitionInfo {
    private Set<String> transitionsAutorisees;
    private ChampsRequis champsRequis;
    
    public TransitionInfo(Set<String> transitionsAutorisees, ChampsRequis champsRequis) {
        this.transitionsAutorisees = transitionsAutorisees;
        this.champsRequis = champsRequis;
    }
    
    public Set<String> getTransitionsAutorisees() {
        return transitionsAutorisees;
    }
    
    public ChampsRequis getChampsRequis() {
        return champsRequis;
    }
    
    public static class ChampsRequis {
        private boolean natureActionObligatoire = true;  // Toujours obligatoire
        private boolean motifImpayeVisible;
        private boolean motifImpayeObligatoire;
        private boolean compteRenduObligatoire;
        private boolean codeResultatObligatoire = true;  // Toujours obligatoire
        private boolean recidiveVisible;
        private boolean recidiveObligatoire;
        private boolean montantPromesseVisible;
        private boolean montantPromesseObligatoire;
        private boolean datePromesseVisible;
        private boolean datePromesseObligatoire;
        private boolean nouveauTelephoneVisible = true;  // Toujours optionnel
        
        public boolean isNatureActionObligatoire() { return natureActionObligatoire; }
        public boolean isMotifImpayeVisible() { return motifImpayeVisible; }
        public void setMotifImpayeVisible(boolean motifImpayeVisible) { this.motifImpayeVisible = motifImpayeVisible; }
        public boolean isMotifImpayeObligatoire() { return motifImpayeObligatoire; }
        public void setMotifImpayeObligatoire(boolean motifImpayeObligatoire) { this.motifImpayeObligatoire = motifImpayeObligatoire; }
        public boolean isCompteRenduObligatoire() { return compteRenduObligatoire; }
        public void setCompteRenduObligatoire(boolean compteRenduObligatoire) { this.compteRenduObligatoire = compteRenduObligatoire; }
        public boolean isCodeResultatObligatoire() { return codeResultatObligatoire; }
        public boolean isRecidiveVisible() { return recidiveVisible; }
        public void setRecidiveVisible(boolean recidiveVisible) { this.recidiveVisible = recidiveVisible; }
        public boolean isRecidiveObligatoire() { return recidiveObligatoire; }
        public void setRecidiveObligatoire(boolean recidiveObligatoire) { this.recidiveObligatoire = recidiveObligatoire; }
        public boolean isMontantPromesseVisible() { return montantPromesseVisible; }
        public void setMontantPromesseVisible(boolean montantPromesseVisible) { this.montantPromesseVisible = montantPromesseVisible; }
        public boolean isMontantPromesseObligatoire() { return montantPromesseObligatoire; }
        public void setMontantPromesseObligatoire(boolean montantPromesseObligatoire) { this.montantPromesseObligatoire = montantPromesseObligatoire; }
        public boolean isDatePromesseVisible() { return datePromesseVisible; }
        public void setDatePromesseVisible(boolean datePromesseVisible) { this.datePromesseVisible = datePromesseVisible; }
        public boolean isDatePromesseObligatoire() { return datePromesseObligatoire; }
        public void setDatePromesseObligatoire(boolean datePromesseObligatoire) { this.datePromesseObligatoire = datePromesseObligatoire; }
        public boolean isNouveauTelephoneVisible() { return nouveauTelephoneVisible; }
    }
}
