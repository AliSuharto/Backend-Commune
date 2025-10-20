package Commune.Dev.Controller;

import Commune.Dev.Models.Contrat;
import Commune.Dev.Services.ContratService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/contrat")
@CrossOrigin(origins = "*")
public class ContratController {

    @Autowired
    private ContratService contratService;

    // Récupérer tous les contrats
    @GetMapping
    public ResponseEntity<List<Contrat>> getAllContrats() {
        try {
            List<Contrat> contrats = contratService.getAllContrats();
            return ResponseEntity.ok(contrats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Récupérer un contrat par ID
    @GetMapping("/{id}")
    public ResponseEntity<Contrat> getContratById(@PathVariable Integer id) {
        try {
            Optional<Contrat> contrat = contratService.getContratById(id);
            return contrat.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Récupérer les contrats d'un marchand
    @GetMapping("/marchand/{idMarchand}")
    public ResponseEntity<List<Contrat>> getContratsByMarchand(@PathVariable Integer idMarchand) {
        try {
            List<Contrat> contrats = contratService.getContratsByMarchand(idMarchand);
            return ResponseEntity.ok(contrats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Récupérer le contrat d'une place
    @GetMapping("/place/{idPlace}")
    public ResponseEntity<Contrat> getContratByPlace(@PathVariable Integer idPlace) {
        try {
            Optional<Contrat> contrat = contratService.getContratByPlace(idPlace);
            return contrat.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Récupérer les contrats par catégorie
    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<Contrat>> getContratsByCategorie(@PathVariable Integer categorieId) {
        try {
            List<Contrat> contrats = contratService.getContratsByCategorie(categorieId);
            return ResponseEntity.ok(contrats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Créer un nouveau contrat
    @PostMapping
    public ResponseEntity<Map<String, Object>> createContrat(@RequestBody Contrat contrat) {
        Map<String, Object> response = new HashMap<>();
        try {
            // LOGS pour debug
            System.out.println("=== CRÉATION CONTRAT ===");
            System.out.println("idPlace: " + contrat.getIdPlace());
            System.out.println("idMarchand: " + contrat.getIdMarchand());
            System.out.println("categorieId: " + contrat.getCategorieId());
            System.out.println("nom: " + contrat.getNom());
            System.out.println("description: " + contrat.getDescription());
            System.out.println("dateOfStart: " + contrat.getDateOfStart());

            Contrat newContrat = contratService.createContrat(contrat);

            System.out.println("Contrat créé avec succès, ID: " + newContrat.getId());

            response.put("success", true);
            response.put("message", "Contrat créé avec succès");
            response.put("contrat", newContrat);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            System.err.println("Erreur de validation: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            System.err.println("Erreur d'état: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de la création du contrat");
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la création du contrat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Mettre à jour un contrat
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateContrat(
            @PathVariable Integer id,
            @RequestBody Contrat contratDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            Contrat updatedContrat = contratService.updateContrat(id, contratDetails);
            response.put("success", true);
            response.put("message", "Contrat mis à jour avec succès");
            response.put("contrat", updatedContrat);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour du contrat");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Supprimer un contrat
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContrat(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            contratService.deleteContrat(id);
            response.put("success", true);
            response.put("message", "Contrat supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression du contrat");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Vérifier si un marchand a un contrat
    @GetMapping("/check/marchand/{idMarchand}")
    public ResponseEntity<Map<String, Boolean>> checkMarchandHasContrat(@PathVariable Integer idMarchand) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean hasContrat = contratService.marchandHasContrat(idMarchand);
            response.put("hasContrat", hasContrat);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Vérifier si une place a un contrat
    @GetMapping("/check/place/{idPlace}")
    public ResponseEntity<Map<String, Boolean>> checkPlaceHasContrat(@PathVariable Integer idPlace) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean hasContrat = contratService.placeHasContrat(idPlace);
            response.put("hasContrat", hasContrat);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
