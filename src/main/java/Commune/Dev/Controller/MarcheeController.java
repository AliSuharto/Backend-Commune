package Commune.Dev.Controller;
import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Dtos.MarcheeResponseDTO;
import Commune.Dev.Models.Marchee;
import Commune.Dev.Services.MarcheeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/marchees")
@CrossOrigin(origins = "*")
public class MarcheeController {

    @Autowired
    private MarcheeService marcheeService;

    // CREATE - Enregistrer un seul marché
    @PostMapping
    // @PreAuthorize("hasAnyRole('CREATEUR_MARCHE', 'DIRECTEUR')")
    public ResponseEntity<ApiResponse<Marchee>> createMarchee(@RequestBody Marchee marchee) {
        return marcheeService.save(marchee);
    }

    // CREATE - Enregistrer plusieurs marchés en même temps
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createMarchees(@RequestBody List<Marchee> marchees) {
        try {
            return marcheeService.saveAll(marchees);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    // READ - Récupérer tous les marchés
    //@GetMapping
//    public ResponseEntity<List<Marchee>> getAllMarchees() {
//        try {
//            List<Marchee> marchees = marcheeService.findAll();
//
//            return ResponseEntity.ok(marchees);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    @GetMapping
    public ResponseEntity<List<MarcheeResponseDTO>> getAllMarcheeStats() {
        try {
            List<MarcheeResponseDTO> marcheeStats = marcheeService.getAllMarcheeStats();
            return ResponseEntity.ok(marcheeStats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // READ - Récupérer un marché par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Marchee> getMarcheeById(@PathVariable Integer id) {
        try {
            Optional<Marchee> marchee = marcheeService.findById(id);
            System.out.println("ID reçu : " + id);
            return marchee.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer des marchés par leurs IDs
    @PostMapping("/by-ids")
    public ResponseEntity<List<Marchee>> getMarcheesByIds(@RequestBody List<Integer> ids) {
        try {
            List<Marchee> marchees = marcheeService.findAllById(ids);
            return ResponseEntity.ok(marchees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Rechercher des marchés par nom
    @GetMapping("/search")
    public ResponseEntity<List<Marchee>> searchMarcheesByNom(@RequestParam String nom) {
        try {
            List<Marchee> marchees = marcheeService.findByNomContainingIgnoreCase(nom);
            return ResponseEntity.ok(marchees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Rechercher des marchés par adresse
    @GetMapping("/search-by-address")
    public ResponseEntity<List<Marchee>> searchMarcheesByAdresse(@RequestParam String adresse) {
        try {
            List<Marchee> marchees = marcheeService.findByAdresseContainingIgnoreCase(adresse);
            return ResponseEntity.ok(marchees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer les marchés par nombre de places minimum
    @GetMapping("/by-capacity")
    public ResponseEntity<List<Marchee>> getMarcheesByMinCapacity(@RequestParam Integer minPlaces) {
        try {
            List<Marchee> marchees = marcheeService.findByNbrPlaceGreaterThanEqual(minPlaces);
            return ResponseEntity.ok(marchees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer les marchés ordonnés par capacité
    @GetMapping("/ordered-by-capacity")
    public ResponseEntity<List<Marchee>> getMarcheesOrderedByCapacity(@RequestParam(defaultValue = "DESC") String direction) {
        try {
            List<Marchee> marchees = direction.equalsIgnoreCase("ASC")
                    ? marcheeService.findAllOrderByNbrPlaceAsc()
                    : marcheeService.findAllOrderByNbrPlaceDesc();
            return ResponseEntity.ok(marchees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // UPDATE - Mettre à jour un marché
    @PutMapping("/{id}")
    public ResponseEntity<ResponseEntity<ApiResponse<Marchee>>> updateMarchee(@PathVariable Integer id, @RequestBody Marchee marcheeDetails) {
        try {
            Optional<Marchee> optionalMarchee = marcheeService.findById(id);
            if (optionalMarchee.isPresent()) {
                Marchee marchee = optionalMarchee.get();

                // Mise à jour des champs
                marchee.setNom(marcheeDetails.getNom());
                marchee.setAdresse(marcheeDetails.getAdresse());
                marchee.setNbrPlace(marcheeDetails.getNbrPlace());

                ResponseEntity<ApiResponse<Marchee>> updatedMarchee = marcheeService.save(marchee);
                return ResponseEntity.ok(updatedMarchee);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // UPDATE - Mettre à jour plusieurs marchés
    @PutMapping("/batch")
    public ResponseEntity<List<Marchee>> updateMarchees(@RequestBody List<Marchee> marchees) {
        try {
            List<Marchee> updatedMarchees = (List<Marchee>) marcheeService.saveAll(marchees);
            return ResponseEntity.ok(updatedMarchees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PATCH - Mise à jour partielle d'un marché
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseEntity<ApiResponse<Marchee>>> partialUpdateMarchee(@PathVariable Integer id, @RequestBody Marchee marcheePartial) {
        try {
            Optional<Marchee> optionalMarchee = marcheeService.findById(id);
            if (optionalMarchee.isPresent()) {
                Marchee marchee = optionalMarchee.get();

                // Mise à jour seulement des champs non null
                if (marcheePartial.getNom() != null) {
                    marchee.setNom(marcheePartial.getNom());
                }
                if (marcheePartial.getAdresse() != null) {
                    marchee.setAdresse(marcheePartial.getAdresse());
                }
                if (marcheePartial.getNbrPlace() != null) {
                    marchee.setNbrPlace(marcheePartial.getNbrPlace());
                }

                ResponseEntity<ApiResponse<Marchee>> updatedMarchee = marcheeService.save(marchee);
                return ResponseEntity.ok(updatedMarchee);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // DELETE - Supprimer un marché par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMarchee(@PathVariable Integer id) {
        try {
            if (marcheeService.existsById(id)) {
                marcheeService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE - Supprimer plusieurs marchés par leurs IDs
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteMarchees(@RequestBody List<Integer> ids) {
        try {
            marcheeService.deleteAllById(ids);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE - Supprimer tous les marchés
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllMarchees() {
        try {
            marcheeService.deleteAll();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour compter le nombre total de marchés
    @GetMapping("/count")
    public ResponseEntity<Long> countMarchees() {
        try {
            long count = marcheeService.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour vérifier si un marché existe
//    @GetMapping("/{id}/exists")
//    public ResponseEntity<Boolean> marcheeExists(@PathVariable Integer id) {
//        try {
//            boolean exists = marcheeService.existsById(id);
//            return ResponseEntity.ok(exists);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Statistiques complètes d'un marché
//    @GetMapping("/{id}/stats")
//    public ResponseEntity<Map<String, Object>> getMarcheeStats(@PathVariable Integer id) {
//        try {
//            Map<String, Object> stats = marcheeService.getMarcheeStatistics(id);
//            if (stats.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//            return ResponseEntity.ok(stats);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Récupérer toutes les zones d'un marché
//    @GetMapping("/{id}/zones")
//    public ResponseEntity<Object> getMarcheeZones(@PathVariable Integer id) {
//        try {
//            return ResponseEntity.ok(marcheeService.getZonesByMarcheeId(id));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Récupérer toutes les places d'un marché
//    @GetMapping("/{id}/places")
//    public ResponseEntity<Object> getMarcheePlaces(@PathVariable Integer id) {
//        try {
//            return ResponseEntity.ok(marcheeService.getPlacesByMarcheeId(id));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    // Récupérer toutes les salles d'un marché
//    @GetMapping("/{id}/salles")
//    public ResponseEntity<Object> getMarcheeSalles(@PathVariable Integer id) {
//        try {
//            return ResponseEntity.ok(marcheeService.getSallesByMarcheeId(id));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    // Statistiques globales de tous les marchés
    @GetMapping("/global-stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        try {
            Map<String, Object> stats = marcheeService.getGlobalStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Comparaison entre deux marchés
    @GetMapping("/compare/{id1}/{id2}")
    public ResponseEntity<Map<String, Object>> compareMarchees(@PathVariable Integer id1, @PathVariable Integer id2) {
        try {
            Map<String, Object> comparison = marcheeService.compareMarchees(id1, id2);
            if (comparison.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}