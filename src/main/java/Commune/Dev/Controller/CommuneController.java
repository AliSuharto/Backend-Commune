package Commune.Dev.Controller;

import Commune.Dev.Dtos.ApiResponse;
import Commune.Dev.Models.Commune;
import Commune.Dev.Repositories.CommuneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communes")
@RequiredArgsConstructor
public class CommuneController {

    private final CommuneRepository communeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Commune>>> getAllCommunes() {
        List<Commune> communes = communeRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(communes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Commune>> getCommuneById(@PathVariable Long id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commune non trouvée"));
        return ResponseEntity.ok(ApiResponse.success(commune));
    }


}
