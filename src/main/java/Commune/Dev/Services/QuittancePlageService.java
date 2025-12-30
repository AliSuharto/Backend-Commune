package Commune.Dev.Services;

import Commune.Dev.Dtos.QuittanceResponseDTO;
import Commune.Dev.Models.Quittance;
import Commune.Dev.Models.QuittancePlage;
import Commune.Dev.Models.StatusQuittance;
import Commune.Dev.Models.User;
import Commune.Dev.Repositories.QuittancePlageRepository;
import Commune.Dev.Repositories.QuittanceRepository;
import Commune.Dev.Repositories.UserRepository;
import Commune.Dev.Request.QuittancePlageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuittancePlageService {

    private final QuittancePlageRepository quittancePlageRepository;
    private final QuittanceRepository quittanceRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<QuittanceResponseDTO> createQuittancePlage(QuittancePlageRequest request) {
        // Vérification que le percepteur existe
        User percepteur = userRepository.findById(request.getPercepteurId())
                .orElseThrow(() -> new RuntimeException("Percepteur introuvable"));

        User controlleur = userRepository.findById(request.getControlleurId())
                .orElseThrow(() -> new RuntimeException("Contrôleur introuvable"));

        // Validation du multiplicateur
        if (request.getMultiplicateur() == null || request.getMultiplicateur() < 1 || request.getMultiplicateur() > 26) {
            throw new IllegalArgumentException("Le multiplicateur doit être entre 1 et 26");
        }

        // Validation que debut et fin sont des nombres uniquement
        Integer numeroDebut = validerEtExtraireNumero(request.getDebut());
        Integer numeroFin = validerEtExtraireNumero(request.getFin());
        //
        String code= request.getCode();

        // Validation de la plage
        if (numeroDebut >= numeroFin) {
            throw new IllegalArgumentException("Le début doit être inférieur à la fin");
        }

        // Validation du code : si null ou vide, on affecte "-KC1"
        if (code == null || code.trim().isEmpty()) {
            code = "-KC1";   // Valeur par défaut
        }


        // Vérification de superposition avec les plages existantes
        List<QuittancePlage> plagesExistantes = quittancePlageRepository.findAll();
        for (QuittancePlage plageExistante : plagesExistantes) {
            Integer debutExistant = validerEtExtraireNumero(plageExistante.getDebut());
            Integer finExistante = validerEtExtraireNumero(plageExistante.getFin());

            // Vérification de superposition
            if (!(numeroFin < debutExistant || numeroDebut > finExistante)) {
                throw new IllegalArgumentException(
                        String.format("La plage [%s - %s] se superpose avec une plage existante [%s - %s]",
                                request.getDebut(), request.getFin(),
                                plageExistante.getDebut(), plageExistante.getFin())
                );
            }
        }

        // Génération de tous les identifiants de quittances pour cette plage
        Set<String> nouveauxIds = genererTousLesIds(numeroDebut, numeroFin, request.getMultiplicateur());

        // Vérification si des quittances existent déjà
        List<String> idsExistants = verifierQuittancesExistantes(nouveauxIds);
        if (!idsExistants.isEmpty()) {
            throw new IllegalArgumentException(
                    "Les quittances suivantes existent déjà: " + String.join(", ", idsExistants)
            );
        }

        // Calcul du nombre total de quittances
        int nombreTotalQuittances = nouveauxIds.size();

        // Création de la plage
        QuittancePlage quittancePlage = QuittancePlage.builder()
                .percepteur(percepteur)
                .controlleur(controlleur)
                .debut(request.getDebut())
                .fin(request.getFin())
                .code(code)
                .multiplicateur(request.getMultiplicateur())
                .nombreQuittance(nombreTotalQuittances)
                .QuittanceRestant(nombreTotalQuittances)
                .build();

        // Sauvegarde de la plage
        quittancePlage = quittancePlageRepository.save(quittancePlage);

        // Création des quittances
        List<Quittance> quittances = new ArrayList<>();
        for (int numero = numeroDebut; numero <= numeroFin; numero++) {
            for (int suffixe = 0; suffixe < request.getMultiplicateur(); suffixe++) {
                char lettre = (char) ('A' + suffixe);
                String nomQuittance = numero + String.valueOf(lettre);
                Quittance quittance = new Quittance();
                quittance.setNom(nomQuittance);
                quittance.setEtat(StatusQuittance.DISPONIBLE);
                quittance.setPercepteurId(quittancePlage.getPercepteur().getId());
                quittance.setQuittancePlage(quittancePlage);
                quittances.add(quittance);
            }
        }

        // Sauvegarde de toutes les quittances
        quittanceRepository.saveAll(quittances);
        QuittanceResponseDTO response = new QuittanceResponseDTO(
                "Plage de quittances créée avec succès !",
                nombreTotalQuittances,
                numeroDebut,
                numeroFin,
                request.getMultiplicateur()
        );
        // Retourner le message de succès
        return ResponseEntity.ok(response);
    }

    /**
     * Valide et extrait le numéro d'une chaîne (doit être un nombre pur)
     * Exemple: "120" -> 120, "34059" -> 34059
     * Lance une exception si la chaîne contient des lettres
     */
    private Integer validerEtExtraireNumero(String chaine) {
        if (chaine == null || chaine.trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro ne peut pas être vide");
        }

        // Vérifier que la chaîne ne contient que des chiffres
        if (!chaine.matches("^\\d+$")) {
            throw new IllegalArgumentException("Le format doit être un nombre pur (ex: 120, 34059). Les lettres ne sont pas autorisées.");
        }

        try {
            return Integer.parseInt(chaine);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format invalide pour: " + chaine);
        }
    }

    /**
     * Génère tous les identifiants de quittances pour une plage donnée
     */
    private Set<String> genererTousLesIds(Integer debut, Integer fin, Integer multiplicateur) {
        Set<String> ids = new HashSet<>();
        for (int numero = debut; numero <= fin; numero++) {
            for (int suffixe = 0; suffixe < multiplicateur; suffixe++) {
                char lettre = (char) ('A' + suffixe);
                ids.add(numero + String.valueOf(lettre));
            }
        }
        return ids;
    }

    /**
     * Vérifie si des quittances existent déjà dans la base
     */
    private List<String> verifierQuittancesExistantes(Set<String> nouveauxIds) {
        List<String> existants = new ArrayList<>();
        for (String nomId : nouveauxIds) {
            Integer id = convertirNomEnId(nomId);
            if (quittanceRepository.existsById(Long.valueOf(id))) {
                existants.add(nomId);
            }
        }
        return existants;
    }

    /**
     * Convertit un nom de quittance (ex: "120A") en ID unique
     * Exemple: 120A -> 1201, 120B -> 1202, etc.
     */
    private Integer convertirNomEnId(String nom) {
        String numeroStr = nom.replaceAll("[A-Z]", "");
        Integer numero = Integer.parseInt(numeroStr);
        char lettre = nom.replaceAll("[0-9]", "").charAt(0);
        int suffixe = lettre - 'A' + 1;

        // Crée un ID unique: numéro * 100 + suffixe
        return numero * 100 + suffixe;
    }

    /**
     * Récupère une plage par son ID
     */
    public QuittancePlage getQuittancePlageById(Long id) {
        return quittancePlageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("QuittancePlage non trouvée avec l'ID: " + id));
    }

    /**
     * Récupère toutes les plages
     */
    public List<QuittancePlage> getAllQuittancePlages() {
        return quittancePlageRepository.findAll();
    }

    /**
     * Récupère les plages d'un percepteur
     */
    public List<QuittancePlage> getPlagesByPercepteur(Long percepteurId) {
        return quittancePlageRepository.findByPercepteurId(percepteurId);
    }
}