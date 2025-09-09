package Commune.Dev.Controller;

import Commune.Dev.Dtos.RecuPlageResponse;
import Commune.Dev.Request.RecuPlageRequest;
import Commune.Dev.Services.RecuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recu-plage")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class RecuController {

    private final RecuService recuService;

    /**
     * Crée une nouvelle plage de reçus
     */
    @PostMapping
    public ResponseEntity<?> creerRecuPlage(@Valid @RequestBody RecuPlageRequest request) {
        try {
            log.info("Requête de création de plage reçue: {}", request);
            RecuPlageResponse response = recuService.createRecuPlage(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Erreur de doublon: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("DUPLICATE_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur interne", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Une erreur interne s'est produite"));
        }
    }

    /**
     * Prévisualise les numéros qui seraient générés
     */
    @PostMapping("/previsualiser")
    public ResponseEntity<?> previsualiserNumeros(@Valid @RequestBody RecuPlageRequest request) {
        try {
            List<String> numeros = recuService.previsualiserNumeros(request);
            return ResponseEntity.ok(new PreviewResponse(numeros));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
        }
    }

    // Classes internes pour les réponses
    public static class ErrorResponse {
        public String code;
        public String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static class PreviewResponse {
        public List<String> numeros;
        public int total;

        public PreviewResponse(List<String> numeros) {
            this.numeros = numeros;
            this.total = numeros.size();
        }
    }
}
