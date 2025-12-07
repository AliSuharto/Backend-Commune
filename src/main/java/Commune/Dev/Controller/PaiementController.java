package Commune.Dev.Controller;

import Commune.Dev.Dtos.MultiplePaiementRequestDTO;
import Commune.Dev.Dtos.PaiementDTO;
import Commune.Dev.Dtos.PaiementRequestDTO;
import Commune.Dev.Services.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    /**
     * Effectuer un paiement unique
     */
    @PostMapping
    public ResponseEntity<PaiementDTO> effectuerPaiement(@Valid @RequestBody PaiementRequestDTO request) {
        PaiementDTO paiement = paiementService.effectuerPaiement(request);
        return new ResponseEntity<>(paiement, HttpStatus.CREATED);
    }

    /**
     * Effectuer plusieurs paiements en même temps
     */
    @PostMapping("/multiple")
    public ResponseEntity<List<PaiementDTO>> effectuerMultiplePaiements(
            @Valid @RequestBody MultiplePaiementRequestDTO request) {
        List<PaiementDTO> paiements = paiementService.effectuerMultiplePaiements(request);
        return new ResponseEntity<>(paiements, HttpStatus.CREATED);
    }

    /**
     * Récupérer tous les paiements
     */
    @GetMapping
    public ResponseEntity<List<PaiementDTO>> getAllPaiements() {
        List<PaiementDTO> paiements = paiementService.getAllPaiements();
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer un paiement par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> getPaiementById(@PathVariable Integer id) {
        PaiementDTO paiement = paiementService.getPaiementById(id);
        return ResponseEntity.ok(paiement);
    }

    /**
     * Récupérer les paiements par marchand
     */
    @GetMapping("/marchand/{idMarchand}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByMarchand(@PathVariable Integer idMarchand) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByMarchand(idMarchand);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements par place
     */
    @GetMapping("/place/{idPlace}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByPlace(@PathVariable Integer idPlace) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByPlace(idPlace);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements par session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsBySession(@PathVariable Integer sessionId) {
        List<PaiementDTO> paiements = paiementService.getPaiementsBySession(sessionId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements par date
     */
    @GetMapping("/date")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByDate(date);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements entre deux dates
     */
    @GetMapping("/periode")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByPeriode(dateDebut, dateFin);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements par agent
     */
    @GetMapping("/agent/{idAgent}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByAgent(@PathVariable Integer idAgent) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByAgent(idAgent);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Récupérer les paiements par mode de paiement
     */

    /**
     * Récupérer les paiements d'un marchand pour un mois spécifique
     */
    @GetMapping("/marchand/{idMarchand}/mois/{mois}")
    public ResponseEntity<List<PaiementDTO>> getPaiementsByMarchandAndMois(
            @PathVariable Integer idMarchand,
            @PathVariable String mois) {
        List<PaiementDTO> paiements = paiementService.getPaiementsByMarchandAndMois(idMarchand, mois);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Supprimer un paiement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(@PathVariable Integer id) {
        paiementService.deletePaiement(id);
        return ResponseEntity.noContent().build();
    }
}