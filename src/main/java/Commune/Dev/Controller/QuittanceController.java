package Commune.Dev.Controller;

import Commune.Dev.Dtos.QuittanceDTO;
import Commune.Dev.Services.QuittanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/quittances")
@RequiredArgsConstructor
public class QuittanceController {

    private final QuittanceService quittanceService;

    @GetMapping("/percepteur/{percepteurId}")
    public ResponseEntity<List<QuittanceDTO>> getQuittancesByPercepteur(
            @PathVariable Long percepteurId) {
        List<QuittanceDTO> quittances = quittanceService.getQuittancesByPercepteurId(percepteurId);
        return ResponseEntity.ok(quittances);
    }
}