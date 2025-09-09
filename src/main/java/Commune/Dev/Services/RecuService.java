package Commune.Dev.Services;

import Commune.Dev.Dtos.RecuPlageResponse;
import Commune.Dev.Models.EtatRecu;
import Commune.Dev.Models.Recu;
import Commune.Dev.Models.RecuPlage;
import Commune.Dev.Models.TypeRecu;
import Commune.Dev.Repositories.RecuPlageRepository;
import Commune.Dev.Repositories.RecuRepository;
import Commune.Dev.Repositories.UserRepository;
import Commune.Dev.Request.RecuPlageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecuService {

    private final RecuRepository recuRepository;
    private final RecuPlageRepository recuPlageRepository;
    private final UserRepository percepteurRepository;

    /**
     * Crée une nouvelle plage de reçus avec génération automatique des numéros
     */
    @Transactional
    public RecuPlageResponse createRecuPlage(RecuPlageRequest request) {
        log.info("Création d'une plage de reçus pour le percepteur {}", request.getPercepteurId());

        // Vérifier que le percepteur existe
        if (!percepteurRepository.existsById(request.getPercepteurId())) {
            throw new IllegalArgumentException("Percepteur non trouvé avec l'ID: " + request.getPercepteurId());
        }

        // Générer la liste des numéros
        List<String> numerosGeneres = genererNumeros(request);
        log.info("Génération de {} numéros", numerosGeneres.size());

        // Vérifier les doublons
        List<String> doublons = recuRepository.findExistingNumeros(numerosGeneres);
        if (!doublons.isEmpty()) {
            throw new IllegalStateException("Doublons détectés: " + String.join(", ", doublons));
        }

        // Sauvegarder la plage
        RecuPlage plage = RecuPlage.builder()
                .percepteurId(request.getPercepteurId())
                .debut(request.getDebut())
                .fin(request.getFin())
                .type(request.getType())
                .multiplicateur(request.getMultiplicateur())
                .build();

        RecuPlage plageSauvee = recuPlageRepository.save(plage);

        // Créer tous les reçus
        List<Recu> recus = numerosGeneres.stream()
                .map(numero -> Recu.builder()
                        .percepteurId(request.getPercepteurId())
                        .numero(numero)
                        .etat(EtatRecu.LIBRE)
                        .build())
                .toList();

        recuRepository.saveAll(recus);
        log.info("Plage {} créée avec {} reçus", plageSauvee.getId(), recus.size());

        return new RecuPlageResponse(plageSauvee.getId(), numerosGeneres);
    }

    /**
     * Génère la liste des numéros selon le type et les paramètres
     */
    private List<String> genererNumeros(RecuPlageRequest request) {
        List<String> numeros = new ArrayList<>();

        if (request.getType() == TypeRecu.NUMERIC) {
            numeros.addAll(genererNumerosNumeriques(request));
        } else if (request.getType() == TypeRecu.ALPHANUMERIC) {
            numeros.addAll(genererNumerosAlphanumeriques(request));
        }

        // Appliquer le multiplicateur si spécifié
        if (request.getMultiplicateur() != null && request.getMultiplicateur() > 0) {
            numeros = appliquerMultiplicateur(numeros, request.getMultiplicateur());
        }

        return numeros;
    }

    /**
     * Génère les numéros purement numériques (ex: 120 à 170)
     */
    private List<String> genererNumerosNumeriques(RecuPlageRequest request) {
        List<String> numeros = new ArrayList<>();

        try {
            int debut = Integer.parseInt(request.getDebut());
            int fin = Integer.parseInt(request.getFin());

            if (fin < debut) {
                throw new IllegalArgumentException("La fin doit être supérieure au début");
            }

            for (int i = debut; i <= fin; i++) {
                numeros.add(String.valueOf(i));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format numérique invalide");
        }

        return numeros;
    }

    /**
     * Génère les numéros alphanumériques (ex: 45601A à 45601Z)
     */
    private List<String> genererNumerosAlphanumeriques(RecuPlageRequest request) {
        List<String> numeros = new ArrayList<>();

        String debut = request.getDebut();
        String fin = request.getFin();

        // Extraire la partie numérique et la lettre
        if (debut.length() < 2 || fin.length() < 2) {
            throw new IllegalArgumentException("Format alphanumérique invalide");
        }

        String baseDebut = debut.substring(0, debut.length() - 1);
        String baseFin = fin.substring(0, fin.length() - 1);
        char lettreDebut = debut.charAt(debut.length() - 1);
        char lettreFin = fin.charAt(fin.length() - 1);

        if (!baseDebut.equals(baseFin)) {
            throw new IllegalArgumentException("La base numérique doit être identique");
        }

        if (lettreDebut > lettreFin) {
            throw new IllegalArgumentException("La lettre de fin doit être supérieure à celle du début");
        }

        for (char c = lettreDebut; c <= lettreFin; c++) {
            numeros.add(baseDebut + c);
        }

        return numeros;
    }

    /**
     * Applique un multiplicateur pour créer des sous-numéros (ex: 120A, 120B, 120C...)
     */
    private List<String> appliquerMultiplicateur(List<String> numerosBase, int multiplicateur) {
        List<String> numerosAvecMultiplicateur = new ArrayList<>();

        for (String numeroBase : numerosBase) {
            for (int i = 0; i < multiplicateur; i++) {
                char suffixe = (char) ('A' + i);
                numerosAvecMultiplicateur.add(numeroBase + suffixe);
            }
        }

        return numerosAvecMultiplicateur;
    }

    /**
     * Prévisualise les numéros qui seraient générés (sans les sauvegarder)
     */
    public List<String> previsualiserNumeros(RecuPlageRequest request) {
        return genererNumeros(request);
    }
}
