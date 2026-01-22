package Commune.Dev.Controller;

import Commune.Dev.Dtos.VisualisationOrdoDTO;
import Commune.Dev.Services.VisualisationOrdoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visualisation-ordo")
@RequiredArgsConstructor
//@Tag(name = "Visualisation Ordo", description = "API pour la visualisation des données Ordo")
//@CrossOrigin(origins = "*")
public class VisualisationOrdoController {

    private final VisualisationOrdoService visualisationOrdoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ORDONNATEUR', 'DIRECTEUR')")
    @Operation(
            summary = "Récupérer les données de visualisation",
            description = "Récupère toutes les données nécessaires pour le tableau de bord Ordo incluant les statistiques des marchés, marchands et utilisateurs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Données récupérées avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - permissions insuffisantes"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur interne")
    })
    public ResponseEntity<VisualisationOrdoDTO> getVisualisationData() {
        VisualisationOrdoDTO data = visualisationOrdoService.getVisualisationData();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ORDONNATEUR', 'ORDO')")
    @Operation(
            summary = "Récupérer uniquement les statistiques",
            description = "Récupère les statistiques agrégées sans les listes détaillées"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    public ResponseEntity<StatisticsDTO> getStatistics() {
        VisualisationOrdoDTO data = visualisationOrdoService.getVisualisationData();

        StatisticsDTO stats = new StatisticsDTO();
        stats.setNbrMarchee(data.getNbr_marchee());
        stats.setNbrMarchands(data.getNbr_marchands());
        stats.setNbrUser(data.getNbr_user());
        stats.setNbrMarchandsEndettee(data.getNbr_marchands_endettee());

        return ResponseEntity.ok(stats);
    }

    // DTO pour les statistiques uniquement
    @lombok.Data
    private static class StatisticsDTO {
        private Integer nbrMarchee;
        private Integer nbrMarchands;
        private Integer nbrUser;
        private Integer nbrMarchandsEndettee;
    }
}
