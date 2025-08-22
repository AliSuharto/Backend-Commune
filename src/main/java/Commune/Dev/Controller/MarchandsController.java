package Commune.Dev.Controller;


import Commune.Dev.Dtos.MarchandsRequestDTO;
import Commune.Dev.Dtos.MarchandsResponseDTO;
import Commune.Dev.Services.MarchandsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marchands")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarchandsController {

    private final MarchandsService marchandsService;

    @PostMapping("/post")
    public ResponseEntity<?> creerMarchand(@Valid @RequestBody MarchandsRequestDTO marchandDTO) {
        try {
            MarchandsResponseDTO marchandCree = marchandsService.creerMarchand(marchandDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(marchandCree);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erreur", "Erreur interne: " + e.getMessage()));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testPost(@RequestBody String data) {

        return ResponseEntity.ok("POST fonctionne ! Data reçue: " + data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarchandsResponseDTO> obtenirMarchand(@PathVariable Integer id) {
        return marchandsService.obtenirMarchandParId(id)
                .map(marchand -> ResponseEntity.ok(marchand))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cin/{numCIN}")
    public ResponseEntity<MarchandsResponseDTO> obtenirMarchandParCIN(@PathVariable String numCIN) {
        return marchandsService.obtenirMarchandParCIN(numCIN)
                .map(marchand -> ResponseEntity.ok(marchand))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MarchandsResponseDTO>> obtenirTousLesMarchands() {
        List<MarchandsResponseDTO> marchands = marchandsService.obtenirTousLesMarchands();
        return ResponseEntity.ok(marchands);
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<MarchandsResponseDTO>> rechercherMarchands(@RequestParam String nom) {
        List<MarchandsResponseDTO> marchands = marchandsService.rechercherMarchandsParNom(nom);
        return ResponseEntity.ok(marchands);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarchandsResponseDTO> mettreAJourMarchand(
            @PathVariable Integer id,
            @Valid @RequestBody MarchandsRequestDTO marchandDTO) {
        try {
            MarchandsResponseDTO marchandMisAJour = marchandsService.mettreAJourMarchand(id, marchandDTO);
            return ResponseEntity.ok(marchandMisAJour);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerMarchand(@PathVariable Integer id) {
        boolean supprime = marchandsService.supprimerMarchand(id);
        return supprime ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> marchandExiste(@PathVariable Integer id) {
        boolean existe = marchandsService.marchandExiste(id);
        return ResponseEntity.ok(existe);
    }
}
