package org.eqdom.creditplatform.service;

import org.eqdom.creditplatform.exception.DossierNotFoundException;
import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.ActionDossierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la suppression en cascade de dossiers.
 * Gère la suppression du dossier et de toutes ses actions associées.
 */
@Service
public class DossierSuppressionService {

    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private ActionDossierRepository actionDossierRepository;

    @Transactional
    public void supprimerDossier(Long dossierId) {
        // Vérifier que le dossier existe
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new DossierNotFoundException("Dossier introuvable: " + dossierId));

        // 1. Supprimer d'abord les actions associées
        actionDossierRepository.deleteByDossierId(dossierId);

        // 2. Supprimer le dossier
        dossierRepository.deleteById(dossierId);

        // Vérifier que la suppression a fonctionné
        if (dossierRepository.existsById(dossierId)) {
            throw new RuntimeException("Erreur: la suppression du dossier a échoué");
        }
    }
}