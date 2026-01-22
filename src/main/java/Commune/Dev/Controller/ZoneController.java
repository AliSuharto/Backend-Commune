package Commune.Dev.Controller;

import Commune.Dev.Dtos.ZoneDTO;
import Commune.Dev.Dtos.ZoneResponse;
import Commune.Dev.Models.Zone;
import Commune.Dev.Request.ZoneRequest;
import Commune.Dev.Services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/zones")
@CrossOrigin(origins = "*")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    // CREATE - Enregistrer une seule zone
    @PostMapping
    public ZoneResponse createZone(@RequestBody ZoneRequest request) {
        return zoneService.createZone(request);
    }

    // CREATE - Enregistrer plusieurs zones en même temps
    @PostMapping("/batch")
    public ResponseEntity<List<Zone>> createZones(@RequestBody List<Zone> zones) {
        try {
            // Vérifier que toutes les zones ont un idMarchee
            for (Zone zone : zones) {
                if (zone.getMarchee() == null) {
                    return ResponseEntity.badRequest().build();
                }
            }
            List<Zone> savedZones = zoneService.saveAll(zones);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedZones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // READ - Récupérer toutes les zones
    @GetMapping
    public ResponseEntity<List<ZoneDTO>> getAllZones() {
        try {
            List<ZoneDTO> zones = zoneService.findAll();
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            // Log l'erreur pour le debug
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ZoneDTO> getZoneById(@PathVariable Integer id) {
//        try {
//            ZoneDTO zone = zoneService.findById(id);
//            return ResponseEntity.ok(zone);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    // READ - Récupérer des zones par leurs IDs
    @PostMapping("/by-ids")
    public ResponseEntity<List<Zone>> getZonesByIds(@RequestBody List<Integer> ids) {
        try {
            List<Zone> zones = zoneService.findAllById(ids);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Récupérer toutes les zones d'un marché
    @GetMapping("/marchee/{marcheeId}")
    public ResponseEntity<List<Zone>> getZonesByMarcheeId(@PathVariable Integer marcheeId) {
        try {
            List<Zone> zones = zoneService.findByMarcheeId(marcheeId);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Rechercher des zones par nom
    @GetMapping("/search")
    public ResponseEntity<List<Zone>> searchZonesByNom(@RequestParam String nom) {
        try {
            List<Zone> zones = zoneService.findByNomContainingIgnoreCase(nom);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // READ - Rechercher des zones par nom dans un marché spécifique
    @GetMapping("/marchee/{marcheeId}/search")
    public ResponseEntity<List<Zone>> searchZonesInMarcheeByNom(
            @PathVariable Integer marcheeId,
            @RequestParam String nom) {
        try {
            List<Zone> zones = zoneService.findByMarcheeIdAndNomContainingIgnoreCase(marcheeId, nom);
            return ResponseEntity.ok(zones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // UPDATE - Mettre à jour une zone
    @PutMapping("/{id}")
    public ResponseEntity<Zone> updateZone(@PathVariable Integer id, @RequestBody Zone zoneDetails) {
        try {
            // Vérifier que idMarchee est fourni
            if (zoneDetails.getMarchee() == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Zone> optionalZone = zoneService.findById(id);
            if (optionalZone.isPresent()) {
                Zone zone = optionalZone.get();

                // Mise à jour des champs
                zone.setNom(zoneDetails.getNom());
                zone.setDescription(zoneDetails.getDescription());
                zone.setMarchee(zoneDetails.getMarchee());

                Zone updatedZone = zoneService.save(zone);
                return ResponseEntity.ok(updatedZone);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // UPDATE - Mettre à jour plusieurs zones
    @PutMapping("/batch")
    public ResponseEntity<List<Zone>> updateZones(@RequestBody List<Zone> zones) {
        try {
            // Vérifier que toutes les zones ont un idMarchee
            for (Zone zone : zones) {
                if (zone.getMarchee() == null) {
                    return ResponseEntity.badRequest().build();
                }
            }
            List<Zone> updatedZones = zoneService.saveAll(zones);
            return ResponseEntity.ok(updatedZones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // PATCH - Mise à jour partielle d'une zone
    @PatchMapping("/{id}")
    public ResponseEntity<Zone> partialUpdateZone(@PathVariable Integer id, @RequestBody Zone zonePartial) {
        try {
            Optional<Zone> optionalZone = zoneService.findById(id);
            if (optionalZone.isPresent()) {
                Zone zone = optionalZone.get();

                // Mise à jour seulement des champs non null
                if (zonePartial.getNom() != null) {
                    zone.setNom(zonePartial.getNom());
                }
                if (zonePartial.getDescription() != null) {
                    zone.setDescription(zonePartial.getDescription());
                }
                if (zonePartial.getMarchee() != null) {
                    zone.setMarchee(zonePartial.getMarchee());
                }

                Zone updatedZone = zoneService.save(zone);
                return ResponseEntity.ok(updatedZone);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // DELETE - Supprimer une zone par son ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Integer id) {
        try {
            if (zoneService.existsById(id)) {
                zoneService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE - Supprimer plusieurs zones par leurs IDs
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteZones(@RequestBody List<Integer> ids) {
        try {
            zoneService.deleteAllById(ids);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE - Supprimer toutes les zones d'un marché
    @DeleteMapping("/marchee/{marcheeId}")
    public ResponseEntity<Void> deleteZonesByMarcheeId(@PathVariable Integer marcheeId) {
        try {
            zoneService.deleteByMarcheeId(marcheeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour compter le nombre total de zones
    @GetMapping("/count")
    public ResponseEntity<Long> countZones() {
        try {
            long count = zoneService.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour compter les zones dans un marché
    @GetMapping("/marchee/{marcheeId}/count")
    public ResponseEntity<Long> countZonesInMarchee(@PathVariable Integer marcheeId) {
        try {
            long count = zoneService.countByMarcheeId(marcheeId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint pour vérifier si une zone existe
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> zoneExists(@PathVariable Integer id) {
        try {
            boolean exists = zoneService.existsById(id);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistiques d'une zone (avec places et salles)
    @GetMapping("/{id}/stats")
    public ResponseEntity<Object> getZoneStats(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(zoneService.getZoneStatistics(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
