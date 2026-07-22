package org.eqdom.creditplatform.config;

import org.eqdom.creditplatform.model.Client;
import org.eqdom.creditplatform.model.Credit;
import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.Role;
import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.ActionDossierRepository;
import org.eqdom.creditplatform.repository.ClientRepository;
import org.eqdom.creditplatform.repository.CreditRepository;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.RoleRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CreditRepository creditRepository;
    private final DossierRepository dossierRepository;
    private final ActionDossierRepository actionDossierRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           ClientRepository clientRepository,
                           CreditRepository creditRepository,
                           DossierRepository dossierRepository,
                           ActionDossierRepository actionDossierRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.creditRepository = creditRepository;
        this.dossierRepository = dossierRepository;
        this.actionDossierRepository = actionDossierRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // ⚠️ NETTOYAGE DÉSACTIVÉ - Décommenter seulement si besoin de réinitialiser
        /*
        System.out.println("🔄 Nettoyage des données importées...");
        
        try {
            // Suppression en ordre avec gestion native SQL pour éviter les contraintes
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            
            long actionCount = actionDossierRepository.count();
            if (actionCount > 0) {
                entityManager.createNativeQuery("DELETE FROM actions_dossier").executeUpdate();
                System.out.println("✅ " + actionCount + " action(s) supprimée(s)");
            }
            
            long dossierCount = dossierRepository.count();
            if (dossierCount > 0) {
                entityManager.createNativeQuery("DELETE FROM dossiers").executeUpdate();
                System.out.println("✅ " + dossierCount + " dossier(s) supprimé(s)");
            }
            
            long creditCount = creditRepository.count();
            if (creditCount > 0) {
                entityManager.createNativeQuery("DELETE FROM credits").executeUpdate();
                System.out.println("✅ " + creditCount + " crédit(s) supprimé(s)");
            }
            
            long clientCount = clientRepository.count();
            if (clientCount > 0) {
                entityManager.createNativeQuery("DELETE FROM clients").executeUpdate();
                System.out.println("✅ " + clientCount + " client(s) supprimé(s)");
            }
            
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            entityManager.flush();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du nettoyage : " + e.getMessage());
            e.printStackTrace();
        }
        */

        // 1. Création des rôles s'ils n'existent pas
        Role adminRole = roleRepository.findByNom("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        Role agentRole = roleRepository.findByNom("AGENT")
                .orElseGet(() -> roleRepository.save(new Role("AGENT")));

        // 2. Création du compte ADMIN de test (si n'existe pas)
        if (userRepository.findByEmail("admin@eqdom.ma").isEmpty()) {
            User admin = new User();
            admin.setNom("Bourmani");
            admin.setPrenom("Amine");
            admin.setEmail("admin@eqdom.ma");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setActif(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println("✅ Compte ADMIN créé : admin@eqdom.ma / Admin@123");
        } else {
            System.out.println("ℹ️  Compte ADMIN existant conservé : admin@eqdom.ma");
        }

        // 3. Création du compte AGENT de test (si n'existe pas)
        if (userRepository.findByEmail("agent@eqdom.ma").isEmpty()) {
            User agent = new User();
            agent.setNom("Haouza");
            agent.setPrenom("Mohammed");
            agent.setEmail("agent@eqdom.ma");
            agent.setPassword(passwordEncoder.encode("Agent@123"));
            agent.setActif(true);
            agent.setRoles(Set.of(agentRole));
            userRepository.save(agent);
            System.out.println("✅ Compte AGENT créé : agent@eqdom.ma / Agent@123");
        } else {
            System.out.println("ℹ️  Compte AGENT existant conservé : agent@eqdom.ma");
        }

        System.out.println("✅ Initialisation terminée");
        System.out.println("📝 Base de données prête - Aucun dossier présent");
        System.out.println("💡 Utilisez la fonction d'import Excel pour ajouter des dossiers");

    }

}