package Commune.Dev.Controller;

import Commune.Dev.Models.Place;
import Commune.Dev.Services.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = "*")
public class PlaceController {

    @Autowired
    private PlaceService placeService;

    // CREATE - Enregistrer une seule place
    @PostMapping
    public ResponseEntity<Place> createPlace(@RequestBody Place place) {
        try {
            Place savedPlace = placeService.save(place);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlace);
        } catch (Exception e) {
            e.printStackTrace(); // ⚠️ mets ça pour voir l'erreur exacte dans les logs
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testPost(@RequestBody String data) {

        return ResponseEntity.ok("POST fonctionne ! Data reçue: " + data);
    }

    // CREATE - Enregistrer plusieurs places en même temps
    @PostMapping("/batch")
    public ResponseEntity<List<Place>> createPlaces(@RequestBody List<Place> places) {
        try {
            List<Place> savedPlaces = placeService.saveAll(places);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // READ - Récupérer toutes les places
    @GetMapping
    public ResponseEntity<List<Place>> getAllPlaces() {
        try {
            List<Place> places = placeService.findAll();
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer une place par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Place> getPlaceById(@PathVariable Integer id) {
        try {
            Optional<Place> place = placeService.findById(id);
            return place.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer des places par leurs IDs
    @PostMapping("/by-ids")
    public ResponseEntity<List<Place>> getPlacesByIds(@RequestBody List<Integer> ids) {
        try {
            List<Place> places = placeService.findAllById(ids);
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Rechercher des places par nom
    @GetMapping("/search")
    public ResponseEntity<List<Place>> searchPlacesByNom(@RequestParam String nom) {
        try {
            List<Place> places = placeService.findByNomContainingIgnoreCase(nom);
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer les places occupées
    @GetMapping("/occupied")
    public ResponseEntity<List<Place>> getOccupiedPlaces() {
        try {
            List<Place> occupiedPlaces = placeService.findByIsOccuped(true);
            return ResponseEntity.ok(occupiedPlaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer les places disponibles
    @GetMapping("/available")
    public ResponseEntity<List<Place>> getAvailablePlaces() {
        try {
            List<Place> availablePlaces = placeService.findByIsOccuped(false);
            return ResponseEntity.ok(availablePlaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // UPDATE - Mettre à jour une place
    @PutMapping("/{id}")
    public ResponseEntity<Place> updatePlace(@PathVariable Integer id, @RequestBody Place placeDetails) {
        try {
            Optional<Place> optionalPlace = placeService.findById(id);
            if (optionalPlace.isPresent()) {
                Place place = optionalPlace.get();

                // Mise à jour des champs
                place.setNom(placeDetails.getNom());
                place.setAdresse(placeDetails.getAdresse());
                place.setIsOccuped(placeDetails.getIsOccuped());
                place.setDateDebutOccupation(placeDetails.getDateDebutOccupation());
                place.setDateFinOccupation(placeDetails.getDateFinOccupation());

                Place updatedPlace = placeService.save(place);
                return ResponseEntity.ok(updatedPlace);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // UPDATE - Mettre à jour plusieurs places
    @PutMapping("/batch")
    public ResponseEntity<List<Place>> updatePlaces(@RequestBody List<Place> places) {
        try {
            List<Place> updatedPlaces = placeService.saveAll(places);
            return ResponseEntity.ok(updatedPlaces);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PATCH - Mise à jour partielle d'une place
    @PatchMapping("/{id}")
    public ResponseEntity<Place> partialUpdatePlace(@PathVariable Integer id, @RequestBody Place placePartial) {
        try {
            Optional<Place> optionalPlace = placeService.findById(id);
            if (optionalPlace.isPresent()) {
                Place place = optionalPlace.get();

                // Mise à jour seulement des champs non null
                if (placePartial.getNom() != null) {
                    place.setNom(placePartial.getNom());
                }
                if (placePartial.getAdresse() != null) {
                    place.setAdresse(placePartial.getAdresse());
                }
                if (placePartial.getIsOccuped() != null) {
                    place.setIsOccuped(placePartial.getIsOccuped());
                }
                if (placePartial.getDateDebutOccupation() != null) {
                    place.setDateDebutOccupation(placePartial.getDateDebutOccupation());
                }
                if (placePartial.getDateFinOccupation() != null) {
                    place.setDateFinOccupation(placePartial.getDateFinOccupation());
                }

                Place updatedPlace = placeService.save(place);
                return ResponseEntity.ok(updatedPlace);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // DELETE - Supprimer une place par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Integer id) {
        try {
            if (placeService.existsById(id)) {
                placeService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE - Supprimer plusieurs places par leurs IDs
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deletePlaces(@RequestBody List<Integer> ids) {
        try {
            placeService.deleteAllById(ids);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE - Supprimer toutes les places
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllPlaces() {
        try {
            placeService.deleteAll();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour compter le nombre total de places
    @GetMapping("/count")
    public ResponseEntity<Long> countPlaces() {
        try {
            long count = placeService.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour vérifier si une place existe
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> placeExists(@PathVariable Integer id) {
        try {
            boolean exists = placeService.existsById(id);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}