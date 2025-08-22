package Commune.Dev.Controller;

import Commune.Dev.Dtos.OrdonnateurDto;
import Commune.Dev.Request.FinalizeRequest;
import Commune.Dev.Services.CreationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CreationController {
    private final CreationService creationService;

    // Injection via constructeur
    public CreationController(CreationService creationService) {
        this.creationService = creationService;
    }

    @PostMapping("/ordonnateur/init")
    public ResponseEntity<?> init(@RequestBody OrdonnateurDto dto) {
        creationService.initOrdonnateur(dto);
        return ResponseEntity.ok(Map.of("message", "Code envoyé"));

    }

    @PostMapping("/finalize")
    public ResponseEntity<?> finalize(@RequestBody FinalizeRequest req) {
        creationService.finalizeCreation(req);
        return ResponseEntity.ok(Map.of("message", "Création terminée"));
    }
}

