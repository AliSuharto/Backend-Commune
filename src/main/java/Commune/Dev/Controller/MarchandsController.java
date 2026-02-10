package Commune.Dev.Controller;

import Commune.Dev.Dtos.*;
import Commune.Dev.Models.Marchands;
import Commune.Dev.Models.StatutMarchands;
import Commune.Dev.Request.CreateMarchandRequest;
import Commune.Dev.Services.ContratService;
import Commune.Dev.Services.MarchandsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/marchands")
@CrossOrigin(origins = "*")
@Validated
public class MarchandsController {

    @Autowired
    private MarchandsService marchandsService;
    @Autowired
    private ContratService contratService;

    // Obtenir tous les marchands
    @GetMapping
    public ResponseEntity<List<MarchandDTO>> getAllMarchands() {
        List<MarchandDTO> marchands = marchandsService.getAllMarchands();
        return ResponseEntity.ok(marchands);
    }

    @GetMapping("/by-cin/{cin}")
    public ResponseEntity<MarchandDetailsDTO> getByCin(@PathVariable String cin) {
        return ResponseEntity.ok(contratService.getMarchandByCIN(cin));
    }


    @GetMapping("/cin/{cin}")
    public ResponseEntity<MarchandsPaiementDTO> getMarchandPaiementByCIN(@PathVariable String cin) {
        return marchandsService.getMarchandsByCIN(cin)
                .map(marchand -> {
                    MarchandsPaiementDTO dto = marchandsService.convertMarchandToDTO(marchand);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Créer un nouveau marchand avec photo optionnelle
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createMarchand(
            @RequestBody CreateMarchandRequest request
    ) {
        try {
            Marchands marchand = new Marchands();
            marchand.setNom(request.getNom());
            marchand.setPrenom(request.getPrenom());
            marchand.setNumCIN(request.getNumCIN());
            marchand.setNumTel1(request.getNumTel1());
            marchand.setSTAT(request.getStat());
            marchand.setNIF(request.getNif());
            marchand.setActivite(request.getActivite());
            marchand.setAdress(request.getAdress());
            marchand.setDescription(request.getDescription());
            marchand.setStatut(StatutMarchands.A_JOUR);


            Marchands savedMarchand =
                    marchandsService.saveMarchand(marchand);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "Marchand créé avec succès",
                            "marchand", savedMarchand
                    ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    // Importer des marchands depuis un fichier Excel
    @PostMapping("/import/excel")
    public ResponseEntity<?> importMarchandsAndContrats(@RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // =========================
            // 1. VALIDATION DU FICHIER
            // =========================
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Le fichier ne peut pas être vide.");
                return ResponseEntity.badRequest().body(response);
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                response.put("success", false);
                response.put("message", "Format non supporté. Utilisez .xlsx ou .xls");
                return ResponseEntity.badRequest().body(response);
            }

            // =========================
            // 2. APPEL AU SERVICE
            // =========================
            ImportResult result = marchandsService.importMarchandsAndContrats(file);

            // =========================
            // 3. CONSTRUCTION RÉPONSE
            // =========================
            response.put("success", true);
            response.put("message", "Import terminé");
            response.put("summary", Map.of(
                    "marchandsImported", result.marchandsSaved.size(),
                    "contratsCreated", result.contratsSaved.size(),
                    "errorsCount", result.errors.size()
            ));
            response.put("marchandsSaved", result.marchandsSaved);
            response.put("contratsSaved", result.contratsSaved);
            response.put("errors", result.errors);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", "Erreur lors de l'importation : " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

//UPDATE MARCHANDS

    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateMarchand(
            @PathVariable Integer id,
            @RequestBody MarchandsUpdateDTO updateDTO) {

        try {
            // Créer l'objet avec les nouvelles données
            Marchands marchandDetails = new Marchands();
            marchandDetails.setNom(updateDTO.getNom());
            marchandDetails.setPrenom(updateDTO.getPrenom());
            marchandDetails.setActivite(updateDTO.getActivite());
            marchandDetails.setNIF(updateDTO.getNif());
            marchandDetails.setSTAT(updateDTO.getStat());
            marchandDetails.setAdress(updateDTO.getAdress());
            marchandDetails.setNumCIN(updateDTO.getNumCIN());
            marchandDetails.setNumTel1(updateDTO.getNumTel1());
            marchandDetails.setNumTel2(updateDTO.getNumTel2());

            Marchands updatedMarchand = marchandsService.updateMarchand(id, marchandDetails, null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marchand mis à jour avec succès");
            response.put("marchand", updatedMarchand);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // Supprimer un marchand
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMarchand(@PathVariable Integer id) {
        try {
            marchandsService.deleteMarchand(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marchand supprimé avec succès");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Servir les photos des marchands
    @GetMapping("/photos/{filename}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String filename) {
        try {
            Path photoPath = marchandsService.getPhotoPath(filename);

            if (photoPath == null || !Files.exists(photoPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(photoPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Déterminer le type de contenu
                String contentType = Files.probeContentType(photoPath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}