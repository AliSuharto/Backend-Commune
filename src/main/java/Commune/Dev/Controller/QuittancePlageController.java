package Commune.Dev.Controller;

import Commune.Dev.Dtos.QuittanceResponseDTO;
import Commune.Dev.Models.QuittancePlage;
import Commune.Dev.Request.QuittancePlageRequest;
import Commune.Dev.Services.QuittancePlageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quittance-plage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuittancePlageController {

    private final QuittancePlageService quittancePlageService;

    /**
     * Créer une nouvelle plage de quittances
     */
    @PostMapping
    public ResponseEntity<QuittanceResponseDTO> createQuittancePlage(
            @Valid @RequestBody QuittancePlageRequest request) {
        try {
            return quittancePlageService.createQuittancePlage(request);
        } catch (IllegalArgumentException e) {
            // Retourne 400 avec le message d'erreur personnalisé
            QuittanceResponseDTO errorResponse = new QuittanceResponseDTO(
                    "Erreur de validation : " + e.getMessage(),
                    0, 0, 0, request.getMultiplicateur()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            // Pour les cas comme percepteur ou contrôleur non trouvé
            QuittanceResponseDTO errorResponse = new QuittanceResponseDTO(
                    "Erreur serveur : " + e.getMessage(),
                    0, 0, 0, request.getMultiplicateur()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            QuittanceResponseDTO errorResponse = new QuittanceResponseDTO(
                    "Une erreur inattendue est survenue.",
                    0, 0, 0, request.getMultiplicateur()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    /**
     * Récupérer une plage par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuittancePlageById(@PathVariable Long id) {
        try {
            QuittancePlage plage = quittancePlageService.getQuittancePlageById(id);
            return ResponseEntity.ok(plage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de la plage: " + e.getMessage());
        }
    }

    /**
     * Récupérer toutes les plages de quittances
     */
    @GetMapping
    public ResponseEntity<?> getAllQuittancePlages() {
        try {
            List<QuittancePlage> plages = quittancePlageService.getAllQuittancePlages();

            // Transformation des données pour retourner uniquement les champs souhaités
            List<Map<String, Object>> response = plages.stream().map(plage -> {
                Map<String, Object> map = new HashMap<>();
                map.put("controlleur", plage.getControlleur() != null ? plage.getControlleur().getNom() : null);
                map.put("percepteur", plage.getPercepteur() != null ? plage.getPercepteur().getNom() : null);
                map.put("debut", plage.getDebut());
                map.put("fin", plage.getFin());
                map.put("createdAt", plage.getCreatedAt());
                map.put("nombreQuittance", plage.getNombreQuittance());
                map.put("quittanceRestant", plage.getQuittanceRestant());
                map.put("multiplicateur", plage.getMultiplicateur());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des plages: " + e.getMessage());
        }
    }


    /**
     * Récupérer les plages d'un percepteur spécifique
     */
    @GetMapping("/percepteur/{percepteurId}")
    public ResponseEntity<?> getPlagesByPercepteur(@PathVariable Long percepteurId) {
        try {
            List<QuittancePlage> plages = quittancePlageService.getPlagesByPercepteur(percepteurId);
            List<Map<String, Object>> response = plages.stream().map(plage -> {
                Map<String, Object> map = new HashMap<>();
                map.put("controlleur", plage.getControlleur() != null ? plage.getControlleur().getNom() : null);
                map.put("percepteur", plage.getPercepteur() != null ? plage.getPercepteur().getNom() : null);
                map.put("debut", plage.getDebut());
                map.put("fin", plage.getFin());
                map.put("createdAt", plage.getCreatedAt());
                map.put("nombreQuittance", plage.getNombreQuittance());
                map.put("quittanceRestant", plage.getQuittanceRestant());
                map.put("multiplicateur", plage.getMultiplicateur());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des plages du percepteur: " + e.getMessage());
        }
    }
}