package Commune.Dev.Controller;

import Commune.Dev.Dtos.*;
import Commune.Dev.Services.SalleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalleController {

    private final SalleService salleService;

    @PostMapping
    public ResponseEntity<?> createSalle(@Valid @RequestBody SalleCreateDTO createDTO) {
        try {
            SalleResponseDTO created = salleService.create(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalleResponseDTO> getSalle(@PathVariable Integer id) {
        return salleService.findById(id)
                .map(salle -> ResponseEntity.ok(salle))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalleResponseDTO> updateSalle(
            @PathVariable Integer id,
            @Valid @RequestBody SalleUpdateDTO updateDTO) {
        SalleResponseDTO updated = salleService.update(id, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalle(@PathVariable Integer id) {
        salleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<SalleResponseDTO>> getAllSalles(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Integer marcheeId,
            @RequestParam(required = false) Integer zoneId,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        SalleFilterDTO filterDTO = new SalleFilterDTO();
        filterDTO.setNom(nom);
        filterDTO.setMarcheeId(marcheeId);
        filterDTO.setZoneId(zoneId);
        filterDTO.setSortBy(sortBy);
        filterDTO.setSortDirection(sortDirection);
        filterDTO.setPage(page);
        filterDTO.setSize(size);

        Page<SalleResponseDTO> result = salleService.findAllWithFilters(filterDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SalleResponseDTO>> getAllSallesList() {
        List<SalleResponseDTO> salles = salleService.findAll();
        return ResponseEntity.ok(salles);
    }

    @GetMapping("/marchee/{marcheeId}")
    public ResponseEntity<List<SalleResponseDTO>> getAllSallesByMarchee(@PathVariable Integer marcheeId) {
        List<SalleResponseDTO> salles = salleService.findAllByMarchee(marcheeId);
        return ResponseEntity.ok(salles);
    }

    @GetMapping("/marchee/{marcheeId}/direct")
    public ResponseEntity<List<SalleResponseDTO>> getSallesDirectlyInMarchee(@PathVariable Integer marcheeId) {
        List<SalleResponseDTO> salles = salleService.findDirectlyByMarchee(marcheeId);
        return ResponseEntity.ok(salles);
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<SalleResponseDTO>> getSallesByZone(@PathVariable Integer zoneId) {
        List<SalleResponseDTO> salles = salleService.findByZone(zoneId);
        return ResponseEntity.ok(salles);
    }

    @GetMapping("/{id}/with-places")
    public ResponseEntity<SalleResponseDTO> getSalleWithPlaces(@PathVariable Integer id) {
        return salleService.findByIdWithPlaces(id)
                .map(salle -> ResponseEntity.ok(salle))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SalleResponseDTO>> searchSalles(@RequestParam String nom) {
        List<SalleResponseDTO> salles = salleService.searchByNom(nom);
        return ResponseEntity.ok(salles);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<SalleResponseDTO>> createSallesBatch(@Valid @RequestBody List<SalleCreateDTO> createDTOs) {
        List<SalleResponseDTO> created = salleService.createBatch(createDTOs);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteSallesBatch(@RequestBody List<Integer> ids) {
        salleService.deleteBatch(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkSalleExists(@PathVariable Integer id) {
        boolean exists = salleService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-nom")
    public ResponseEntity<Boolean> checkNomExists(@RequestParam String nom) {
        boolean exists = salleService.existsByNom(nom);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/places/count")
    public ResponseEntity<Long> countPlaces(@PathVariable Integer id) {
        long count = salleService.countPlaces(id);
        return ResponseEntity.ok(count);
    }
}