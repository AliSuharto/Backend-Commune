package Commune.Dev.Controller;
import Commune.Dev.Dtos.AttributionResponse;
import Commune.Dev.Dtos.MarchandDTO;
import Commune.Dev.Dtos.PlaceAttrDTO;
import Commune.Dev.Request.AttributionRequest;
import Commune.Dev.Services.AttributionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*") // À configurer selon vos besoins de sécurité
public class AttributionController {

    @Autowired
    private AttributionService attributionService;

    /**
     * Attribuer une place à un marchand
     */
    @PostMapping("/attribuer")
    public ResponseEntity<AttributionResponse> attribuerPlace(@Valid @RequestBody AttributionRequest request) {
        AttributionResponse response = attributionService.attribuerPlace(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Libérer une place
     */
    @PostMapping("/liberer/{placeId}")
    public ResponseEntity<AttributionResponse> libererPlace(@PathVariable Integer placeId) {
        AttributionResponse response = attributionService.libererPlace(placeId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtenir la liste des marchands sans place
     */
    @GetMapping("/marchands-sans-place")
    public ResponseEntity<List<MarchandDTO>> getMarchandsSansPlace() {
        List<MarchandDTO> marchands = attributionService.getMarchandsSansPlace();
        return ResponseEntity.ok(marchands);
    }

    /**
     * Obtenir la liste des places disponibles
     */
    @GetMapping("/places-disponibles")
    public ResponseEntity<List<PlaceAttrDTO>> getPlacesDisponibles() {
        List<PlaceAttrDTO> places = attributionService.getPlacesDisponibles();
        return ResponseEntity.ok(places);
    }

    /**
     * Obtenir la liste des places occupées
     */
    @GetMapping("/places-occupees")
    public ResponseEntity<List<PlaceAttrDTO>> getPlacesOccupees() {
        List<PlaceAttrDTO> places = attributionService.getPlacesOccupees();
        return ResponseEntity.ok(places);
    }

    /**
     * Obtenir les statistiques d'occupation
     */


    /**
     * Rechercher des places par critères
     */
    @GetMapping("/rechercher")
    public ResponseEntity<List<PlaceAttrDTO>> rechercherPlaces(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) Boolean disponible) {

        List<PlaceAttrDTO> places;

        if (disponible != null && disponible) {
            places = attributionService.getPlacesDisponibles();
        } else if (disponible != null && !disponible) {
            places = attributionService.getPlacesOccupees();
        } else {
            // Retourner toutes les places si aucun filtre de disponibilité
            places = attributionService.getPlacesDisponibles();
            places.addAll(attributionService.getPlacesOccupees());
        }

        // Filtrer par nom si spécifié
        if (nom != null && !nom.trim().isEmpty()) {
            places = places.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(nom.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filtrer par adresse si spécifié
        if (adresse != null && !adresse.trim().isEmpty()) {
            places = places.stream()
                    .filter(p -> p.getAdresse().toLowerCase().contains(adresse.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        return ResponseEntity.ok(places);
    }
}
