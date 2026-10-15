package org.eqdom.creditplatform.dto;

import java.util.List;

public class AffectationRequest {
    private List<Long> dossierIds;
    private Long agentId;

    public List<Long> getDossierIds() { return dossierIds; }
    public void setDossierIds(List<Long> dossierIds) { this.dossierIds = dossierIds; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
}