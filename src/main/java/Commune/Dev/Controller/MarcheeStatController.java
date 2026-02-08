package Commune.Dev.Controller;

import Commune.Dev.Dtos.MarcheStatDTO;
import Commune.Dev.Services.MarcheeStatServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marchees/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarcheeStatController {

    private final MarcheeStatServices marcheStatService;

    /**
     * GET /api/marchees/stats
     * Récupère les statistiques de tous les marchés
     */
    @GetMapping
    public ResponseEntity<List<MarcheStatDTO>> getAllMarcheesStats() {
        try {
            List<MarcheStatDTO> stats = marcheStatService.getAllMarcheesStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/marchees/stats/{id}
     * Récupère les statistiques d'un marché spécifique
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarcheStatDTO> getMarcheStatById(@PathVariable Long id) {
        try {
            MarcheStatDTO stat = marcheStatService.getMarcheStatById(id);
            return ResponseEntity.ok(stat);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}