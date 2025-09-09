package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Repositories.CommuneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommuneCheckController {

    private final CommuneRepository communeRepository;

    /**
     * Endpoint pour vérifier s'il existe une commune dans le système
     * GET /api/commune-check
     *
     * Vérifie simplement s'il y a au moins une commune enregistrée
     * car le système ne permet qu'une seule commune à la fois.
     *
     * Exemple d'utilisation:
     * - <a href="http://localhost:8080/api/commune-check">...</a>
     */
    @GetMapping("/commune-check")
    public ResponseEntity<ApiResponse<Boolean>> checkCommuneExists() {

        
        long communeCount = communeRepository.count();
        boolean exists = communeCount > 0;
        
        String message = exists ? 
            "Une commune est déjà enregistrée dans le système" : 
            "Aucune commune n'est encore enregistrée dans le système";

        System.out.println("Réponse API : " + message + ", exists=" + exists);
        
        return ResponseEntity.ok(ApiResponse.success(message, exists));
    }
}
