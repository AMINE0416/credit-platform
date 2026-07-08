package org.eqdom.creditplatform.config;

import org.eqdom.creditplatform.model.Client;
import org.eqdom.creditplatform.model.Credit;
import org.eqdom.creditplatform.model.Dossier;
import org.eqdom.creditplatform.model.Role;
import org.eqdom.creditplatform.model.StatutDossier;
import org.eqdom.creditplatform.model.User;
import org.eqdom.creditplatform.repository.ClientRepository;
import org.eqdom.creditplatform.repository.CreditRepository;
import org.eqdom.creditplatform.repository.DossierRepository;
import org.eqdom.creditplatform.repository.RoleRepository;
import org.eqdom.creditplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CreditRepository creditRepository;
    private final DossierRepository dossierRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           ClientRepository clientRepository,
                           CreditRepository creditRepository,
                           DossierRepository dossierRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.creditRepository = creditRepository;
        this.dossierRepository = dossierRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // 1. Création des rôles s'ils n'existent pas
        Role adminRole = roleRepository.findByNom("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        Role agentRole = roleRepository.findByNom("AGENT")
                .orElseGet(() -> roleRepository.save(new Role("AGENT")));

        // 2. Création du compte ADMIN de test
        if (userRepository.findByEmail("admin@eqdom.ma").isEmpty()) {
            User admin = new User();
            admin.setNom("Bennani");
            admin.setPrenom("Yassine");
            admin.setEmail("admin@eqdom.ma");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setActif(true);
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println("✅ Compte ADMIN créé : admin@eqdom.ma / Admin@123");
        }

        // 3. Création du compte AGENT de test
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
        }

        User agent = userRepository.findByEmail("agent@eqdom.ma").orElseThrow();
        String referenceContrat = "C30J0214";
        String cin = "A640214";

        Client client = clientRepository.findByCin(cin).orElseGet(() -> {
            Client newClient = new Client();
            newClient.setCin(cin);
            newClient.setNom("MOHAMMED");
            newClient.setPrenom("HAOUZA");
            newClient.setTelephone("06 12 12 12 21");
            newClient.setTelephone2("06 12 12 12 21");
            newClient.setAdresse("475 GROUPE EL AHD HAY NAHDA");
            newClient.setVille("RABAT");
            newClient.setCodeAgence("918");
            newClient.setLibAgence("Agence Kénitra Mohamed V");
            return clientRepository.save(newClient);
        });

        Credit credit = creditRepository.findByReferenceContrat(referenceContrat).orElseGet(() -> {
            Credit newCredit = new Credit();
            newCredit.setClient(client);
            newCredit.setReferenceContrat(referenceContrat);
            newCredit.setFromProv("");
            newCredit.setGestCin("AOUAD");
            newCredit.setTypeCredit("AUTO_CLASSIQUE");
            newCredit.setSegment("AUTO PP");
            newCredit.setNbrImpaye(0);
            newCredit.setMensualite(new BigDecimal("3713.95"));
            newCredit.setEncours(new BigDecimal("98284.82"));
            newCredit.setImpayeMontant(new BigDecimal("18569.75"));
            newCredit.setPrvCum(new BigDecimal("9618.86"));
            newCredit.setStage("S3");
            return creditRepository.save(newCredit);
        });

        if (dossierRepository.findByCreditId(credit.getId()).isEmpty()) {
            Dossier dossier = new Dossier();
            dossier.setCredit(credit);
            dossier.setAgentAssigne(agent);
            dossier.setStatutActuel(StatutDossier.NOUVEAU);
            dossierRepository.save(dossier);
            System.out.println("✅ Dossier de test créé pour l'agent : " + agent.getEmail());
        }

    }

}