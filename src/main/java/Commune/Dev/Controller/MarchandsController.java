package Commune.Dev.Controller;

import Commune.Dev.Dtos.ImportResult;
import Commune.Dev.Dtos.MarchandDTO;
import Commune.Dev.Models.Marchands;
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

    // Obtenir tous les marchands
    @GetMapping

    public ResponseEntity<List<MarchandDTO>> getAllMarchands() {
        List<MarchandDTO> marchands = marchandsService.getAllMarchands();
        return ResponseEntity.ok(marchands);
    }


    // Obtenir les marchands avec places
//    @GetMapping("/with-places")
//    public ResponseEntity<List<Marchands>> getMarchandsWithPlaces() {
//        try {
//            List<Marchands> marchands = marchandsService.getMarchandsWithPlaces();
//            return ResponseEntity.ok(marchands);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    // Rechercher un marchand par ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Marchands> getMarchandById(@PathVariable Integer id) {
//        try {
//            Optional<Marchands> marchand = marchandsService.getMarchandById(id);
//            return marchand.map(ResponseEntity::ok)
//                    .orElse(ResponseEntity.notFound().build());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    // Rechercher des marchands par nom/prénom
//    @GetMapping("/search")
//    public ResponseEntity<List<Marchands>> searchMarchands(@RequestParam String q) {
//        try {
//            if (q == null || q.trim().isEmpty()) {
//                return ResponseEntity.badRequest().build();
//            }
//            List<Marchands> marchands = marchandsService.searchMarchands(q.trim());
//            return ResponseEntity.ok(marchands);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    // Rechercher par numéro CIN
//    @GetMapping("/cin/{numCIN}")
//    public ResponseEntity<Marchands> getMarchandByNumCIN(@PathVariable String numCIN) {
//        try {
//            Marchands marchand = marchandsService.getMarchandByNumCIN(numCIN);
//            return marchand != null ? ResponseEntity.ok(marchand) : ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    // Créer un nouveau marchand avec photo optionnelle
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createMarchand(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("numCIN") String numCIN,
            @RequestParam(value = "numTel1", required = false) String numTel1,
            @RequestParam(value = "numTel2", required = false) String numTel2,
            @RequestParam(value = "adress", required = false) String adress,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            // Créer l'objet Marchands
            Marchands marchand = new Marchands();
            marchand.setNom(nom);
            marchand.setPrenom(prenom);
            marchand.setNumCIN(numCIN);
            marchand.setNumTel1(numTel1);
            marchand.setNumTel2(numTel2);
            marchand.setAdress(adress);
            marchand.setDescription(description);

            // Sauvegarder avec photo
            Marchands savedMarchand = marchandsService.saveMarchandWithPhoto(marchand, photo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Marchand créé avec succès");
            response.put("marchand", savedMarchand);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la création du marchand: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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


    // Mettre à jour un marchand
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateMarchand(
            @PathVariable Integer id,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("numCIN") String numCIN,
            @RequestParam(value = "numTel1", required = false) String numTel1,
            @RequestParam(value = "numTel2", required = false) String numTel2,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            // Créer l'objet avec les nouvelles données
            Marchands marchandDetails = new Marchands();
            marchandDetails.setNom(nom);
            marchandDetails.setPrenom(prenom);
            marchandDetails.setNumCIN(numCIN);
            marchandDetails.setNumTel1(numTel1);
            marchandDetails.setNumTel2(numTel2);

            Marchands updatedMarchand = marchandsService.updateMarchand(id, marchandDetails, photo);

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