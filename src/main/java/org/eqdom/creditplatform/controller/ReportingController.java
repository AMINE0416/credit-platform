package org.eqdom.creditplatform.controller;

import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    @Autowired private DossierRepository dossierRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        Map<String, Object> kpis = new LinkedHashMap<>();

        long totalDossiers = dossierRepository.count();
        kpis.put("totalDossiers", totalDossiers);

        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutDossier s : StatutDossier.values()) {
            parStatut.put(s.name(), (long) dossierRepository.findByStatutActuel(s).size());
        }
        kpis.put("parStatut", parStatut);

        kpis.put("totalAgents", userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNom().equals("AGENT")))
                .count());

        kpis.put("totalAdmins", userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getNom().equals("ADMIN")))
                .count());

        long nonAssignes = dossierRepository.findAll().stream()
                .filter(d -> d.getAgentAssigne() == null).count();
        kpis.put("dossiersNonAssignes", nonAssignes);

        return ResponseEntity.ok(kpis);
    }
}